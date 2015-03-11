package com.sensomate.blenear;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.URI;


public class MainActivity extends ActionBarActivity {
    Button HelpButton;
    double latitude;
    double longitude;
    GPSTracker gps;
    ApiCaller api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        latitude=0.0;
        longitude=0.0;
        setContentView(R.layout.activity_main);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "sorry no ble in your device.", Toast.LENGTH_SHORT).show();
            finish();
        }
        HelpButton = (Button)findViewById(R.id.helpme_button);
        if(isMyServiceRunning(MainService.class))
        {
            HelpButton.setText("Stop");
        }
        else
        {
            HelpButton.setText("Start");
        }
    }

    public void simulate(View v)
    {
        gps = new GPSTracker(this);

        if(gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Log.e("Latitude: ", latitude + "");
            Log.e("Longitude: ", longitude + "");
            /* on test*/
            api = new ApiCaller();
            api.execute();
        }
    }
   public void startService(View v)
   {
       if(HelpButton.getText().toString().equals("Start")) {
           startService(new Intent(this, MainService.class));
           HelpButton.setText("Stop");
       }
       else {
           stopService(new Intent(this, MainService.class));
           HelpButton.setText("Start");
           finish();
       }
   }
    /*
    TO check if service is running
     */
   private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
   }
/*
testing only
 */


    private class ApiCaller extends AsyncTask<Void, Void, String> {
        ProgressDialog pDialog;
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            if(result.equals("ok"))
              Toast.makeText(getBaseContext(),"delivered",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getBaseContext(),"check network",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                URI website = new URI("http://104.236.18.147:1337/beacons/create?lat="+latitude+"&lon="+longitude+"&beaconid=1111&recieverid=2222");
                Log.e("hmm", website.toString());
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);

                HttpEntity entity = response.getEntity();
                Log.e("haha ",entity.toString());


            } catch (Exception e) {
                Log.e("ERROR IN LOGIN", e.getMessage());
                return "sorry";
            }
            return "ok";
        }
    }
}
