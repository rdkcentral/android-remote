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

package com.comcast.constant;

import android.view.KeyEvent;

import com.comcast.R;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps button resource id to the correlated key code
 *
 * @version 1.0.0
 */
public class KeyLayoutMap {

    private static final Map<Integer, Integer[]> sKeyLayoutMap = new HashMap<>();

    // Mapping all KeyCode for the UI elements
    static {
        sKeyLayoutMap.put(R.id.remote_numpad_0, new Integer[] { KeyEvent.KEYCODE_0 });
        sKeyLayoutMap.put(R.id.remote_numpad_1, new Integer[] { KeyEvent.KEYCODE_1 });
        sKeyLayoutMap.put(R.id.remote_numpad_2, new Integer[] { KeyEvent.KEYCODE_2 });
        sKeyLayoutMap.put(R.id.remote_numpad_3, new Integer[] { KeyEvent.KEYCODE_3 });
        sKeyLayoutMap.put(R.id.remote_numpad_4, new Integer[] { KeyEvent.KEYCODE_4 });
        sKeyLayoutMap.put(R.id.remote_numpad_5, new Integer[] { KeyEvent.KEYCODE_5 });
        sKeyLayoutMap.put(R.id.remote_numpad_6, new Integer[] { KeyEvent.KEYCODE_6 });
        sKeyLayoutMap.put(R.id.remote_numpad_7, new Integer[] { KeyEvent.KEYCODE_7 });
        sKeyLayoutMap.put(R.id.remote_numpad_8, new Integer[] { KeyEvent.KEYCODE_8 });
        sKeyLayoutMap.put(R.id.remote_numpad_9, new Integer[] { KeyEvent.KEYCODE_9 });

        sKeyLayoutMap.put(R.id.remote_dpad_up, new Integer[] {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_UP });
        sKeyLayoutMap.put(R.id.remote_dpad_down, new Integer[] {
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_DOWN });
        sKeyLayoutMap.put(R.id.remote_dpad_left, new Integer[] {
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_LEFT });
        sKeyLayoutMap.put(R.id.remote_dpad_right, new Integer[] {
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_SYSTEM_NAVIGATION_RIGHT });
        sKeyLayoutMap.put(R.id.remote_dpad_circle, new Integer[] {
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER });

        sKeyLayoutMap.put(R.id.remote_button_ch_down, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_DPAD_DOWN });
        sKeyLayoutMap.put(R.id.remote_button_ch_up, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_DPAD_UP });

        sKeyLayoutMap.put(R.id.remote_button_pg_down, new Integer[] { KeyEvent.KEYCODE_PAGE_DOWN });
        sKeyLayoutMap.put(R.id.remote_button_pg_up, new Integer[] { KeyEvent.KEYCODE_PAGE_UP });
        sKeyLayoutMap.put(R.id.remote_button_play, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_P});
        sKeyLayoutMap.put(R.id.remote_button_forward, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_F });
        sKeyLayoutMap.put(R.id.remote_button_previous, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_W });

        sKeyLayoutMap.put(R.id.remote_button_info, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_I });
        sKeyLayoutMap.put(R.id.remote_button_record, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT,
                KeyEvent.KEYCODE_R });


        // STB (Xi 5) key mapping
        // Exit - Ctrl + E
        sKeyLayoutMap.put(R.id.remote_button_exit, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_E });

        // Guide - Ctrl + G
        sKeyLayoutMap.put(R.id.remote_button_guide, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_G });

        // Menu - Ctrl + M
        sKeyLayoutMap.put(R.id.remote_button_xfinity, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_M });

        // A - Ctrl + 0
        sKeyLayoutMap.put(R.id.remote_dpad_a, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_0 });

        // B - Ctrl + 1
        sKeyLayoutMap.put(R.id.remote_dpad_b, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_1 });

        // C - Ctrl + 2
        sKeyLayoutMap.put(R.id.remote_dpad_c, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_2 });

        // D - Ctrl + 3
        sKeyLayoutMap.put(R.id.remote_dpad_d, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_3 });

        // Last - Ctrl + L
        sKeyLayoutMap.put(R.id.remote_button_left_arrow, new Integer[] {
                KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_L });
    }

    /**
     * Returns an array of KeyCode correlated with the button layout id.
     * @param layoutId  The button resource id
     * @return An Arrayof keycode
     */
    public static Integer[] getKeyCode(int layoutId) {
        return sKeyLayoutMap.get(layoutId);
    }
}
