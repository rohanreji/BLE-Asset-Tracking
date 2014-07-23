package com.example.nfcreader;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;











import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi") public class MainActivity extends FragmentActivity  {
	private static final String TAG = MainActivity.class.getName();

	protected NfcAdapter nfcAdapter;
	protected PendingIntent nfcPendingIntent;
	String tagid;
	TextView idassetcount;
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	Intent in;
	public void moveTab(View v)
	{
		viewPager.setCurrentItem(1);
		idassetcount.setTextColor(Color.RED);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3498db")));
		viewPager = (ViewPager) findViewById(R.id.pager);
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
	      
	    viewPager.setAdapter(mAdapter);
	    viewPager.setCurrentItem(0);
	    
	    idassetcount=(TextView)findViewById(R.id.idAssetCount);
		
	    
	    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
	   		 
            @Override
            public void onPageSelected(int position) {
            	viewPager.setCurrentItem(position);
            	if(position==0)
            		idassetcount.setTextColor(Color.WHITE);
            	else
            		idassetcount.setTextColor(Color.RED);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
               	  onRefreshListener fragment = (onRefreshListener) mAdapter.instantiateItem(viewPager, arg0);
                  if (fragment != null) {
                    fragment.onRefresh();
                } 
            
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                
            }
        });
	
		// initialize NFC
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		//enableForegroundMode();
	
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
	public void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
	

		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {		
				in=intent;
				new ApiCaller().execute(" ");
			    
			    
			
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
	
	
	private String bytesToHexString(byte[] src) {
	    StringBuilder stringBuilder = new StringBuilder("0x");
	    if (src == null || src.length <= 0) {
	        return null;
	    }

	    char[] buffer = new char[2];
	    for (int i = 0; i < src.length; i++) {
	        buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);  
	        buffer[1] = Character.forDigit(src[i] & 0x0F, 16);  
	        System.out.println(buffer);
	        stringBuilder.append(buffer);
	    }

	    return stringBuilder.toString();
	}
	
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}
	
	
	  public class ApiCaller extends AsyncTask<String, Void, String> {

	        @Override
	        protected String doInBackground(String... params) {
	        	try{
	        	
	        	
	        	}
	        	catch(Exception e)
	        	{
	        		
	        	}
	        	       	
	            return "Executed";
	        }
	       
	        
	        @Override
	        protected void onPostExecute(String result) {
	            // txt.setText(result);
	            // might want to change "executed" for the returned string passed
	            // into onPostExecute() but that is upto you
	        	
	        		
	       		          	
	    		 
	    		 /*
	    		  * set the cab details here 
	    		  * using some for loop
	    		  */
	   	
	        	  if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(in.getAction()))
				    {

				HomeFragment.r.setVisibility(View.VISIBLE);
				HomeFragment.t.setVisibility(View.INVISIBLE);
				Typeface typeface = Typeface.createFromAsset(MainActivity.this.getAssets(), "prfont.ttf");
				TextView idname=(TextView)findViewById(R.id.idName);
				
				idname.setTypeface(typeface);
				idname.setText("PrajiLesh N");
				idname.setVisibility(View.VISIBLE);
				
				
				TextView idassettitle=(TextView)findViewById(R.id.idAssetTitle);
				idassettitle.setTypeface(typeface);
				idassettitle.setVisibility(View.VISIBLE);
				
				idassetcount.setTypeface(typeface);
				idassetcount.setVisibility(View.VISIBLE);
				
		//		TextView idage=(TextView)findViewById(R.id.idAge);
			//	idage.setTypeface(typeface);
				//idage.setText("EY, Kinfra");
				//idage.setVisibility(View.VISIBLE);
				ImageView im=(ImageView)findViewById(R.id.idImage);
				new DownloadImageTask((ImageView) findViewById(R.id.idImage))
	            .execute("https://lh3.googleusercontent.com/-0MGYt_7FE4s/UnC4F5hSK1I/AAAAAAAABJI/a0eZYIl6NKI/w536-h535/6708_614562911891322_1048838883_n.jpg");
				
				im.setVisibility(View.VISIBLE);
				vibrate(); 
			   
			
			   
			    
			    
			    
			    byte[] typ,payload ;
			    NdefMessage ndefMesg;
			/*  
			    Tag myTag = (Tag) in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			    
			    Ndef ndefTag = Ndef.get(myTag);
			    int size = ndefTag.getMaxSize();         // tag size
			    String type = ndefTag.getType();  // tag type
			     ndefMesg = ndefTag.getCachedNdefMessage();*/
			    
			 
			    	 Parcelable[] rawMsgs = in.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			          
			            	
			            
			            	ndefMesg = (NdefMessage) rawMsgs[0];
			               
			    	
			  
			    NdefRecord[] ndefRecords = ndefMesg.getRecords();
			    int len = ndefRecords.length;
			    for (int i = 0; i < len; i++) {
			      typ = ndefRecords[i].getType();
			       payload = ndefRecords[i].getPayload();
			       tagid= getTextData(payload);
			       Toast.makeText(getApplicationContext(), tagid,Toast.LENGTH_SHORT).show();
			       
			    }
		//	    TextView t=(TextView)findViewById(R.id.title);
				
			//	t.setText(tagid);
			    
				//t.setTypeface(typeface);
			    
			    
	    
	    		
	        	  Cabs weather_data[] = new Cabs[]{
	        			  new Cabs("Dell Inspiron"), new Cabs("Motorolla Tablet"), new Cabs("Samsung Galaxy Tab")
	        	  };
	        	
	                     
	         CabAdapter adapter = new CabAdapter(MainActivity.this,
	                              R.layout.listview_item_row, weather_data);
	                     
	                    // adapter.notifyDataSetChanged();
	              
	                     
	         ListView listView1 = (ListView)findViewById(R.id.listView1);
	                      
	                  //  adapter.notifyDataSetChanged();
	         listView1.setAdapter(adapter);
			    }
	        	  
	     		this.cancel(true);
	    		
	        }

	        @Override
	        protected void onPreExecute() {
	        	Cabs weather_data[] = new Cabs[]
	                      {
	                          new Cabs("Loading.."),
	                          
	                      };
	                     
	         CabAdapter adapter = new CabAdapter(MainActivity.this,
	                              R.layout.listview_item_row, weather_data);
	                     
	                    // adapter.notifyDataSetChanged();
	              
	                     
	         ListView listView1 = (ListView)findViewById(R.id.listView1);
	                      
	                  //  adapter.notifyDataSetChanged();
	         listView1.setAdapter(adapter);
	         
	        
	        	
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        	
	        }
	    }
	
	  
	  
	 
}
