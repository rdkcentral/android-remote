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

package com.comcast.service;

import android.view.KeyEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.valueOf;


/**
 * This class implements HID Keyboard Manager
 *
 * @version 1.0.0
 */
public class KeyManager {

    private List<Integer> mHIDKeyboardCodeList = Collections.synchronizedList(new LinkedList<Integer>());
    private int b;

    /**
     * default constructor
     */
    KeyManager() {
    }


    /**
     * Put key code
     *
     * @param i int
     */
    public void key(int i) {
        if (KeyEvent.isModifierKey(i)) {
            this.b |= getHIDKeyModifierCode(i);
        } else {
            this.mHIDKeyboardCodeList.add(getHIDKeyboardCode(i));
        }
    }

    /**
     * Put key code
     *
     * @param num Integer
     */
    public void key(Integer num) {
        if (KeyEvent.isModifierKey(num.intValue())) {
            this.b &= getHIDKeyModifierCode(num.intValue()) ^ -1;
        } else {
            this.mHIDKeyboardCodeList.remove(getHIDKeyboardCode(num.intValue()));
        }
    }

    /**
     * get the HID Key Modifier Code
     *
     * @param androidKeyCode int
     * @return int
     */
    private int getHIDKeyModifierCode(int androidKeyCode) {
        switch (androidKeyCode) {
            case 57:
                return 4;
            case 59:
                return 2;
            case 113:
                return 1;
            case 117:
                return 16;
            default:
                return 0;
        }
    }

    /**
     * Build HID report data to send to connected device
     *
     * @return  byte[] the raw data.
     */
    public byte[] build() {
        byte[] bArr = new byte[8];
        // no report number
        bArr[0] = (byte) this.b; // modifier
        // [1] = 0 - reserved byte
        int i = 2; // 2-6 - keys
        for (Integer intValue : this.mHIDKeyboardCodeList) {
            int intValue2 = intValue;
            if (i >= bArr.length) {
                break;
            }
            bArr[i] = (byte) intValue2;
            i++;
        }
        return bArr;
    }

    /**
     * Get HID Keyboard Code
     *
     * @param androidKeyCode int
     * @return int
     */
    private static Integer getHIDKeyboardCode(int androidKeyCode) {
        switch (androidKeyCode) {
            case 7:
                return valueOf(39);
            case 8:
                return valueOf(30);
            case 9:
                return valueOf(31);
            case 10:
                return valueOf(32);
            case 11:
                return valueOf(33);
            case 12:
                return valueOf(34);
            case 13:
                return valueOf(35);
            case 14:
                return valueOf(36);
            case 15:
                return valueOf(37);
            case 16:
                return valueOf(38);
            case 19:
                return valueOf(82);
            case 20:
                return valueOf(81);
            case 21:
                return valueOf(80);
            case 22:
                return valueOf(79);
            case 24:
                return valueOf(128);
            case 25:
                return valueOf(129);
            case 26:
                return valueOf(102);
            case 29:
                return valueOf(4);
            case 30:
                return valueOf(5);
            case 31:
                return valueOf(6);
            case 32:
                return valueOf(7);
            case 33:
                return valueOf(8);
            case 34:
                return valueOf(9);
            case 35:
                return valueOf(10);
            case 36:
                return valueOf(11);
            case 37:
                return valueOf(12);
            case 38:
                return valueOf(13);
            case 39:
                return valueOf(14);
            case 40:
                return valueOf(15);
            case 41:
                return valueOf(16);
            case 42:
                return valueOf(17);
            case 43:
                return valueOf(18);
            case 44:
                return valueOf(19);
            case 45:
                return valueOf(20);
            case 46:
                return valueOf(21);
            case 47:
                return valueOf(22);
            case 48:
                return valueOf(23);
            case 49:
                return valueOf(24);
            case 50:
                return valueOf(25);
            case 51:
                return valueOf(26);
            case 52:
                return valueOf(27);
            case 53:
                return valueOf(28);
            case 54:
                return valueOf(29);
            case 55:
                return valueOf(54);
            case 56:
                return valueOf(55);
            case 57:
                return valueOf(226);
            case 58:
                return valueOf(230);
            case 59:
                return valueOf(225);
            case 60:
                return valueOf(229);
            case 61:
                return valueOf(43);
            case 62:
                return valueOf(44);
            case 66:
                return valueOf(40);
            case 67:
                return valueOf(42);
            case 68:
                return valueOf(53);
            case 69:
                return valueOf(45);
            case 70:
                return valueOf(46);
            case 71:
                return valueOf(47);
            case 72:
                return valueOf(48);
            case 73:
                return valueOf(49);
            case 74:
                return valueOf(51);
            case 75:
                return valueOf(52);
            case 76:
                return valueOf(56);
            case 82:
                return valueOf(101);
            case 85:
                return valueOf(232);
            case 86:
                return valueOf(120);
            case 89:
                return valueOf(241);
            case 90:
                return valueOf(242);
            case 92:
                return valueOf(75);
            case 93:
                return valueOf(78);
            case 111:
                return valueOf(41);
            case 112:
                return valueOf(76);
            case 113:
                return valueOf(224);
            case 114:
                return valueOf(228);
            case 115:
                return valueOf(57);
            case 116:
                return valueOf(71);
            case 117:
                return valueOf(227);
            case 118:
                return valueOf(231);
            case 120:
                return valueOf(70);
            case 121:
                return valueOf(72);
            case 122:
                return valueOf(74);
            case 123:
                return valueOf(77);
            case 124:
                return valueOf(73);
            case 131:
                return valueOf(58);
            case 132:
                return valueOf(59);
            case 133:
                return valueOf(60);
            case 134:
                return valueOf(61);
            case 135:
                return valueOf(62);
            case 136:
                return valueOf(63);
            case 137:
                return valueOf(64);
            case 138:
                return valueOf(65);
            case 139:
                return valueOf(66);
            case 140:
                return valueOf(67);
            case 141:
                return valueOf(68);
            case 142:
                return valueOf(69);
            case 143:
                return valueOf(83);
            case 144:
                return valueOf(98);
            case 145:
                return valueOf(89);
            case 146:
                return valueOf(90);
            case 147:
                return valueOf(91);
            case 148:
                return valueOf(92);
            case 149:
                return valueOf(93);
            case 150:
                return valueOf(94);
            case 151:
                return valueOf(95);
            case 152:
                return valueOf(96);
            case 153:
                return valueOf(97);
            case 154:
                return valueOf(84);
            case 155:
                return valueOf(85);
            case 156:
                return valueOf(86);
            case 157:
                return valueOf(87);
            case 158:
                return valueOf(99);
            case 159:
                return valueOf(133);
            case 160:
                return valueOf(88);
            case 161:
                return valueOf(103);
            case 162:
                return valueOf(182);
            case 163:
                return valueOf(183);
            case 164:
                return valueOf(127);
            default:
                return valueOf(0);
        }
    }
}
