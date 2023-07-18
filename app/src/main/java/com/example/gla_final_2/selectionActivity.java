package com.example.gla_final_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class selectionActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_KEY_HAS_LOGGED = "hasLogged";

    private Spinner mainSpinner;
    private Spinner nestedSpinner;
    private Button searchButton;

    private DatabaseReference busDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout button click here
                saveLoggedStatus(false); // Mark user as logged out

                Intent intent = new Intent(selectionActivity.this, login_Activity.class);
                startActivity(intent);
                finish(); // Optionally finish the current activity
            }
        });

        mainSpinner = findViewById(R.id.firstSpinner);
        nestedSpinner = findViewById(R.id.secondSpinner);
        searchButton = findViewById(R.id.nextButton);

        busDataRef = FirebaseDatabase.getInstance().getReference("Bus_Data");

        initializeMainSpinner();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedMainItem = mainSpinner.getSelectedItem().toString();
                String selectedNestedItem = nestedSpinner.getSelectedItem().toString();

                Intent intent = new Intent(selectionActivity.this, MapsActivity.class);
                intent.putExtra("selectedMainItem", selectedMainItem);
                intent.putExtra("selectedNestedItem", selectedNestedItem);
                startActivity(intent);
            }
        });
    }

    private void initializeMainSpinner() {
        busDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> mainSpinnerItems = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String mainItem = childSnapshot.getKey();
                    mainSpinnerItems.add(mainItem);
                }

                ArrayAdapter<String> mainSpinnerAdapter = new ArrayAdapter<>(selectionActivity.this, android.R.layout.simple_spinner_item, mainSpinnerItems);
                mainSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mainSpinner.setAdapter(mainSpinnerAdapter);

                mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = mainSpinnerItems.get(position);

                        retrieveNestedSpinnerItems(selectedItem);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database read error if needed
            }
        });
    }

    private void retrieveNestedSpinnerItems(String selectedItem) {
        busDataRef.child(selectedItem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> nestedSpinnerItems = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String nestedItem = childSnapshot.getKey();
                    nestedSpinnerItems.add(nestedItem);
                }

                ArrayAdapter<String> nestedSpinnerAdapter = new ArrayAdapter<>(selectionActivity.this, android.R.layout.simple_spinner_item, nestedSpinnerItems);
                nestedSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                nestedSpinner.setAdapter(nestedSpinnerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database read error if needed
            }
        });
    }

    private void saveLoggedStatus(boolean hasLogged) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_HAS_LOGGED, hasLogged);
        editor.apply();
    }
}
