package com.kongtech.plutocon.sdk.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.ParcelUuid;

import com.kongtech.plutocon.sdk.Plutocon;
import com.kongtech.plutocon.sdk.util.Plog;
import com.kongtech.plutocon.sdk.util.PlutoconUuid;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlutoconConnection {

    private Context context;

    private Plutocon plutocon;
    private BluetoothGatt bluetoothGatt;

    private ConcurrentHashMap<ParcelUuid, BluetoothGattCharacteristic> characteristics;

    private String address;
    private boolean isConnected;

    private OnConnectionStateChangeCallback onConnectionStateChangeCallback;
    private OnConnectionRemoteRssiCallback onConnectionRemoteRssiCallback;

    private PlutoconEditor editor;
    private PlutoconReader reader;

    public PlutoconConnection(Context context, Plutocon plutocon) {
        this.context = context;
        this.plutocon = plutocon;
        this.address = plutocon.getMacAddress();
        this.characteristics = new ConcurrentHashMap<>();
    }

    public PlutoconConnection(Context context, String address) {
        this.context = context;
        this.address = address;
        this.characteristics = new ConcurrentHashMap<>();
    }

    public Plutocon getPlutocon() {
        return this.plutocon;
    }

    public void connect(OnConnectionStateChangeCallback onConnectionStateChangeCallback) {

        this.onConnectionStateChangeCallback = onConnectionStateChangeCallback;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(this.address);

        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Plog.d("Connected to GATT server.");
                    Plog.d("Attempting to start service discovery:"
                            + PlutoconConnection.this.bluetoothGatt.discoverServices());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    PlutoconConnection.this.notifyDisconnected();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Plog.d("GATT Service discovered");
                    isConnected = true;
                    PlutoconConnection.this.getCharacteristicList();
                    PlutoconConnection.this.readDefaultProperty(new PlutoconOperator.OnOperationCompleteCallback() {
                        @Override
                        public void onOperationComplete(BluetoothGattCharacteristic characteristic, boolean isLast) {
                            if(PlutoconConnection.this.onConnectionStateChangeCallback != null && isLast){
                                PlutoconConnection.this.onConnectionStateChangeCallback.onConnectionStateConnected();
                            }
                        }
                    });
                } else {
                    Plog.d("GATT Service discover Failed");
                    PlutoconConnection.this.notifyDisconnected();
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                reader.operationComplete(characteristic, !reader.execute());
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                editor.operationComplete(characteristic, !editor.execute());
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                if(onConnectionRemoteRssiCallback != null){
                    onConnectionRemoteRssiCallback.onConnectionRemoteRssiCallback(rssi);
                }
            }
        });

    }

    private void readDefaultProperty(PlutoconOperator.OnOperationCompleteCallback onOperationCompleteCallback) {
        PlutoconReader reader = this.reader();
        reader.getProperty(PlutoconUuid.ADV_INTERVAL_CHARACTERISTIC)
                .getProperty(PlutoconUuid.TX_LEVEL_CHARACTERISTIC)
                .getProperty(PlutoconUuid.BATTERY_CHARACTERISTIC)
                .getProperty(PlutoconUuid.SOFTWARE_VERSION_CHARACTERISTIC)
                .getProperty(PlutoconUuid.HARDWARE_VERSION_CHARACTERISTIC)
                .getProperty(PlutoconUuid.MODEL_NUMBER_CHARACTERISTIC)
                .getProperty(PlutoconUuid.MANUFACTURE_NAME_CHARACTERISTIC)
                .getProperty(PlutoconUuid.UUID_CHARACTERISTIC)
                .setOnOperationCompleteCallback(onOperationCompleteCallback);
        reader.commit();
    }

    private void notifyDisconnected() {
        isConnected = false;
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.close();
            characteristics = null;
            if (onConnectionStateChangeCallback != null)
                onConnectionStateChangeCallback.onConnectionStateDisconnected();
        }
    }

    private void getCharacteristicList() {
        characteristics = new ConcurrentHashMap<>();
        for (BluetoothGattService service : bluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                characteristics.put(new ParcelUuid(characteristic.getUuid()), characteristic);
            }
        }
    }

    public void getRemoteRssi(OnConnectionRemoteRssiCallback onConnectionRemoteRssiCallback) {
        this.onConnectionRemoteRssiCallback = onConnectionRemoteRssiCallback;
        if (bluetoothGatt != null) bluetoothGatt.readRemoteRssi();
    }

    public int getAdvertisingInterval() {
        byte[] data = characteristics.get(PlutoconUuid.ADV_INTERVAL_CHARACTERISTIC).getValue();
        return (((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }

    public int getBroadcastingPower() {
        byte[] data = characteristics.get(PlutoconUuid.TX_LEVEL_CHARACTERISTIC).getValue();
        return (short)(((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }

    public int getBatteryVoltage() {
        byte[] data = characteristics.get(PlutoconUuid.BATTERY_CHARACTERISTIC).getValue();
        return (short)(((int) data[0]) << 8) | ((int) data[1] & 0xFF);
    }
    public String getSoftwareVersion() {
        return new String(characteristics.get(PlutoconUuid.SOFTWARE_VERSION_CHARACTERISTIC).getValue());
    }

    public String getHardwareVersion() {
        return new String(characteristics.get(PlutoconUuid.HARDWARE_VERSION_CHARACTERISTIC).getValue());
    }

    public String getModelNumber() {
        return new String(characteristics.get(PlutoconUuid.MODEL_NUMBER_CHARACTERISTIC).getValue());
    }

    public String getManufactureName(){
        return new String(characteristics.get(PlutoconUuid.MANUFACTURE_NAME_CHARACTERISTIC).getValue());
    }

    public PlutoconEditor editor() {
        return this.editor = new PlutoconEditor(bluetoothGatt);
    }

    public PlutoconReader reader() {
        return this.reader = new PlutoconReader(bluetoothGatt);
    }

    public ParcelUuid getUuid(){
        byte[] data = characteristics.get(PlutoconUuid.UUID_CHARACTERISTIC).getValue();
        ByteBuffer bb = ByteBuffer.wrap(data);
        long high = bb.getLong();
        long low = bb.getLong();
        return new ParcelUuid(new UUID(high, low));
    }

    public double getLatitude(){
        ParcelUuid uuid = getUuid();
        if (uuid == null) return 0;
        try {
            double latitudeHigh = Double.parseDouble(uuid.toString().substring(9, 13));
            double latitudeLow = Double.parseDouble(uuid.toString().substring(14, 18)
                    + uuid.toString().substring(19, 21));
            return (latitudeHigh * 1000000 + latitudeLow) / 1000000;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getLongitude(){
        ParcelUuid uuid = getUuid();
        if (uuid == null) return 0;
        try {
            double longitudeHigh = Double.parseDouble(uuid.toString().substring(21, 23)
                    + uuid.toString().substring(24, 26));
            double longitudeLow = Double.parseDouble(uuid.toString().substring(26, 32));
            return (longitudeHigh * 1000000 + longitudeLow) / 1000000;
        } catch (Exception e) {
            return 0;
        }
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        this.notifyDisconnected();
    }

    public interface OnConnectionRemoteRssiCallback {
        void onConnectionRemoteRssiCallback(int rssi);
    }

    public interface OnConnectionStateChangeCallback {
        void onConnectionStateDisconnected();

        void onConnectionStateConnected();
    }
}
