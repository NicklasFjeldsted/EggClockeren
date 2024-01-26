package com.example.eggclockeren;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout timersContainer; // Container for adding timer views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the container for timers
        timersContainer = findViewById(R.id.timersContainer);
        // Button to add new timers
        Button addTimerBtn = findViewById(R.id.btn_add);

        // Set listener for the add button
        addTimerBtn.setOnClickListener(v -> addTimerDialog());
    }

    // Method to add a new timer view
    private void addTimerView(int numberOfSeconds, String title) {
        // Inflate the timer layout and add it to the container
        View newTimerLayout = LayoutInflater.from(this).inflate(R.layout.timer_layout, timersContainer, false);

        // Initialize and set the title and timer text views, start/pause and stop buttons
        TextView txtTitle = newTimerLayout.findViewById(R.id.txt_title);
        TextView txtTimer = newTimerLayout.findViewById(R.id.txt_timer);
        MaterialButton btnStartPause = newTimerLayout.findViewById(R.id.btn_startPause);
        MaterialButton btnStop = newTimerLayout.findViewById(R.id.btnStop);

        // Set the provided title to the title text view
        txtTitle.setText(title);

        // Add the inflated layout to the main container
        timersContainer.addView(newTimerLayout);

        // Create a new TimerThread instance for this timer
        TimerThread timerThread = new TimerThread(numberOfSeconds, this, txtTimer, txtTitle);

        // Set listeners for the start/pause and stop buttons
        btnStartPause.setOnClickListener(v -> {
            // Toggle the timer's running state and update the button icon
            if(timerThread.getIsRunning()) {
                btnStartPause.setIcon(ContextCompat.getDrawable(this, R.drawable.timer_icon_play));
                timerThread.pauseTimer();
            } else {
                timerThread.startTimer();
                btnStartPause.setIcon(ContextCompat.getDrawable(this, R.drawable.timer_icon_pause));
            }
        });

        btnStop.setOnClickListener(v -> {
            // Stop the timer and reset the button icon
            timerThread.stopTimer();
            btnStartPause.setIcon(ContextCompat.getDrawable(this, R.drawable.timer_icon_play));
        });

        // Set up edit actions for the title and timer text views
        txtTitle.setOnClickListener(v -> editTitleDialog(txtTitle, timerThread));
        txtTimer.setOnClickListener(v -> {
            if(!timerThread.getIsRunning()) {
                editTimeDialog(timerThread, timerThread.getTime());
            }
        });

        // Set a long click listener to remove the timer after holding for 3 seconds
        newTimerLayout.setOnLongClickListener(view -> {
            new Handler().postDelayed(() -> removeTimerDialog(newTimerLayout, timerThread), 3000); // Delay of 3 seconds
            return true;
        });
    }

    private void addTimerDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this); // Create alert dialog
        adb.setTitle("Add timer");

        // Inflate dialog with layout
        View viewInflated = LayoutInflater.from(this)
                .inflate(R.layout.dialog_timer, (ViewGroup) getWindow().getDecorView().getRootView(), false);

        // Set up the input
        EditText input = viewInflated.findViewById(R.id.input_timer);

        NumberPicker numPickHrs = viewInflated.findViewById(R.id.numPicker_hrs);
        NumberPicker numPickMins = viewInflated.findViewById(R.id.numPicker_mins);
        NumberPicker numPickSecs = viewInflated.findViewById(R.id.numPicker_secs);

        // Set min max values of NumberPickers
        numPickHrs.setMaxValue(24);
        numPickHrs.setMinValue(0);

        numPickMins.setMaxValue(60);
        numPickMins.setMinValue(0);

        numPickSecs.setMaxValue(60);
        numPickSecs.setMinValue(0);

        adb.setView(viewInflated);
        // Set up the buttons
        adb.setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                input.getText();
                int hours = numPickHrs.getValue() * 3600;
                int minutes = numPickMins.getValue() * 60;
                int seconds = numPickSecs.getValue() + minutes + hours;
                addTimerView(seconds, input.getText().toString());
            }
        });
        adb.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        adb.show();
    }

    private void editTitleDialog(TextView txtTitle, TimerThread thread) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this); // Create alert dialog

        adb.setTitle("Change title of timer.");

        View viewInflated = LayoutInflater.from(this)
                .inflate(R.layout.dialog_timer_edit, (ViewGroup) getWindow().getDecorView().getRootView(), false);

        EditText editTitle = viewInflated.findViewById(R.id.txtEdit_title);

        editTitle.setText(txtTitle.getText());

        adb.setView(viewInflated);

        adb.setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                txtTitle.setText(editTitle.getText().toString());
                thread.setTitle(editTitle.getText().toString());
            }
        });
        adb.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        adb.show();
    }

    private void editTimeDialog(TimerThread timerThread, long remainingSeconds) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle("Change title of timer.");

        View viewInflated = LayoutInflater.from(this)
                .inflate(R.layout.dialog_timer, (ViewGroup) getWindow().getDecorView().getRootView(), false);

        EditText inputTimer = viewInflated.findViewById(R.id.input_timer);

        inputTimer.setVisibility(View.GONE);

        NumberPicker numPickHrs = viewInflated.findViewById(R.id.numPicker_hrs);
        NumberPicker numPickMins = viewInflated.findViewById(R.id.numPicker_mins);
        NumberPicker numPickSecs = viewInflated.findViewById(R.id.numPicker_secs);

        int hours = (int) remainingSeconds / 3600;
        int minutes = (int) (remainingSeconds % 3600) / 60;
        int seconds = (int) (remainingSeconds % 60);

        numPickHrs.setMaxValue(24);
        numPickHrs.setMinValue(0);
        numPickHrs.setValue(hours);

        numPickMins.setMaxValue(60);
        numPickMins.setMinValue(0);
        numPickMins.setValue(minutes);

        numPickSecs.setMaxValue(60);
        numPickSecs.setMinValue(0);
        numPickSecs.setValue(seconds);

        adb.setView(viewInflated);

        adb.setPositiveButton(android.R.string.paste, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int hours = numPickHrs.getValue() * 3600;
                int minutes = numPickMins.getValue() * 60;
                int seconds = numPickSecs.getValue() + minutes + hours;
                timerThread.setTime(seconds);
            }
        });
        adb.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        adb.show();
    }

    private void removeTimerDialog(View view, TimerThread thread) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle("Do you wish to remove this timer?");
        adb.setIcon(android.R.drawable.ic_dialog_info);

        adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                timersContainer.removeView(view);
                thread.stopTimer();
            }
        });
        adb.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        adb.show();
    }

}

