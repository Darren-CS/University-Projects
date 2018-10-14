package com.example.darrencs.cryptograph;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class FavouriteCoinsActivity extends AppCompatActivity {

    List<RowItem> favouriteRowItems;
    SharedPreferences sortedPref;
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    Context context;
    SingletonClass instance = SingletonClass.getInstance();

    CustomListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_coin_list);
        if (instance.getFavouriteRowItems() != null) {
            Log.d("items", String.valueOf(instance.getFavouriteRowItems()));
        }
        favouriteRowItems = instance.getFavouriteRowItems();
        layoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        context = getApplicationContext();
        sortedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (favouriteRowItems != null) {
            adapter = new CustomListViewAdapter(context, favouriteRowItems, false);

            Boolean checkSort = sortedPref.getBoolean("sort_settings", false);

            if (!checkSort) {
                Collections.sort(favouriteRowItems, RowItem.Comparators.title);
                Log.d("SharedPref Sort", "false");
            } else {
                Collections.sort(favouriteRowItems, RowItem.Comparators.name);
                Log.d("SharedPref Sort", "true");
            }
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(adapter);


    }

    public void onResume() {
        super.onResume();
        instance.setFavouriteCoinListActivity();
        recyclerView.refreshDrawableState();
        if (instance.getFavouriteRowItems() != null && instance.getFavouriteRowItems().size() > 0) {
            Log.d("items", String.valueOf(instance.getFavouriteRowItems()));

            favouriteRowItems = instance.getFavouriteRowItems();
            adapter = new CustomListViewAdapter(context, favouriteRowItems);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            sortedPref = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean checkSort = sortedPref.getBoolean("change_sort_by", false);
            if (favouriteRowItems != null) {
                if (!checkSort) {
                    Collections.sort(favouriteRowItems, RowItem.Comparators.title);
                    Log.d("SharedPref Sort", "false");
                } else {
                    Collections.sort(favouriteRowItems, RowItem.Comparators.name);
                    Log.d("SharedPref Sort", "true");
                }
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }
        } else {
            recyclerView.setAdapter(adapter);
            Toast.makeText(this, "No Favourite Coins Selected", Toast.LENGTH_LONG).show();
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

}
