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

// The following imports are listed in the alphabetic order, and these imports are needed for BLE support.
// So the code snippet might be the same as other BLE open source.
// The only way to make it unique is to change them to import android.bluetooth.*
// But that's not a good practice of Java.
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import com.comcast.BuildConfig;
import com.comcast.HerculesApp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED;
import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;


/**
 * This class implements Bluetooth LE HID Service
 *
 * @version 1.0.0
 */
public class HIDService extends Service {

    public static final String TAG = HIDService.class.getSimpleName();
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    // GATT Service UUIDs
    // 1. Human Interface Device
    private static final UUID SERVICE_HID_OVER_GATT_UUID = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb");
    // 2. Battery Service
    private static final UUID SERVICE_BATTERY_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    // 3. Device information
    private static final UUID SERVICE_DEVICE_INFO_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");

    // GATT Characteristic UUIDs
    // 1. Protocol Mode
    private static final UUID PROTOCOL_MODE_UUID = UUID.fromString("00002A4E-0000-1000-8000-00805f9b34fb");
    // 2. Report
    private static final UUID REPORT_UUID = UUID.fromString("00002A4D-0000-1000-8000-00805f9b34fb");
    // 3. Report Map
    private static final UUID REPORT_MAP_UUID = UUID.fromString("00002A4B-0000-1000-8000-00805f9b34fb");
    // 4. HID Information
    private static final UUID HID_INFO_UUID = UUID.fromString("00002A4A-0000-1000-8000-00805f9b34fb");
    // 5. HID Control Point
    private static final UUID HID_CONTROL_POINT_UUID = UUID.fromString("00002A4C-0000-1000-8000-00805f9b34fb");
    // 6. Battery Level
    private static final UUID BATTERY_LEVEL_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");
    // 7. Manufacturer Name String
    private static final UUID MODEL_NUMBER_UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");
    // 8. Serial Number String
    private static final UUID SERIAL_NUMBER_UUID = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb");

    // GATT Descriptors
    // 1. Report Reference Descriptor
    private static final UUID DESCRIPTOR_REPORT_REFERENCE_UUID = UUID.fromString("00002908-0000-1000-8000-00805f9b34fb");
    // 2. Client Characteristic Configuration
    private static final UUID DESCRIPTOR_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Sample Characteristics.
    // 1. Manufacturer Name String
    private static final UUID VENDOR_NAME_UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");

    private static final int PROTOCOL_MODE_REPORT = 1;


    private static final byte[] keyboardReportMap =
            HIDService.hexStringToByteArray("05010906A101050719E029E71500250175019508810295017508810195057508150025650507190029688100C0");

//            0x05, 0x01,        // Usage Page (Generic Desktop Ctrls)
//            0x09, 0x06,        // Usage (Keyboard)
//            0xA1, 0x01,        // Collection (Application)
//            0x05, 0x07,        //   Usage Page (Kbrd/Keypad)
//            0x19, 0xE0,        //   Usage Minimum (0xE0)
//            0x29, 0xE7,        //   Usage Maximum (0xE7)
//            0x15, 0x00,        //   Logical Minimum (0)
//            0x25, 0x01,        //   Logical Maximum (1)
//            0x75, 0x01,        //   Report Size (1)
//            0x95, 0x08,        //   Report Count (8)
//            0x81, 0x02,        //   Input (Data,Var,Abs)
//            0x95, 0x01,        //   Report Count (1)
//            0x75, 0x08,        //   Report Size (8)
//            0x81, 0x01,        //   Input (Const)
//            0x95, 0x05,        //   Report Count (5)
//            0x75, 0x08,        //   Report Size (8)
//            0x15, 0x00,        //   Logical Minimum (0)
//            0x25, 0x65,        //   Logical Maximum (101)
//            0x05, 0x07,        //   Usage Page (Kbrd/Keypad)
//            0x19, 0x00,        //   Usage Minimum (0x00)
//            0x29, 0x68,        //   Usage Maximum (0x68)
//            0x81, 0x00,        //   Input (Data,Array)
//            0xC0,              // End Collection

    // USB spec keyboard including output for leds:
    //  HIDService.hexStringToByteArray("05010906a101050719e029e71500250175019508810295017508810395057501050819012905910295017503910395067508150025650507190029658100c0");
    //            0x85, 0x01,       			//     REPORT_ID (1)


    private BluetoothAdapter mAdapter;
    private BluetoothManager mManager;
    private BluetoothLeAdvertiser mAdvertiser;
    private BluetoothGattServer mGattServer;
    private BluetoothGattCharacteristic mInputReportCharacteristic;
    @Nullable
    private Device mDevice;

    private AdvertiseCallback mAdvertiseCallback;
    private Handler mHandler;
    private boolean wasDisable = false;

    private final Queue<byte[]> mDataQueue = new ConcurrentLinkedQueue<>();
    private Map<String, Device> mPairedDevices = new HashMap<>();
    private KeyManager mKeyManager = new KeyManager();

    private final Queue<BluetoothGattService> mGattServiceQueue = new ConcurrentLinkedQueue<>();

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public HIDService getService() {
            // Return this instance of HIDService so clients can call public methods
            return HIDService.this;
        }
    }

    public HIDService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (intent.hasExtra(EXTRA_DEVICE)) {
            mDevice = (Device) intent.getSerializableExtra(EXTRA_DEVICE);
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (intent.hasExtra(EXTRA_DEVICE)) {
            mDevice = (Device) intent.getSerializableExtra(EXTRA_DEVICE);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(this.mBLDeviceBondStateReceiver,
                new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        mManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mManager == null) {
            return;
        }

        mAdapter = mManager.getAdapter();
        if (mAdapter == null) {
            return;
        }
        if (!mAdapter.isEnabled()) {
            wasDisable = true;
            registerReceiver(this.mBLAdapterStateReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            mAdapter.enable();
        }
        else {
            initGattServer();
        }

    }

    @Override
    public void onDestroy() {
        HerculesApp.setLatestBondedDeviceAddress(this, null);
        stopAdvertising();
        if (wasDisable) {
            unregisterReceiver(this.mBLAdapterStateReceiver);
        }

        if (mGattServer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // do not cleanup service prior to Oreo
                mGattServer.clearServices();
                mGattServer.close();
            } else {
                mGattServer.close();
            }
        }
        unregisterReceiver(this.mBLDeviceBondStateReceiver);
        super.onDestroy();
    }

    public void clearQueue() {
        mDataQueue.clear();
    }

    private final BroadcastReceiver mBLAdapterStateReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, Integer.MIN_VALUE)) {
                    case BluetoothAdapter.STATE_ON:
                        unregisterReceiver(mBLAdapterStateReceiver);
                        wasDisable = false;
                        initGattServer();
                        return;
                    default:
                }
            }
        }
    };

    private final BroadcastReceiver mBLDeviceBondStateReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, Integer.MIN_VALUE)) {
                    case BOND_BONDED:
                        mHandler.post(() -> {
                            Log.d(TAG, "Connecting in response to device Bonded broadcast");
                            if (mGattServer != null && shouldConnect(bluetoothDevice)) {
                                mGattServer.connect(bluetoothDevice, true);
                                HerculesApp.setLatestBondedDeviceAddress(getApplicationContext(), bluetoothDevice.getAddress());
                            }
                        });
                        storeDevice(bluetoothDevice, shouldConnect(bluetoothDevice));
                        return;
                    default:
                }
            }
        }
    };

    private boolean shouldConnect(BluetoothDevice bluetoothDevice) {
        if (mDevice == null) {
            return true;
        }
        return mDevice.address.equalsIgnoreCase(bluetoothDevice.getAddress());
    }

    private void initGattServer() {

        Log.d(TAG, "Starting gatt..");
        mAdvertiser = this.mAdapter.getBluetoothLeAdvertiser();

        mGattServer = mManager.openGattServer(this, mGattServerCallback);
        if (mGattServer == null) {
            return;
        }
        if (mGattServer.getServices().size() == 0) {
            this.mGattServiceQueue.offer(hidService(true, true, false));
            this.mGattServiceQueue.offer(batteryService());
            this.mGattServiceQueue.offer(deviceInfoService());
            addGattService(mGattServer, this.mGattServiceQueue.poll());
        }
        else {
            initServiceAdvertiser();
        }
        initHandler();
    }

    private void closeGattServer() {
        stopAdvertising();
        if (mGattServer != null) {
            mGattServer.close();
            mGattServer = null;
        }
    }

    private void addGattService(BluetoothGattServer bluetoothGattServer, BluetoothGattService bluetoothGattService) {
        int i = 0;
        while (i < 5) {
            try {
                if (!bluetoothGattServer.addService(bluetoothGattService)) {
                    i++;
                } else {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.human_interface_device.xml&u=org.bluetooth.service.human_interface_device.xml
    // at least one of report types has to be supported, we only need input report
    private BluetoothGattService hidService(boolean inputReport, boolean outputReport, boolean featureReport) {
        BluetoothGattService bluetoothGattService = new BluetoothGattService(SERVICE_HID_OVER_GATT_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        do {
        } while (!bluetoothGattService.addCharacteristic(new BluetoothGattCharacteristic(HID_INFO_UUID, PROPERTY_READ, PERMISSION_READ_ENCRYPTED)));

        do {
        } while (!bluetoothGattService.addCharacteristic(new BluetoothGattCharacteristic(REPORT_MAP_UUID, PROPERTY_READ, PERMISSION_READ_ENCRYPTED)));

        BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(PROTOCOL_MODE_UUID, PROPERTY_READ | PROPERTY_WRITE_NO_RESPONSE, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED);
        bluetoothGattCharacteristic.setWriteType(WRITE_TYPE_NO_RESPONSE);
        do {
        } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));

        bluetoothGattCharacteristic = new BluetoothGattCharacteristic(HID_CONTROL_POINT_UUID, PROPERTY_WRITE_NO_RESPONSE, PERMISSION_WRITE_ENCRYPTED);
        bluetoothGattCharacteristic.setWriteType(WRITE_TYPE_NO_RESPONSE);
        do {
        } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));

        if (inputReport) {
            bluetoothGattCharacteristic = new BluetoothGattCharacteristic(REPORT_UUID, PROPERTY_READ | PROPERTY_WRITE | PROPERTY_NOTIFY, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED);
            BluetoothGattDescriptor bluetoothGattDescriptor = new BluetoothGattDescriptor(DESCRIPTOR_CHARACTERISTIC_CONFIG_UUID, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED | BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGattCharacteristic.addDescriptor(bluetoothGattDescriptor);
            bluetoothGattCharacteristic.addDescriptor(new BluetoothGattDescriptor(DESCRIPTOR_REPORT_REFERENCE_UUID, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED));
            do {
            } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));
            this.mInputReportCharacteristic = bluetoothGattCharacteristic;
        }
        if (outputReport) {
            bluetoothGattCharacteristic = new BluetoothGattCharacteristic(REPORT_UUID, PROPERTY_READ | PROPERTY_WRITE_NO_RESPONSE | PROPERTY_WRITE, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED);
            bluetoothGattCharacteristic.setWriteType(WRITE_TYPE_NO_RESPONSE);
            bluetoothGattCharacteristic.addDescriptor(new BluetoothGattDescriptor(DESCRIPTOR_REPORT_REFERENCE_UUID, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED));
            do {
            } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));
        }
        if (featureReport) {
            bluetoothGattCharacteristic = new BluetoothGattCharacteristic(REPORT_UUID, PROPERTY_READ | PROPERTY_WRITE, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED);
            bluetoothGattCharacteristic.addDescriptor(new BluetoothGattDescriptor(DESCRIPTOR_REPORT_REFERENCE_UUID, PERMISSION_WRITE_ENCRYPTED | PERMISSION_READ_ENCRYPTED));
            do {
            } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));
        }

        return bluetoothGattService;
    }

    public void sendKey(int i, boolean z) {
        if (z) {
            this.mKeyManager.key(i);
        } else {
            this.mKeyManager.key(Integer.valueOf(i));
        }
        sendArray(this.mKeyManager.build());
    }

    public void sendPointer(int i, int i2, boolean z) {
        byte[] bArr = new byte[5];
        bArr[0] = (byte) 2;
        bArr[1] = (byte) i;
        bArr[2] = (byte) i2;
        if (z) {
            bArr[3] = (byte) 1;
        }
        sendArray(bArr);
    }

    private void storeDevice(BluetoothDevice bluetoothDevice, boolean connected) {
        String address = bluetoothDevice.getAddress();
        String name = bluetoothDevice.getName();
        if (name == null) {
            name = address;
        }
        if (this.mPairedDevices.containsKey(address)) {
            Device bVar = this.mPairedDevices.get(address);
            if (bVar.connected != connected) {
                bVar.connected = connected;
                bVar.address = name;
            }
        } else if (connected) {
            this.mPairedDevices.put(address, new Device(name, connected));
        }
    }

    private boolean compareCharacteristic(UUID uuid, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return bluetoothGattCharacteristic.getUuid().equals(uuid);
    }

    private boolean compareDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor, UUID uuid, UUID uuid2) {
        return bluetoothGattDescriptor.getCharacteristic().getUuid().equals(uuid) && bluetoothGattDescriptor.getUuid().equals(uuid2);
    }

    private byte[] getDescriptorValue(BluetoothGattDescriptor bluetoothGattDescriptor, int offset) {
        if (compareDescriptor(bluetoothGattDescriptor, REPORT_UUID, DESCRIPTOR_REPORT_REFERENCE_UUID)) {
            int properties = bluetoothGattDescriptor.getCharacteristic().getProperties();
            if (properties == 26) {
                return new byte[]{(byte) 0, (byte) 1}; // input
            }
            if (properties == 14) {
                return new byte[]{(byte) 0, (byte) 2}; // output
            }
            if (properties == 10) {
                return new byte[]{(byte) 0, (byte) 3}; // feature
            }
        } else if (compareDescriptor(bluetoothGattDescriptor, REPORT_UUID, DESCRIPTOR_CHARACTERISTIC_CONFIG_UUID) || compareDescriptor(bluetoothGattDescriptor, REPORT_UUID, DESCRIPTOR_REPORT_REFERENCE_UUID)) {
            return BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        }
        return new byte[0];
    }

    private byte[] getCharacteristicValue(BluetoothGattCharacteristic bluetoothGattCharacteristic, int offset) {
        if (compareCharacteristic(REPORT_MAP_UUID, bluetoothGattCharacteristic)) {
            return Arrays.copyOfRange(keyboardReportMap, offset, keyboardReportMap.length - offset);
        }
        if (compareCharacteristic(HID_CONTROL_POINT_UUID, bluetoothGattCharacteristic)) {
            return new byte[]{(byte) 0};
        }
        if (compareCharacteristic(REPORT_UUID, bluetoothGattCharacteristic)) {
            return new byte[]{};
        }
        if (compareCharacteristic(PROTOCOL_MODE_UUID, bluetoothGattCharacteristic)) {
            return new byte[]{(byte) PROTOCOL_MODE_REPORT};
        }
        if (compareCharacteristic(HID_INFO_UUID, bluetoothGattCharacteristic)) {
            return new byte[]{(byte) 17, (byte) 1, (byte) 0, (byte) 3}; // 11.1, not localized, supports wake & is connectible
        }
        if (compareCharacteristic(VENDOR_NAME_UUID, bluetoothGattCharacteristic)) {
            return "AndroidRemote".getBytes(StandardCharsets.UTF_8);
        }
        if (compareCharacteristic(SERIAL_NUMBER_UUID, bluetoothGattCharacteristic)) {
            return "1.0".getBytes(StandardCharsets.UTF_8);
        }
        if (compareCharacteristic(MODEL_NUMBER_UUID, bluetoothGattCharacteristic)) {
            return "AndroidRemote".getBytes(StandardCharsets.UTF_8);
        }
        if (compareCharacteristic(BATTERY_LEVEL_UUID, bluetoothGattCharacteristic)) {
            return new byte[]{(byte) 100};
        }

        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value != null)
            return Arrays.copyOfRange(value, offset, value.length - offset);
        return null;
    }

    protected final void sendArray(byte[] bArr) {
        if (bArr.length > 0) {
            Log.d(TAG, "Sending: " + bytesToHex(bArr));
            this.mDataQueue.offer(bArr);
        }
    }


    /**
     * The following code, the method bytesToHex and hexStringToByteArray are
     * based on other sources and MIT-licensed at:
     * https://github.com/cloudinary/cloudinary_java/blob/master/LICENSE
     *
     * The original source can be accessed from
     * https://github.com/cloudinary/cloudinary_java/blob/48e64f7d563821d5c6a642cd86900862b989228b/cloudinary-core/src/main/java/com/cloudinary/utils/StringUtils.java#L93
     *
     */

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * converts byte array to hex string
     *
     * @param bytes byte array
     * @return hex string
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     * converts hex string to byte array
     *
     * @param s hex string
     * @return byte array
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void initHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getApplicationContext().getMainLooper());
            Timer mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    byte[] polled = mDataQueue.poll();
                    if (polled != null && mInputReportCharacteristic != null) {
                        mInputReportCharacteristic.setValue(polled);
                        mHandler.post(() -> {
                            for (BluetoothDevice bluetoothDevice : mManager.getConnectedDevices(BluetoothProfile.GATT)) {
                                try {
                                    Device device = mPairedDevices.get(bluetoothDevice.getAddress());
                                    if (mGattServer != null
                                            && device != null
                                            && device.connected
                                            && shouldConnect(bluetoothDevice)) {
                                        mGattServer.notifyCharacteristicChanged(bluetoothDevice, mInputReportCharacteristic, false);
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        });
                    }
                }
            }, 0, BuildConfig.SCHEDULE_KEY_TIME);
        }
    }

    private void initServiceAdvertiser() {
        Log.d(TAG, "Starting advertiser");
        if (this.mAdvertiseCallback == null && this.mAdvertiser != null) {
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(true)
                    .setTimeout(0)
                    .build();
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(false)
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_HID_OVER_GATT_UUID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_BATTERY_UUID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_DEVICE_INFO_UUID.toString()))
                    .build();
            AdvertiseData scanResponse = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_HID_OVER_GATT_UUID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_BATTERY_UUID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(SERVICE_DEVICE_INFO_UUID.toString()))
                    .build();
            this.mAdvertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                }
            };
            this.mAdvertiser.startAdvertising(advertiseSettings, advertiseData, scanResponse, this.mAdvertiseCallback);
            if (this.mGattServer != null) {
                Log.d(TAG, "Checking bonded devices..");
                Set<BluetoothDevice> bonded = mAdapter.getBondedDevices();
                for (BluetoothDevice device : bonded) {
                    storeDevice(device, shouldConnect(device));
                }
            }
        }
    }

    private void stopAdvertising() {
        if (mAdvertiser != null && mAdvertiseCallback != null) {
            mAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertiseCallback = null;
        }
    }

    private static BluetoothGattService deviceInfoService() {
        BluetoothGattService bluetoothGattService = new BluetoothGattService(SERVICE_DEVICE_INFO_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        do {
        } while (!bluetoothGattService.addCharacteristic(new BluetoothGattCharacteristic(VENDOR_NAME_UUID, PROPERTY_READ, PERMISSION_READ_ENCRYPTED)));
        do {
        } while (!bluetoothGattService.addCharacteristic(new BluetoothGattCharacteristic(MODEL_NUMBER_UUID, PROPERTY_READ, PERMISSION_READ_ENCRYPTED)));
        do {
        } while (!bluetoothGattService.addCharacteristic(new BluetoothGattCharacteristic(SERIAL_NUMBER_UUID, PROPERTY_READ, PERMISSION_READ_ENCRYPTED)));
        return bluetoothGattService;
    }

    private static BluetoothGattService batteryService() {
        BluetoothGattService bluetoothGattService = new BluetoothGattService(SERVICE_BATTERY_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(BATTERY_LEVEL_UUID, PROPERTY_NOTIFY | PROPERTY_READ, PERMISSION_READ_ENCRYPTED);
        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(DESCRIPTOR_CHARACTERISTIC_CONFIG_UUID, BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        configDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        do {
        } while (!bluetoothGattCharacteristic.addDescriptor(configDescriptor));
        do {
        } while (!bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic));
        return bluetoothGattService;
    }

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        public void onConnectionStateChange(BluetoothDevice bluetoothDevice, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange() called with: device = [" + bluetoothDevice.getAddress() + "], status = [" + status + "], newState = [" + newState + "] bondState: " + bluetoothDevice.getBondState());
            boolean connected = newState == BluetoothAdapter.STATE_CONNECTED;
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                if (!connected) {
                    mHandler.post(() -> {
                        Log.d(TAG, "Connecting in connection state change mHandler");
                        if (mGattServer != null && shouldConnect(bluetoothDevice)) {
                            mGattServer.connect(bluetoothDevice, true);
                            HerculesApp.setLatestBondedDeviceAddress(getApplicationContext(),
                                    bluetoothDevice.getAddress());
                        }
                    });
                } else {
                    mHandler.post(() -> stopAdvertising());
                }
                storeDevice(bluetoothDevice, connected);
            }
            else if (connected) {
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                    mHandler.post(() -> {
                        stopAdvertising();

                        Log.d(TAG, "Initiating bonding");
                        if (shouldConnect(bluetoothDevice)) {
                            try {
                                bluetoothDevice.setPairingConfirmation(true);
                            } catch (Exception ignored) {
                            }
                            bluetoothDevice.createBond();
                        }
                    });
                }
            }
            else {
                initServiceAdvertiser();
            }
        }

        public void onServiceAdded(int status, BluetoothGattService bluetoothGattService) {
            Log.d(TAG, "onServiceAdded() called with: status = [" + status + "], service = [" + bluetoothGattService.getUuid() + "] " + bluetoothGattService.getCharacteristics().size());
            if (status != 0) {
                mGattServer.addService(bluetoothGattService);
            }
            else if (mGattServiceQueue.isEmpty()) {
                initServiceAdvertiser();
            }
            else {
                mGattServer.addService(mGattServiceQueue.poll());
            }
        }

        private byte[] log(String name, byte[] value) {
            Log.d(TAG, name + " value: " + (value == null ? "null" : bytesToHex(value)));
            return value;
        }

        /*
        * The following code is based on sources from the developer.android.com website
        * Copyright (C) 2017 The Android Open Source Project
        * Licensed under the Apache License, Version 2.0 (the "License");
        */
        
        public void onCharacteristicReadRequest(BluetoothDevice bluetoothDevice, int requestId, int offset, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            mHandler.post(() -> {
                Log.d(TAG, "onCharacteristicReadRequest");
                if (mGattServer == null)
                    return;
                mGattServer.sendResponse(bluetoothDevice, requestId, 0, offset, log("Sending characteristic " + bluetoothGattCharacteristic.getUuid(), getCharacteristicValue(bluetoothGattCharacteristic, offset)));
            });

        }

        public void onCharacteristicWriteRequest(BluetoothDevice bluetoothDevice, int requestId, BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            bluetoothGattCharacteristic.setValue(value);
            log("Writing characteristic " + bluetoothGattCharacteristic.getUuid(), value);
            if (responseNeeded) {
                mGattServer.sendResponse(bluetoothDevice, requestId, 0, 0, new byte[]{});
            }
        }

        public void onDescriptorReadRequest(BluetoothDevice bluetoothDevice, int requestId, int offset, BluetoothGattDescriptor bluetoothGattDescriptor) {
            mHandler.post(() -> {
                Log.d(TAG, "onDescriptorReadRequest");
                if (mGattServer == null)
                    return;
                mGattServer.sendResponse(bluetoothDevice, requestId, 0, offset, log("Sending descriptor " + bluetoothGattDescriptor.getUuid(), getDescriptorValue(bluetoothGattDescriptor, offset)));
            });
        }

        public void onDescriptorWriteRequest(BluetoothDevice bluetoothDevice, int requestId, BluetoothGattDescriptor bluetoothGattDescriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            bluetoothGattDescriptor.setValue(value);
            log("Writing descriptor " + bluetoothGattDescriptor.getUuid(), value);
            if (compareDescriptor(bluetoothGattDescriptor, REPORT_UUID, DESCRIPTOR_CHARACTERISTIC_CONFIG_UUID)) {
                storeDevice(bluetoothDevice, true);
            }
            if (responseNeeded) {
                mGattServer.sendResponse(bluetoothDevice, requestId, 0, 0, new byte[]{});
            }
        }

        public void onExecuteWrite(BluetoothDevice bluetoothDevice, int requestId, boolean execute) {
            Log.d(TAG, "onExecuteWrite() called with: device = [" + bluetoothDevice.getAddress() + "], requestId = [" + requestId + "] execute: " + (execute ? "true" : "false"));
        }

        public void onNotificationSent(BluetoothDevice bluetoothDevice, int status) {
            Log.d(TAG, "onNotificationSent() called with: device = [" + bluetoothDevice.getAddress() + "], status = [" + status + "] bondState: " + bluetoothDevice.getBondState());
        }
    };

}
