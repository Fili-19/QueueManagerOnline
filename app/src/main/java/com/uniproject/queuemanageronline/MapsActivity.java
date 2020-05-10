package com.uniproject.queuemanageronline;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener,OnMapReadyCallback {

    private final String TAG = "MapsActivity";
    private final int DEFAULT_ZOOM = 15;
    private String loggedUser = null;
    private String eventId = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        String[] data = getIntent().getStringArrayExtra(getString(R.string.LOGIN_EVENT_ID_NAME));
        if (data != null) {
            eventId = data[0];
            loggedUser = data[1];
        }
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // search for all event present on the database and add markers to the map
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map event = document.getData();
                                Log.d(TAG, document.getId() + " => " + event);
                                GeoPoint location = (GeoPoint) event.get("location");
                                String name = (String) event.get("name");
                                LatLng eventLocation ;
                                if (location != null) {
                                    eventLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(eventLocation).title(name));
                                    mMarker.setTag(document.getId());
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        // set the map's camera position to the current location of the device
        getDeviceLocation();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    // default behaviour, move map's camera to current location
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    // only for debug
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(),
                                            location.getLongitude()), DEFAULT_ZOOM));
                        }
                        else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
        /*FusedLocationProviderClient mFusedLocationProviderClient = new FusedLocationProviderClient(this);
        try {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location mLastKnownLocation =(Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }*/
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // get eventId from the marker being tapped
        String markerEventId = (String) marker.getTag();
        // if eventId is not null it means that the user is already on a queue so he cannot queue up again
        if (markerEventId != null && eventId == null) {
            // moving to eventActivity, this activity shows event information
            Intent i = new Intent(getString(R.string.EVENT_ACTIVITY));
            i.putExtra(getString(R.string.LOGIN_EVENT_ID_NAME), new String[]{markerEventId, loggedUser});
            startActivity(i);
        }
        else {
            Toast.makeText(MapsActivity.this, "You are already on a queue", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
