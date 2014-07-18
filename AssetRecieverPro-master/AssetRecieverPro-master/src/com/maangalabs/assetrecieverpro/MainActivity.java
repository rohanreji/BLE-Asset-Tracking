package com.maangalabs.assetrecieverpro;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity{
	Button b1;
	Button b2;
	BluetoothAdapter bluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;
	static Context context;	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
			
		setContentView(R.layout.activity_main);
	    new LongOperation().execute(" ");
		
		TextView txt1 = (TextView) findViewById(R.id.textView3);
		Typeface font = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
		
		txt1.setTypeface(font);
		context=getApplicationContext();
		ActionBar bar = getActionBar();
		//for color
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1569C7")));
		 if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	            Toast.makeText(this, "ble not supported", Toast.LENGTH_SHORT).show();
	            finish();
	            return;
	        }

	        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
	        // BluetoothAdapter through BluetoothManager.
	        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	        bluetoothAdapter = bluetoothManager.getAdapter();

	        // Checks if Bluetooth is supported on the device.
	        if (bluetoothAdapter == null) {
	            Toast.makeText(this, "bluetooth not supported", Toast.LENGTH_SHORT).show();
	            finish();
	            return;
	        }
	        b1=(Button)findViewById(R.id.button1);
	        b2=(Button)findViewById(R.id.button2);
	        b2.setVisibility(View.INVISIBLE);
	        checker();
		}
		public void checker()
		{
			if(isMyServiceRunning())
			{
						
				b1.setText("Stop Service");
						
			}
			else
			{
			//setContentView(R.layout.mymain);
				b1.setText("Start Service");
			//b2.setVisibility(View.INVISIBLE);
			
			}
		}
		public void starter(View v)
		{
			Button b=(Button)v;
			if(b.getText()=="Start Service")
			{
				Intent i= new Intent(getApplicationContext(), MyService.class);
				// potentially add data to the intent
				i.putExtra("KEY1", "Value to be used by the service");
				this.startService(i); 
			}
			else
			{
				stopService(new Intent(MainActivity.this, MyService.class));
			}
			checker();
		}
		public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
    public boolean onOptionsItemSelected(MenuItem item) {
	     switch (item.getItemId()) {
	            case R.id.action_location_found:
	       	
	            ddemo();
	            return true;
	       
	            default:
	        	return super.onOptionsItemSelected(item);
	        }
	 }
    public boolean haveNetworkConnection() {
		 boolean haveConnectedWifi = false;
		 boolean haveConnectedMobile = false;

		 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		 for (NetworkInfo ni : netInfo) {
			 if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				 if (ni.isConnected())
					 haveConnectedWifi = true;
			 if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				 if (ni.isConnected())
					 haveConnectedMobile = true;
		 }
	return haveConnectedWifi || haveConnectedMobile;
	 }
    public void ddemo()
    {
    	final EditText e=new EditText(this);
    	AlertDialog.Builder alert=new AlertDialog.Builder(this);
    	alert.setTitle("Set Server");
    	alert.setView(e);
    	e.setText(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("MYIP","192.168.9.1"));
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			
    			PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("MYIP",e.getText().toString()).commit(); 
    			dialog.cancel();
    			
				
    		}
    	});
    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
					    // Canceled.
    		}
    	});
    	alert.show();

    	}
	 	private boolean isMyServiceRunning() {
	 		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	 			if (MyService.class.getName().equals(service.service.getClassName())) {
	 				return true;
	 			}	
	 		}
	 		return false;
	 	}
	 	@Override
	    protected void onResume() {
	 		super.onResume();

	        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
	        // fire an intent to display a dialog asking the user to grant permission to enable it.
	 		if (!bluetoothAdapter.isEnabled()) {
	 			final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	 			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	            return;
	        }

	       
	    }
		private class LongOperation extends AsyncTask<String, Void, String> {

	        @Override
	        protected String doInBackground(String... params) {
	        	
	        	if(haveNetworkConnection())
	    		{
	    			StringBuilder builder = new StringBuilder();
	        	    HttpClient client = new DefaultHttpClient();
	        	    HttpGet httpGet = new HttpGet("http://192.168.2.10:1337/zones/");
	        	    try {
	        	      HttpResponse response = client.execute(httpGet);
	        	      StatusLine statusLine = response.getStatusLine();
	        	      int statusCode = statusLine.getStatusCode();
	        	      if (statusCode == 200) {
	        	        HttpEntity entity = response.getEntity();
	        	        InputStream content = entity.getContent();
	        	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	        	        String line;
	        	        while ((line = reader.readLine()) != null) {
	        	          builder.append(line);
	        	        }
	        	      } else {
	        	        Log.e("sorry", "Failed to download file");
	        	      }
	        	    } catch (ClientProtocolException e) {
	        	      e.printStackTrace();
	        	    } catch (Exception e) {
	        	      e.printStackTrace();
	        	    } 
	            /*checker
	             * 
	             */
	        	   Log.d("checker",builder.toString());
	        	    try {

	        	        JSONArray obj = new JSONArray(builder.toString());

	        	        Log.d("My App", obj.getJSONObject(0).toString());

	        	    } catch (Throwable t) {
	        	        Log.e("My App", "Could not parse malformed JSON: " + builder.toString());
	        	    }
	    		}
	    	
				return null;
	        	
	        	
	            
	        }
	       
	        
	        @Override
	        protected void onPostExecute(String result) {
	            // txt.setText(result);
	            // might want to change "executed" for the returned string passed
	            // into onPostExecute() but that is upto you
	        }

	        @Override
	        protected void onPreExecute() {}

	        @Override
	        protected void onProgressUpdate(Void... values) {}
	    }
	 	public void toAudit(View v)
	 	{
	 		Intent i=new Intent(this,AuditLister.class);
	 		startActivity(i);
	 		
	 	}
	  @Override
	   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        // User chose not to enable Bluetooth.
		  if (requestCode == REQUEST_ENABLE_BT) {
	            	if (resultCode == Activity.RESULT_CANCELED) {
	            		finish();
	            } 
	            	else {
	                           
	            	}
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }

}
