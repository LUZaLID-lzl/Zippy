package com.luza.zippy.ui.sidebarList.intonation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.PitchDisplayView;

public class IntonationFragment extends BaseFragment {
    private static final String TAG = "IntonationFragment";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT;
    
    private static final float MIN_VOICE_FREQ = 80f;  // 最低人声频率
    private static final float MAX_VOICE_FREQ = 1100f; // 最高人声频率
    private static final float VOICE_THRESHOLD = 0.3f; // 人声检测阈值
    
    private PitchDisplayView pitchDisplayView;
    private TextView tvCurrentPitch;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private SeekBar seekBarPitch;
    private float lastFrequency = -1;
    private double lastMidiNote = -1;
    private static final float FREQUENCY_SMOOTHING = 0.85f; // 频率平滑因子
    private static final float NOTE_SMOOTHING = 0.7f;      // 音符平滑因子
    private static final int MIN_VALID_READINGS = 3;       // 最小有效读数
    private int validReadingsCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intonation, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_intonation);
    }

    @Override
    protected void initViews(View view) {
        pitchDisplayView = view.findViewById(R.id.pitch_display_view);
        tvCurrentPitch = view.findViewById(R.id.tv_current_pitch);
        seekBarPitch = view.findViewById(R.id.seekbar_pitch);
        
        seekBarPitch.setMax(88);
        seekBarPitch.setProgress(44);
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    pitchDisplayView.setScrollPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        if (checkPermission()) {
            startPitchDetection();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    private void startPitchDetection() {
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid buffer size: " + minBufferSize);
            return;
        }

        Log.d(TAG, "Starting audio recording with buffer size: " + minBufferSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed");
            return;
        }

        isRecording = true;
        audioRecord.startRecording();
        Log.d(TAG, "Audio recording started");

        recordingThread = new Thread(() -> {
            float[] audioData = new float[1024];
            while (isRecording) {
                int readSize = audioRecord.read(audioData, 0, audioData.length, AudioRecord.READ_BLOCKING);
                if (readSize > 0) {
                    float maxAmplitude = getMaxAmplitude(audioData, readSize);
                    if (maxAmplitude > 0) {
                        normalizeAudioData(audioData, readSize, maxAmplitude);
                        float frequency = detectPitch(audioData, readSize);
                        if (frequency > 0) {
                            requireActivity().runOnUiThread(() -> {
                                updatePitchDisplay(frequency);
                                pitchDisplayView.updateWaveform(audioData);
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "Error reading audio data: " + readSize);
                }
            }
        });
        recordingThread.start();
    }

    private float getMaxAmplitude(float[] audioData, int size) {
        float maxAmplitude = 0;
        for (int i = 0; i < size; i++) {
            maxAmplitude = Math.max(maxAmplitude, Math.abs(audioData[i]));
        }
        return maxAmplitude;
    }

    private void normalizeAudioData(float[] audioData, int size, float maxAmplitude) {
        for (int i = 0; i < size; i++) {
            audioData[i] /= maxAmplitude;
        }
    }

    private float detectPitch(float[] audioData, int size) {
        float[] preemphasized = new float[size];
        float preemphasis = 0.97f;
        preemphasized[0] = audioData[0];
        for (int i = 1; i < size; i++) {
            preemphasized[i] = audioData[i] - preemphasis * audioData[i - 1];
        }

        float[] windowedData = new float[size];
        for (int i = 0; i < size; i++) {
            float window = 0.5f * (1 - (float)Math.cos(2 * Math.PI * i / (size - 1)));
            windowedData[i] = preemphasized[i] * window;
        }

        int minDelay = (int)(SAMPLE_RATE / MAX_VOICE_FREQ);
        int maxDelay = (int)(SAMPLE_RATE / MIN_VOICE_FREQ);
        float maxCorrelation = 0;
        int bestDelay = 0;
        float threshold = VOICE_THRESHOLD;

        float energy = calculateEnergy(windowedData, size);
        if (energy < 0.01f) {
            return -1;
        }

        float[] correlation = new float[maxDelay];
        for (int delay = minDelay; delay < maxDelay; delay++) {
            float sum = calculateCorrelation(windowedData, size, delay);
            correlation[delay] = sum;
            if (correlation[delay] > maxCorrelation) {
                maxCorrelation = correlation[delay];
                bestDelay = delay;
            }
        }

        float normalizedCorrelation = maxCorrelation / calculateZeroCrossing(windowedData, size);
        if (normalizedCorrelation < threshold) {
            return -1;
        }

        float frequency = (float) SAMPLE_RATE / bestDelay;
        if (lastFrequency > 0 && frequency > 0) {
            float freqRatio = frequency / lastFrequency;
            if (freqRatio < 0.5 || freqRatio > 2.0) {
                return -1;
            }
        }

        return (frequency >= MIN_VOICE_FREQ && frequency <= MAX_VOICE_FREQ) ? frequency : -1;
    }

    private float calculateEnergy(float[] data, int size) {
        float energy = 0;
        for (int i = 0; i < size; i++) {
            energy += data[i] * data[i];
        }
        return energy;
    }

    private float calculateCorrelation(float[] data, int size, int delay) {
        float sum = 0;
        for (int i = 0; i < size - delay; i++) {
            sum += data[i] * data[i + delay];
        }
        return sum;
    }

    private float calculateZeroCrossing(float[] data, int size) {
        float zeroCrossing = 0;
        for (int i = 0; i < size; i++) {
            zeroCrossing += data[i] * data[i];
        }
        return zeroCrossing + 1e-6f;
    }

    private void updatePitchDisplay(float frequency) {
        if (frequency <= 0) {
            validReadingsCount = 0;
            return;
        }

        if (lastFrequency > 0) {
            frequency = lastFrequency * FREQUENCY_SMOOTHING + frequency * (1 - FREQUENCY_SMOOTHING);
            float freqChange = Math.abs(frequency - lastFrequency) / lastFrequency;
            if (freqChange > 0.5) {
                return;
            }
            validReadingsCount++;
        } else {
            validReadingsCount = 1;
        }
        lastFrequency = frequency;

        if (validReadingsCount < MIN_VALID_READINGS) {
            return;
        }

        double midiNote = 69 + 12 * Math.log(frequency / 440.0) / Math.log(2.0);
        if (lastMidiNote >= 0) {
            midiNote = lastMidiNote * NOTE_SMOOTHING + midiNote * (1 - NOTE_SMOOTHING);
        }
        lastMidiNote = midiNote;

        String noteName = getNoteName(midiNote);
        pitchDisplayView.updatePitch((float) midiNote);
        tvCurrentPitch.setText(getString(R.string.intonation_current_pitch, String.format("%s (%.1fHz)", noteName, frequency)));
    }

    private String getNoteName(double midiNote) {
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int noteIndex = (int) Math.round(midiNote) % 12;
        int octave = (int) ((midiNote - 12) / 12);
        return noteNames[noteIndex] + octave;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPitchDetection();
            } else {
                Toast.makeText(requireContext(), 
                    R.string.intonation_permission_required, 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stopping audio recording");
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (recordingThread != null) {
            recordingThread.interrupt();
            recordingThread = null;
        }
        lastFrequency = -1;
        lastMidiNote = -1;
        validReadingsCount = 0;
        super.onDestroy();
    }
}