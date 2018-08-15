package com.rotor.chappy.fragments.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.R;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Location;
import com.rotor.chappy.model.User;
import com.rotor.core.RFragment;
import com.stringcare.library.SC;
import com.tapadoo.alerter.Alerter;

import net.glxn.qrgen.android.QRCode;

public class ProfileFragment extends RFragment implements Frag, ProfileInterface.View, OnMapReadyCallback {

    public ProfilePresenter presenter;

    private MapView mapView;
    private GoogleMap map;

    private RoundedImageView profile;
    private ImageView qr;
    private TextView name;
    private TextView steps;

    @Nullable
    @Override
    public View onCreateRView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onRViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            profile = view.findViewById(R.id.image);
            name = view.findViewById(R.id.name);
            qr = view.findViewById(R.id.user_qr);
            steps = view.findViewById(R.id.steps);

            mapView = view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        presenter = new ProfilePresenter(this);
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
    public void connected() {
        Alerter.clearCurrent(getActivity());
    }

    @Override
    public void disconnected() {
        Alerter.create(getActivity()).setTitle("Device not connected")
                .setText("Trying to reconnect")
                .enableProgress(true)
                .disableOutsideTouch()
                .enableInfiniteDuration(true)
                .setProgressColorRes(R.color.primary)
                .show();
    }

    @Override
    public FragmentType type() {
        return FragmentType.CHAT;
    }

    @Override
    public String title() {
        return presenter.user().getName();
    }

    public static ProfileFragment instance() {
        return new ProfileFragment();
    }

    @Override
    public void userUpdated() {
        if (presenter.user() != null) {
            ImageLoader.getInstance().displayImage(presenter.user().getPhoto(), profile);
            name.setText(presenter.user().getName());
            steps.setText(String.valueOf(presenter.user().getSteps()));
            Bitmap myBitmap = QRCode.from(SC.encryptString(presenter.user().getUid())).withColor(0xFF000000, 0x00FFFFFF).withSize(350, 350).bitmap();
            qr.setImageBitmap(myBitmap);
            moveToLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setAllGesturesEnabled(false);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(false);
        moveToLocation();
    }

    private void moveToLocation() {
        if (presenter.user() != null && map != null) {
            User user = presenter.user();
            if (user != null && user.getLocations() != null) {
                final Location location = user.getLastLocation();
                if (location == null) return;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));

            }
        }
    }
}
