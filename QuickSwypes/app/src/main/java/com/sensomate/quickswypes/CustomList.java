package com.sensomate.quickswypes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rohan on 15/5/15.
 */
public class CustomList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] name;
    private final Long[] time;
    private final Boolean[] ci;
    private final Boolean[] co;
    public CustomList(Activity context,
                      String[] name, Long[] time,Boolean[] ci,Boolean[] co) {
        super(context, R.layout.custom_view, name);
        this.context = context;
        this.name = name;
        this.time = time;
        this.ci=ci;
        this.co=co;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.custom_view, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.empname);
        TextView txtTime = (TextView) rowView.findViewById(R.id.timeworked);

        ImageView checkinImg = (ImageView) rowView.findViewById(R.id.ck2);
        ImageView checkoutImg = (ImageView) rowView.findViewById(R.id.ck1);
        txtTitle.setText(name[position]);
        Date date = new Date(time[position]);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
        String dateFormatted = formatter.format(date);
        txtTime.setText(dateFormatted);

        if(ci[position]==true)
            checkinImg.setImageResource(R.drawable.ic_launcher);
        else{
            checkinImg.setImageResource(R.drawable.greytick);
        }
        if(co[position]==true)
            checkoutImg.setImageResource(R.drawable.ic_launcher);
        else{
            checkoutImg.setImageResource(R.drawable.greytick);
        }
        return rowView;
    }
}