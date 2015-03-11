package com.sensomate.blenear;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    Button HelpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
