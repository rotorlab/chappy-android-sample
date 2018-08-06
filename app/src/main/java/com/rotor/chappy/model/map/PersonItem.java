package com.rotor.chappy.model.map;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PersonItem implements ClusterItem {

    private String id;
    private String name;
    private String snippet;
    private String url;
    private LatLng position;
    private Bitmap image;

    public PersonItem() {
        // nothing to do here
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
