package com.example.darrencs.cryptograph;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class SingletonClass {

    private static volatile SingletonClass sSoleInstance;
    private List<RowItem> favouriteRowItems = null;
    private Boolean favouriteCoinListActivity;
    //private constructor.
    private MotionEvent event;

    private SingletonClass() {
        event = null;
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    // favourite coin list activity bool is used to determine if the coinlist activity or the favourite coin activity is active
    // these activities use the same adapter so this allows for the adapter to adjust the items accordingly

    public void setFavouriteCoinListActivity() {
        this.favouriteCoinListActivity = true;
    }

    public void clearFavouriteCoinListActivity() {
        favouriteCoinListActivity = false;
    }

    public Boolean getFavouriteCoinListActivity() {
        return favouriteCoinListActivity;
    }

    public void setFavouiteRowItems(List<RowItem> rowItems) {
        this.favouriteRowItems = rowItems;
    }

    public List<RowItem> getFavouriteRowItems() {
        return this.favouriteRowItems;
    }

    public void appendFavouriteRowItems(RowItem rowItem) {
        if (this.getFavouriteRowItems() == null) {
            List<RowItem> singleList = new ArrayList<RowItem>();
            singleList.add(rowItem);
            this.favouriteRowItems = singleList;
        } else {
            this.favouriteRowItems.add(rowItem);
        }
    }

    public void removeFavouriteRowItems(RowItem rowItem) {
        this.favouriteRowItems.remove(rowItem);
    }

    public static SingletonClass getInstance() {
        //Double check locking pattern
        if (sSoleInstance == null) { //Check for the first time

            synchronized (SingletonClass.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (sSoleInstance == null) sSoleInstance = new SingletonClass();
            }
        }

        return sSoleInstance;
    }
}