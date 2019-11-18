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

package com.comcast.util;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.comcast.HerculesApp;

/**
 * This is helper class for sending bluetooth key events when user is long pressing ui key
 *
 * @version 1.0.0
 */
public class SendKeyEventOnHoldController implements View.OnLongClickListener, View.OnTouchListener, Runnable {

    private static final String TAG = SendKeyEventOnHoldController.class.getSimpleName();

    /**
     * Handler for post delaying next key send after key is sent
     */
    private final Handler handler = new Handler();

    /**
     * Determines if send key events is enabled
     */
    private boolean enableSend;

    /**
     * Determines if long press events is enabled
     */
    private boolean longPress;

    /**
     * The ui view to send key events for
     */
    private View view;

    /**
     * Listener to send key events
     */
    private SendViewKeyEventListener listener;

    public SendKeyEventOnHoldController(View view, SendViewKeyEventListener listener) {
        this.view = view;
        this.listener = listener;
        this.view.setOnTouchListener(this);
        this.view.setOnLongClickListener(this);
    }

    /**
     * Handles enabling send bluetooth key events when ui key is long clicked.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View v) {
        handler.post(this);
        longPress = true;
        return true;
    }

    /**
     * Handles disabling send bluetooth key events when ui key is deselected.
     *
     * @param touchEventView The target view of the touch event
     * @param event The MotionEvent object contains the touch info.
     * @return A boolean value to indicate if the listener has consumed the event or not.
     */
    @Override
    public boolean onTouch(final View touchEventView, MotionEvent event) {
        // The kind of action being performed
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                enableSend = false;
                if (longPress) {
                    handler.removeCallbacksAndMessages(null);
                    if (listener != null) {
                        listener.longPressEndForView(touchEventView);
                    }
                    longPress = false;
                }
                else {
                    HerculesApp app = (HerculesApp) touchEventView.getContext().getApplicationContext();
                    app.sendKeyForView(view);
                    touchEventView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            touchEventView.setPressed(false);
                        }
                    }, 25);
                    return true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                enableSend = true;
                break;
        }
        return false;
    }

    /**
     * Handles sending bluetooth key events.
     */
    @Override
    public void run() {
        if (!enableSend) {
            return;
        }
        if (listener != null) {
            listener.sendKeyEventForView(view);
        }
        handler.postDelayed(this, 500);
    }

    /**
     * Creates address new instance of {@link SendKeyEventOnHoldController}
     *
     * @param view The ui view to send key events for
     * @param listener The listener to send key events for ui view
     * @return {@link SendKeyEventOnHoldController}
     */
    public static SendKeyEventOnHoldController init(View view,
                                                    SendViewKeyEventListener listener) {
        return new SendKeyEventOnHoldController(view, listener);
    }

    /**
     * Listener to send key events for view
     */
    public interface SendViewKeyEventListener {

        /**
         * Sends key event for view
         *
         * @param view The view to send key events for
         */
        void sendKeyEventForView(View view);

        /**
         * Long press has ended event for view
         *
         * @param view The view to send events for
         */
        void longPressEndForView(View view);

    }
}