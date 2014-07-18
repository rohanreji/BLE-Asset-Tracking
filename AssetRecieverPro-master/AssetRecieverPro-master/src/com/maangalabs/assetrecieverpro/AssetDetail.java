package com.maangalabs.assetrecieverpro;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AssetDetail extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asset_detail);
		ActionBar bar = getActionBar();
		//for color
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1569C7")));
		/*
		 * makes the title bar clickable
		 */
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView t=(TextView)findViewById(R.id.uuid);
		t.setText(getIntent().getExtras().getString("uuid"));
		
	}
	/*
	 * what to do on clicking the titlebar
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
			
	public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == android.R.id.home) { //app icon in action bar clicked; go back
	        finish();
	        return true;
	    }

	    return super.onOptionsItemSelected(item);
	}

}
