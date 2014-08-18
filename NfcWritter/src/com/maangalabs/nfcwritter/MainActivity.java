package com.maangalabs.nfcwritter;

import com.example.nfcwritter.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
			
	}
	public void clicks(View v)
	{
		// TODO Auto-generated method stub
		Intent intent = new Intent(getApplicationContext(),
		com.google.zxing.client.android.CaptureActivity.
		class);
		startActivity(intent);
		}
	
}
