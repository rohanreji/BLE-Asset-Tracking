package com.example.nfcreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.*;

/*Array Adapter for the favourites Listview
 * 
 */
public class CabAdapter extends ArrayAdapter<Cabs>{

    Context context;
    int layoutResourceId;   
    Cabs data[] = null;
   
    public CabAdapter(Context context, int layoutResourceId, Cabs[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder = null;
       
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new WeatherHolder();
           
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
           
            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }
       
        Cabs weather = data[position];
        holder.txtTitle.setText(weather.title);
       
       
        return row;
    }
   
    static class WeatherHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
    }
}
