package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NearbyRecyclingCenter extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener{
    private GoogleMap mGoogleMap = null;
    private Marker myLocationMarker = null;
    private TextView recycleCenterName = null;
    private TextView recycleCenterAddress = null;
    private TextView recycleCenterContact = null;
    private TextView recycleCenterRecyclingType = null;
    private TextView recycleCenterDistance = null;
    private AppCompatButton schedulePickUpButton = null;



    private LatLng myLocation = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby_recycling_center_layout);


        final SupportMapFragment mapFragment =(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Enable the "My Location" layer
        if (mGoogleMap != null) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(true);

                // Add markers here
                LatLng latLng = new LatLng(3.129503, 101.649423);
                addRecyclingCenterMarker(latLng);

                mGoogleMap.setOnMyLocationChangeListener(this);
                mGoogleMap.setOnMarkerClickListener(this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void addRecyclingCenterMarker(LatLng position) {
        if (mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(position));
        }
    }

    private void moveCamera(LatLng position) {
        if (mGoogleMap != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            myLocation = latLng;
            moveCamera(latLng);
            mGoogleMap.setOnMyLocationChangeListener(null);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showDialog(marker);
        return true;
    }

    private void showDialog(Marker marker) {
        final FrameLayout overlay = new FrameLayout(this);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(Color.parseColor("#80000000"));
        getWindow().addContentView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_drawer_layout);
        setRecyclingCenterDetails(dialog, marker);


        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            overlay.setVisibility(View.GONE);
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.drawerAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void setRecyclingCenterDetails(Dialog dialog, Marker marker) {
        String name = "RECYCLE ABC";
        String address = "BLK 99-01-01, Taman Ungku Tun Aminah, 81300 Skudai, Johor";
        String contact = "012-34567890";
        String type = "Plastic, Paper, Aluminium, Electronics Materials";
        recycleCenterName = dialog.findViewById(R.id.recycling_center_name);
        recycleCenterName.setText(name);
        recycleCenterAddress = dialog.findViewById(R.id.recycling_center_address);
        recycleCenterAddress.setText(address);
        recycleCenterContact = dialog.findViewById(R.id.recycling_center_contact_number);
        recycleCenterContact.setText(contact);
        recycleCenterRecyclingType = dialog.findViewById(R.id.recycling_type);
        recycleCenterRecyclingType.setText(type);
        recycleCenterDistance = dialog.findViewById(R.id.distance);

        if (myLocation != null) {
            float[] result = new float[1];
            Location.distanceBetween(myLocation.latitude, myLocation.longitude, marker.getPosition().latitude, marker.getPosition().longitude,result);
            recycleCenterDistance.setText(result[0] + "m");
        }

        schedulePickUpButton = dialog.findViewById(R.id.schedule_button);


        schedulePickUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(NearbyRecyclingCenter.this, SchedulePickUp.class);
                        intent.putExtra("name", name);
                        intent.putExtra("address", address);
                        intent.putExtra("contact", contact);
                        startActivity(intent);
                    }
                }
        );
    }
}
