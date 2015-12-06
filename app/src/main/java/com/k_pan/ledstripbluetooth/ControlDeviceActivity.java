package com.k_pan.ledstripbluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class ControlDeviceActivity extends AppCompatActivity {

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream btOutputStream;
    private InputStream btInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    Button btnOn, btnOff, btnConf,btnDis;
    SeekBar barC1Red,barC1Green,barC1Blue,barC2Red,barC2Green,barC2Blue;
    int r1,r2,g1,g2,b1,b2 = 0;
    int operationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(ScanDeviceActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device
        setContentView(R.layout.activity_control_device);

        //call the widgtes
        btnOn = (Button)findViewById(R.id.btn_on);
        btnOff = (Button)findViewById(R.id.btn_off);
        btnConf = (Button)findViewById(R.id.btn_conf);
        btnDis = (Button)findViewById(R.id.btn_dis);
        barC1Red = (SeekBar)findViewById(R.id.bar_r1);
        barC1Green = (SeekBar)findViewById(R.id.bar_g1);
        barC1Blue = (SeekBar)findViewById(R.id.bar_b1);
        barC2Red = (SeekBar)findViewById(R.id.bar_r2);
        barC2Green = (SeekBar)findViewById(R.id.bar_g2);
        barC2Blue = (SeekBar)findViewById(R.id.bar_b2);

        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                onMode();
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                offMode();
            }
        });
        btnConf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confMode();
            }
        });
        btnDis.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnect();
            }
        });
        barC1Red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                r1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });
        barC1Green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                g1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });
        barC1Blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });
        barC2Red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                r2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });
        barC2Green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                g2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });
        barC2Blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendConfigValue();
            }
        });

        new ConnectBT().execute(); //Call the class to connect
    }

    //Switch to on mode:
    private void onMode(){
        btnOn.setEnabled(false);
        btnOff.setEnabled(true);
        btnConf.setEnabled(true);
        barC1Red.setEnabled(false);
        barC1Green.setEnabled(false);
        barC1Blue.setEnabled(false);
        barC2Red.setEnabled(false);
        barC2Green.setEnabled(false);
        barC2Blue.setEnabled(false);

        // Send Command to start START Mode
        sendCommand("*#");
        operationMode = 1;
    }

    //Switch to off mode:
    private void offMode(){
        btnOn.setEnabled(true);
        btnOff.setEnabled(false);
        btnConf.setEnabled(true);
        barC1Red.setEnabled(false);
        barC1Green.setEnabled(false);
        barC1Blue.setEnabled(false);
        barC2Red.setEnabled(false);
        barC2Green.setEnabled(false);
        barC2Blue.setEnabled(false);

        // Send Command to start OFF Mode
        sendCommand("%#");
        operationMode = 2;
    }

    //Switch to configuration mode:
    private void confMode(){
        btnOn.setEnabled(true);
        btnOff.setEnabled(true);
        btnConf.setEnabled(false);
        barC1Red.setEnabled(true);
        barC1Green.setEnabled(true);
        barC1Blue.setEnabled(true);
        barC2Red.setEnabled(true);
        barC2Green.setEnabled(true);
        barC2Blue.setEnabled(true);

        // Send Command to start CONF Mode
        sendCommand("&#");
        operationMode = 3;
    }

    //Disconnect Device
    private void disconnect(){
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    //Send Command
    private void sendCommand(String input){
        if (btSocket!=null)
        {
            try
            {
                btOutputStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    //Send Current Value
    private void sendConfigValue(){
        if(operationMode == 3){
            sendCommand("@"+r1+";"+g1+";"+b1+";"+r2+";"+g2+";"+b2+"#");
        }
    }

    //connect bluetooth Device
    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ControlDeviceActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btOutputStream = btSocket.getOutputStream();
                    btInputStream = btSocket.getInputStream();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Please try again.");
                finish();
            }
            else
            {
                msg("Connected");
                isBtConnected = true;
                readValueThread();
                confMode();
            }
            progress.dismiss();
        }
    }

    // Create Thread to read incommingstream
    private void readValueThread(){

        final Handler handler = new Handler();
        final byte delimiter = 35; //This is the ASCII code for #

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = btInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            btInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            processInputString(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    // process input data
    private void processInputString(String input){
        switch(operationMode){
            case 1:
                if(Objects.equals(input, "*OK")){
                    msg("LED ON");
                }else{
                    msg("Unknow Message:"+input);
                }
                break;
            case 2:
                if(Objects.equals(input, "%OK")){
                    msg("LED OFF");
                }else{
                    msg("Unknow Message:"+input);
                }
                break;
            case 3:
                if(Objects.equals(input, "&OK")){
                    msg("Configuration Mode");
                    sendCommand("@#");
                }else if(input.charAt(0) == '@'){
                    input = input.substring(1);
                    // check setup, if not ok, resend:
                    Pattern pattern = Pattern.compile(Pattern.quote(";"));
                    String[] values = pattern.split(input);
                    try{
                        if(     r1 != Integer.parseInt(values[0]) ||
                                g1 != Integer.parseInt(values[1]) ||
                                b1 != Integer.parseInt(values[2]) ||
                                r2 != Integer.parseInt(values[3]) ||
                                g2 != Integer.parseInt(values[4]) ||
                                b2 != Integer.parseInt(values[5])){
                            msg("Error: Sending again");
                            sendConfigValue();
                        }
                    }catch (Exception e){
                        msg("Error: Sending again");
                        sendConfigValue();
                    }

                }else if(input.charAt(0) == '$'){
                    input = input.substring(1);
                    Pattern pattern = Pattern.compile(Pattern.quote(";"));
                    String[] values = pattern.split(input);
                    try {
                        if (Integer.parseInt(values[0]) <= 255 && Integer.parseInt(values[0]) >= 0 &&
                                Integer.parseInt(values[1]) <= 255 && Integer.parseInt(values[1]) >= 0 &&
                                Integer.parseInt(values[2]) <= 255 && Integer.parseInt(values[2]) >= 0 &&
                                Integer.parseInt(values[3]) <= 255 && Integer.parseInt(values[3]) >= 0 &&
                                Integer.parseInt(values[4]) <= 255 && Integer.parseInt(values[4]) >= 0 &&
                                Integer.parseInt(values[5]) <= 255 && Integer.parseInt(values[5]) >= 0
                                ) {
                            r1 = Integer.parseInt(values[0]);
                            barC1Red.setProgress(r1);
                            g1 = Integer.parseInt(values[1]);
                            barC1Green.setProgress(g1);
                            b1 = Integer.parseInt(values[2]);
                            barC1Blue.setProgress(b1);
                            r2 = Integer.parseInt(values[3]);
                            barC2Red.setProgress(r2);
                            g2 = Integer.parseInt(values[4]);
                            barC2Green.setProgress(g1);
                            b2 = Integer.parseInt(values[5]);
                            barC2Blue.setProgress(b1);
                        } else {
                            msg("Error: Fail to sync data, Sending again");
                            sendCommand("@#");
                        }
                    }catch(Exception e){
                        msg("Error: Fail to sync data, Sending again");
                        sendCommand("@#");
                    }
                }
                else{
                    msg("Unknow Message:"+input);
                }
                break;
            default:
                msg("Error: unknow operation mode: " + operationMode);
                break;
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
