package com.kongtech.plutocon.sdk.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.kongtech.plutocon.sdk.Plutocon;
import com.kongtech.plutocon.sdk.repackaged.ScanRecord;
import com.kongtech.plutocon.sdk.service.scanner.LollipopScanner;
import com.kongtech.plutocon.sdk.service.scanner.OldScanner;
import com.kongtech.plutocon.sdk.service.scanner.PlutoconScanner;
import com.kongtech.plutocon.sdk.util.Plog;

public class PlutoconServive extends Service {

    private RequestHandler requestHandler;
    private Messenger messengerService;
    private Messenger responseMessenger;

    private PlutoconScanner plutoconScanner;

    public PlutoconServive() {}

    @Override
    public IBinder onBind(Intent intent) {
        return messengerService.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("Messenger Thread");
        thread.start();

        requestHandler = new RequestHandler(thread.getLooper());
        messengerService = new Messenger(requestHandler);

        if(Build.VERSION.SDK_INT >= 21){
            plutoconScanner = new LollipopScanner(getBaseContext(), LollipopScanner.SCAN_FOREGROUND, createScannerCallback());
        }
        else {
            plutoconScanner = new OldScanner(getBaseContext(), createScannerCallback());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestHandler.getLooper().quit();
    }

    private PlutoconScanner.ScannerCallback createScannerCallback(){
        return new PlutoconScanner.ScannerCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, ScanRecord scanRecord) {
                Plog.i("ScanResult: " + scanRecord.toString());
                Plutocon plutocon = Plutocon.createFromScanResult(device, scanRecord, rssi);
                if(plutocon != null) {
                    sendScanResult(plutocon);
                }
            }
        };
    }

    private void sendScanResult(Plutocon plutocon){
        if(this.responseMessenger == null) return;

        Message scanResultMsg = Message.obtain(null, MessageUtil.RESPONSE_SCAN_RESULT);
        scanResultMsg.getData().putParcelable(MessageUtil.SCAN_RESULT, plutocon);

        try {
            this.responseMessenger.send(scanResultMsg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class RequestHandler extends Handler {
        private RequestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            final Messenger replyTo = msg.replyTo;
            responseMessenger = replyTo;

            switch (msg.what) {
                case MessageUtil.REQUEST_SCAN_START:
                    plutoconScanner.start();
                    break;
                case MessageUtil.REQUEST_SCAN_STOP:
                    plutoconScanner.stop();
                    break;
                case MessageUtil.REQUEST_MODE_BACKGROUND:
                    plutoconScanner.setScanMode(LollipopScanner.SCAN_BACKGROUND);
                    break;
                case MessageUtil.REQUEST_MODE_FOREGROUND:
                    plutoconScanner.setScanMode(LollipopScanner.SCAN_FOREGROUND);
                    break;
            }
        }
    }
}
