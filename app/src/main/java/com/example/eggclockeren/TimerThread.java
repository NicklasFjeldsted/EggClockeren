package com.example.eggclockeren;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;

public class TimerThread implements Runnable {

    // Fields for activity, timer TextView, and timing control
    private final MainActivity activity;
    private final TextView txtTimer;
    private final TextView txtTitle;
    private volatile boolean isRunning = true;
    private volatile long remainingSeconds;
    private volatile long initialSeconds;
    private MediaPlayer mediaPlayer;
    private Thread thread;

    // Constructor to initialize the timer thread
    public TimerThread(long numberOfSeconds, MainActivity activity, TextView txtTimer, TextView txtTitle) {
        this.activity = activity;
        this.txtTimer = txtTimer;
        this.remainingSeconds = numberOfSeconds;
        this.initialSeconds = remainingSeconds;
        this.txtTitle = txtTitle;
        updateText(); // Update the timer text initially
    }

    @Override
    public void run() {
        // Main timer loop
        while (remainingSeconds > 0 && isRunning) {
            updateText(); // Update timer text
            try {
                Thread.sleep(1000); // Sleep for one second
                remainingSeconds--; // Decrement the remaining time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Handle timer completion
        if(remainingSeconds <= 0 && isRunning) {
            pauseTimer(); // Pause the timer
            remainingSeconds = 0; // Reset remaining seconds
            updateText(); // Update the timer text
            showTimerCompleteAlert(); // Show alert dialog
            playTimerCompleteSound(); // Play completion sound
        }
    }

    public void pauseTimer() {
        isRunning = false;
    }

    public void startTimer() {
        if(thread == null || !thread.isAlive()) {
            isRunning = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stopTimer() {
        isRunning = false;
        this.remainingSeconds = initialSeconds;
        updateText();
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public long getTime() {
        return remainingSeconds + 1;
    }

    public void setTime(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
        updateText();
    }

    public void setTitle(String title) {
        txtTitle.setText(title);
    }

    private void updateText() {
        // Updates text based of the remaining seconds
        int hours = (int) remainingSeconds / 3600;
        int minutes = (int) (remainingSeconds % 3600) / 60;
        int seconds = (int) (remainingSeconds % 60);
        String time = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);

        activity.runOnUiThread(() -> txtTimer.setText(time));
    }

    private void showTimerCompleteAlert() {
        activity.runOnUiThread(() -> {
            // Create an AlertDialog to show a popup
            AlertDialog.Builder adb = new AlertDialog.Builder(activity);
            adb.setTitle("Timer Complete");
            adb.setMessage("Your timer titled: " + txtTitle.getText().toString() + " has finished");

            adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    stopTimerCompleteSound();
                    stopTimer();
                }
            });

            adb.show();
        });
    }

    // Start timer sound
    private void playTimerCompleteSound() {
        activity.runOnUiThread(() -> {
            // Use MediaPlayer to play a sound
            mediaPlayer = MediaPlayer.create(activity, Settings.System.DEFAULT_RINGTONE_URI);
            mediaPlayer.start();
        });
    }


    // Stop Timer sound
    private void stopTimerCompleteSound() {
        activity.runOnUiThread(() -> {
            // Use MediaPlayer to play a sound
            mediaPlayer.stop();
        });
    }

}

