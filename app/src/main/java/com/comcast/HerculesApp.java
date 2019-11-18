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

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.comcast.constant.KeyLayoutMap;
import com.comcast.service.Device;
import com.comcast.service.HIDService;

import java.util.Set;

public class HerculesApp extends Application {

    private static final String PREF_CONNECTED_DEVICE = "PREF_CONNECTED_DEVICE";
    private static final String PREF_LATEST_DEVICE_ADDRESS = "PREF_LATEST_DEVICE_ADDRESS";

    /**
     * HIDService
     */
    private HIDService mService;

    /**
     * Flag to store HIDService binding status
     */
    private boolean mBound = false;


    /**
     * Called when the application is created
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Add a new ComponentCallbacks to the base application of the Context,
        // which will be called at the same times as the ComponentCallbacks methods
        // of activities and other components are called.
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            // The following are boilerplate code for implementing `ActivityLifecycleCallbacks` interface

            /**
             * Called when the Activity calls Activity#onCreate.
             *
             * @param activity
             * @param savedInstanceState
             */
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            /**
             * Called when the Activity calls Activity#onStart.
             *
             * @param activity
             */
            @Override
            public void onActivityStarted(Activity activity) {

            }

            /**
             * Called when the Activity calls Activity#onResume.
             *
             * @param activity
             */
            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof ControlActivity) {
                    connectHIDService(activity);
                }
            }

            /**
             * Called when the Activity calls Activity#onPause.
             *
             * @param activity
             */
            @Override
            public void onActivityPaused(Activity activity) {

            }

            /**
             * Called when the Activity calls Activity#onStop.
             *
             * @param activity
             */
            @Override
            public void onActivityStopped(Activity activity) {

            }

            /**
             * Called when the Activity calls Activity#onSaveInstanceState
             *
             * @param activity
             * @param outState
             */
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            /**
             * Called when the Activity calls Activity#onDestroy.
             *
             * @param activity
             */
            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        disconnectHIDService();
    }

    private synchronized void connectHIDService(Activity activity) {
        if (mBound) {
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String connectedDevice = getConnectedDevice(this);
        String latestBondedDeviceAddress = getLatestBondedDeviceAddress(this);
        if (bluetoothAdapter == null ||
                (latestBondedDeviceAddress != null && latestBondedDeviceAddress.equalsIgnoreCase(connectedDevice))) {
            startHidService(new Device(getLatestBondedDeviceAddress(this), false));
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (fragmentManager.findFragmentByTag(PairedDeviceListDialogFragment.TAG) == null) {
                PairedDeviceListDialogFragment dialogFragment = new PairedDeviceListDialogFragment()
                        .setOnDeviceSelectedListener(device -> {
                            if (connectedDevice != null) {
                                displayStepsToConnectAlert(activity, device);
                            } else {
                                startHidService(device);
                            }
                        });
                dialogFragment.show(fragmentManager, PairedDeviceListDialogFragment.TAG);
            }
        } else {
            startHidService(null);
        }
    }

    private void displayStepsToConnectAlert(Activity activity, Device device) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle("No valid existing bluetooth connection")
                .setPositiveButton("OK", (dialog, which) -> {
                    startHidService(device);
                })
                .setMessage(
                        "The existing bluetooth connection does not include HID profile.\n\n" +
                                "Please follow below steps:\n\n" +
                                "1) Remove the existing pairing with the device.\n\n" +
                                "2)Launch the App and and then initiate a new pairing, if you want to control this device.")
                .create();
        alertDialog.show();
    }

    private void startHidService(Device device) {
        Intent intent = new Intent(this, HIDService.class);
        intent.putExtra(HIDService.EXTRA_DEVICE, device);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private synchronized void disconnectHIDService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * The following code is based on other sources from developer.android.com website
         * with the original license can be accessed from
         * https://developer.android.com/license
         *
         * The original source can be accessed from
         * https://developer.android.com/guide/components/bound-services#java
         *
         */

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            HIDService.LocalBinder binder = (HIDService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public synchronized void sendKey(int key, boolean z) {
        if (mBound && mService != null) {
            mService.sendKey(key, z);
        }
    }

    public synchronized void sendKeyForView(View view) {
        if (mBound && mService != null) {
            Integer[] keyCode = KeyLayoutMap.getKeyCode(view.getId());
            if (keyCode != null && keyCode.length > 0) {
                for (int key : keyCode) {
                    mService.sendKey(key, true);
                }
                for (int key : keyCode) {
                    mService.sendKey(key, false);
                }
            }
        }
    }

    public synchronized void clearKeyQueue() {
        if (mBound && mService != null) {
            mService.clearQueue();
        }
    }

    public static String getConnectedDevice(Context context) {
        return context.getSharedPreferences(HerculesApp.class.getSimpleName(), Context.MODE_PRIVATE)
                .getString(PREF_CONNECTED_DEVICE, null);
    }

    public static void setConnectedDevice(Context context, String address) {
        context.getSharedPreferences(HerculesApp.class.getSimpleName(), Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_CONNECTED_DEVICE, address)
                .apply();
    }

    public static String getLatestBondedDeviceAddress(Context context) {
        return context.getSharedPreferences(HerculesApp.class.getSimpleName(), Context.MODE_PRIVATE)
                .getString(PREF_LATEST_DEVICE_ADDRESS, null);
    }

    public static void setLatestBondedDeviceAddress(Context context, String deviceAddress) {
        context.getSharedPreferences(HerculesApp.class.getSimpleName(), Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_LATEST_DEVICE_ADDRESS, deviceAddress)
                .apply();
    }

}
