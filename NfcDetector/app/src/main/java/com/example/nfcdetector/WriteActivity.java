package com.example.nfcdetector;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rohan on 4/2/15.
 */
//change nfc_tech_filter  for mifare tags
public class WriteActivity extends Activity {

    public static Tag tagid;
    int app_mode;
    String IP="http://192.168.2.14:3000";
    public static String emplid;
    public static String empname;
    private ListView lv1;
    private EditText ed;
    private String lv_arr[];
    protected NfcAdapter nfcAdapter;
    protected PendingIntent nfcPendingIntent;
    ArrayList<String> emplist;
    ArrayList<String> idlist;
    ArrayList<String> arr_sort = new ArrayList<String>();
    ImageDownloader im;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.write_activity);
        im = new ImageDownloader();
        im.execute();
        app_mode = 0;
        emplist = new ArrayList<String>();
        idlist = new ArrayList<String>();
        emplid = new String();
        empname = new String();


        Typeface font = Typeface.createFromAsset(getAssets(), "demo.otf");
        TextView t = (TextView) findViewById(R.id.textView);
        TextView t1=(TextView) findViewById(R.id.textView5);
        t.setTypeface(font);
        t1.setTypeface(font);
        if(!MainActivity.last_emp_name.equals("")) {
            t1.setText("*written "+MainActivity.last_emp_name+" to tag!");
        }
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color
                .parseColor("#34495e")));
        bar.setTitle(Html
                .fromHtml("<font color='#ecf0f1'>Almoayyed - write</font>"));
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
                this, this.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


			/*
             * perform on background
			 */
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(this.getIntent()
                .getAction())) {

            Tag tagFromIntent = this.getIntent().getParcelableExtra(
                    NfcAdapter.EXTRA_TAG);

            tagid = tagFromIntent;
            Toast.makeText(getApplicationContext(), "fwef", Toast.LENGTH_LONG).show();


            // UploadASyncTask upload = new UploadASyncTask();
            // upload.execute();
            // above two line or


        }


    }

    public void onBackPressed() {
        im.cancel(true);

        MainActivity.app_mode = 0;

        finish();
    }

    @Override
    public void onNewIntent(Intent intent) {


        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {


            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            tagid = tagFromIntent;

            // UploadASyncTask upload = new UploadASyncTask();
            // upload.execute();

            // above two line or


            if (emplid == null) {
                Toast.makeText(getApplicationContext(), "Select an employee", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    if (tagid == null) {
                        Toast.makeText(getApplicationContext(), "error detected", Toast.LENGTH_LONG).show();
                    } else {
                        write(emplid, tagid);
                        Toast.makeText(getApplicationContext(), "ok writting", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();

                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(getApplicationContext(), "error writting", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }


        } else {
            Toast.makeText(getApplicationContext(), "No Tag Discovered",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void enableForegroundMode() {


        IntentFilter tagDetected = new
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED); // filter for all
        IntentFilter[] writeTagFilters = new IntentFilter[]{tagDetected};
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent,
                writeTagFilters, null);
    }

    public void disableForegroundMode() {


        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {


        super.onResume();
        // commented for testing
        enableForegroundMode();
    }

    @Override
    protected void onPause() {

        super.onPause();
        // commented for testing
        disableForegroundMode();
    }


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

    private void write(String text, Tag tag) throws IOException, FormatException {
        try {
            MifareClassic ultralight = MifareClassic.get(tag);

//        NdefRecord[] records = {createRecord(text)};
//        NdefMessage message = new NdefMessage(records);
//        Ndef ndef = Ndef.get(tag);
            ultralight.connect();
            ultralight.writeBlock(1, text.getBytes(Charset.forName("US-ASCII")));
            ultralight.close();
        }
        catch(Exception e){

        }
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,
                data));
    }


    class ImageDownloader extends AsyncTask<Void, Void, Void> {

        String employ;

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("fedf", "csdc");
//            Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
//
//            // Spinner adapter
//            mySpinner
//                    .setAdapter(new ArrayAdapter<String>(WriteActivity.this,
//                            android.R.layout.simple_spinner_dropdown_item,
//                            emplist));
//
//            mySpinner
//                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//                        @Override
//                        public void onItemSelected(AdapterView<?> arg0,
//                                                   View arg1, int position, long arg3) {
//                            // TODO Auto-generated method stub
//
//                                emplid=idlist.get(position);
//                                Log.d("employee",emplist.get(position));
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> arg0) {
//                            // TODO Auto-generated method stub
//                        }
//                    });


            lv_arr = emplist.toArray(new String[emplist.size()]);


            lv1 = (ListView) findViewById(R.id.ListView01);
            ed = (EditText) findViewById(R.id.editText);
// By using setAdpater method in listview we an add string array in list.
            lv1.setAdapter(new ArrayAdapter<String>(WriteActivity.this, android.R.layout.simple_list_item_1, lv_arr));
            lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    emplid = idlist.get(position);
                    empname=emplist.get(position);
                    Log.d("employee", emplist.get(position));
                    ed.setText(emplist.get(position));
                    ed.setSelection(ed.getText().length());
                    Toast.makeText(getApplicationContext(), "scan nfc now to write " + emplist.get(position), Toast.LENGTH_SHORT).show();
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

                    lv1.setAdapter(new ArrayAdapter<String>(WriteActivity.this, android.R.layout.simple_list_item_1, arr_sort));

                }
            });


        }


        @Override
        protected Void doInBackground(Void... voids) {
            //create api call here for employee list







            DatabaseHandler db = new DatabaseHandler(WriteActivity.this);

            List<EmpDetails> contacts = db.getAllContacts();
            for(EmpDetails e:contacts)
            {
                emplist.add(e.getName());
                idlist.add(e.getID());

            }
            db.close();












//
//            ServiceHandler sh = new ServiceHandler();
//            String url = PreferenceManager.getDefaultSharedPreferences(
//                    WriteActivity.this).getString("MYIP",
//                    IP);
//            url = url
//                    + "/api/v1/project/getallemployees/"+PreferenceManager.getDefaultSharedPreferences(
//                    WriteActivity.this).getInt("PROJECT_CODE", 0);
//
////            url += "?";
////            List<NameValuePair> params = new LinkedList<NameValuePair>();
////
////            params.add(new BasicNameValuePair("x-access-token", PreferenceManager
////                    .getDefaultSharedPreferences(WriteActivity.this).getString(
////                            "TOKEN", "NULL")));
////            params.add(new BasicNameValuePair("x-key", PreferenceManager
////                    .getDefaultSharedPreferences(WriteActivity.this).getString(
////                            "USERNAME", "NULL")));
////            String paramString = URLEncodedUtils.format(params, "utf-8");
////
////            url += paramString;
//
//            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,WriteActivity.this.getApplicationContext());
//
//            Log.d("Response: ", "> " + jsonStr);
//
//
//            if (jsonStr != null) {
//                try {
//                    // JSONObject jsonObj = new JSONObject(jsonStr);
//
//                    // Getting JSON Array node
//                    JSONArray emp = new JSONArray(jsonStr);
//                    // worksites=new JSONArray[contacts.length()];
//                    // looping through All Contacts
//                    for (int i = 0; i < emp.length(); i++) {
//                        JSONObject c = emp.getJSONObject(i);
//                        JSONObject employ=c.getJSONObject("Employee");
//                        emplist.add(employ.getString("name"));
//                        idlist.add(Integer.toString(employ.getInt("empid")));
//                        // worksites[i]=c.getJSONArray("Worksites");
//
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.e("ServiceHandler", "Couldn't get any data from the url");
//            }



            return null;
        }
    }

}
