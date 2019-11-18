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

import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.comcast.R;
import com.comcast.service.Device;

import java.util.ArrayList;
import java.util.List;

public class PairedDeviceListDialogFragment extends DialogFragment {

    public static final String TAG = PairedDeviceListDialogFragment.class.getSimpleName();

    private ArrayAdapter<Device> adapter;
    private OnDeviceSelectedListener onDeviceSelectedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.dialog_paired_device_list_message));
        builder.setNegativeButton(android.R.string.cancel, null);
        List<Device> bluetoothDevices = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice bt : bluetoothAdapter.getBondedDevices()) {
            bluetoothDevices.add(new Device(bt));
        }
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, bluetoothDevices);
        builder.setAdapter(adapter, this::onDeviceClick);
        return builder.create();
    }

    private void onDeviceClick(DialogInterface dialogInterface, int position) {
        if (onDeviceSelectedListener == null || adapter == null) {
            return;
        }
        Device device = adapter.getItem(position);
        onDeviceSelectedListener.onDeviceSelected(device);
    }

    public PairedDeviceListDialogFragment setOnDeviceSelectedListener(
            OnDeviceSelectedListener onDeviceSelectedListener) {
        this.onDeviceSelectedListener = onDeviceSelectedListener;
        return this;
    }

    public interface OnDeviceSelectedListener {
        void onDeviceSelected(Device device);
    }
}
