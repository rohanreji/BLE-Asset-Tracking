package com.example.nfcdetector;



import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi") public class MainActivity extends Activity {
	protected NfcAdapter nfcAdapter;
	protected PendingIntent nfcPendingIntent;
	String tagid;
	Intent in;
	TextView t;
	boolean active;
	private static final String TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#e74c3c")));
        bar.setTitle(Html.fromHtml("<font color='#000000'>NFC Detector</font>"));
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		t=(TextView)findViewById(R.id.textView1);
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(this.getIntent().getAction()))
		{
			
			   byte[] typ,payload ;
			    NdefMessage ndefMesg;
			/*  
			    Tag myTag = (Tag) in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			    
			    Ndef ndefTag = Ndef.get(myTag);
			    int size = ndefTag.getMaxSize();         // tag size
			    String type = ndefTag.getType();  // tag type
			     ndefMesg = ndefTag.getCachedNdefMessage();*/
			    
			 
			    	 Parcelable[] rawMsgs = this.getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			          
			            	
			            
			            	ndefMesg = (NdefMessage) rawMsgs[0];
			               
			    	
			  
			    NdefRecord[] ndefRecords = ndefMesg.getRecords();
			    int len = ndefRecords.length;
			    for (int i = 0; i < len; i++) {
			      typ = ndefRecords[i].getType();
			       payload = ndefRecords[i].getPayload();
			       tagid= getTextData(payload);
			      
			       Toast.makeText(getApplicationContext(), tagid+" scan again",Toast.LENGTH_SHORT).show();
			       t.setText("TagId:\n"+tagid);
			       UploadASyncTask upload = new UploadASyncTask();
		            upload.execute();
			       
			    }
		}
	
	}
	
    
    

    


@SuppressLint("NewApi") public void enableForegroundMode() {
	Log.d(TAG, "enableForegroundMode");

	IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED); // filter for all
	IntentFilter[] writeTagFilters = new IntentFilter[] {tagDetected};
	nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, writeTagFilters, null);
}

@SuppressLint("NewApi") public void disableForegroundMode() {
	Log.d(TAG, "disableForegroundMode");

	nfcAdapter.disableForegroundDispatch(this);
}






@Override
public void onStart() {
   super.onStart();
   active = true;
} 

@Override
public void onStop() {
   super.onStop();
   active = false;
}





@Override
public void onNewIntent(Intent intent) {
	Log.d(TAG, "onNewIntent");


	if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {		
			in=intent;
			 byte[] typ,payload ;
			    NdefMessage ndefMesg;
			    Parcelable[] rawMsgs = in.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			    ndefMesg = (NdefMessage) rawMsgs[0];
			               
			    		  
			    NdefRecord[] ndefRecords = ndefMesg.getRecords();
			    int len = ndefRecords.length;
			    for (int i = 0; i < len; i++) {
			    	typ = ndefRecords[i].getType();
			    	payload = ndefRecords[i].getPayload();
			    	tagid= getTextData(payload);
			      // Toast.makeText(getApplicationContext(), tagid,Toast.LENGTH_SHORT).show();
			       
			   	}
        	
		
				Toast.makeText(getApplicationContext(), "Connected "+tagid,Toast.LENGTH_SHORT).show();
				 t.setText("TagId:\n"+tagid);
				 UploadASyncTask upload = new UploadASyncTask();
		            upload.execute();
			
			
		
	} else {
		Toast.makeText(getApplicationContext(),"No Tag Discovered", Toast.LENGTH_SHORT).show();
		
	}
}


private String getTextData(byte[] payload) {
	  if(payload == null)
	    return null;
	  try {
	    String encoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
	    int langageCodeLength = payload[0] & 0077;
	    return new String(payload, langageCodeLength + 1, payload.length - langageCodeLength - 1, encoding);    
	  } catch(Exception e) {
	    e.printStackTrace();
	  }
	  return null;
	}


private void vibrate() {
	Log.d(TAG, "vibrate");
	
	Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
	vibe.vibrate(500);
}


@Override
protected void onResume() {
	Log.d(TAG, "onResume");

	super.onResume();

	enableForegroundMode();
}

@Override
protected void onPause() {
	Log.d(TAG, "onPause");

	super.onPause();

	disableForegroundMode();
}

    


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
  
/*
 * upload scanned tag to server    
 */
    

private class UploadASyncTask extends AsyncTask<Void,Void, Void>{

	@Override
	protected Void doInBackground(Void... params) {
            try{
            	
            	
            
               TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
               JSONObject location = new JSONObject();
                
                
               location.put("uid", tagid);
               location.put("capturedAt", System.currentTimeMillis());
               location.put("deviceid", mngr.getDeviceId());
               
            	
            	
            
                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost("https://sensomate-checkin.herokuapp.com/attendance/pushtodb");

                String json = "";

                json = location.toString();

                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);

                httpPost.setHeader("User-Agent", "NFC-DETECTOR/1.0");
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
              

                HttpResponse httpResponse = httpclient.execute(httpPost);

                InputStream inputStream = httpResponse.getEntity().getContent();
            //    Toast.makeText(getApplicationContext(), inputStream, Toast.LENGTH_SHORT).
              

            }catch(Exception e){

                Log.e("ERROR IN SEVER UPLOAD", e.getMessage());
            }
            return null;


        }
}    
    
   
}


















	

