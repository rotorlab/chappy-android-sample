package com.rotor.chappy.fragments.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.home.HomeActivity;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.activities.main.MainActivity;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Location;
import com.rotor.chappy.model.User;
import com.rotor.chappy.model.map.PersonItem;
import com.rotor.chappy.model.map.PersonRender;
import com.rotor.core.RFragment;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends RFragment implements Frag, OnMapReadyCallback, MapInterface.View, ClusterManager.OnClusterClickListener<PersonItem>, ClusterManager.OnClusterInfoWindowClickListener<PersonItem>, ClusterManager.OnClusterItemClickListener<PersonItem>, ClusterManager.OnClusterItemInfoWindowClickListener<PersonItem> {

    private MapView mapView;
    private GoogleMap map;
    private MapPresenter presenter;

    private HashMap<String, PersonItem> markers;
    private ClusterManager<PersonItem> mClusterManager;
    private boolean resumed;


    @Nullable
    @Override
    public View onCreateRView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onRViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        presenter = new MapPresenter(this);
        markers = new HashMap<>();
        if (view != null) {
            mapView = view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
        setHasOptionsMenu(true);
    }


    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public FragmentType type() {
        return FragmentType.MAP;
    }

    @Override
    public String title() {
        return "Map";
    }

    public static MapFragment instance() {
        return new MapFragment();
    }

    @Override
    public void onResumeView() {
        mapView.onResume();
        presenter.start();
        resumed = true;
    }

    @Override
    public void onPauseView() {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);

        mClusterManager = new ClusterManager<PersonItem>(getActivity(), map);
        mClusterManager.setRenderer(new PersonRender((AppCompatActivity) getActivity(), map, mClusterManager));
        map.setOnCameraIdleListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
    }

    @Override
    public void updateUI() {
        HashMap<String, User> users = presenter.users();
        for (final Map.Entry<String, User> entry : users.entrySet()) {
            if (!markers.containsKey(entry.getKey())) {
                if (!entry.getValue().getLocations().isEmpty()) {
                    final Location location = entry.getValue().getLocations().entrySet().iterator().next().getValue();
                    final PersonItem item = new PersonItem();
                    item.setId(entry.getValue().getUid());
                    item.setName(entry.getValue().getName());
                    item.setSnippet(entry.getValue().getName());
                    item.setUrl(entry.getValue().getPhoto());
                    item.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    ImageLoader.getInstance().loadImage(item.getUrl(), new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            item.setImage(loadedImage);
                            markers.put(item.getId(), item);
                            mClusterManager.addItem(item);
                            if (item.getId().equals(FirebaseAuth.getInstance().getUid()) && resumed) {
                                resumed = false;
                                goToPosition(location);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }
            } else {
                if (!entry.getValue().getLocations().isEmpty()) {
                    Location location = entry.getValue().getLocations().entrySet().iterator().next().getValue();
                    markers.get(entry.getKey()).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    if (entry.getValue().getUid().equals(FirebaseAuth.getInstance().getUid()) && resumed) {
                        resumed = false;
                        goToPosition(location);
                    }
                }
            }
        }
    }

    @Override
    public boolean onClusterClick(Cluster<PersonItem> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        final LatLngBounds bounds = builder.build();
        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<PersonItem> cluster) {

    }

    @Override
    public boolean onClusterItemClick(PersonItem personItem) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(PersonItem personItem) {

    }

    @Override
    public void goToPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
    }
}
