package com.sensomate.blenear;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by rohan on 17/2/15.
 */
public class OfferActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offer_layout);
        TextView t=(TextView)findViewById(R.id.address_text);
        String DeviceAddress=new String();
        DeviceAddress= getIntent().getStringExtra("address");



        t.setText(DeviceAddress);
        Typeface font = Typeface.createFromAsset(getAssets(), "demo.otf");
        t.setTypeface(font);

    }


}
