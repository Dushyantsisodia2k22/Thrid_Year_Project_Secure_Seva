package com.example.gla_final_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private GoogleMap mMap;
    private Marker busMarker;
    private DatabaseReference busDataRef;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        busDataRef = FirebaseDatabase.getInstance().getReference("Bus_Data");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            retrieveCoordinates();

            // Adjust the zoom level
            mMap.moveCamera(CameraUpdateFactory.zoomTo(19.2f)); // Change the zoom level as desired
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void retrieveCoordinates() {
        String selectedMainItem = getIntent().getStringExtra("selectedMainItem");
        String selectedNestedItem = getIntent().getStringExtra("selectedNestedItem");

        busDataRef.child(selectedMainItem).child(selectedNestedItem).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double latitude = dataSnapshot.child("lat").getValue(Double.class);
                Double longitude = dataSnapshot.child("long").getValue(Double.class);

                if (latitude != null && longitude != null) {
                    LatLng busLatLng = new LatLng(latitude, longitude);

                    if (busMarker == null) {
                        // Add a new marker if it doesn't exist
                        busMarker = mMap.addMarker(new MarkerOptions().position(busLatLng).title("Bus Marker"));
                    } else {
                        // Animate the marker to the new position
                        smoothAnimateMarker(busMarker, busLatLng);
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(busLatLng));
                } else {
                    // Coordinates not found, handle accordingly
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void smoothAnimateMarker(final Marker marker, final LatLng toPosition) {
        final LatLng startPosition = marker.getPosition();
        final LatLng endPosition = toPosition;
        final long duration = 1000; // Animation duration in milliseconds
        final Interpolator interpolator = new LinearInterpolator();
        final Handler handler = new Handler();

        final long startTime = System.currentTimeMillis();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * endPosition.longitude + (1 - t) * startPosition.longitude;
                double lat = t * endPosition.latitude + (1 - t) * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1) {
                    handler.postDelayed(this, 16); // 60 frames per second
                }
            }
        };

        handler.postDelayed(runnable, 16);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update the marker position with the user's current location
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (busMarker != null) {
            busMarker.setPosition(userLatLng);
        } else {
            busMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Bus Marker"));
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLatLng));
    }

    // Implement other LocationListener methods (onProviderEnabled, onProviderDisabled, onStatusChanged)
    // ...

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    retrieveCoordinates();
                }
            }
        }
    }
}
