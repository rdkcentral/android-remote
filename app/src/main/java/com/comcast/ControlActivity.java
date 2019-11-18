/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2016 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.comcast.BuildConfig;
import com.comcast.R;
import com.comcast.databinding.FragmentRemoteBinding;
import com.comcast.util.SendKeyEventOnHoldController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is main activity of the app
 *
 * @version 1.0.0
 */
public class ControlActivity extends AppCompatActivity implements
        View.OnClickListener, View.OnKeyListener, SendKeyEventOnHoldController.SendViewKeyEventListener {

    /**
     * Keycodes to ignore
     */
    private static final List<Integer> IGNORE_KEY_CODES = Arrays.asList(
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME);

    /**
     * Symbol keycodes with special handling.
     */
    private static final List<Integer> SYMBOL_KEY_CODES = Arrays.asList(
            KeyEvent.KEYCODE_AT, KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_POUND, KeyEvent.KEYCODE_STAR);

    /**
     * Flag to be updated when the soft keyboard is toggled.
     */
    private boolean softKeyboardVisible;

    /**
     * This is method which helps in creating the UI for the activity
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentRemoteBinding binding = DataBindingUtil.setContentView(this, R.layout.fragment_remote);
        // this makes sure we don't show the title
        getSupportActionBar().hide();

        // Set keyboard key listener
        findViewById(R.id.remote_view).setOnKeyListener(this);

        // Add the handlers for the remote buttons
        View[] buttons = {
                binding.remoteButtonKeyboard,
                binding.remoteButtonXfinity,
                binding.remoteButtonChUp,
                binding.remoteButtonChDown,
                binding.remoteButtonPrevious,
                binding.remoteButtonPlay,
                binding.remoteButtonForward,
                binding.remoteButtonExit,
                binding.remoteButtonRecord,
                binding.remoteButtonPgUp,
                binding.remoteButtonPgDown,
                binding.remoteButtonGuide,
                binding.remoteDpadUp,
                binding.remoteDpadDown,
                binding.remoteDpadLeft,
                binding.remoteDpadRight,
                binding.remoteDpadCircle,
                binding.remoteDpadA,
                binding.remoteDpadB,
                binding.remoteDpadC,
                binding.remoteDpadD,
                binding.remoteButtonLeftArrow,
                binding.remoteButtonMic,
                binding.remoteButtonInfo,
                binding.remoteNumpad0,
                binding.remoteNumpad1,
                binding.remoteNumpad2,
                binding.remoteNumpad3,
                binding.remoteNumpad4,
                binding.remoteNumpad5,
                binding.remoteNumpad6,
                binding.remoteNumpad7,
                binding.remoteNumpad8,
                binding.remoteNumpad9
        };

        for (View button : buttons) {
            if (!binding.remoteButtonKeyboard.equals(button)) {
                SendKeyEventOnHoldController.init(button, this);
            }
            else {
                button.setOnClickListener(this);
            }
        }

        binding.textVersion.setText(BuildConfig.VERSION_NAME);
    }

    /**
     * This handled the click event for the UI elements. This is place where KeyEvent are
     * sent to the Service for processing for xfinity and Last button.
     *
     * @param view View - this is UI control the user clicked
     */
    @Override
    public void onClick(View view) {
        if (R.id.remote_button_keyboard == view.getId()) {
            if (!softKeyboardVisible) {
                showSoftKeyboard();
            } else {
                hideSoftKeyboard();
            }
        }
    }

    /**
     * Handles sending key events for view when view is pressed
     *
     * @param view The view to send key events for
     */
    @Override
    public void sendKeyEventForView(View view) {
        HerculesApp app = (HerculesApp) getApplicationContext();
        app.sendKeyForView(view);
    }

    /**
     * Handles long press has ended
     *
     * @param view The view to events for
     */    @Override
    public void longPressEndForView(View view) {
        HerculesApp app = (HerculesApp) getApplicationContext();
        app.clearKeyQueue();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (IGNORE_KEY_CODES.contains(keyEvent.getKeyCode())) {
            return false;
        }

        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            HerculesApp app = (HerculesApp) getApplicationContext();
            int keyCode = keyEvent.getKeyCode();

            List<Integer> keyCodes = new ArrayList<>();
            if (SYMBOL_KEY_CODES.indexOf(keyCode) > -1) {
                keyCodes.addAll(keysForSymbolKeyCode(keyCode));
            } else {
                if (keyEvent.isShiftPressed() || keyEvent.isCapsLockOn()) {
                    keyCodes.add(KeyEvent.KEYCODE_SHIFT_LEFT);

                }
                keyCodes.add(keyCode);
            }

            for (Integer key : keyCodes) {
                app.sendKey(key, true);
                if (key.intValue() != KeyEvent.KEYCODE_SHIFT_LEFT) {
                    app.sendKey(key, false);
                }
            }

            if (keyCodes.size() > 0 && keyCodes.get(0) == KeyEvent.KEYCODE_SHIFT_LEFT) {
                app.sendKey(keyCodes.get(0), false);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onPause() {
        hideSoftKeyboard();
        super.onPause();
    }

    private void showSoftKeyboard() {
        final View view = findViewById(R.id.remote_view);
        if (view == null) {
            return;
        }

        final InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Handler handler = new Handler();
        handler.post(() -> {
            view.requestFocus();
            im.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            softKeyboardVisible = true;
        });
        view.requestFocus();
    }

    private void hideSoftKeyboard() {
        View view = findViewById(R.id.remote_view);
        if (view == null) {
            return;
        }

        // hide the keyboard
        view.clearFocus();
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
        softKeyboardVisible = false;
    }

    private static List<Integer> keysForSymbolKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_AT:
                // @ key - Shift + '2'
                return Arrays.asList(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_2);

            case KeyEvent.KEYCODE_PLUS:
                // + key - Shift + '='
                return Arrays.asList(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_EQUALS);

            case KeyEvent.KEYCODE_POUND:
                // # key - Shift + '3'
                return Arrays.asList(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_3);

            case KeyEvent.KEYCODE_STAR:
                // * key - Shift + '8'
                return Arrays.asList(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_8);
        }

        return null;
    }

}
