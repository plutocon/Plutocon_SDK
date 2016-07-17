package com.kongtech.plutocon.sdk.connection;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.ParcelUuid;

public class PlutoconReader extends PlutoconOperator {


    public PlutoconReader(BluetoothGatt sensorGatt) {
        super(sensorGatt);
    }

    public PlutoconReader getProperty(ParcelUuid uuid){
        BluetoothGattCharacteristic characteristic = this.getCharacteristics(uuid);
        if(characteristic != null && this.characteristicValidate(characteristic)){
            this.addCharacteristic(characteristic);
        }
        return this;
    }

    @Override
    public boolean characteristicValidate(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return (bluetoothGattCharacteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ)) != 0;
    }

    @Override
    public boolean execute() {
        BluetoothGattCharacteristic characteristic = this.nextOperation();
        if(characteristic != null) {
            plutoconGatt.readCharacteristic(characteristic);
            return true;
        }
        return false;
    }
}
