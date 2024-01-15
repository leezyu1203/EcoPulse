package com.example.ecopulse.Information;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ecopulse.MainActivity;
import com.example.ecopulse.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LocationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mGoogleMap = null;
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
    private ImageButton backButton = null;
    protected View locationFragmentView;
    private static final ArrayList<searchItem> SEARCH = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        locationFragmentView = inflater.inflate(R.layout.fragment_location, container, false);
        title = (TextView) getActivity().findViewById(R.id.current_title);
        db = FirebaseFirestore.getInstance();
        backButton = getActivity().findViewById(R.id.backButton);
        backButton.setVisibility(View.INVISIBLE);


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

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this::onMapReady);

        searchButton = locationFragmentView.findViewById(R.id.search_btn);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search_text = searchET.getText().toString().toLowerCase();
                if (search_text.equals("")) {
                    Toast.makeText(getContext(), "Please enter a search term.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("recycling_center_information").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean searchSuccess = false;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> recyclingCenterData = document.getData();
                                String name = (recyclingCenterData.get("name") + "").toLowerCase();
                                String address = (recyclingCenterData.get("address") + "").toLowerCase();

                                if (name.contains(search_text) || address.contains(search_text)) {
                                    searchSuccess = true;
                                    Double lat = Double.parseDouble(recyclingCenterData.get("lat") + "");
                                    Double lng = Double.parseDouble(recyclingCenterData.get("lng") + "");
                                    LatLng position = new LatLng(lat, lng);
                                    moveCamera(position);
                                    break;
                                }
                            }

                            if (!searchSuccess) {
                                Toast.makeText(getContext(), "No result!", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(getContext(), "Search fail!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return locationFragmentView;
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // Check the permission
        checkLocationPermission();

        // Retrieve recycling center information from FireStore
        db.collection("recycling_center_information").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Geocoder gcd;
                    if (getActivity() != null && getActivity().getBaseContext() != null) {
                        gcd = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            // Add the record to SEARCH list
                            if (SEARCH.size() != task.getResult().size()) {
                                SEARCH.add(new searchItem(document.getData().get("name") + "", document.getData().get("address") + ""));
                            }

                            String fsLat = document.getData().get("lat") + "";
                            String fsLng = document.getData().get("lng") + "";

                            try {
                                LatLng latLng;
                                if (fsLat.equals("")) {
                                    // If Lat is not set, get the address and use Geocoder to
                                    // find the Lat and Lng and update the Lat and Lng in FireStore
                                    String fsAdd = document.getData().get("address") + "";
                                    List<Address> addresses = gcd.getFromLocationName(fsAdd, 1);
                                    double lat = addresses.get(0).getLatitude();
                                    double lng = addresses.get(0).getLongitude();
                                    updateLatLng(document.getId(), lat, lng);
                                    latLng = new LatLng(lat, lng);
                                } else {
                                    // If Lat is set, get the lat and lng from the FireStore
                                    latLng = new LatLng(Double.parseDouble(fsLat), Double.parseDouble(fsLng));
                                }
                                // Add the recycling center marker on the map with its lat and lng
                                addRecyclingCenterMarker(latLng);
                            } catch (IOException e) {
                                Log.e("GET LOCATION ERROR", e.getStackTrace().toString());
                                Toast.makeText(getActivity().getBaseContext(),
                                        "Fail to ge the location's latitude and longitude",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(getActivity().getBaseContext(),
                            "Fail to ge the location's latitude and longitude",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Enable location change listener and marker click listner
        mGoogleMap.setOnMyLocationChangeListener(this);
        mGoogleMap.setOnMarkerClickListener(this);

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
        // Create a length 1 array to store recycling centre ID
        String[] recyclingCenterID = new String[1] ;

        // Get all the textView reference in the dialog
        recycleCenterName = dialog.findViewById(R.id.recycling_center_name);
        recycleCenterAddress = dialog.findViewById(R.id.recycling_center_address);
        recycleCenterContact = dialog.findViewById(R.id.recycling_center_contact_number);
        recycleCenterRecyclingType = dialog.findViewById(R.id.recycling_type);
        recycleCenterOpening = dialog.findViewById(R.id.recycling_center_opening);
        recycleCenterDistance = dialog.findViewById(R.id.distance);

        // Query FireStore to get the record where the latitude and longitude same as the marker.
        Filter equalLat = Filter.equalTo("lat", marker.getPosition().latitude);
        Filter equalLng = Filter.equalTo("lng", marker.getPosition().longitude);
        Filter equalLatAndLng = Filter.and(equalLat, equalLng);

        db.collection("recycling_center_information")
                .where(equalLatAndLng).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().getDocuments().isEmpty()) {
                        DocumentSnapshot recyclingCenterData = task.getResult().getDocuments().get(0);
                        // Set the recycling center id
                        recyclingCenterID[0] = recyclingCenterData.getId();

                        // set the recycling center information to the TextViews in the dialog
                        recycleCenterName.setText(recyclingCenterData.get("name") + "");
                        recycleCenterAddress.setText(recyclingCenterData.get("address") + "");
                        recycleCenterContact.setText(recyclingCenterData.get("contact") + "");
                        recycleCenterRecyclingType.setText(recyclingCenterData.get("type") + "");
                        recycleCenterOpening.setText(recyclingCenterData.get("opening") + "");
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error getting recycling center information", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // If user's location is not null (location permission granted),
        // calculate the distance between user and recycling center and
        // set the the TextView in dialog, else set "No Permission"
        if (myLocation != null) {
            float[] result = new float[1];
            Location.distanceBetween(myLocation.latitude, myLocation.longitude, marker.getPosition().latitude, marker.getPosition().longitude,result);
            recycleCenterDistance.setText(result[0] >= 1000 ? String.format("%.2f", result[0]/1000)  + " km" : String.format("%.2f", result[0]) + " m");
        } else {
            recycleCenterDistance.setText("No Permission");
        }

        // Get the schedule pick up button from dialog
        schedulePickUpButton = dialog.findViewById(R.id.schedule_button);

        // Set onClick listener on the schedule pick up button,
        // go to a new activity with some details (name, address, contact)
        schedulePickUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), SchedulePickUp.class);
                        intent.putExtra("name", recycleCenterName.getText().toString());
                        intent.putExtra("address", recycleCenterAddress.getText().toString());
                        intent.putExtra("contact", recycleCenterContact.getText().toString());
                        intent.putExtra("id", recyclingCenterID[0]);
                        startActivity(intent);
                    }
                }
        );
    }

        public void updateLatLng(String id, Double lat, Double lng) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("recycling_center_information").document(id);
            ref.update("lat", lat);
            ref.update("lng", lng);
        }

}