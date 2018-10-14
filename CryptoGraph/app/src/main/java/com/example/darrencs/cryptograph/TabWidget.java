package com.example.darrencs.cryptograph;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

//imports

public class TabWidget extends TabActivity {


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    TabHost tabHost;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabHost = getTabHost();  // The activity TabHost
        android.widget.TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab


        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, CoinList.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("List");
        spec.setContent(intent);
        spec.setIndicator("Coin List");
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, FavouriteCoinsActivity.class);

        spec = tabHost.newTabSpec("Favourites");
        spec.setContent(intent);
        spec.setIndicator("Favourite Coins");

        tabHost.addTab(spec);


        intent = new Intent().setClass(this, Graph.class);
        spec = tabHost.newTabSpec("Graph");
        spec.setContent(intent);
        spec.setIndicator("Graph");
        tabHost.addTab(spec);

        //tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 40;
        tabHost.setCurrentTab(0);

        // ask for permissions

        if ((checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) != (PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }


    public void onSettingsClick(View view) {
        Log.d("Settings", "Button Pressed");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
