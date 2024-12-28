package com.luza.zippy.ui.sidebarList.timer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

public class TimerService extends Service {
    private final IBinder binder = new TimerBinder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;
    private long countdownTime = 0L;
    private boolean isRunning = false;
    private boolean isCountdown = false;
    private TimerListener listener;
    private final int UPDATE_INTERVAL = 10; // 10ms更新一次

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public void startTimer(boolean isCountdown, long countdownMillis) {
        this.isCountdown = isCountdown;
        if (isCountdown) {
            this.countdownTime = countdownMillis;
            startTime = SystemClock.elapsedRealtime();
        } else {
            startTime = SystemClock.elapsedRealtime();
        }
        isRunning = true;
        handler.post(updateTimerThread);
    }

    public void pauseTimer() {
        isRunning = false;
        handler.removeCallbacks(updateTimerThread);
    }

    public void stopTimer() {
        isRunning = false;
        handler.removeCallbacks(updateTimerThread);
        if (listener != null) {
            listener.onTimerStop(isCountdown ? countdownTime - (SystemClock.elapsedRealtime() - startTime) : 
                               SystemClock.elapsedRealtime() - startTime);
        }
    }

    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            if (isRunning) {
                long timeInMilliseconds = SystemClock.elapsedRealtime() - startTime;
                if (isCountdown) {
                    long remainingTime = countdownTime - timeInMilliseconds;
                    if (remainingTime <= 0) {
                        stopTimer();
                        if (listener != null) {
                            listener.onCountdownFinish();
                        }
                        return;
                    }
                    if (listener != null) {
                        listener.onTimerTick(remainingTime);
                    }
                } else {
                    if (listener != null) {
                        listener.onTimerTick(timeInMilliseconds);
                    }
                }
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    public interface TimerListener {
        void onTimerTick(long millisUntilFinished);
        void onTimerStop(long totalTime);
        void onCountdownFinish();
    }
} 