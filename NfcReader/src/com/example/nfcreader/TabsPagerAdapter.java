package com.example.nfcreader;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
public class TabsPagerAdapter extends FragmentStatePagerAdapter {
 
	public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
        	
            return new HomeFragment();
            
        case 1:
            // Games fragment activity
        	//new FavFragment().new LongOperation().execute(" ");
            return new FavFragment();
            
       /* case 2:
            // Movies fragment activity
            return new SearchFragment();*/
       
       
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
     //   return 3;
    	return 2;
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}