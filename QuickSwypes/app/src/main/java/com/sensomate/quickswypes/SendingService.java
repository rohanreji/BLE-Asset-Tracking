package com.sensomate.quickswypes;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import redis.clients.jedis.Jedis;

/**
 * Created by rohan on 15/5/15.
 */
public class SendingService extends Service {

    SwypedDbHandler sdb;
    JedisThread jt;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Timer t = new Timer();

//
//                jt = new JedisThread();
//                jt.execute();
//                Toast.makeText(this, "Service Started!", Toast.LENGTH_LONG).show();

        t.scheduleAtFixedRate(
                new TimerTask()
                {
                    public void run()
                    {
                 if (!PreferenceManager
                     .getDefaultSharedPreferences(
                             SendingService.this)
                         .getString("TOKEN", "ZERO")
                         .equals("ZERO")) {
                     //also check if it is cancelled :
                     if ((jt != null)) {
                         jt = new JedisThread();
                         jt.execute();
                     }
                     if (jt == null) {
                         jt = new JedisThread();
                         jt.execute();
                     }
                 }


                    }
                },
                0,      // run first occurrence immediatetly
                3000);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
    class JedisThread extends AsyncTask<Void, Void, Void> {

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog


        }

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            sdb  = new SwypedDbHandler(SendingService.this);
            if(sdb!=null) {
                List<AttendanceDetails> sdb_dummy_list = sdb.getAllContacts();
                JedisTrial j = new JedisTrial();

                for (AttendanceDetails a : sdb_dummy_list) {
                    j.setupPublisher(a);

                }

            }


            return null;
        }

    }

    public class JedisTrial {
        // private ArrayList<String> messageContainer = new ArrayList<String>();
        private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
        private CountDownLatch publishLatch = new CountDownLatch(1);
        int l = (PreferenceManager
                .getDefaultSharedPreferences(SendingService.this.getApplicationContext())
                .getString("MYIP", "").toString().length());
        // no http://

        String JEDIS_SERVER = PreferenceManager
                .getDefaultSharedPreferences(SendingService.this.getApplicationContext())
                .getString("MYIP", "").toString()
                .subSequence(7, l).toString();

        private void setupPublisher(AttendanceDetails a) {
            try {
                if(JEDIS_SERVER.contains(":")){
                    String s[]=new String[2];
                    s=JEDIS_SERVER.split(":");
                    JEDIS_SERVER=s[0];
                    //  Toast.makeText(getApplicationContext(),JEDIS_SERVER,Toast.LENGTH_SHORT).show();
                    Log.e("server redis", JEDIS_SERVER);
                }

                System.out.println("Connecting");
                System.out.println(JEDIS_SERVER);
                Jedis jedis = new Jedis(JEDIS_SERVER, 6379);

                jedis.auth("sensomate_123#");

                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                JSONObject location = new JSONObject();

                location.put("uid", a.getID());
                location.put("capturedAt", a.getCapturedAt());
                location.put("deviceid", mngr.getDeviceId());
                int siteid = PreferenceManager.getDefaultSharedPreferences(
                        SendingService.this.getApplicationContext()).getInt("SITE_CODE", 0);
                Log.e("worksiteid:", siteid + " ");
                location.put("projcode", siteid);
                location.put("batlevel", getBatteryLevel());


                String json = "";
                json = location.toString();
                System.out.println("Waiting to publish");
                // publishLatch.await();
                System.out.println("Ready to publish, waiting one sec");
                // Thread.sleep(1000);
                System.out.println("publishing");

                // jsonstring here...
                jedis.publish("sensomate_channel", json);
                jedis.auth("sensomate_123#");
                System.out.println("published, closing publishing connection");
                jedis.quit();
                System.out.println("publishing connection closed");
                sdb.deleteContact(a);

            } catch (Exception e) {
                System.out.println(">>> OH NOES Pub, " + e.getMessage());

                e.printStackTrace();
            }
        }
    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }

}
