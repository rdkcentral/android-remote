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

import android.bluetooth.BluetoothDevice;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * This class helps process parcels
 *
 * @version 1.0.0
 */
@Parcel
public class Device implements Serializable {

    public String name;
    public String address;
    public boolean connected;

    public Device() {
    }

    public Device(BluetoothDevice device) {
        this.name = device.getName();
        this.address = device.getAddress();
    }

    public Device(String address, boolean connected) {
        this.address = address;
        this.connected = connected;
    }

    @Override
    public String toString() {
        return name;
    }
}
