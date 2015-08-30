package com.sensomate.quickswypes;

import android.app.ActionBar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;

import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import redis.clients.jedis.Jedis;


/*
the preferences:
PROJECT_CODE : locationid
PROJECT_NAME : location name

SITE_CODES : projectid
SITE_NAMES : projectname

SITE_CODE : worksiteid
SITE_NAME : worksite name

TOKEN
MY_IP
USERNAME
FIRST_USE
 */

public class MainActivity extends ActionBarActivity implements
        android.support.v7.app.ActionBar.TabListener {
    //the write flag
    public static int writeflag;

    //the read tag value
    static String tag_value="";
    static String name_value="";

    //the string obtained on login-api call
    String loginString;

    //first use call
    int first_use;

    //the progress dialog box
    ProgressDialog pDialog;

    //IP address of the project.
    String IP="http://almoayyed.sensomate.com";
    String nam,nam2;
    MapId m;
    int del=0;

    ArrayList<HashMap<String, String>> contactList, contactList1,contactList2;
    ArrayList<String> Loclist;
    ArrayList<String> emplist;
    ArrayList<String> idlist;
    ArrayList<String> Sitelist,Sitelist2;

    //variables for the write fragments listview
    private ListView lv1;
    private ListView lv1_att;
    private EditText ed;
    private String lv_arr[];
    private String lv_arr_att[];
    private Long time_arr_att[];
    private Boolean ci_arr_att[];
    private Boolean co_arr_att[];
    ArrayList<String> emplist_write;
    ArrayList<String> idlist_write;
    ArrayList<String> emplist_write_att;
    ArrayList<Long> time_write_att;
    ArrayList<Boolean> ci_write_att;
    ArrayList<Boolean> co_write_att;
    ArrayList<String> arr_sort = new ArrayList<String>();
    public static String emplid;
    public static String empname;
    public static String emplid_att;
    public static String empname_att;
    SetList sl;
    SetAttendance al;
    AttendanceDetails ad;

    JSONArray contacts = null,contacts1 = null,contacts2=null;
    int Locposition,Siteposition,Siteposition2;
    String prnam;

    NfcAdapter adapter;
    PendingIntent pendingIntent;
    Tag mytag;
    Context ctx;
    IntentFilter writeTagFilters[];
    boolean writeMode;

    //variables for tabpager
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.support.v7.app.ActionBar actionBar;

    // Tab titles
    private String[] tabs = { "Home", "Attendance", "Write" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.registerReceiver(this.mConnReceiver,
//                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        writeflag=0;
        if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("TOKEN", "ZERO").equals("ZERO")) {
            setContentView(R.layout.login_page);

        }
        else {
            setContentView(R.layout.activity_main);

            addTabs();
        }

    }

    /*
    1.this is the function for creating NDEF message in the correct format.
     */
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
    }

    /*
    1.This is the function for writting the NDEF message to tag.
     */
    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    /*
    1.fuction called when activity is already running
     */
    @Override
    protected void onNewIntent(Intent intent){
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            Toast.makeText(this, "Detected Tag: " + mytag.toString(), Toast.LENGTH_LONG ).show();

            /*
            1.this part is for writting to tag
             */
            if(writeflag==1) {
                try {
                    if (mytag == null) {
                        Toast.makeText(ctx, "Try scanning the Tag again, and press Write!", Toast.LENGTH_SHORT).show();

                    } else {
                        if(emplid.equals(""))
                            Toast.makeText(ctx, "Please select an employee that you want to write", Toast.LENGTH_SHORT).show();
                        else {
                            write(emplid, mytag);
                            Toast.makeText(ctx, "Tag write successfull!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(ctx, "Please scan the Tag properly!" , Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(ctx, "Please scan the Tag properly!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            /*
            1.This part is for reading from tag
             */
            if(writeflag==0){
                Toast.makeText(ctx,"read",Toast.LENGTH_SHORT).show();
                new NdefReaderTask().execute(mytag);
            }
        }
    }


    /*
    1.function called on clicking write button
     */
    public void write(View v)
    {
        MainActivity.writeflag=1;
    }

    /*
     1.called on login button press
      */
    public void login(View v) {

        new LoginTask().execute();

    }

    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    /*
  * called when options is clicked
  */
    public void ddemo() {
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.settings_view);
        // Set dialog title
        dialog.setTitle("Options");

        // set values for custom dialog components - text, image and button
        dialog.show();

        Button declineButton = (Button) dialog.findViewById(R.id.button1);
        // if decline button is clicked, close the custom dialog
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                final EditText e = new EditText(MainActivity.this);

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        MainActivity.this);
                alert.setTitle("Set Server");
                alert.setView(e);
                e.setText(PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this)
                        .getString("MYIP",
                                IP));
                alert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                PreferenceManager
                                        .getDefaultSharedPreferences(
                                                MainActivity.this)
                                        .edit()
                                        .putString("MYIP",
                                                e.getText().toString())
                                        .commit();
                                dialog.cancel();
                                if (!PreferenceManager
                                        .getDefaultSharedPreferences(
                                                MainActivity.this)
                                        .getString("TOKEN", "ZERO")
                                        .equals("ZERO")) {
                                    firstusecall();
                                }
                            }
                        });
                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Canceled.
                            }
                        });
                alert.show();

            }
        });

        Button declineButton1 = (Button) dialog.findViewById(R.id.button2);
        declineButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                firstusecall();
            }
        });
        Button declineButton2 = (Button) dialog.findViewById(R.id.button3);
        declineButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                firstusecall2();
            }
        });
        Button declineButton3 = (Button) dialog.findViewById(R.id.button4);
        declineButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                firstusecall3();
            }
        });
    }
    /*
    1. The class for reading NdefTag
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;


            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

                tag_value=result;
                DatabaseHandler db = new DatabaseHandler(MainActivity.this);
                AttendanceDbHandler adb = new AttendanceDbHandler(MainActivity.this);
                SwypedDbHandler sdb  = new SwypedDbHandler(MainActivity.this);

                List<EmpDetails> dummy_list = db.getAllContacts();
                List<AttendanceDetails> adb_dummy_list= adb.getAllContacts();
                AttendanceDetails dummy_attendance;
                int adb_found_flag=0;
                int dummy_count=0;
                String dummy_site_name="";
                int dummy_proj_code=0;
                boolean dummy_checkin=false;
                boolean dummy_checkout=false;

                Long times = System.currentTimeMillis();
                int siteid = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.this).getInt("SITE_CODE", 0);
                String sitename = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.this).getString("SITE_NAME", "");
                AttendanceDetails swyped_attendance = new AttendanceDetails(tag_value, times, siteid, sitename, name_value, 0, true, false,0);
                sdb.addContact(swyped_attendance);

                for(EmpDetails e:dummy_list)
                {

                    if(tag_value.equals(e.getID())){
                        name_value=e.getName();
                        dummy_site_name=e.getSiteName();
                        dummy_proj_code=e.getProjectCode();

                        break;
                    }

                }

                for(AttendanceDetails a:adb_dummy_list)
                {
                    if((a.getID()).equals(tag_value)){

                        adb_found_flag=1;
                        ad=a;

                        if(a.getCheckedOut()==true){
                            /*
                            1. code to do on checking in
                             */
                            dummy_count=0;
                            dummy_checkin=true;
                            dummy_checkout=false;

                        }
                        else if(a.getCheckedOut()==false){
                            /*
                            1. code to do on checking out
                             */
                            dummy_count=1;
                            dummy_checkin=true;
                            dummy_checkout=true;

                        }

                        Long time = System.currentTimeMillis();
                        Long work_time=time-a.getCapturedAt();

                        dummy_attendance =new AttendanceDetails(a.getID(),a.getCapturedAt(),a.getProjCode(),a.getSiteName(),a.getName(),dummy_count,dummy_checkin,dummy_checkout,work_time);

                        adb.updateContact(dummy_attendance);

                        break;

                    }
                }
                if(adb_found_flag==0)
                {

                    if(dummy_proj_code!=0) {
                        Long time = System.currentTimeMillis();
                        dummy_attendance = new AttendanceDetails(tag_value, time, dummy_proj_code, dummy_site_name, name_value, 0, true, false,0);
                        adb.addContact(dummy_attendance);

                    }
                }
                adb.close();
                db.close();
                
                HomeFragment fragment=(HomeFragment)mAdapter.getItem(0);
                if(((HomeFragment) fragment).tag!=null)
                    ((HomeFragment) fragment).tag.setText("Read content: " + name_value);

                viewPager.setCurrentItem(0);
                //call the profile setter here.
                if(isNetworkAvailable())
                new JedisThread().execute();
                else{
                    Toast.makeText(getApplicationContext(),"no network",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /*
    1.called to check login.
     */
    private class LoginTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();
            try {

                JSONObject obj;


                obj = new JSONObject(loginString);

                if (result.equals("sorry")) {

                    Toast.makeText(getApplicationContext(),
                            obj.getString("status"),Toast.LENGTH_SHORT).show();

                } else {


                    PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this)
                            .edit().putString("TOKEN", obj.getString("token"))
                            .commit();
                    PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this)
                            .edit()
                            .putString(
                                    "USERNAME",
                                    obj.getJSONObject("user").getString(
                                            "username")).commit();


                    setContentView(R.layout.activity_main);
                    addTabs();
                    first_use = PreferenceManager.getDefaultSharedPreferences(
                            MainActivity.this).getInt("FIRST_USE", 0);
                    if (first_use == 0) {
                        firstusecall();
                    }

                }
            } catch (Exception e) {

            }


        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if((m!=null)&&(!m.isCancelled()))

                m.cancel(true);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                JSONObject location = new JSONObject();
                EditText uname = (EditText) findViewById(R.id.editText1);
                EditText pass = (EditText) findViewById(R.id.editText2);
                location.put("username", uname.getText().toString());
                location.put("password", pass.getText().toString());
                Log.e("uname",uname.getText().toString());
                HttpClient httpclient = new DefaultHttpClient();
                String JEDIS_SERVER1 = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this)
                        .getString("MYIP",
                                IP);
                JEDIS_SERVER1 = JEDIS_SERVER1 + "/auth/login";
                URL url = new URL(JEDIS_SERVER1);
                HttpPost httpPost = new HttpPost(JEDIS_SERVER1);
                String json = "";
                json = location.toString();
                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                httpPost.setEntity(se);

                httpPost.setHeader("User-Agent", "NFC-DETECTOR/1.0");
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity entity = response.getEntity();

                loginString = EntityUtils.toString(entity);
                Log.d("rear", loginString);
                if (loginString.equals("Not Found")) {
                    return "sorry";
                } else
                    return "true";
            } catch (Exception e) {
                Log.e("ERROR IN LOGIN", e.getMessage());
                return "sorry";
            }

        }
    }

    /*
    To map device to worksite.
     */
    private class MapId extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            sync_emp();

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Mapping Device...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                if(isCancelled()){
                    cancel(true);
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                }
                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                JSONObject location = new JSONObject();

                location.put("deviceid", mngr.getDeviceId());
                int siteid = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.this).getInt("SITE_CODE", 1);
                location.put("projcode", siteid);


                String JEDIS_SERVER1 = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this)
                        .getString("MYIP",
                                IP);
                JEDIS_SERVER1 = JEDIS_SERVER1 + "/api/v1/device/addmap";

                URL url = new URL(JEDIS_SERVER1);

                HttpParams httpParameters = new BasicHttpParams();

                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

                int timeoutSocket = 5000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);


                HttpPost httpPost = new HttpPost(JEDIS_SERVER1);
                String json = "";
                json = location.toString();
                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                httpPost.setEntity(se);

                httpPost.setHeader("x-access-token",PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("TOKEN", "NULL"));
                httpPost.setHeader("x-key",PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("USERNAME", "NULL"));
                DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpResponse response = httpclient.execute(httpPost);
                HttpEntity entity = response.getEntity();

                String jsonString = EntityUtils.toString(entity);
                Log.d("rear", jsonString);
                if (jsonString.equals("Not Found")) {
                    return "sorry";
                } else
                    return "true";
            } catch (Exception e) {
                // Log.e("ERROR IN SEVER UPLOAD", e.getMessage());
                return "sorry";
            }

        }
    }

    /*
    1.Get Location
     */
    private class GetLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if((m!=null)&&(!m.isCancelled()))
                m.cancel(true);
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            contactList = new ArrayList<HashMap<String, String>>();
            Loclist = new ArrayList<String>();

            String url = PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getString("MYIP",
                    IP);
            url = url
                    + "/api/v1/location";

            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,getApplicationContext());
            Log.d("URL: ", "> " +url.toString());

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {

                    // Getting JSON Array node
                    contacts = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("id");
                        String loc = c.getString("name");
                        // worksites[i]=c.getJSONArray("Worksites");

                        System.out.println("name:  " + loc);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("loc", loc);

                        // adding contact to contact list
                        contactList.add(contact);
                        Loclist.add(loc);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

            final Spinner s = new Spinner(MainActivity.this);
            s.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, Loclist));

            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    // TODO Auto-generated method stub
                    // Locate the textviews in activity_main.xml
                    Locposition = Integer.parseInt(contactList.get(position)
                            .get("id"));
                    prnam = contactList.get(position).get("loc");

                    System.out.println("id is:" + Locposition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    MainActivity.this);
            alert.setTitle("Select Location");
            alert.setView(s);
            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("FIRST_USE", 1).commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("PROJECT_CODE", Locposition)
                                    .commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putString("PROJECT_NAME", prnam)
                                    .commit();

                            dialog.cancel();
                            firstusecall2();
                        }
                    });
            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            // Canceled.
                        }
                    });
            alert.show();

        }

    }

    /*
    employee sync
     */
    public void sync_emp() {
        /**
         * CRUD Operations
         * */
        // Inserting Contacts
        Log.d("Insert: ", "Inserting ..");
        new Populator().execute();
    }
    /*
    Populating the database
     */
    private class Populator extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if((m!=null)&&(!m.isCancelled()))
                m.cancel(true);
            // Showing progress dialog

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Database Populating..");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            emplist = new ArrayList<String>();
            idlist = new ArrayList<String>();
            ServiceHandler sh = new ServiceHandler();
            String url = PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getString("MYIP",
                    IP);
            Log.e("projectcode",PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("SITE_CODES", 0)+"");
            url = url
                    + "/api/v1/project/getallemployees/"+PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("SITE_CODES", 0);

            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,MainActivity.this.getApplicationContext());
            Log.e("projcode",PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("PROJECT_CODE", 0)+"");

            Log.d("Response: ", "> " + jsonStr);


            if (jsonStr != null) {

                try {

                    // Getting JSON Array node
                    JSONArray emp = new JSONArray(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < emp.length(); i++) {
                        JSONObject c = emp.getJSONObject(i);
                        JSONObject employ=c.getJSONObject("Employee");
                        emplist.add(employ.getString("name"));
                        JSONObject trade=employ.getJSONObject("Trade");
                        idlist.add((employ.getString("empid")));

                        // worksites[i]=c.getJSONArray("Worksites");


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("hello","hmm");
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int projectcode= PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("SITE_CODES", 0);
            String sitename= PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getString("SITE_NAME", null);
            DatabaseHandler db = new DatabaseHandler(MainActivity.this);
            db.onUpgrade(db.getReadableDatabase(), 1, 1);
            for(int i=0;i<emplist.size();i++) {
                db.addContact(new EmpDetails(idlist.get(i),emplist.get(i), sitename, projectcode, "http://www.littleblackdressgroup.com.au/wp-content/uploads/2012/11/MA-Profile-Photo.jpg"));

            }
            Toast.makeText(getApplicationContext(), "synced", Toast.LENGTH_SHORT)
                    .show();
            db.close();
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }


    /*
    Get the worksites.
     */
    private class GetWorkSite extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            // Showing progress dialog
            if((m!=null)&&(!m.isCancelled()))
                m.cancel(true);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            // for an older version
            contactList1 = new ArrayList<HashMap<String, String>>();
            Sitelist = new ArrayList<String>();

            ServiceHandler sh = new ServiceHandler();
            String url = PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getString("MYIP",
                    IP);
            url = url
                    + "/api/v1/location/getallprojects/"
                    + PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("PROJECT_CODE", 0);

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,getApplicationContext());
            Log.d("URL: ", "> " +url.toString());
            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {


                    // Getting JSON Array node

                    contacts1 = new JSONArray(jsonStr);
                    // looping through All Contacts
                    for (int i = 0; i < contacts1.length(); i++) {
                        JSONObject c = contacts1.getJSONObject(i);

                        String id = c.getString("id");
                        String loc = c.getString("name");
                        System.out.println("name:  " + loc);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put("location", id);
                        contact.put("code", loc);

                        // adding contact to contact list
                        contactList1.add(contact);
                        Sitelist.add(loc);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            final Spinner s = new Spinner(MainActivity.this);
            s.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, Sitelist));

            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    // TODO Auto-generated method stub
                    // Locate the textviews in activity_main.xml
                    Siteposition = Integer.parseInt(contactList1.get(position)
                            .get("location"));
                    nam = contactList1.get(position).get("code");
                    System.out.println("site code in shared: "
                            + PreferenceManager.getDefaultSharedPreferences(
                            MainActivity.this).getInt("SITE_CODE", 0));
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    MainActivity.this);
            alert.setTitle("Select Project");
            alert.setView(s);
            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("FIRST_USE", 1).commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putString("SITE_NAMES", nam).commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("SITE_CODES", Siteposition).commit();
                            dialog.cancel();
                            firstusecall3();
                            // sync_emp();

                        }
                    });
            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            // Canceled.
                        }
                    });
            alert.show();

        }

    }


    private class GetWorkSiteOrig extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if((m!=null)&&(!m.isCancelled()))
                m.cancel(true);
            // Showing progress dialog

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance

            // for an older version
            contactList2 = new ArrayList<HashMap<String, String>>();


            Sitelist2 = new ArrayList<String>();
            ServiceHandler sh = new ServiceHandler();
            String url = PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getString("MYIP",
                    IP);
            url = url
                    + "/api/v1/project/getallworksites/"

                    + PreferenceManager.getDefaultSharedPreferences(
                    MainActivity.this).getInt("SITE_CODES", 0);


            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,getApplicationContext());

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {

                    // Getting JSON Array node

                    contacts2 = new JSONArray(jsonStr);
                    // looping through All Contacts
                    for (int i = 0; i < contacts2.length(); i++) {
                        JSONObject c = contacts2.getJSONObject(i);

                        String id = c.getString("id");
                        String loc = c.getString("name");
                        System.out.println("name:  " + loc);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put("location", id);
                        contact.put("code", loc);

                        // adding contact to contact list
                        contactList2.add(contact);
                        Sitelist2.add(loc);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            final Spinner s = new Spinner(MainActivity.this);
            s.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, Sitelist2));

            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    // TODO Auto-generated method stub
                    // Locate the textviews in activity_main.xml
                    Siteposition2 = Integer.parseInt(contactList2.get(position)
                            .get("location"));
                    nam2 = contactList2.get(position).get("code");
                    System.out.println("site code in shared: "
                            + PreferenceManager.getDefaultSharedPreferences(
                            MainActivity.this).getInt("SITE_CODE", 0));
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    MainActivity.this);
            alert.setTitle("Select Worksite");
            alert.setView(s);
            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("FIRST_USE", 1).commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putString("SITE_NAME", nam2).commit();
                            PreferenceManager
                                    .getDefaultSharedPreferences(
                                            MainActivity.this).edit()
                                    .putInt("SITE_CODE", Siteposition2).commit();
                            m=new MapId();
                            m.execute();

                            //setting timeout thread for async task

                            dialog.cancel();



                        }
                    });
            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            // Canceled.
                        }
                    });
            alert.show();

        }

    }

    /*
    The write fragments listview populator
         */
    class SetList extends AsyncTask<Void, Void, Void> {

        String employ;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("fedf", "csdc");

            lv_arr = emplist_write.toArray(new String[emplist_write.size()]);

            lv1 = (ListView) findViewById(R.id.ListView01);
            ed = (EditText) findViewById(R.id.editText);
// By using setAdpater method in listview we an add string array in list.
            lv1.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, lv_arr));
            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    emplid = idlist_write.get(position);
                    empname=emplist_write.get(position);
                    Log.d("employee", emplist_write.get(position));
                    ed.setText(emplist_write.get(position));
                    ed.setSelection(ed.getText().length());
                    Toast.makeText(getApplicationContext(), "scan nfc now to write " + emplist_write.get(position), Toast.LENGTH_SHORT).show();
                }
            });


            ed.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {

                    int textlength = ed.getText().length();
                    arr_sort.clear();
                    for (int i = 0; i < lv_arr.length; i++) {
                        if (textlength <= lv_arr[i].length()) {
                            if (ed.getText().toString().equalsIgnoreCase((String) lv_arr[i].subSequence(0, textlength))) {
                                arr_sort.add(lv_arr[i]);
                            }
                        }
                    }

                    lv1.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arr_sort));

                }
            });


        }


        @Override
        protected Void doInBackground(Void... voids) {
            //create api call here for employee list
             DatabaseHandler db = new DatabaseHandler(MainActivity.this);

            List<EmpDetails> contacts = db.getAllContacts();
            for(EmpDetails e:contacts)
            {
                emplist_write.add(e.getName());
                idlist_write.add(e.getID());

            }
            db.close();


            return null;
        }
    }

    /*
    fill the list view of attendance fragment
     */
    class SetAttendance extends AsyncTask<Void, Void, Void> {

        String employ;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("fedf", "csdc");

            lv_arr_att = emplist_write_att.toArray(new String[emplist_write_att.size()]);
            time_arr_att = time_write_att.toArray(new Long[time_write_att.size()]);
            ci_arr_att=ci_write_att.toArray(new Boolean[ci_write_att.size()]);
            co_arr_att=co_write_att.toArray(new Boolean[co_write_att.size()]);

            lv1_att = (ListView) findViewById(R.id.List);
            if(emplist_write_att!=null) {

                CustomList adapter = new
                        CustomList(MainActivity.this,  lv_arr_att,time_arr_att,ci_arr_att,co_arr_att);
                lv1_att.setAdapter(adapter);
            }

        }


        @Override
        protected Void doInBackground(Void... voids) {
            //create api call here for employee list
            AttendanceDbHandler db = new AttendanceDbHandler(MainActivity.this);

            List<AttendanceDetails> contacts = db.getAllContacts();
            for(AttendanceDetails e:contacts)
            {
                emplist_write_att.add(e.getName());
                time_write_att.add(e.getTime());
                ci_write_att.add(e.getCheckedIn());
                co_write_att.add(e.getCheckedOut());

            }
            db.close();


            return null;
        }
    }


    /*
    * called on first use
    */
    public void firstusecall() {
        new GetLocation().execute();

    }

    public void firstusecall2() {

        new GetWorkSite().execute();

    }

    public void firstusecall3() {

        new GetWorkSiteOrig().execute();

    }
    public void addTabs(){
        /*
        1.steps for adding the tabs to action bar
         */
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        // actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        //listening to tab changes
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);

                if(position==2)
                {
                    MainActivity.writeflag=1;
                    emplist_write = new ArrayList<String>();
                    idlist_write = new ArrayList<String>();
                    emplid = new String();
                    empname = new String();

                    sl = new SetList();
                    sl.execute();
                }

                if(position==1){
                    emplist_write_att = new ArrayList<String>();
                    time_write_att = new ArrayList<Long>();
                    ci_write_att= new ArrayList<Boolean>();
                    co_write_att = new ArrayList<Boolean>();
                    emplid_att = new String();
                    empname_att = new String();

                    al = new SetAttendance();
                    al.execute();

                }
                if (position != 1) {
                    if(al!=null)
                        al.cancel(true);

                }

                if (position != 2) {
                    if(sl!=null)
                    sl.cancel(true);
                    MainActivity.writeflag = 0;
                }
                if (position == 0) {
                    HomeFragment fragment = (HomeFragment) mAdapter.getItem(0);
                    ((HomeFragment) fragment).tag.setText("Read content: " + name_value);

                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        ctx = this;
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent().getAction())) {

            mytag = this.getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            Toast.makeText(this, "Detected Tag: " + mytag.toString(), Toast.LENGTH_LONG ).show();

            /*
            1.this part is for writting to tag
             */
            if(writeflag==1) {
                try {
                    if (mytag == null) {
                        Toast.makeText(ctx, "Try scanning the Tag again, and press Write!", Toast.LENGTH_SHORT).show();

                    } else {
                        if(emplid.equals(""))
                            Toast.makeText(ctx, "Please select an employee that you want to write", Toast.LENGTH_SHORT).show();
                        else {
                            write(emplid, mytag);
                            Toast.makeText(ctx, "Tag write successfull!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(ctx, "Please scan the Tag properly!" , Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(ctx, "Please scan the Tag properly!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            /*
            1.This part is for reading from tag
             */
            if(writeflag==0){
                Toast.makeText(ctx,"read",Toast.LENGTH_SHORT).show();
                new NdefReaderTask().execute(mytag);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.logout) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit().putString("TOKEN", "ZERO").commit();

            setContentView(R.layout.login_page);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();
        if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("TOKEN", "ZERO").equals("ZERO")) {
            WriteModeOff();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if (!PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("TOKEN", "ZERO").equals("ZERO")) {
            WriteModeOn();
        }
    }

    private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }

    /*
    Broadcast for checking network
     */
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if(currentNetworkInfo.isConnected()){
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                startService(new Intent(MainActivity.this, SendingService.class));

            }else{
                Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
                stopService(new Intent(MainActivity.this, SendingService.class));
            }
        }
    };

    class JedisThread extends AsyncTask<Void, Void, Void> {

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            if (del == 0) {
                Toast.makeText(getApplicationContext(),
                        "Cannot send scan again!", Toast.LENGTH_SHORT)
                        .show();
            } else
                Toast.makeText(getApplicationContext(), "Delivered",
                        Toast.LENGTH_SHORT).show();


        }

        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            JedisTrial j = new JedisTrial();

            j.setupPublisher();

            return null;
        }

    }

    public class JedisTrial {
        // private ArrayList<String> messageContainer = new ArrayList<String>();
        private CountDownLatch messageReceivedLatch = new CountDownLatch(1);
        private CountDownLatch publishLatch = new CountDownLatch(1);
        int l = (PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this)
                .getString("MYIP", IP).toString().length());
        // no http://

        String JEDIS_SERVER = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this)
                .getString("MYIP", IP).toString()
                .subSequence(7, l).toString();

        private void setupPublisher() {
            try {
                if(JEDIS_SERVER.contains(":")){
                    String s[]=new String[2];
                    s=JEDIS_SERVER.split(":");
                    JEDIS_SERVER=s[0];
                    //  Toast.makeText(getApplicationContext(),JEDIS_SERVER,Toast.LENGTH_SHORT).show();
                    Log.e("server redis",JEDIS_SERVER);
                }

                System.out.println("Connecting");
                System.out.println(JEDIS_SERVER);
                Jedis jedis = new Jedis(JEDIS_SERVER, 6379);

                jedis.auth("sensomate_123#");

                TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                JSONObject location = new JSONObject();

                location.put("uid", tag_value);
                location.put("capturedAt", System.currentTimeMillis());
                location.put("deviceid", mngr.getDeviceId());
                int siteid = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.this).getInt("SITE_CODE", 0);
                Log.e("worksiteid:", siteid + " ");
                location.put("projcode", siteid);
                location.put("batlevel", getBatteryLevel());


                String json = "";
                json = location.toString();
                System.out.println("Waiting to publish");
                // publishLatch.await();
                System.out.println("Ready to publish, waiting one sec");
                // Thread.sleep(1000);
                System.out.println("publishing");

                // jsonstring here...
                jedis.publish("sensomate_channel", json);
                jedis.auth("sensomate_123#");
                System.out.println("published, closing publishing connection");
                jedis.quit();
                System.out.println("publishing connection closed");
                del = 1;

            } catch (Exception e) {
                System.out.println(">>> OH NOES Pub, " + e.getMessage());
                del = 0;
                e.printStackTrace();
            }
        }
        public float getBatteryLevel() {
            Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            // Error checking that probably isn't needed but I added just in case.
            if(level == -1 || scale == -1) {
                return 50.0f;
            }

            return ((float)level / (float)scale) * 100.0f;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
