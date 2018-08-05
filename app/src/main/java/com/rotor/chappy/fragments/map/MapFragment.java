package com.rotor.chappy.fragments.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.rotor.chappy.R;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.core.RFragment;

public class MapFragment extends RFragment implements Frag, OnMapReadyCallback, MapInterface.View {

    private MapView mapView;
    private GoogleMap map;
    private MapPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new MapPresenter(this);
        if (view != null) {
            mapView = view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
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
    }

    @Override
    public void updateUI() {

    }
}
