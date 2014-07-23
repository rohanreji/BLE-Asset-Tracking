package com.example.nfcreader;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi") public class HomeFragment extends Fragment implements onRefreshListener {
	static RelativeLayout r;
	static TextView t;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		 	
		 
	
		 	
			  //Toast.makeText(getActivity(), "gugi",Toast.LENGTH_SHORT).show();
			  
		 	View v = inflater.inflate(R.layout.home_layout, container, false);
		  			
			  //Fragment fr=(Fragment)v.findViewById(R.id.map)			      
		  	
		 	 r=(RelativeLayout)v.findViewById(R.id.relativeLayout1);
		 	 t=(TextView)v.findViewById(R.id.notdetect);
	
		 	return v;
		}
	
		@Override
		public void onResume() {
			//mapView.onResume();
			super.onResume();
		}
		@Override
		public void onDestroy() {
			super.onDestroy();
			//	mapView.onDestroy();
		}
		@Override
		public void onLowMemory() {
			super.onLowMemory();
			//	mapView.onLowMemory();
		}
		@Override
		public void onRefresh() {
		// TODO Auto-generated method stub
		
		}

}
