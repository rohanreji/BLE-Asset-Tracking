package com.maangalabs.assetreciever;


import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import android.widget.Toast;


@SuppressLint("NewApi")
public class MyService extends Service implements BluetoothAdapter.LeScanCallback {

	 JedisPoolConfig poolConfig = new JedisPoolConfig();
    
	 double latit,longit;
	 JSONObject devices = new JSONObject();
            
             
	  
	    private static final long SCAN_PERIOD = 1000;
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
	    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
	    
	        private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
GPSTracker gps;
	    JSONObject jsonstruct = new JSONObject();
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		gps = new GPSTracker(MyService.this);
		 final BluetoothManager bluetoothManager =
	                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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
	}
	 public void displayer()
	    {
	    		
	    	try{
	 	if(gps.canGetLocation())
	        {
	    		
	    		
	 		
	 	showCurrentLocation();


	 		 latit=loc.getLatitude();
	 		  longit=loc.getLongitude();
	    		members=new String();
	    	
	    		
	    	//	members="{lat:"+latit+",long:"+longit;
	    	//	members=members+",members:[";
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
			
			//members=members+"{rssi:"+rssiList.get(u)+",mac:"+deviceList.get(u)+"}";
				
				//Log.e("hai........","csdcsdvfsdv");
				//Toast.makeText(getApplicationContext(), "gps",Toast.LENGTH_SHORT).show();
				}	
		
	    try {
	    	devices.put("lat", latit);
	    	devices.put("long", longit);
	    	devices.put("count",deviceList.size());
			devices.put("members",mem);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    count=0;
	    deviceList.clear();
		rssiList.clear();
			//members=members+"]";
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
	        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
	        super.onDestroy();
	    }
	    // Device scan callback.
	    
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
	                        			// Toast.makeText(getApplicationContext(), device+ " "+deviceList.get(y),Toast.LENGTH_SHORT).show();
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
	                //    private final BluetoothAdapter.LeScanCallback mLeScanCallback;
	            public int counter=0;
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
	                                    if(counter>=4)
	                                    {
	                                    	
	                                    	counter=0;
	                                    		
	                                    	displayer();
	                                    	
	                                    }
	                                    counter++;
	                                   
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
	                	 
	                	
	                 
	                	//private ArrayList<String> messageContainer = new ArrayList<String>();
	                 
	                private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
	                private CountDownLatch publishLatch = new CountDownLatch(1);
	                 
	                
	              
	                 
	                private  final String JEDIS_SERVER = PreferenceManager.getDefaultSharedPreferences(MyService.this).getString("MYIP","192.168.2.8");
	                 
	                	private void setupPublisher() {
	                		
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
	                
	                
	                
          
	                
	                
	                
	                
	                
	                
	                
	                protected void showCurrentLocation() {
	                	
	                	 
	                	
	                	        
;
								Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                	
	                	 
	                	
	                	        if (location != null) {
	                	
	                	      loc=location;
	                	
	                	         
	                	
	                	        
	                	
	                	        }
	                	        
	                	 
	                	
	                	    }  


	                private class MyLocationListener implements LocationListener {
	                	
	                	 
	                	
	                	        public void onLocationChanged(Location location) {
	                	
	                	            String message = String.format(
	                	
	                	                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
	                	
	                	                    location.getLongitude(), location.getLatitude()
	                	                   
	                	            );
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






