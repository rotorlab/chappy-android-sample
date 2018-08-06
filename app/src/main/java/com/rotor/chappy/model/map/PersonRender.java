package com.rotor.chappy.model.map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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

public class PersonRender extends DefaultClusterRenderer<PersonItem> {

    private AppCompatActivity activity;
    private final IconGenerator mIconGenerator = new IconGenerator(App.context());
    private final IconGenerator mClusterIconGenerator = new IconGenerator(App.context());
    private final RoundedImageView mImageView;
    private final RoundedImageView mClusterImageView;
    private final int mDimension;

    public PersonRender(AppCompatActivity activity, GoogleMap map, ClusterManager<PersonItem> clusterManager) {
        super(activity, map, clusterManager);
        this.activity = activity;

        View multiProfile = activity.getLayoutInflater().inflate(R.layout.multi_profile, null);
        mClusterImageView = (RoundedImageView) multiProfile.findViewById(R.id.image);
        mClusterIconGenerator.setContentView(multiProfile);

        View profile = activity.getLayoutInflater().inflate(R.layout.person_profile, null);
        mImageView = (RoundedImageView) profile.findViewById(R.id.image);
        mIconGenerator.setContentView(profile);

        // mImageView = new RoundedImageView(activity);
        mDimension = (int) activity.getResources().getDimension(R.dimen.custom_profile_image);
        //mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        //int padding = (int) activity.getResources().getDimension(R.dimen.custom_profile_padding);
        //mImageView.setPadding(padding, padding, padding, padding);
        //mIconGenerator.setContentView(mImageView);
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
        final List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
        final int width = mDimension;
        final int height = mDimension;

        for (final PersonItem p : cluster.getItems()) {
            BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), p.getImage());
            drawable.setBounds(0, 0, width, height);
            profilePhotos.add(drawable);

            if (profilePhotos.size() == 4 || cluster.getSize() == profilePhotos.size()) {
                clusterRendered(cluster, profilePhotos, height, width, markerOptions);
                break;
            }
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<PersonItem> cluster) {
        return cluster.getSize() > 1;
    }

    private void clusterRendered(Cluster<PersonItem> cluster, List<Drawable> images, int height, int width, MarkerOptions markerOptions) {
        MultiDrawable multiDrawable = new MultiDrawable(images);
        multiDrawable.setBounds(0, 0, width, height);

        mClusterImageView.setImageDrawable(multiDrawable);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

}
