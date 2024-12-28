package com.luza.zippy.ui.sidebarList.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import android.os.AsyncTask;
import android.util.Log;

public class TestFragment extends BaseFragment {
    private Button startButton;
    private ProgressBar progressBar;
    private TextView resultText;
    private IOSpeedTest currentTest;
    
    private static final int BUFFER_SIZE = 4 * 1024; // 4KB
    private static final int MIN_CHUNK_SIZE = 10 * 1024; // 10KB
    private static final int MAX_CHUNK_SIZE = 100 * 1024; // 100KB
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 最大文件大小10MB
    private static final long TEST_DURATION = 30000; // 30秒
    private static final int UPDATE_INTERVAL = 500; // 每500ms更新一次显示

    @Override
    protected String getTitle() {
        return getString(R.string.menu_test);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    protected void initViews(View view) {
        startButton = view.findViewById(R.id.btn_start_test);
        progressBar = view.findViewById(R.id.progress_bar);
        resultText = view.findViewById(R.id.text_result);

        startButton.setOnClickListener(v -> startTest());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentTest != null) {
            currentTest.cancel(true);
        }
    }

    private void startTest() {
        if (currentTest != null) {
            currentTest.cancel(true);
        }
        startButton.setEnabled(false);  // 禁用按钮
        progressBar.setVisibility(View.VISIBLE);  // 显示进度条
        progressBar.setProgress(0);  // 重置进度
        resultText.setText(getString(R.string.preparing_test));  // 显示准备信息
        
        currentTest = new IOSpeedTest();
        currentTest.execute();
    }

    private class IOSpeedTest extends AsyncTask<Void, TestProgress, TestResult> {
        private long startTestTime;
        private long lastUpdateTime;
        private File testFile;
        private FileInputStream fis;
        private FileOutputStream fos;

        @Override
        protected TestResult doInBackground(Void... ignored) {
            byte[] readBuffer = new byte[BUFFER_SIZE];
            long totalTime = 0;
            long totalBytes = 0;
            int successfulReads = 0;
            Random random = new Random();

            try {
                testFile = File.createTempFile("speedtest", null, requireContext().getCacheDir());
                fos = new FileOutputStream(testFile);
                fis = new FileInputStream(testFile);
                
                startTestTime = System.currentTimeMillis();
                lastUpdateTime = startTestTime;
                long fileSize = 0;

                while (!isCancelled() && (System.currentTimeMillis() - startTestTime) < TEST_DURATION) {
                    // 检查是否取消
                    if (isCancelled()) {
                        cleanupResources();
                        return null;
                    }

                    // 生成新的随机大小的数据块
                    if (fileSize < MAX_FILE_SIZE) {
                        int currentChunkSize = MIN_CHUNK_SIZE + random.nextInt(MAX_CHUNK_SIZE - MIN_CHUNK_SIZE + 1);
                        byte[] writeBuffer = new byte[currentChunkSize];
                        random.nextBytes(writeBuffer);
                        fos.write(writeBuffer);
                        fos.flush();
                        fileSize += currentChunkSize;
                    }

                    try {
                        if (fileSize > BUFFER_SIZE) {
                            long position = (long) (random.nextDouble() * (fileSize - BUFFER_SIZE));
                            fis.getChannel().position(position);

                            long readStartTime = System.nanoTime();
                            int bytesRead = fis.read(readBuffer);
                            long readEndTime = System.nanoTime();

                            if (bytesRead > 0) {
                                totalTime += (readEndTime - readStartTime);
                                totalBytes += bytesRead;
                                successfulReads++;

                                // 更新进度
                                long now = System.currentTimeMillis();
                                if (now - lastUpdateTime >= UPDATE_INTERVAL) {
                                    if (!isCancelled()) {
                                        double currentDuration = (now - startTestTime) / 1000.0;
                                        double currentSpeed = (totalBytes / 1024.0 / 1024.0) / currentDuration;
                                        double currentLatency = (totalTime / 1_000_000.0) / successfulReads;

                                        publishProgress(new TestProgress(
                                            (int)((now - startTestTime) * 100 / TEST_DURATION),
                                            currentSpeed,
                                            currentLatency,
                                            successfulReads
                                        ));
                                    }
                                    lastUpdateTime = now;
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e("IOSpeedTest", "Error during read: " + e.getMessage());
                    }
                }

                double testDurationSeconds = (System.currentTimeMillis() - startTestTime) / 1000.0;
                double speedMBps = (totalBytes / 1024.0 / 1024.0) / testDurationSeconds;
                double avgLatencyMs = (totalTime / 1_000_000.0) / successfulReads;

                return new TestResult(speedMBps, avgLatencyMs, testDurationSeconds, successfulReads);

            } catch (Exception e) {
                Log.e("IOSpeedTest", "Test failed: " + e.getMessage());
                return new TestResult(e);
            } finally {
                cleanupResources();
            }
        }

        private void cleanupResources() {
            try {
                if (fis != null) fis.close();
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                if (testFile != null && testFile.exists()) {
                    testFile.delete();
                }
            } catch (IOException e) {
                Log.e("IOSpeedTest", "Error cleaning up: " + e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            startButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            resultText.setText(getString(R.string.preparing_test));
        }

        @Override
        protected void onProgressUpdate(TestProgress... values) {
            if (!isCancelled() && isAdded()) {
                TestProgress progress = values[0];
                progressBar.setVisibility(View.VISIBLE);  // 确保进度条可见
                progressBar.setProgress(progress.progressPercent);
                resultText.setText(getString(R.string.test_result_realtime,
                    progress.currentSpeedMBps,
                    progress.currentLatencyMs,
                    (System.currentTimeMillis() - startTestTime) / 1000.0,
                    progress.successfulReads));
            }
        }

        @Override
        protected void onCancelled() {
            cleanupResources();
            if (startButton != null) {
                startButton.setEnabled(true);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (resultText != null) {
                resultText.setText(R.string.test_cancelled);
            }
        }

        @Override
        protected void onPostExecute(TestResult result) {
            if (isAdded()) {
                startButton.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                
                if (result.error != null) {
                    resultText.setText(getString(R.string.test_error, result.error.getMessage()));
                } else {
                    resultText.setText(getString(R.string.test_result_extended,
                        result.speedMBps,
                        result.avgLatencyMs,
                        result.testDurationSeconds,
                        result.successfulReads));
                }
            }
        }
    }

    private static class TestProgress {
        final int progressPercent;
        final double currentSpeedMBps;
        final double currentLatencyMs;
        final int successfulReads;

        TestProgress(int progressPercent, double currentSpeedMBps, 
                    double currentLatencyMs, int successfulReads) {
            this.progressPercent = progressPercent;
            this.currentSpeedMBps = currentSpeedMBps;
            this.currentLatencyMs = currentLatencyMs;
            this.successfulReads = successfulReads;
        }
    }

    private static class TestResult {
        final double speedMBps;
        final double avgLatencyMs;
        final double testDurationSeconds;
        final int successfulReads;
        final Exception error;

        TestResult(double speedMBps, double avgLatencyMs, double testDurationSeconds, int successfulReads) {
            this.speedMBps = speedMBps;
            this.avgLatencyMs = avgLatencyMs;
            this.testDurationSeconds = testDurationSeconds;
            this.successfulReads = successfulReads;
            this.error = null;
        }

        TestResult(Exception error) {
            this.speedMBps = 0;
            this.avgLatencyMs = 0;
            this.testDurationSeconds = 0;
            this.successfulReads = 0;
            this.error = error;
        }
    }
}