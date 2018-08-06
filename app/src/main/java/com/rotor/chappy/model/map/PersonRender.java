package com.rotor.chappy.model.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
        final List<Bitmap> profilePhotos = new ArrayList<Bitmap>(Math.min(4, cluster.getSize()));
        final int width = mDimension;
        final int height = mDimension;

        for (final PersonItem p : cluster.getItems()) {
            profilePhotos.add(p.getImage());

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

    private void clusterRendered(Cluster<PersonItem> cluster, List<Bitmap> images, int height, int width, MarkerOptions markerOptions) {
        Bitmap aux = null;
        for (int i = 0; i < images.size(); i++) {
            if (aux == null) {
                aux = images.get(i);
            } else {
                aux = overlay(images.get(i), aux, height, i);
            }
        }

        mClusterImageView.setImageBitmap(aux);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, int height, int index) {
        int med = bmp1.getWidth();
        Bitmap bmOverlay = Bitmap.createBitmap(med * (index + 1), height, bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, med * (index - 1), 0, null);
        return bmOverlay;
    }

}
