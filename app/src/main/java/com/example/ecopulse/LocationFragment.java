package com.example.ecopulse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LocationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener{
    private GoogleMap mGoogleMap = null;
    private Marker myLocationMarker = null;
    private TextView recycleCenterName = null;
    private TextView recycleCenterAddress = null;
    private TextView recycleCenterContact = null;
    private TextView recycleCenterRecyclingType = null;
    private TextView recycleCenterDistance = null;

    private TextView recycleCenterOpening = null;
    private AppCompatButton schedulePickUpButton = null;

    private TextView title = null;

    private AppCompatImageButton searchButton = null;
    private AutoCompleteTextView searchET = null;

    private LatLng myLocation = null;
    protected View locationFragmentView;
    private static final ArrayList<searchItem> SEARCH = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        locationFragmentView =  inflater.inflate(R.layout.fragment_location, container, false);
        title = (TextView) getActivity().findViewById(R.id.current_title);


        ArrayAdapter<searchItem> adapter = new SearchAdapter(getContext(), SEARCH);

        searchET = (AutoCompleteTextView) locationFragmentView.findViewById(R.id.search_ET);

        searchET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchET.showDropDown();
            }
        });
        searchET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchItem selectedItem = (searchItem) parent.getItemAtPosition(position);
                if (selectedItem != null) {
                    searchET.setText(selectedItem.getTitle());
                }
            }
        });
        searchET.setAdapter(adapter);


        if (title != null) {
            title.setText("Recycling Center Location");
        }

        final SupportMapFragment mapFragment =(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this::onMapReady);

        searchButton = locationFragmentView.findViewById(R.id.search_btn);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_text = searchET.getText().toString().toLowerCase();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("recycling_center_information").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("SEARCH TAG", task.getResult().getDocuments().toString());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> recyclingCenterData = document.getData();
                                String name = (recyclingCenterData.get("name") + "").toLowerCase();
                                String address = (recyclingCenterData.get("address") + "").toLowerCase();
                                if (name.contains(search_text) || address.contains(search_text)) {
                                    Double lat = Double.parseDouble(recyclingCenterData.get("lat") + "");
                                    Double lng = Double.parseDouble(recyclingCenterData.get("lng") + "");
                                    LatLng position = new LatLng(lat, lng);
                                    moveCamera(position);
                                    break;
                                }
                            }
//                            if (!task.getResult().getDocuments().isEmpty()) {
//                                DocumentSnapshot recyclingCenterData = task.getResult().getDocuments().get(0);
//                               Double lat = Double.parseDouble(recyclingCenterData.get("lat") + "");
//                               Double lng = Double.parseDouble(recyclingCenterData.get("lng") + "");
//                               LatLng position = new LatLng(lat, lng);
//                               moveCamera(position);
//                            }

                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        return locationFragmentView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (mGoogleMap != null) {
            if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(true);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("recycling_center_information").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (SEARCH.isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    SEARCH.add(new searchItem(document.getData().get("name") + "", document.getData().get("address") + ""));
                                    LatLng latLng = new LatLng(Double.parseDouble(document.getData().get("lat") + ""), Double.parseDouble(document.getData().get("lng") + ""));
                                    addRecyclingCenterMarker(latLng);
                                }
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    LatLng latLng = new LatLng(Double.parseDouble(document.getData().get("lat") + ""), Double.parseDouble(document.getData().get("lng") + ""));
                                    addRecyclingCenterMarker(latLng);
                                }
                            }


                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

                mGoogleMap.setOnMyLocationChangeListener(this);
                mGoogleMap.setOnMarkerClickListener(this);
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
        final FrameLayout overlay = new FrameLayout(this.getContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundColor(Color.parseColor("#80000000"));
        this.getActivity().getWindow().addContentView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Dialog dialog = new Dialog(this.getContext());

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
        String[] info = new String[5];
        recycleCenterName = dialog.findViewById(R.id.recycling_center_name);
        recycleCenterAddress = dialog.findViewById(R.id.recycling_center_address);
        recycleCenterContact = dialog.findViewById(R.id.recycling_center_contact_number);
        recycleCenterRecyclingType = dialog.findViewById(R.id.recycling_type);
        recycleCenterOpening = dialog.findViewById(R.id.recycling_center_opening);
        recycleCenterDistance = dialog.findViewById(R.id.distance);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("recycling_center_information").where(Filter.and(Filter.equalTo("lat", marker.getPosition().latitude), Filter.equalTo("lng", marker.getPosition().longitude))).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().getDocuments().isEmpty()) {
                        DocumentSnapshot recyclingCenterData = task.getResult().getDocuments().get(0);
                        info[0] = recyclingCenterData.get("name") + "";
                        info[1] = recyclingCenterData.get("address") + "";
                        info[2] = recyclingCenterData.get("contact") + "";
                        info[3] = recyclingCenterData.get("type") + "";
                        info[4] = recyclingCenterData.get("opening") + "";
                        recycleCenterName.setText(recyclingCenterData.get("name") + "");
                        recycleCenterAddress.setText(recyclingCenterData.get("address") + "");
                        recycleCenterContact.setText(recyclingCenterData.get("contact") + "");
                        recycleCenterRecyclingType.setText(recyclingCenterData.get("type") + "");
                        recycleCenterOpening.setText(recyclingCenterData.get("opening") + "");
                    }

                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });


            if (myLocation != null) {
                float[] result = new float[1];
                Location.distanceBetween(myLocation.latitude, myLocation.longitude, marker.getPosition().latitude, marker.getPosition().longitude,result);

                recycleCenterDistance.setText(result[0] >= 1000 ? String.format("%.2f", result[0]/1000)  + " km" : String.format("%.2f", result[0]) + " m");
            }

            schedulePickUpButton = dialog.findViewById(R.id.schedule_button);


            schedulePickUpButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), SchedulePickUp.class);
                            Log.d("Log out info", info[0]);
                            intent.putExtra("name", info[0]);
                            intent.putExtra("address", info[1]);
                            intent.putExtra("contact", info[2]);
                            startActivity(intent);
                        }
                    }
            );
        }


}