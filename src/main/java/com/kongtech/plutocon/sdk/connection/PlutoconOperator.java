package com.kongtech.plutocon.sdk.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PlutoconOperator {

    private ConcurrentHashMap<ParcelUuid, BluetoothGattCharacteristic> characteristics;
    private List<BluetoothGattCharacteristic> editList;

    protected BluetoothGatt plutoconGatt;

    private OnOperationCompleteCallback onOperationCompleteCallback;

    public PlutoconOperator(BluetoothGatt sensorGatt){
        this.plutoconGatt = sensorGatt;
        this.editList = new ArrayList<>();
        this.characteristics = new ConcurrentHashMap<>();

        List<BluetoothGattService> sensorServices = sensorGatt.getServices();
        for(BluetoothGattService service : sensorServices){
            for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                    characteristics.put(new ParcelUuid(characteristic.getUuid()), characteristic);
            }
        }
    }

    public PlutoconOperator setOnOperationCompleteCallback(OnOperationCompleteCallback onOperationCompleteCallback){
        this.onOperationCompleteCallback = onOperationCompleteCallback;
        return this;
    }

    public void operationComplete(BluetoothGattCharacteristic characteristic, boolean isLast){
        if(onOperationCompleteCallback != null) onOperationCompleteCallback.onOperationComplete(characteristic, isLast);
    }

    public void commit(){
        execute();
    }

    public BluetoothGattCharacteristic getCharacteristics(ParcelUuid uuid){
        return characteristics.get(uuid);
    }

    protected BluetoothGattCharacteristic nextOperation(){
        if(editList.size() > 0){
            BluetoothGattCharacteristic characteristic = editList.get(0);
            editList.remove(0);
            return characteristic;
        }
        return null;
    }

    protected void addCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        editList.add(bluetoothGattCharacteristic);
    }

    public abstract boolean characteristicValidate(BluetoothGattCharacteristic bluetoothGattCharacteristic);

    public abstract boolean execute();

    public interface OnOperationCompleteCallback{
        void onOperationComplete(BluetoothGattCharacteristic characteristic, boolean isLast);
    }
}
