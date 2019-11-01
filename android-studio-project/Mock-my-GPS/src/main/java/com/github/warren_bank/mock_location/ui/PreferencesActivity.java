package com.github.warren_bank.mock_location.ui;

import com.github.warren_bank.mock_location.R;
import com.github.warren_bank.mock_location.data_model.SharedPrefs;
import com.github.warren_bank.mock_location.data_model.SharedPrefsState;
import com.github.warren_bank.mock_location.event_hooks.ISharedPrefsListener;
import com.github.warren_bank.mock_location.looper.LocationThreadManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class PreferencesActivity extends Activity {
    private SharedPrefsState originalState;

    private TextView input_time_interval;
    private TextView input_fixed_count;
    private CheckBox input_fixed_joystick_enabled;
    private TextView input_fixed_joystick_increment;
    private Button   button_cancel;
    private Button   button_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        originalState = new SharedPrefsState(PreferencesActivity.this, true);

        input_time_interval            = (TextView) findViewById(R.id.input_time_interval);
        input_fixed_count              = (TextView) findViewById(R.id.input_fixed_count);
        input_fixed_joystick_enabled   = (CheckBox) findViewById(R.id.input_fixed_joystick_enabled);
        input_fixed_joystick_increment = (TextView) findViewById(R.id.input_fixed_joystick_increment);
        button_cancel                  = (Button)   findViewById(R.id.button_cancel);
        button_save                    = (Button)   findViewById(R.id.button_save);

        reset();

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesActivity.this.finish();
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time_interval;
                int fixed_count;
                boolean fixed_joystick_enabled;
                double fixed_joystick_increment;

                String text = null;

                try {
                    text = input_time_interval.getText().toString();
                    time_interval = Integer.parseInt(text, 10);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }
                try {
                    text = input_fixed_count.getText().toString();
                    fixed_count = Integer.parseInt(text, 10);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }
                fixed_joystick_enabled = input_fixed_joystick_enabled.isChecked();
                try {
                    text = input_fixed_joystick_increment.getText().toString();
                    fixed_joystick_increment = Double.parseDouble(text);
                }
                catch(Exception e) {
                    showError(getString(R.string.error_number_format, text));
                    return;
                }

                SharedPrefsState modifiedState = new SharedPrefsState(
                    originalState.bookmarks,

                    // fields that could be modified
                    time_interval,
                    fixed_count,
                    fixed_joystick_enabled,
                    fixed_joystick_increment,

                    originalState.trip_origin_lat,
                    originalState.trip_origin_lon,
                    originalState.trip_destination_lat,
                    originalState.trip_destination_lon,
                    originalState.trip_duration
                );

                short diff_fields = originalState.diff(modifiedState);
                boolean is_equal  = (diff_fields == 0);

                if (!is_equal) {
                    SharedPreferences.Editor editor = SharedPrefs.getSharedPreferencesEditor(PreferencesActivity.this);
                    boolean flush = false;
                    short mask;

                    mask = (1 << 1);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putTimeInterval(editor, PreferencesActivity.this, time_interval, flush);
                    }

                    mask = (1 << 2);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedCount(editor, PreferencesActivity.this, fixed_count, flush);
                    }

                    mask = (1 << 3);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedJoystickEnabled(editor, PreferencesActivity.this, fixed_joystick_enabled, flush);
                    }

                    mask = (1 << 4);
                    if ((diff_fields & mask) == mask) {
                        SharedPrefs.putFixedJoystickIncrement(editor, PreferencesActivity.this, fixed_joystick_increment, flush);
                    }

                    editor.commit();

                    ISharedPrefsListener prefsListener = LocationThreadManager.get();
                    prefsListener.onSharedPrefsChange(diff_fields);
                }

                PreferencesActivity.this.finish();
            }
        });
    }

    private void reset() {
        input_time_interval.setText(Integer.toString(originalState.time_interval, 10));
        input_fixed_count.setText(Integer.toString(originalState.fixed_count, 10));
        input_fixed_joystick_enabled.setChecked(originalState.fixed_joystick_enabled);
        input_fixed_joystick_increment.setText(Double.toString(originalState.fixed_joystick_increment));
    }

    private void showError(String text) {
        Toast.makeText(PreferencesActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isFinishing())
            finish();
    }
}
