package com.example.martinez.arduinoband;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;


import java.util.Timer;
import java.util.TimerTask;

public class BtService extends Service {
    String nameDeviceScan="";
    BluetoothAdapter btAdapter;
    Boolean DispositivoEncontrado=false;
    Timer timer = new Timer();
    @Override
    public void onCreate() {
        super.onCreate();
        btAdapter=BluetoothAdapter.getDefaultAdapter();
        DispositivoEncontrado=false;
        IntentFilter filter=new IntentFilter();

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                ModoEscaner();
            }
        }, 0, 5000);

    }


    private void ModoEscaner(){
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        btAdapter.startDiscovery();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nameDeviceScan = intent.getStringExtra("Name");
        btAdapter.startDiscovery();
        return super.onStartCommand(intent, flags, startId);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name= device.getName();
                if(nameDeviceScan.equals(name)) {
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    Intent enviarRssi=new Intent();
                    enviarRssi.setAction("RssiSend");
                    enviarRssi.putExtra("AddressBt",device.getAddress());
                    enviarRssi.putExtra("rssiVal", rssi);
                    context.sendBroadcast(enviarRssi);
                    btAdapter.cancelDiscovery();
                    DispositivoEncontrado=true;
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                if(DispositivoEncontrado){
                    DispositivoEncontrado=false;
                }else{
                    Intent enviarRssi=new Intent();
                    enviarRssi.setAction("RssiSend");
                    enviarRssi.putExtra("rssiVal", 0);
                    enviarRssi.putExtra("AddressBt","No Name");
                    context.sendBroadcast(enviarRssi);
                }
            }
        }
    };




    @Override
    public void onDestroy() {
        timer.cancel();
        timer=null;
    }

    public BtService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }


}
