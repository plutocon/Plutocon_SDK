package com.kongtech.plutocon.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.kongtech.plutocon.sdk.service.MessageUtil;
import com.kongtech.plutocon.sdk.service.PlutoconServive;
import com.kongtech.plutocon.sdk.service.scanner.LollipopScanner;
import com.kongtech.plutocon.sdk.util.Plog;

import java.util.ArrayList;
import java.util.List;

public class PlutoconManager {
    public final static int MONITORING_BACKGROUND = LollipopScanner.SCAN_BACKGROUND;
    public final static int MONITORING_FOREGROUND = LollipopScanner.SCAN_FOREGROUND;

    private Messenger messengerService;
    private Messenger responseMessenger;

    private PlutoconServiceConnection serviceConnection;
    private OnReadyServiceListener onReadyService;



    private OnMonitoringPlutoconListener onMonitoringPlutoconListener;
    private MonitoringResult monitoringResult;
    private Context context;

    public PlutoconManager(Context context){
        this.context = context;
        this.serviceConnection = new PlutoconServiceConnection();
        this.monitoringResult = new MonitoringResult();

        HandlerThread thread = new HandlerThread("Messenger Thread");
        thread.start();
        this.responseMessenger = new Messenger(new ResponseHandler(thread.getLooper()));
    }

    public boolean connectService(OnReadyServiceListener onReadyService) {
        if (isServiceConnected()) {
            if(onReadyService != null) onReadyService.onReady();
            return true;
        }
        this.onReadyService = onReadyService;
        return this.context.bindService(new Intent(this.context, PlutoconServive.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void startMonitoring(int mode, OnMonitoringPlutoconListener onMonitoringPlutoconListener) {

        Message scanModeMsg = null;
        Message scanStartMsg = Message.obtain(null, MessageUtil.REQUEST_SCAN_START);

        this.onMonitoringPlutoconListener = onMonitoringPlutoconListener;

        scanStartMsg.replyTo = responseMessenger;

        if (mode == LollipopScanner.SCAN_FOREGROUND)
            scanModeMsg = Message.obtain(null, MessageUtil.REQUEST_MODE_FOREGROUND);
        else scanModeMsg = Message.obtain(null, MessageUtil.REQUEST_MODE_BACKGROUND);

        try {
            if(messengerService != null) {
                this.messengerService.send(scanModeMsg);
                this.messengerService.send(scanStartMsg);
            }
        } catch (RemoteException e) {
            Plog.e("Error Start Monitoring: " + e.toString());
            e.printStackTrace();
        }
    }

    public void stopMonitoring(){
        Message scanStopMsg = Message.obtain(null, MessageUtil.REQUEST_SCAN_STOP);
        try {
            if(messengerService != null)
                this.messengerService.send(scanStopMsg);
        } catch (RemoteException e) {
            Plog.e("Error Stop Monitoring: " + e.toString());
            e.printStackTrace();
        }
    }

    public void close(){
        if(this.isServiceConnected()){
            this.stopMonitoring();
            context.unbindService(this.serviceConnection);
        }
    }

    public boolean isServiceConnected() {
        return messengerService != null;
    }

    public void setOnMonitoringPlutoconListener(OnMonitoringPlutoconListener onMonitoringPlutoconListener) {
        this.onMonitoringPlutoconListener = onMonitoringPlutoconListener;
    }

    private class ResponseHandler extends Handler {
        private ResponseHandler(Looper looper) {super(looper);}

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MessageUtil.RESPONSE_SCAN_RESULT:
                    msg.getData().setClassLoader(Plutocon.class.getClassLoader());
                    Plutocon plutocon = msg.getData().getParcelable(MessageUtil.SCAN_RESULT);

                    int p = PlutoconManager.this.monitoringResult.isContained(plutocon);

                    if (p > -1) PlutoconManager.this.monitoringResult.updateSensor(plutocon, p);
                    else PlutoconManager.this.monitoringResult.addSensor(plutocon);

                    if (onMonitoringPlutoconListener != null) {
                        onMonitoringPlutoconListener.onPlutoconDiscovered(plutocon, monitoringResult.getList());
                    }
                break;
            }
        }
    }

    private class PlutoconServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlutoconManager.this.messengerService = new Messenger(service);
            if(onReadyService != null)
                onReadyService.onReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            PlutoconManager.this.messengerService = null;
        }
    }


    public interface OnReadyServiceListener{
        void onReady();
    }

    public interface OnMonitoringPlutoconListener {
        void onPlutoconDiscovered(Plutocon plutocon, List<Plutocon> plutocons);
    }
}
