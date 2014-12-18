package com.example.nfcdetector;



import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;



import redis.clients.jedis.Jedis;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction()))
		{
			
        	
        	
        	Tag tagFromIntent = this.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        	  
         	Log.d(TAG, "UID: " + bin2hex(tagFromIntent.getId()));
         	tagid=bin2hex(tagFromIntent.getId());
         	t.setText(tagid);
         	Toast.makeText(getApplicationContext(),bin2hex(tagFromIntent.getId()), Toast.LENGTH_SHORT).show();
         	UploadASyncTask upload = new UploadASyncTask();
            upload.execute();
        	
			
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


	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {		
			in=intent;
						 
			 Tag tagFromIntent = in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			      	  
       	 
       	  
       	  
       	Log.d(TAG, "UID: " + bin2hex(tagFromIntent.getId()));
   
       	Toast.makeText(getApplicationContext(),bin2hex(tagFromIntent.getId()), Toast.LENGTH_SHORT).show();
			 
       	tagid=bin2hex(tagFromIntent.getId());
     	t.setText(tagid);
			 
       	UploadASyncTask upload = new UploadASyncTask();
       upload.execute();
			 
			 
			 
			 
			 
			 
			 
			 
			 
//			    NdefMessage ndefMesg;
//			    Parcelable[] rawMsgs = in.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//			    ndefMesg = (NdefMessage) rawMsgs[0];
			               
			    
//					 Tag tagFromIntent = in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//	            	 typ=tagFromIntent.getId();
//	            	 tagid= getTextData(typ);
//	            	 
//	            	 Toast.makeText(getApplicationContext(), tagid+" scan again",Toast.LENGTH_SHORT).show();
//			    		  
//			    NdefRecord[] ndefRecords = ndefMesg.getRecords();
//			    int len = ndefRecords.length;
//			    for (int i = 0; i < len; i++) {
//			    	typ = ndefRecords[i].getType();
//			    	payload = ndefRecords[i].getPayload();
//			    	tagid= getTextData(payload);
//			      // Toast.makeText(getApplicationContext(), tagid,Toast.LENGTH_SHORT).show();
//			       
//			   	}
//        	
//		
//				Toast.makeText(getApplicationContext(), "Connected "+tagid,Toast.LENGTH_SHORT).show();
//				 t.setText("TagId:\n"+tagid);
//				// new RetrieveFeedTask().execute();
//				 UploadASyncTask upload = new UploadASyncTask();
//		            upload.execute();
//
			
			
		
	} else {
		Toast.makeText(getApplicationContext(),"No Tag Discovered", Toast.LENGTH_SHORT).show();
		
	}
}














/*
 * To set server
 */


static String bin2hex(byte[] data) {
    return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
}
public void ddemo()
{
final EditText e=new EditText(this);
AlertDialog.Builder alert=new AlertDialog.Builder(this);
alert.setTitle("Set Server");
alert.setView(e);
e.setText(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("MYIP","http://ec2-54-148-0-61.us-west-2.compute.amazonaws.com:1337/attendance/pushtodb"));
alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int whichButton) {
PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("MYIP",e.getText().toString()).commit();
dialog.cancel();
}
});
alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int whichButton) {
//Canceled.
}
});
alert.show();
}














/*
 * Async task to call jedistrial's function setuppublisher
 */
//
//class RetrieveFeedTask extends AsyncTask<Void, Void, Void> {
//
//    private Exception exception;
//
//
//    protected void onPostExecute(String feed) {
//        // TODO: check this.exception 
//        // TODO: do something with the feed
//    }
//
//	@Override
//	protected Void doInBackground(Void... params) {
//		// TODO Auto-generated method stub
//		JedisTrial j=new JedisTrial();
//	      
//        j.setupPublisher();
//		return null;
//	}
//
//	
//}
//
///*
// * class to send data to redis server
// */
//public class JedisTrial {
////private ArrayList<String> messageContainer = new ArrayList<String>();
//private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
//private CountDownLatch publishLatch = new CountDownLatch(1);
//private final String JEDIS_SERVER ="54.148.0.61";
//private void setupPublisher() {
//try {
//System.out.println("Connecting");
//System.out.println(JEDIS_SERVER);
//Jedis jedis = new Jedis(JEDIS_SERVER,6379);
//System.out.println("Waiting to publish");
////publishLatch.await();
//System.out.println("Ready to publish, waiting one sec");
////Thread.sleep(1000);
//System.out.println("publishing");
//jedis.publish("ch1",tagid);
//System.out.println("published, closing publishing connection");
//jedis.quit();
//System.out.println("publishing connection closed");
//} catch (Exception e) {
//System.out.println(">>> OH NOES Pub, " + e.getMessage());
//e.printStackTrace();
//}
//}
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//private String getTextData(byte[] payload) {
//	  if(payload == null)
//	    return null;
//	  try {
//	    String encoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
//	    int langageCodeLength = payload[0] & 0077;
//	    return new String(payload, langageCodeLength + 1, payload.length - langageCodeLength - 1, encoding);    
//	  } catch(Exception e) {
//	    e.printStackTrace();
//	  }
//	  return null;
//	}
//
//
//private void vibrate() {
//	Log.d(TAG, "vibrate");
//	
//	Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
//	vibe.vibrate(500);
//}
//

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
        	ddemo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
  
/*
 * upload scanned tag to server by http post 
 */
//    
    String responseBody ;
    ProgressDialog pDialog;
private class UploadASyncTask extends AsyncTask<Void,Void, Void>{

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		 if (pDialog.isShowing())
             pDialog.dismiss();
		//Toast.makeText(getApplicationContext(), r.toString(), Toast.LENGTH_SHORT).show();
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
	protected Void doInBackground(Void... params) {
            try{
            	
            	
            
               TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
               JSONObject location = new JSONObject();
                
                
               location.put("uid", tagid);
               location.put("capturedAt", System.currentTimeMillis());
               location.put("deviceid", mngr.getDeviceId());
               
            	
            	
            
                HttpClient httpclient = new DefaultHttpClient();
                
               // t.setText("rohan");
              final String JEDIS_SERVER1 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("MYIP","http://ec2-54-148-0-61.us-west-2.compute.amazonaws.com:1337/attendance/pushtodb");
             //   final String JEDIS_SERVER1 =  "http://ec2-54-148-0-61.us-west-2.compute.amazonaws.com:1337/attendance/pushtodb";
               
              URL url = new URL(JEDIS_SERVER1);

              HttpPost httpPost = new HttpPost(JEDIS_SERVER1);

                String json = "";

                json = location.toString();

                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);

                httpPost.setHeader("User-Agent", "NFC-DETECTOR/1.0");
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
              
                	
//                ResponseHandler<String> responseHandler=new BasicResponseHandler();
//                responseBody = httpclient.execute(httpPost, responseHandler);
//               
              
               HttpResponse response = httpclient.execute(httpPost);
               HttpEntity entity = response.getEntity();

               String jsonString = EntityUtils.toString(entity);
               Log.d("rear", jsonString);
//r=httpResponse;
               // InputStream inputStream = httpResponse.getEntity().getContent();
         

            }catch(Exception e){

                Log.e("ERROR IN SEVER UPLOAD", e.getMessage());
             //   t.setText(responseBody +"\n\nto "+PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("MYIP","http://ec2-54-148-0-61.us-west-2.compute.amazonaws.com:1337/")); 
        	//	t.setText("hello");
              
            }
            return null;


        }
}    
    
   
}


















	

