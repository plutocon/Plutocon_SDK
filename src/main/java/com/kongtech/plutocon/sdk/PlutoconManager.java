package com.kongtech.plutocon.sdk;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

import com.kongtech.plutocon.sdk.repackaged.ScanRecord;
import com.kongtech.plutocon.sdk.scanner.LollipopScanner;
import com.kongtech.plutocon.sdk.scanner.OldScanner;
import com.kongtech.plutocon.sdk.scanner.PlutoconScanner;
import com.kongtech.plutocon.sdk.util.Plog;

import java.util.List;

public class PlutoconManager {
    public final static int MONITORING_BACKGROUND = LollipopScanner.SCAN_BACKGROUND;
    public final static int MONITORING_FOREGROUND = LollipopScanner.SCAN_FOREGROUND;

    private OnMonitoringPlutoconListener onMonitoringPlutoconListener;
    private PlutoconScanner plutoconScanner;
    private MonitoringResult monitoringResult;
    private Context context;


    public PlutoconManager(Context context) {
        this.context = context;
        this.monitoringResult = new MonitoringResult();

        if (Build.VERSION.SDK_INT >= 21) {
            plutoconScanner = new LollipopScanner(context, LollipopScanner.SCAN_FOREGROUND, createScannerCallback());
        } else {
            plutoconScanner = new OldScanner(context, createScannerCallback());
        }
    }

    private PlutoconScanner.ScannerCallback createScannerCallback() {
        return new PlutoconScanner.ScannerCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
                Plog.i("ScanResult: " + scanRecord.toString());
                Plutocon plutocon = Plutocon.createFromScanResult(device, scanRecord, rssi);
                if (plutocon != null) {
                    int p = PlutoconManager.this.monitoringResult.isContained(plutocon);

                    if (p > -1) PlutoconManager.this.monitoringResult.updateSensor(plutocon, p);
                    else PlutoconManager.this.monitoringResult.addSensor(plutocon);

                    if (onMonitoringPlutoconListener != null) {
                        onMonitoringPlutoconListener.onPlutoconDiscovered(plutocon, monitoringResult.getList());
                    }
                }
            }
        };
    }

    public void startMonitoring(int mode, OnMonitoringPlutoconListener onMonitoringPlutoconListener) {
        this.onMonitoringPlutoconListener = onMonitoringPlutoconListener;
        startMonitoring(mode);
    }

    public void startMonitoring(int mode) {
        monitoringResult.clear();
        plutoconScanner.setScanMode(mode);
        plutoconScanner.start();
    }

    public void stopMonitoring() {
        plutoconScanner.stop();
    }

    public MonitoringResult getMonitoringResult() {
        return monitoringResult;
    }

    public boolean isScanning() {
        return plutoconScanner.isScanning();
    }

    public void setOnMonitoringPlutoconListener(OnMonitoringPlutoconListener onMonitoringPlutoconListener) {
        this.onMonitoringPlutoconListener = onMonitoringPlutoconListener;
    }

    public interface OnMonitoringPlutoconListener {
        void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons);
    }
}
