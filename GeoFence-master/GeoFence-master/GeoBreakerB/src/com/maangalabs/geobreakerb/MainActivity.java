package com.maangalabs.geobreakerb;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;
@SuppressLint("NewApi")
public class MainActivity extends Activity {
	int e=0;
	 private final static String TAG = "LocationLoggerService";
	 LocationManager lm;
	 
	 public int g;
	 public int o;
	 TextView T;
	public double latitude;
	public double longitude;
	
	
	public double km;
	int flags=0,ck=0;
	
	
	AlertDialog.Builder alertDialogBuilder;
	Location loc;
	LocationManager locationManager;
	static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // in Meters
	static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		o=0;
		flags=0;
		ck=0;
		T=(TextView)findViewById(R.id.textView1);
		 alertDialogBuilder = new AlertDialog.Builder(
				MainActivity.this);
		
		  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		    locationManager.requestLocationUpdates(
					                 LocationManager.GPS_PROVIDER,
					 
					                 MINIMUM_TIME_BETWEEN_UPDATES,
					 
					                 MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
					 
					                 new MyLocationListener());
		  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	    
	  private double distance(double lat1, double lon1, double lat2, double lon2) {
	  	  double theta = lon1 - lon2;
	  	  double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
	  	  dist = Math.acos(dist);
	  	  dist = rad2deg(dist);
	  	  dist = dist * 60 * 1.1515;
	  	
	  	    dist = dist * 1.609344;
	  	  
	  	  return (dist);
	  	}

	  private double deg2rad(double deg) {
	  	  return (deg * Math.PI / 180.0);
	  	}
	  private double rad2deg(double rad) {
	  	  return (rad * 180 / Math.PI);
	  	}
	

	 protected void showCurrentLocation() {
		
		 double lat1[]={8.546784,8.546748,8.551032,8.565891,8.580530,8.566805,8.574945};
		 double lon1[]={76.905410,76.902158,76.910849,76.874861,76.878068,76.889577,76.868771};
		String st[]={"CET","ThiruNagar","Chavadimukku","Kazhakootam","EYKinfra","Karyavattom","Al-Saj"};
		 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
     	if (location != null) {
     		loc=location;
		
     	}
     
     	if(loc!=null)
    	{
     		latitude = loc.getLatitude();
    		longitude = loc.getLongitude();
    		double km1=distance(lat1[o],lon1[o],latitude,longitude);
    		T.setText(km1+" ");
    	if(flags==0){
    	
    	
    	 
		
		for(int i=0;i<7;i++)
		{
		km=distance(lat1[i],lon1[i],latitude,longitude);
		Log.e("tag: "+i,"km: "+km);
		if((km<0.1)&&(ck==0)){
			flags=1;
			ck=1;
			o=i;
			Log.e("gd","fe");
			T.setText(km+" ");
				// set title
				alertDialogBuilder.setTitle("Welcome");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("Into the fence "+st[i])
					.setCancelable(false)
					.setNegativeButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							ck=0;
							dialog.cancel();
						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
					         break;
		}
		}
		if((flags==0)&&(ck==0))
		{
			T.setText("Not inside Any Fence....");
			
			/*ck=1;
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
	 
				// set title
				alertDialogBuilder.setTitle("Not broken");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("Not inside a fence")
					.setCancelable(false)
					.setNegativeButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							ck=0;
							dialog.cancel();
						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();*/
		}
    	}
    	
    	else
    	{
    		km=distance(lat1[o],lon1[o],latitude,longitude);
    		if((km>=0.1)&&(ck==0))
    		{
    			ck=1;
    			T.setText("Just out "+st[0]);
    			Log.e("gd","fe");
    				// set title
    				alertDialogBuilder.setTitle("Visit Again ");
    	 
    				// set dialog message
    				alertDialogBuilder
    					.setMessage("outside fence "+st[o])
    					.setCancelable(false)
    					.setNegativeButton("OK",new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog,int id) {
    							// if this button is clicked, just close
    							// the dialog box and do nothing
    							ck=0;
    							dialog.cancel();
    						}
    					});
    	 
    					// create alert dialog
    					AlertDialog alertDialog = alertDialogBuilder.create();
    	 
    					// show it
    					alertDialog.show();
    					flags=0;
    		}
    	}
    	}
    	else{
    		
    		
				// set title
				alertDialogBuilder.setTitle("Sorry!!");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage("Confused")
					.setCancelable(false)
					.setNegativeButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();
						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
			
    	}
     }  
	 public void clicked(View v)
	 {
		 Intent i= new Intent(getApplicationContext(), MyService.class);
		 i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);	
		 // potentially add data to the intent
			i.putExtra("KEY1", "Value to be used by the service");
			this.startService(i); 
			finish();
	 }
	private class MyLocationListener implements LocationListener {
    	
    	public void onLocationChanged(Location location) {
            	
    		   		showCurrentLocation();
            	           
            	          
    	}
    	public void onStatusChanged(String s, int i, Bundle b) {
    	}
    	public void onProviderDisabled(String s) {
           }
    	public void onProviderEnabled(String s) {
      }
    }
}


