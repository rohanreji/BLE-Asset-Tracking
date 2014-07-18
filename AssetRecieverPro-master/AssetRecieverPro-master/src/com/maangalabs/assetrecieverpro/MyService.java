package com.maangalabs.assetrecieverpro;


import java.util.ArrayList;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import redis.clients.jedis.JedisPoolConfig;


import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.widget.Toast;


@SuppressLint("NewApi")
public class MyService extends Service implements BluetoothAdapter.LeScanCallback {
	JedisPoolConfig poolConfig = new JedisPoolConfig();
    double latit,longit;
	JSONObject devices = new JSONObject();
    public int batlevel;
    private static final long SCAN_PERIOD = 3000;
	ArrayList<String> deviceList = new ArrayList<String>();
    ArrayList<Integer> rssiList = new ArrayList<Integer>();
	public BleDevicesAdapter leDeviceListAdapter;
	private BluetoothAdapter bluetoothAdapter;
	private Scanner scanner;
	int count=0;
	int rssimean;
	public String members;
	Location loc;
	LocationManager locationManager;
	static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 3; // in Meters
	static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	GPSTracker gps;
	JSONObject jsonstruct = new JSONObject();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 @Override
     public int onStartCommand(Intent intent, int flags, int startId) { 
		 super.onStartCommand(intent, flags, startId);
		 return START_STICKY;
     }
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		gps = new GPSTracker(MyService.this);
		final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	    bluetoothAdapter = bluetoothManager.getAdapter();
     	
	    
	    if (scanner == null) {
             scanner = new Scanner(bluetoothAdapter);
             scanner.startScanning();
		 }
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(
				                 LocationManager.GPS_PROVIDER,
				 
				                 MINIMUM_TIME_BETWEEN_UPDATES,
				 
				                 MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
				 
				                 new MyLocationListener()
				 
				         );
		 getBatteryPercentage();
		
	}
	
	 private void getBatteryPercentage() {
		  BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
		         public void onReceive(Context context, Intent intent) {
		             context.unregisterReceiver(this);
		             int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		             int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		             int level = -1;
		             if (currentLevel >= 0 && scale > 0) {
		                 level = (currentLevel * 100) / scale;
		             }
		             batlevel=level;
		         }
		     }; 
		  IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		  registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	 }
	 public void displayer()
	 {
	 	try{
	 		if(gps.canGetLocation())
	        {
	 			showCurrentLocation();
	 			getBatteryPercentage();
	 			latit=loc.getLatitude();
	 			longit=loc.getLongitude();
	    		members=new String();
	    		JSONObject indmem[] = new JSONObject[2000];
	    		JSONArray mem = new JSONArray();
	    		Log.e("demotest",deviceList.size()+" ");
				for(int u=0;u<deviceList.size();u++)
				{
					try {
							indmem[u]=new JSONObject();
							indmem[u].put("rssi",rssiList.get(u));
							String mac1=deviceList.get(u).replace(":","");
							indmem[u].put("mac", mac1);
			   			} catch (JSONException e) {
			    // TODO Auto-generated catch block
			   				e.printStackTrace();
			   			}
				mem.put(indmem[u]);
			
				}	
			    try {
			    		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			    		Long tsLong = System.currentTimeMillis()/1000;
			    		String ts = tsLong.toString();
			    		devices.put("lat", latit);
			    		devices.put("long", longit);
			    		devices.put("count",deviceList.size());
			    		devices.put("imei",telephonyManager.getDeviceId());
			    		devices.put("timestamp", ts);
			    		devices.put("members",mem);
			    		devices.put("batterylevel", batlevel);
			    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			    		e.printStackTrace();
			    	}
			    count=0;
			    deviceList.clear();
			    rssiList.clear();
				Log.e("dev:",devices.toString());
				JedisTrial j=new JedisTrial();
				if(latit!=0.0)
				{
					j.setupPublisher();
				}	
	        }
    	}
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	 }
	 public void onDestroy(){
		
	     super.onDestroy();
	     sendBroadcast(new Intent("neverdie"));
		 Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
		
	 }
	 @Override
	 public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
	     Log.d("count:",count+" ");
	     if(count==0)
	     { 
	    	 deviceList.add(device.toString());
	    	 rssiList.add(rssi);
	    	 count++;
	     }
	     else
	     {
	    	 int y;
	    	 int f=0;
	    	 for( y=0;y<deviceList.size();y++)
	    	 {
	    		 if((deviceList.get(y)).equals(device.toString()))
	    		 {
	    			 rssimean=(rssiList.get(y)+rssi)/2;
	    			 rssiList.set(y,rssimean);
	    			 f=1;
	    			 break;
	    		 }
	                                             			           
	    	 }
	    	 if(f==0)
	    	 {
	    		 deviceList.add(device.toString());
	    		 rssiList.add(rssi);
	    		 
	    	 }
	    	 count++; 
	     }
	                        	
	 }
	                
	 private  class Scanner extends Thread {
		 private final BluetoothAdapter bluetoothAdapter;
		 private volatile boolean isScanning = false;
		 Scanner(BluetoothAdapter adapter) {
			 bluetoothAdapter = adapter;
		 }
		 public boolean isScanning() {
			 return isScanning;
		 }
		 public void startScanning() {
			 synchronized (this) {
				 isScanning = true;
				 start();
			 }
		 }
		 public void stopScanning() {
			 synchronized (this) {
				 isScanning = false;
				 bluetoothAdapter.stopLeScan(MyService.this);
			 }
		 }

		 @Override
		 public void run() {
			 try {
				 while (true) {
					 synchronized (this) {
						 if (!isScanning)
							 break;
						 displayer();  

	                     		
	                     bluetoothAdapter.startLeScan(MyService.this);
	                    					 }
					 
					 sleep(SCAN_PERIOD);
					 	
					 synchronized (this) {
					      
						 bluetoothAdapter.stopLeScan(MyService.this);
					 }
	                                
				 }
			 } catch (InterruptedException ignore) {
			 } finally {
				 bluetoothAdapter.stopLeScan(MyService.this);
			 }
		 }
		 
		 	
	 }
	                
	 public class JedisTrial {
		 
			 
		 private  final String JEDIS_SERVER = PreferenceManager.getDefaultSharedPreferences(MyService.this).getString("MYIP","192.168.2.8");
		 private void setupPublisher() {
			 if(haveNetworkConnection())
			 {
				 try {
					 System.out.println("Connecting");
					 System.out.println(JEDIS_SERVER);
					 
					 Jedis jedis = new Jedis(JEDIS_SERVER,6379);
					 System.out.println("Waiting to publish");
					 //publishLatch.await();
					 System.out.println("Ready to publish, waiting one sec");
					 //	Thread.sleep(1000);
					 System.out.println("publishing");
					 jedis.publish("ch1",devices.toString());
					 System.out.println("published, closing publishing connection");
					 jedis.quit();
					 System.out.println("publishing connection closed");
				 } catch (Exception e) {
	    	                					
					 System.out.println(">>> OH NOES Pub, " + e.getMessage());
					 e.printStackTrace();
				 }
			 }
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
	 
	        protected void showCurrentLocation() {
	        	Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        	if (location != null) {
	        		loc=location;
			
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






