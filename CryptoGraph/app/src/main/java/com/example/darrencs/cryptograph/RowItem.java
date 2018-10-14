package com.example.darrencs.cryptograph;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Comparator;

/**
 * Created by DarrenCS on 04/03/18.
 */

public class RowItem extends RecyclerView.ViewHolder {
    private String imageId;
    private String title;
    private String name;
    private Boolean checked;
    private int adapterPostion;

    public int getAdapterPostion() {
        return adapterPostion;
    }

    public void setAdapterPostion(int adapterPostion) {
        this.adapterPostion = adapterPostion;
    }
    //    public RowItem(String image, String title, String name) {
//        //super(view);
//        this.imageId = image;
//        this.title = title;
//        this.name = name;
//        this.checked = false;
//    }

    public RowItem(View itemView) {
        super(itemView);
    }

//    public RowItem(View itemView) {
//        super(itemView);
//    }

    public static class Comparators {
        public static final Comparator<RowItem> title = Comparator.comparing(o -> o.title.toLowerCase());
        public static final Comparator<RowItem> name = Comparator.comparing(o -> o.name.toLowerCase());
    }

    public Boolean getChecked() {
        return checked;
    }

    public String getImageId() {
        return imageId;
    }

    public String getCoinTitle() {
        return title;
    }

    public String getCoinName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoinTitle(String title) {
        this.title = title;
    }

    public void setImageId(String imageId) {

        this.imageId = imageId;
    }

    public void setChecked() {
        checked = true;
    }

    public void clearChecked() {
        checked = false;
    }
}
