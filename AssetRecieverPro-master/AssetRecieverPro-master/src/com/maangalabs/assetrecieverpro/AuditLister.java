package com.maangalabs.assetrecieverpro;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("NewApi")
public class AuditLister extends ListActivity   {
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter bluetoothAdapter;
	public BleDevicesAdapter leDeviceListAdapter;
	private BleDevicesScanner scanner;
	TextView t;
	LongOperation l;
	public static List<String> myList = new ArrayList<String>();
	List<String> deviceName = new ArrayList<String>();
	List<Integer> myList1 = new ArrayList<Integer>();
    String deviceList[] ;
	int rssiList[];
    int count=0;
	int rssimean;
	ListView lv;
	/*
	 * Clearing both the list when the backbutton is pressed
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	public void onBackPressed()
	{
		scanner.stop();
		l.cancel(true);
		finish();
		myList.clear();
		deviceName.clear();
		myList1.clear();
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
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		deviceList = new String[5];
		rssiList = new int[5];
		final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	    bluetoothAdapter = bluetoothManager.getAdapter();
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.audit_lister);
    	ActionBar bar = getActionBar();
		//for color
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1569C7")));
		TextView tx1 = (TextView) findViewById(R.id.openpdf);
		Typeface font = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
		tx1.setTypeface(font);
		TextView tx2 = (TextView) findViewById(R.id.exportpdf);
		Typeface font1 = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
		tx2.setTypeface(font1);
		TextView tx3 = (TextView) findViewById(R.id.rescan);
		Typeface font2 = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
		tx3.setTypeface(font2);
		l=new LongOperation();
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		t=(TextView)findViewById(R.id.textView1);
		t.setText("assets found : "+myList.size());
	    lv=(ListView) findViewById(android.R.id.list);
	    /*
	     * scans the bles nearby with a time period of 1000ms
	     */
		scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
	            @Override
	            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
	            	//Toast.makeText(getApplicationContext(), count+" t", Toast.LENGTH_SHORT).show();
	            	
	            	if(count==0)
	           	 	{ 
	           		 myList.add(device.toString());
	           		 myList1.add(rssi);
	           		 deviceName.add(device.toString());
	           		 l.execute("");
	           		 count++;
	           	 	}
	           	 	else
	           	 	{
	           		 int y;
	           		 int f=0;
	           		/*
	           		 * finding the mean of rssi
	           		 */
	           		 for( y=0;y<myList.size();y++)
	           		 {
	           			 if((myList.get(y)).equals(device.toString()))
	           			 {
	           				 rssimean=(myList1.get(y)+rssi)/2;
	           				 myList1.set(y,rssimean);
	           				 f=1;
	           				 break;
	           			 }
	   	                                             			           
	           	   }
	           		 /*
	           		  * adding the new device to the list item
	           		  */
	           	   if(f==0)
	           	   {
	           			 myList.add(device.toString());
	           			 deviceName.add(device.toString());
	           			 myList1.add(rssi);
	           			 new LongOperation().execute("");
	           	   }
	   	    	 }
	   	        /*
	   	         * converting the lists to string
	   	         */
	           	String[] arr = deviceName.toArray(new String[deviceName.size()]); 	 
	           	Integer[] arr1 = myList1.toArray(new Integer[myList1.size()]);
	           	t.setText("assets found : "+myList.size());
	           	if(myList.size()>=1)
	           	{
	           		
	           		TextView t1=(TextView)findViewById(R.id.textView2);
	           		t1.setVisibility(View.INVISIBLE);
	           		
	           	}
	           /*setting the listviews up
	            * 
	            */
	            lv.setAdapter(new CustomAdapter(AuditLister.this, arr,arr1));
	           
	           
	            View v=lv.getAdapter().getView(0, null, null);
	            TextView tv;
	            tv=(TextView)v.findViewById(R.id.textView1);
	        //  Toast.makeText(getApplicationContext(), tv.getText(), Toast.LENGTH_SHORT).show();
	           }
	           
	           
	            
	    });
	    scanner.setScanPeriod(1000);
	    scanner.start();
       //	Toast.makeText(getApplicationContext(), count+" t", Toast.LENGTH_SHORT).show();
	}
	/*
	 * code to rescan
	 */
	public void rescan(View v)
	{
		myList.clear();
		myList1.clear();
		deviceName.clear();
		Toast.makeText(getApplicationContext(), "rescanning", Toast.LENGTH_SHORT).show();
	}
	/*
	 * code for opening the directory
	 */
	public void open(View v)
	{
		    String path1 = Environment.getExternalStorageDirectory().getPath() + "/ASSETRECEIVER/pdf";
		    Uri startDir = Uri.fromFile(new File(path1));
		    Uri uri = Uri.parse(path1);
		    Toast.makeText(getApplicationContext(), uri.toString(), Toast.LENGTH_SHORT).show();
		    
		    Intent intent = new Intent();
		    intent.setData(uri);
		    intent.setType("application/pdf");
		    intent.setAction(Intent.ACTION_VIEW);
		    startActivity(Intent.createChooser(intent, "Open Folder")); 
	}
	/*
	 * code for creating a pdf out of known listview
	 */
	public void export(View v)
    {
		if(myList.size()==0)
		{
			Toast.makeText(getApplicationContext(), "No assets found to export", Toast.LENGTH_SHORT).show();
		}
		else
		{
		Document doc = new Document(PageSize.A4, 10f, 10f, 10f, 10f);
		 try {
			   String path = Environment.getExternalStorageDirectory().getPath() + "/ASSETRECEIVER/pdf";
			   File dir = new File(path);
			   if(!dir.exists())
			    dir.mkdirs();

			   SimpleDateFormat s = new SimpleDateFormat("dd\\MM\\yyyy hh:mm:ss");
			   SimpleDateFormat s1=new SimpleDateFormat("dd_MM_yyyy_hh_mm");
			   Log.d("PDFCreator", "PDF Path: " + path);
			   File file = new File(dir, s1.format(new Date())+"_assetlist"+".pdf");
			  
			   FileOutputStream fOut = new FileOutputStream(file);

			   PdfWriter.getInstance(doc, fOut);
			  
			   //open the document
			   doc.open();
			  
			   Paragraph p2 = new Paragraph(new Phrase(20f,"\n\nTHE ASSET LIST "+s.format(new Date()),
	                   FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30f)));

				  /* You can also SET FONT and SIZE like this */
				  
				   
				 
				 
				   p2.setAlignment(Paragraph.ALIGN_CENTER);
				   doc.add(p2);
			   for(int i=0;i<deviceName.size();i++)
			   {
				   float fntSize, lineSpacing;
				    fntSize = 20.7f;
				    lineSpacing = 20f;
				    int j=i+1;
				    Paragraph p = new Paragraph(new Phrase(lineSpacing,"\n\n   "+j+". "+deviceName.get(i)+"     rssi: "+(128+(myList1.get(i)))*100/98+"%",
				                   FontFactory.getFont(FontFactory.COURIER, fntSize)));
				   
				    
				    doc.add(p);
			   }
			  

			   
			 
			   

			   Toast.makeText(getApplicationContext(), "Created...", Toast.LENGTH_LONG).show();
		 }
		 catch (DocumentException de) {
			   Log.e("PDFCreator", "DocumentException:" + de);
			  } catch (IOException e) {
			   Log.e("PDFCreator", "ioException:" + e);
			  } 
			  finally
			  {
			   doc.close();
			  }
		}
    }
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == android.R.id.home) { //app icon in action bar clicked; go back
	    	scanner.stop();
			
			l.cancel(true);
			finish();
			myList.clear();
			deviceName.clear();
			myList1.clear();
	        return true;
	    }

	    return super.onOptionsItemSelected(item);
	}
	
	
	private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
        	try{
        	for (int i = 0; i <myList.size() ; i++) {
        		
                if(deviceName.get(i).equals("20:CD:39:81:C5:50"))
                {
                Log.e("ert","sdf "+myList.size());
                deviceName.remove(i);
            	deviceName.add(i,"Dell Inspiron");
                	
                	
                }
               
        	}
        	}
        	
        	catch(Exception e)
        	{
        		
        	}
        	/*checker
        	 * *
        	 */
        	if(haveNetworkConnection())
        	{
        		
            	StringBuilder builder = new StringBuilder();
        	    HttpClient client = new DefaultHttpClient();
        	    HttpGet httpGet = new HttpGet("http://foodies.uphero.com/get_all_products.php");
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
        	    } catch (IOException e) {
        	      e.printStackTrace();
        	    } 
            /*checker
             * 
             */
        	    Log.d("checker",builder.toString());
        	}
            return "Executed";
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
	 
}
