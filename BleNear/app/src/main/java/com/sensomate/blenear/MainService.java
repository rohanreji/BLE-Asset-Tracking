package com.sensomate.blenear;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohan on 17/2/15.
 */


public class MainService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;
    String PreviousDevice;
    int PreviousRssi;
    double latitude;
    double longitude;
    GPSTracker gps;
    ApiCaller api;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        PreviousDevice=new String();
        PreviousRssi=-500;
        latitude=0.0;
        longitude=0.0;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        gps = new GPSTracker(this);

        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.e("Latitude: ",latitude+"");
            Log.e("Longitude: ",longitude+"");

            scanLeDevice(true);
        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
           Toast.makeText(getApplicationContext(),"please enable gps.",Toast.LENGTH_SHORT).show();
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    scanLeDevice(true);

                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {


                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.e("rssi",rssi+"");
                    if(rssi>=-90){

                        if(!PreviousDevice.equals(device.getAddress().toString())) {
                            PreviousDevice = device.getAddress().toString();
                            if(PreviousRssi<rssi) {
                                PreviousDevice = device.getAddress().toString();
                                PreviousRssi=rssi;
                                Log.e("device", PreviousDevice);
                                gps = new GPSTracker(MainService.this);

                                if(gps.canGetLocation()) {
                                    latitude = gps.getLatitude();
                                    longitude = gps.getLongitude();
                                }
                           //     Toast.makeText(getApplicationContext(), PreviousDevice + ": " + rssi, Toast.LENGTH_SHORT).show();
                            //    Toast.makeText(getApplicationContext(), "latitude :" + latitude+"longitude :" + longitude, Toast.LENGTH_SHORT).show();

                                sendNotification("We found you :)");
                                api=new ApiCaller();
                                api.execute();
                            }
                        }
                    }
                    else
                    {
                        if(PreviousDevice.equals(device.getAddress().toString())) {
                            PreviousDevice = new String();
                        }
                    }
                }
            };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);
        PreviousDevice="";
        PreviousRssi=-500;
        Toast.makeText(this, "finished.", Toast.LENGTH_LONG).show();
    }

    private void sendNotification(String msg) {

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, OfferActivity.class);
        Log.e("device is:",PreviousDevice);
        intent.putExtra("address", PreviousDevice);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent contentIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Offers around")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);


        Uri alarmSound = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);



//        long[] pattern = { 500, 500, 500, 500, 500, 500, 500, 500, 500 };
//        mBuilder.setVibrate(pattern);




        mBuilder.setContentIntent(contentIntent);
        Notification n=mBuilder.build();
        n.flags|= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1, n);
    }



    private class ApiCaller extends AsyncTask<Void, Void, String> {
        ProgressDialog pDialog;
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);


        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                URI website = new URI("http://104.236.18.147:1337/beacons/create?lat="+latitude+"&lon="+longitude+"&beaconid=1111&recieverid=2222");
                Log.e("hmm",website.toString());
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);

                HttpEntity entity = response.getEntity();
                Log.e("haha ",entity.toString());

            } catch (Exception e) {
                Log.e("ERROR IN LOGIN", e.getMessage());
                return "sorry";
            }
            return "sorry";
        }
    }

}
