package com.satsml1projects.btserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothSend{
    private OutputStream outputStream;
    private InputStream inStream;
    private String txt;
    volatile boolean stopWorker;
    int readBufferPosition;
    byte[] readBuffer;
    static ArrayList<Model> list=new ArrayList<>();




    public void init() throws IOException {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = (Set<BluetoothDevice>) blueAdapter.getBondedDevices();

                if (bondedDevices.size() > 0) {
                    Set<BluetoothDevice> devices = bondedDevices;
                    BluetoothDevice device=null;  // = (BluetoothDevice) devices[position];
                    for(BluetoothDevice b:devices){
                        if(b.getName().equals("HC-06")){
                            device=b;
                            break;
                        }
                    }
                    ParcelUuid[] uuids = device.getUuids();
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    inStream = socket.getInputStream();
                    beginListenForData();
                }

                Log.e("error", "No appropriate paired devices.");
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }
    }


    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        Thread workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
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
                                            String arr[] = data.toString().split("A");
                                            list.add(new Model(new Integer(arr[0]),new Integer(arr[1]),new Integer(arr[2])));
                                            Log.e("=============>",arr[0]+" "+arr[1]+" "+arr[2]);



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

}

class Model{
    private int rps;
    private int ph;
    private int color;

    public Model(int rps, int ph, int color) {
        this.rps = rps;
        this.ph = ph;
        this.color = color;
    }

    public Model(int rps) {
        this.rps = rps;
    }

    public int getRps() {
        return rps;
    }

    public void setRps(int rps) {
        this.rps = rps;
    }

    public int getPh() {
        return ph;
    }

    public void setPh(int ph) {
        this.ph = ph;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
