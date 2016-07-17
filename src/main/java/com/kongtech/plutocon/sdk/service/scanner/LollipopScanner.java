package com.kongtech.plutocon.sdk.service.scanner;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import com.kongtech.plutocon.sdk.repackaged.ScanRecord;
import com.kongtech.plutocon.sdk.util.Plog;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class LollipopScanner implements PlutoconScanner {

    public static final int SCAN_FOREGROUND = ScanSettings.SCAN_MODE_LOW_LATENCY;
    public static final int SCAN_BACKGROUND = ScanSettings.SCAN_MODE_LOW_POWER;

    private final BluetoothAdapter adapter;
    private final BluetoothLeScanner leScanner;

    private ScanSettings.Builder setting;
    private List<ScanFilter> filters = new ArrayList<>();
    private ScannerCallback callback;
    private ScanCallback scanCallback;

    private boolean isScanning;

    public LollipopScanner(Context context, int scanMode, final ScannerCallback callback) {

        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.leScanner = adapter.getBluetoothLeScanner();

        this.setting = new ScanSettings.Builder();
        this.setting.setScanMode(scanMode);
        this.callback = callback;
        this.scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                callback.onLeScan(result.getDevice(), result.getRssi(), LollipopScanner.this.wrap(result.getScanRecord()));
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for(ScanResult result:results){
                    callback.onLeScan(result.getDevice(), result.getRssi(), LollipopScanner.this.wrap(result.getScanRecord()));
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        if (Build.VERSION.SDK_INT >= 23)
            this.setting.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

        filters.clear();
    }

    @Override
    public void start() {
        if (!this.isScanning) {
            this.leScanner.startScan(this.filters, this.setting.build(), scanCallback);
            this.isScanning = true;
        } else
            Plog.i("Already Scan");
    }

    @Override
    public void stop() {
        if (this.isScanning) {
            this.isScanning = false;
            this.leScanner.flushPendingScanResults(scanCallback);
            this.leScanner.stopScan(scanCallback);
        }
    }

    @Override
    public void setScanMode(int mode) {
        this.setting.setScanMode(mode);
    }

    private ScanRecord wrap(android.bluetooth.le.ScanRecord scanRecord) {
        return ScanRecord.parseFromBytes(scanRecord.getBytes());
    }

}
