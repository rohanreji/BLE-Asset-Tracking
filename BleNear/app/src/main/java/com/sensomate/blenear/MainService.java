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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rohan on 17/2/15.
 */


@SuppressWarnings("ALL")
public class MainService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 60 seconds.
    private static final long SCAN_PERIOD = 60000;
    private static final long SEND_PERIOD = 10000;
    private static final int THRESHOLD_RSSI = -90;

    //for testing , random number
    private int a=0,i;
    private Random t;
    //Rabbitmq message
    private String message;
    private JSONArray students;
    private JSONObject studentlist[];
    private final String EXCHANGE_NAME = "logs";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel1;

    private String PreviousDevice;
    private int PreviousRssi;
    private double latitude;
    private double longitude;
    private GPSTracker gps;
    private ApiCaller api;
    TelephonyManager mngr;
    ArrayList<String> deviceList = new ArrayList<String>();
    ArrayList<Integer> rssiList = new ArrayList<Integer>();

    int rssimean;
    ConnectionDetector cd;

    //writting log to file for testing
    public static BufferedWriter out;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        PreviousDevice= "";
        PreviousRssi=-500;
        latitude=0.0;
        longitude=0.0;
        cd = new ConnectionDetector(getApplicationContext());
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        gps = new GPSTracker(this);

        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.e("Latitude: ",latitude+"");
            Log.e("Longitude: ",longitude+"");
            try {
                createFileOnDevice(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            scanLeDevice(true);
           // senddata();

        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
           Toast.makeText(getApplicationContext(),"please enable gps.",Toast.LENGTH_SHORT).show();
        }
    }
    public void senddata(){
        //original


        //checks if device is not present for 5 scans
        try {
            a = deviceList.size();


        }catch (Exception e){

        }

        try {
            createFileOnDevice(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
                /*
                checking
                 */
        a=deviceList.size();
        studentlist=new JSONObject[a+1];
        students = new JSONArray();


        for(i=0;i<a;i++){
                /*

                checking end
                 */
            try {
                studentlist[i]=new JSONObject();
                studentlist[i].put("id",deviceList.get(i));
                students.put(studentlist[i]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("sudentslist",students.toString());
        }

        message="[{\"name\":\"class1\",\"columns\":[\"recieverid\",\"time\",\"value\",\"students\"],\"points\" : [["+mngr.getDeviceId()+","+System.currentTimeMillis()+","+a+",\""+students.toString().replace("\"","").replace("[","").replace("]","")+"\"]]}]";
        new send1().execute(message);


    }
//    public void senddata(){
//
//        Timer timer = new Timer();
//
//        timer.schedule( new TimerTask(){
//            public void run() {
//                //code for testing it without ble, replace it for production
////                Random rnd = new Random();
////                a=(rnd.nextInt()+1)%30;
////                if(a<0)
////                    a=a*-1;
////                studentlist=new JSONObject[30];
////                for(i=0;i<30;i++){
////                    studentlist[i]=new JSONObject();
////                }
////                try {
////                    studentlist[0].put("id","123");
////                    studentlist[1].put("id","124");
////                    studentlist[2].put("id","125");studentlist[3].put("id","126");
////                    studentlist[4].put("id","127");
////                    studentlist[5].put("id","128");
////                    studentlist[6].put("id","129");
////                    studentlist[7].put("id","130");studentlist[8].put("id","131");
////                    studentlist[9].put("id","132");
////                    studentlist[10].put("id","133");
////                    studentlist[11].put("id","134");
////                    studentlist[12].put("id","135");
////                    studentlist[13].put("id","136");
////                    studentlist[14].put("id","137");studentlist[15].put("id","138");
////                    studentlist[16].put("id","139");
////                    studentlist[17].put("id","140");studentlist[18].put("id","141");
////                    studentlist[19].put("id","142");
////                    studentlist[20].put("id","143");studentlist[21].put("id","144");
////                    studentlist[22].put("id","145");
////                    studentlist[23].put("id","146");
////                    studentlist[24].put("id","147");studentlist[25].put("id","148");
////                    studentlist[26].put("id","149");studentlist[27].put("id","150");
////                    studentlist[28].put("id","151");
////                    studentlist[29].put("id","152");
////
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////                students = new JSONArray();
////                shuffleArray(studentlist);
////                for(i=(30-a);i<30;i++){
////                    try {
////                        students.put(studentlist[i].get("id"));
////
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                }
//
//                //original
//
//
//                //checks if device is not present for 5 scans
//                try {
//                    a = deviceList.size();
//
//
//                }catch (Exception e){
//
//                }
//
//                try {
//                    createFileOnDevice(true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                /*
//                checking
//                 */
//            a=deviceList.size();
//            studentlist=new JSONObject[a+1];
//            students = new JSONArray();
//
//
//            for(i=0;i<a;i++){
//                /*
//
//                checking end
//                 */
//                try {
//                    studentlist[i]=new JSONObject();
//                    studentlist[i].put("id",deviceList.get(i));
//                    students.put(studentlist[i]);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                Log.e("sudentslist",students.toString());
//            }
//
//                  message="[{\"name\":\"class1\",\"columns\":[\"recieverid\",\"time\",\"value\",\"students\"],\"points\" : [["+mngr.getDeviceId()+","+System.currentTimeMillis()+","+a+",\""+students.toString().replace("\"","").replace("[","").replace("]","")+"\"]]}]";
//                new send1().execute(message);
//                senddata();
//            }
//        }, SEND_PERIOD);
//    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    senddata();


                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);


        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }


    private void createFileOnDevice(Boolean append) throws IOException {
                /*
                 * Function to initially create the log file and it also writes the time of creation to file.
                 */
        File Root = Environment.getExternalStorageDirectory();
        if(Root.canWrite()){
            File  LogFile = new File(Root, "Log.txt");
            FileWriter LogWriter = new FileWriter(LogFile, append);
            out = new BufferedWriter(LogWriter);
            Date date = new Date();
            out.write("Logged at: " + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            try {
                a=deviceList.size();

                for(i=0;i<a;i++){
                    out.write(deviceList.get(i)+" ,rssi: "+rssiList.get(i)+"\n");

                }
                if(a==0){
                    out.write("No devices");
                }
                out.write("\n\n");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                out.close();
            }


        }
        else {
            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
        }
    }

    //code for testing it without ble, replace it for production
    static void shuffleArray(JSONObject[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            JSONObject a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {


                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.e("device",device.toString());

                   /*
Code for BLE NEAR
 */
//                    if(rssi>=-90){
//
//                        if(!PreviousDevice.equals(device.getAddress())) {
//                            PreviousDevice = device.getAddress();
//                            if(PreviousRssi<rssi) {
//                                PreviousDevice = device.getAddress();
//                                PreviousRssi=rssi;
//                                Log.e("device", PreviousDevice);
//                                gps = new GPSTracker(MainService.this);
//
//                                if(gps.canGetLocation()) {
//                                    latitude = gps.getLatitude();
//                                    longitude = gps.getLongitude();
//                                }
//                                //sendNotification("We found you :)");
//                                api=new ApiCaller();
//                                api.execute();
//                            }
//                        }
//                    }
//                    else
//                    {
//                        if(PreviousDevice.equals(device.getAddress())) {
//                            PreviousDevice = "";
//                        }
//                    }
                    //https://github.com/rohanreji93/BLE-Asset-Tracking/blob/master/AssetReceiverService-master/AssetReceiverService-master/src/com/maangalabs/assetreciever/MyService.java




                    if(a==0)
                    {
                        deviceList.add(device.toString());
                        rssiList.add(rssi);

                        a++;
                    }
                    else
                    {

                        int y;
                        int f=0;
                        for( y=0;y<deviceList.size();y++)
                        {
                            // Toast.makeText(getApplicationContext(), device+ " "+deviceList.get(y),Toast.LENGTH_SHORT).show();
                            if((deviceList.get(y)).equals(device.toString()))
                            {
                                rssimean=(rssiList.get(y)+rssi)/2;

                                if(rssi<THRESHOLD_RSSI) {
                                    deviceList.remove(y);
                                    rssiList.remove(y);

                                    Log.e("rssi:","  "+rssi+" ");
                                    Log.e("size removed ",deviceList.size()+" ");
                                }
                                else {
                                    rssiList.set(y, rssimean);

                                    Log.e("rssi:","  "+rssi+" ");
                                    Log.e("rssimean:","  "+rssimean+" ");
                                    Log.e("size ",deviceList.size()+" ");
                                }
                                f=1;
                                break;

                            }

                        }
                        if(f==0)
                        {
                            deviceList.add(device.toString());
                            rssiList.add(rssi);
                            a++;
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
        try {

                channel1.close();
                Log.e("channel1","closed the channel");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("channel1","cannot close the channel");
        }
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

    private class send1 extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            deviceList = new ArrayList<String>();
            rssiList = new ArrayList<Integer>();

            scanLeDevice(true);

        }

        @Override
        protected Void doInBackground(String... Message) {
            try {

                String tempstr = "";
                for (String aMessage : Message) tempstr += aMessage;
                JSONArray message=new JSONArray(tempstr);
                if (channel1 == null) {

                    factory = new ConnectionFactory();
                    factory.setHost("104.236.18.147");
                    // my internet connection is a bit restrictive so I have use an
                    // external server
                    // which has RabbitMQ installed on it. So I use "setUsername"
                    // and "setPassword"
                    factory.setUsername("aswindevps");
                    factory.setPassword("adps94");
                    //factory.setVirtualHost("/");
                    factory.setPort(5672);
                    System.out.println("" + factory.getHost() + factory.getPort() + factory.getRequestedHeartbeat() + factory.getUsername());
                    connection = factory.newConnection();
                    channel1 = connection.createChannel();
                    channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
                }
                else if(!channel1.isOpen()){
                    factory = new ConnectionFactory();
                    factory.setHost("104.236.18.147");

                    factory.setUsername("aswindevps");
                    factory.setPassword("adps94");
                    //factory.setVirtualHost("/");
                    factory.setPort(5672);
                    System.out.println("" + factory.getHost() + factory.getPort() + factory.getRequestedHeartbeat() + factory.getUsername());
                    connection = factory.newConnection();
                    channel1= connection.createChannel();
                    channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
                }
                channel1.basicPublish(EXCHANGE_NAME, "", null,
                        message.toString().getBytes());
                System.out.println("\nsend message:" + tempstr);


            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();




            }
            // TODO Auto-generated method stub
            return null;
        }
    }
    public class ConnectionDetector {

        private Context _context;

        public ConnectionDetector(Context context){
            this._context = context;
        }

        public boolean isConnectingToInternet(){
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null)
            {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }

            }
            return false;
        }
    }
}

