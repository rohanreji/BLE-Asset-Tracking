package com.maangalabs.assetrecieverpro;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAdapter extends BaseAdapter { 
    String [] result;
    Context context;
    Integer [] imageId;
    private static LayoutInflater inflater=null;
    public CustomAdapter(AuditLister mainActivity, String[] prgmNameList, Integer[] prgmImages) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }
 
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
 
    public class Holder
    {
        TextView tv;
        ProgressBar img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
    	 
    	Holder holder=new Holder();
        View rowView;        
        rowView = inflater.inflate(R.layout.list_item, null);
        if(imageId[position]!=0)
        {
             holder.tv=(TextView) rowView.findViewById(R.id.textView1);
             holder.img=(ProgressBar) rowView.findViewById(R.id.progressBar1); 
             holder.tv.setText(result[position]);
             holder.img.setProgress((128+(imageId[position]))*100/98);
             Log.d("rssival",((imageId[position]))+" ");
             Log.d("rssi",(128+(imageId[position]))*100/98+" ");
        }
        rowView.setOnClickListener(new OnClickListener() {         
        @Override
        public void onClick(View v) {
                     // TODO Auto-generated method stub
        	Intent i=new Intent(v.getContext(),AssetDetail.class);
        	i.putExtra("uuid", AuditLister.myList.get(position));
        	v.getContext().startActivity(i);
        	
        }
        });  
        return rowView;
    }
   
}