package com.rotor.chappy.model.map;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rotor.chappy.App;
import com.rotor.chappy.R;

import java.util.ArrayList;
import java.util.List;

public class PersonRender extends RClusterRender<PersonItem> {

    private AppCompatActivity activity;
    private final IconGenerator mIconGenerator = new IconGenerator(App.context());
    private final IconGenerator mClusterIconGenerator = new IconGenerator(App.context());
    private final RoundedImageView mImageView;
    private final TextView clusterCounter;

    public PersonRender(AppCompatActivity activity, GoogleMap map, ClusterManager<PersonItem> clusterManager) {
        super(activity, map, clusterManager);
        this.activity = activity;

        View multiProfile = activity.getLayoutInflater().inflate(R.layout.multi_profile, null);
        clusterCounter = multiProfile.findViewById(R.id.contacts);
        mClusterIconGenerator.setContentView(multiProfile);

        View profile = activity.getLayoutInflater().inflate(R.layout.person_profile, null);
        mImageView = (RoundedImageView) profile.findViewById(R.id.image);
        mIconGenerator.setContentView(profile);
        mIconGenerator.setBackground(null);
    }

    @Override
    protected void onBeforeClusterItemRendered(final PersonItem item, final MarkerOptions markerOptions) {
        if (item.getImage()!= null) {
            mImageView.setImageBitmap(item.getImage());
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getName());
        }
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onBeforeClusterRendered(final Cluster<PersonItem> cluster, final MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (PersonItem item : cluster.getItems()) {
            if (i >= 3 || cluster.getItems().size() - 1 == i) {
                builder.append(" y ");
                builder.append(item.getName());
                break;
            } else {
                builder.append(item.getName());
            }
            i++;
        }
        clusterCounter.setText(builder.toString());
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<PersonItem> cluster) {
        return cluster.getSize() > 4;
    }

}
