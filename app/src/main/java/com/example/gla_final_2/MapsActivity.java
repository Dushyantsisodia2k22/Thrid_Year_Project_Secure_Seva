package com.example.gla_final_2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.animation.Interpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.animation.LinearInterpolator;




public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Marker busMarker;
    private Marker userMarker;
    private FirebaseDatabase firebaseDatabase;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firebaseDatabase = FirebaseDatabase.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel Description");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            listenToBusLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Add marker on map click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (userMarker != null) {
                    userMarker.remove();
                }
                userMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("User Marker"));
            }
        });
    }

    private void listenToBusLocation() {
        // Assuming your bus location data is stored under "Bus_Data" in Firebase Realtime Database
        firebaseDatabase.getReference("Bus_Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve latitude, longitude, and speed from the dataSnapshot
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    double speed = dataSnapshot.child("speed").getValue(Double.class);

                    // Update the marker position
                    LatLng busLatLng = new LatLng(longitude,latitude);
                    if (busMarker == null) {
                        // Add marker if it doesn't exist
                        busMarker = mMap.addMarker(new MarkerOptions().position(busLatLng).title("Bus Marker"));
                    } else {
                        // Move the existing marker to the new position
                        busMarker.setPosition(busLatLng);
                    }

                    // Update the title of the marker to include the speed
                    String speedText = "Bus Speed: " + speed + " km/h";
                    busMarker.setTitle(speedText);

                    // Check if user marker is set and if the bus marker is near it
                    if (userMarker != null && isBusNearUser(busLatLng, userMarker.getPosition())) {
                        // Show notification
                        showNotification("Bus is near your marked location");
                    }

                    // Move the camera to the bus location
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(busLatLng));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors in retrieving data from Firebase
                Toast.makeText(MapsActivity.this, "Failed to retrieve bus location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isBusNearUser(LatLng busLatLng, LatLng userLatLng) {
        // Calculate the distance between the bus and user markers
        float[] distance = new float[1];
        Location.distanceBetween(busLatLng.latitude, busLatLng.longitude, userLatLng.latitude, userLatLng.longitude, distance);

        // Check if the distance is within a threshold (e.g., 100 meters)
        return distance[0] <= 100;
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Bus Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI) // Set the default notification sound
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify(1, builder.build());
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    listenToBusLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
