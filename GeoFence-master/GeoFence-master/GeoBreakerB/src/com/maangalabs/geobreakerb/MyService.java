package com.maangalabs.geobreakerb;



import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class MyService extends Service {
	int e=0;
	 private final static String TAG = "LocationLoggerService";
	 LocationManager lm;
	 
	 public int g;
	 public int o;
	
	public double latitude;
	public double longitude;
	
	
	public double km;
	int flags=0,ck=0;
	
	
	Ringtone r;
	Location loc;
	LocationManager locationManager;
	static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // in Meters
	static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 public void onCreate() {
		 // subscribeToLocationUpdates();
		 o=0;
			flags=0;
			ck=0;
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			    locationManager.requestLocationUpdates(
						                 LocationManager.GPS_PROVIDER,
						 
						                 MINIMUM_TIME_BETWEEN_UPDATES,
						 
						                 MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
						 
						                 new MyLocationListener());
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
	

	 @SuppressWarnings("deprecation")
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
 		
 	if(flags==0){
 	
 	
 	 
		
		for(int i=0;i<7;i++)
		{
		km=distance(lat1[i],lon1[i],latitude,longitude);
		Log.e("service: "+i,"km: "+km);
		if((km<0.1)&&(ck==0)){
			flags=1;
			o=i;
			Log.e("gd","service");
		
				// set title
				
	 
				// set dialog message
			
			 NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
			 
			    Intent intent = new Intent(MyService.this, MainActivity.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			    //use the flag FLAG_UPDATE_CURRENT to override any notification already there
			    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			 
			    Notification notification = new Notification(R.drawable.ic_launcher, "Welcome!", System.currentTimeMillis());
			    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
			 
			    notification.setLatestEventInfo(this, "Welcome", "inside "+st[i], contentIntent);
			    //10 is a random number I chose to act as the id for this notification
			    notificationManager.notify(10, notification);
			
			r.play();
			
			
					         break;
		}
		}
		if((flags==0)&&(ck==0))
		{
			
			
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
 			
 			
 			
 			 NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
 			 
 		    Intent intent = new Intent(MyService.this, MainActivity.class);
 		     
 		    //use the flag FLAG_UPDATE_CURRENT to override any notification already there
 		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		 
 		    Notification notification = new Notification(R.drawable.ic_launcher, "Visit Again!", System.currentTimeMillis());
 		    notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
 		 
 		    notification.setLatestEventInfo(this, "ThankYou", "just out "+st[o], contentIntent);
 		    //10 is a random number I chose to act as the id for this notification
 		    notificationManager.notify(10, notification);
 		   r.play();
 			flags=0;
 		}
 	}
 	}
 	else{
 		
 		
 	}
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
