package com.kongtech.plutocon.sdk.scanner;

import android.bluetooth.BluetoothDevice;

import com.kongtech.plutocon.sdk.repackaged.ScanRecord;


public interface PlutoconScanner {
    void start();
    void stop();
    void setScanMode(int mode);
    boolean isScanning();
    interface ScannerCallback {
        void onLeScan(BluetoothDevice device, int rssi, ScanRecord scanRecord);
    }
}
