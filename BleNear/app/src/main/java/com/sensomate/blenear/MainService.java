package com.sensomate.blenear;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        scanLeDevice(true);

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
                            if(PreviousRssi<rssi) {
                                PreviousDevice = device.getAddress().toString();
                                PreviousRssi=rssi;
                                Log.e("device", PreviousDevice);
                                Toast.makeText(getApplicationContext(), PreviousDevice + ": " + rssi, Toast.LENGTH_SHORT).show();
                                sendNotification("We found you :)");
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
        Toast.makeText(this, "We are helping you.", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        scanLeDevice(false);

        Toast.makeText(this, "Thanks for trusting me.", Toast.LENGTH_LONG).show();
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



        long[] pattern = { 500, 500, 500, 500, 500, 500, 500, 500, 500 };
        mBuilder.setVibrate(pattern);


        mBuilder.setContentIntent(contentIntent);
        Notification n=mBuilder.build();
        n.flags|= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(1, n);
    }
}
