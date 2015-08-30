package com.sensomate.quickswypes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by rohan on 13/5/15.
 */
public class HomeFragment extends Fragment {

    //the tag to display text
    public static TextView tag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.home_layout, container, false);
        tag=(TextView)rootView.findViewById(R.id.tag);
        /*
        1.to write to tag on starting.
         */
        tag.setText("Read content: " + MainActivity.tag_value);

        return rootView;
    }
}
