package com.example.martinez.arduinoband;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;
import android.widget.AdapterView.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter blueAdp;
    BluetoothDevice btDevice;
    BluetoothSocket btLocalSocket;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final int CONECCION_ACTIVA=1;
    Button btnBuscarD;
    boolean btSoportado;
    ListView lv;
    private static final int REQUEST_CODE_ASK_PERMISSION = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS=123;
    private Context mContex;
    private Button btnApagar;
    private Intent iService;
    private Set<BluetoothDevice> pairedDevices;
    private List<String> deviceList;
    private ArrayAdapter adapter;
    private TextView tvEstado;
    private Button sonarBt;
    private ToggleButton scanModeButton;
    private LinearLayout lyBuscar, lyScanMode, lyFunciones, lyFBuscar;
    private int Distancia_Max=-70;
    String btDeviceAddress="", btDeviceName="";
    private String textoEstado;
    private SeekBar Bar;
    private TextView tvRSSI, tvDistElegida;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnApagar = (Button) findViewById(R.id.ApagarS);
        mContex = this;
        tvEstado=(TextView)findViewById(R.id.estado);
        sonarBt=(Button) findViewById(R.id.sonar);
        tvRSSI = (TextView) findViewById(R.id.tvSignal);
        tvDistElegida= (TextView)findViewById(R.id.distElegida);
        //layouts
        lyBuscar =(LinearLayout)findViewById(R.id.lyestado);
        lyScanMode = (LinearLayout)findViewById(R.id.lyScanMode);
        lyFunciones=(LinearLayout)findViewById(R.id.btConexiones);
        lyFBuscar = (LinearLayout)findViewById(R.id.lyBuscar);
        scanModeButton = (ToggleButton)findViewById(R.id.ScanMode);
        Bar= (SeekBar)findViewById(R.id.seekBar);
        blueAdp = BluetoothAdapter.getDefaultAdapter();
        btnBuscarD = (Button) findViewById(R.id.btnBuscar);
        lv=(ListView) findViewById(R.id.lstDisponibles);
        lv.setVisibility(View.INVISIBLE);

        registerReceiver(uiUpdated, new IntentFilter("RssiSend"));

        if(blueAdp==null){
            textoEstado="Estado: Bluetooth No Compatible";
            tvEstado.setText(textoEstado);
            btSoportado=false;
        }else{
            textoEstado="Estado: Accediendo a Bluetooth...";
            tvEstado.setText(textoEstado);
            btSoportado=true;
        }
        if(btSoportado){
            if(!blueAdp.isEnabled()){
                accessPermission();
            }else{
                textoEstado="Estado: Bluetooth Activado";
                tvEstado.setText(textoEstado);
            }

        }
        //Programacion de SeekBar
        Bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvDistElegida.setText(" "+(progressChangedValue/10-2)+" Mts");
            }
        });
        //listando dispositivos al dar buscar
        btnBuscarD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                lyScanMode.setVisibility(View.INVISIBLE);
                pairedDevices = blueAdp.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    textoEstado="Estado: Buscando Dispositivos...";
                    tvEstado.setText(textoEstado);
                    //Toast.makeText(MainActivity.this,"Buscando Dispositivos",Toast.LENGTH_SHORT).show();
                    deviceList=new ArrayList<String>();
                    for(BluetoothDevice bt : pairedDevices) {
                        deviceList.add(bt.getName()+":\n"+bt.getAddress());
                    }
                    adapter = new ArrayAdapter(mContex,android.R.layout.simple_list_item_1, deviceList);
                    lv.setAdapter(adapter);
                    lv.setVisibility(View.VISIBLE);
                }else{
                    textoEstado="Estado: Consulte Manual";
                    tvEstado.setText(textoEstado);
                    String noDevices = "Ningun Dispositivo";
                        deviceList.add(noDevices);
                }
            }
        });
        //Click Adapter list de dispositivos
       lv.setOnItemClickListener(new OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
               String textoItem, address;
               textoItem = ((TextView) v).getText().toString();
               address = textoItem.substring(textoItem.length() - 17);
               textoEstado="Estado: Enlazando...";
               tvEstado.setText(textoEstado);
               lv.setVisibility(View.INVISIBLE);
               lyBuscar.setVisibility(View.INVISIBLE);
               lyScanMode.setVisibility(View.VISIBLE);
               if(conectarBt(address)){
                   textoEstado="Estado: Conectado";
               }
               else{
                   textoEstado="Estado: Error";
               }

               tvEstado.setText(textoEstado);
           }
       });
        scanModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                boolean conexion = false;
                if (isChecked) {
                    if(btLocalSocket!=null) {
                        lyFunciones.setVisibility(View.INVISIBLE);
                        lyFBuscar.setVisibility(View.INVISIBLE);
                        btDeviceName=btDevice.getName();
                        btDeviceAddress=btLocalSocket.getRemoteDevice().getAddress();
                        desconectarBt();
                        permissionActionFound();

                    }
                } else {
                    desconectarBt();
                    conexion = conectarBt(btDeviceAddress);
                    if(conexion) {
                        lyFunciones.setVisibility(View.VISIBLE);
                        lyFBuscar.setVisibility(View.VISIBLE);
                        stopService(new Intent(MainActivity.this, BtService.class));
                    }
                }
            }
        });
        //Provisional Apagar Servicio
        btnApagar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btLocalSocket!=null) //
                {
                        lyScanMode.setVisibility(View.INVISIBLE);
                        desconectarBt();
                        btnBuscarD.setEnabled(true);
                }
                textoEstado="Estado: No Enlazado";
                tvEstado.setText(textoEstado);
            }
        });
        sonarBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                if (btLocalSocket != null) {
                    btLocalSocket.getOutputStream().write("1".getBytes());
                }
            }catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"Error al enviar dato"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
            }
        });

    }
    void iniciarServicio(){
        iService = new Intent(MainActivity.this, BtService.class);
        iService.putExtra("Name", btDeviceName);
        startService(iService);
    }
    void desconectarBt(){
        try {
            blueAdp.cancelDiscovery();
            if(btLocalSocket!=null){ btLocalSocket.close();} //close connection
            btLocalSocket = null;
            btDevice = null;
        }catch (IOException e)
        {
            Toast.makeText(this,"Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public boolean conectarBt(final String Remoteaddress) {
        if(blueAdp==null){
            return false;
        }
        btDevice = blueAdp.getRemoteDevice(Remoteaddress);
        if(btDevice==null){
            return false;
        }
        try {
            if(btLocalSocket==null) {
                btnBuscarD.setEnabled(false);
                btLocalSocket = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                btLocalSocket.connect();
            }

        }catch (IOException e){
            btnBuscarD.setEnabled(true);
            Toast.makeText(this,"Coneccion Fallida"+e.getMessage(), Toast.LENGTH_SHORT).show();
            return  false;
        }

        return true;
    }
    private BroadcastReceiver uiUpdated= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int varRssiVal = intent.getExtras().getInt("rssiVal");
            String AddressBt = intent.getExtras().getString("AddressBt");

                int intRssi;
                tvRSSI.setText(" "+varRssiVal+"dBm");
                intRssi = varRssiVal;
                Distancia_Max=Bar.getProgress()*-1;
                if(intRssi<Distancia_Max) {
                    notificacion(varRssiVal, context, AddressBt, Bar.getProgress());
                }
            if(varRssiVal==0){
                notificacion(varRssiVal, context, AddressBt, Bar.getProgress());
            }


        }
    };

    private void notificacion(int sNotify, Context c, String dir, int distMax){
        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        btDeviceAddress=dir;
        Bar.setProgress(distMax);
        servicioActivo();
        int icono = R.mipmap.ic_launcher;
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent iNotificacion=new Intent(c, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, iNotificacion, 0);
        if(sNotify==0){
            mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(icono)
                    .setContentTitle("Dispositivo Perdido")
                    .setContentText("Dispositivo Fuera de Rango")
                    .setSound(soundUri)
                    .setVibrate(new long[]{100, 250, 100, 500})
                    .setAutoCancel(true);
        }else {
            mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(icono)
                    .setContentTitle("ArduinoBand")
                    .setContentText("Alerta de Señal Baja: " + sNotify)
                    .setSound(soundUri)
                    .setVibrate(new long[]{100, 250, 100, 500})
                    .setAutoCancel(true);
        }


        mNotifyMgr.notify(1, mBuilder.build());
    }

    //Funcion Permisios
//ACCESS COARSE LOCATION-> Impotante para permitirnos acceder al ACTION_FOUND luego de realizar el BluetoothAdapter.startDiscovery()
    private void permissionActionFound(){
        int readBtPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if(readBtPermission!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSIONS);
        }else {
            iniciarServicio();
        }
    }
//Permiso ´para la activacion y desactivacion de BluetoothAdapter
    private void accessPermission(){
        int readBtPermission = checkSelfPermission(Manifest.permission.BLUETOOTH);
        if(readBtPermission!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH},
                    REQUEST_CODE_ASK_PERMISSION);
        }else {
            textoEstado="Estado: Activo";
            tvEstado.setText(textoEstado);
            //Toast.makeText(MainActivity.this,"Se Activo Bluetooth",Toast.LENGTH_SHORT).show();
            blueAdp.enable();
        }
    }
//Recepcion de Respuesta permsisos en tiempo de Ejecucion
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSION:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    textoEstado="Estado: Activo";
                    tvEstado.setText(textoEstado);

                    blueAdp.enable();
                }else {
                    textoEstado="Estado: Debe permitir el uso de Bluetooth";
                    tvEstado.setText(textoEstado);

                }
                break;
            case REQUEST_COARSE_LOCATION_PERMISSIONS:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iniciarServicio();
                } else {
                    Toast.makeText(this,
                            "No Coarse Persmiso",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

//salvando Activity
public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    Boolean EstadoToggle=scanModeButton.isChecked();
    savedInstanceState.putBoolean("ButtonCheck", EstadoToggle);
    savedInstanceState.putString("AddressBt",btDeviceAddress);
    savedInstanceState.putInt("ValorSeek",Bar.getProgress());


}
//Recuperando Activity
public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    Boolean EstadoToggle;
    // Restore state from the savedInstanceState.

    if(savedInstanceState != null) {
        //EstadoToggle = savedInstanceState.getBoolean("ButtonCheck");
        //scanModeButton.setChecked(EstadoToggle);
        if(isMyServiceRunning(BtService.class)){
            servicioActivo();
            Toast.makeText(getApplicationContext(),"Activo Servicio",Toast.LENGTH_SHORT).show();
            btDeviceAddress=savedInstanceState.getString("AddressBt");
            Bar.setProgress(savedInstanceState.getInt("ValorSeek"));
        }

    }
}
//Verificacion de Servicio
private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
            return true;
        }
    }
    return false;
}
//Si servicio activo Servicio activo
    private void servicioActivo(){
        lyFunciones.setVisibility(View.INVISIBLE);
        lyFBuscar.setVisibility(View.INVISIBLE);
        lyScanMode.setVisibility(View.VISIBLE);
        scanModeButton.setChecked(true);
    }
}
