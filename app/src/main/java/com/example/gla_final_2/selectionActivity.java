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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class selectionActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_KEY_HAS_LOGGED = "hasLogged";

    private Spinner firstSpinner;
    private Spinner secondSpinner;
    private ArrayAdapter<String> firstSpinnerAdapter;
    private ArrayAdapter<String> secondSpinnerAdapter;
    private DatabaseReference busDataRef;
    private Map<String, List<String>> parentToChildOptions;

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

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(selectionActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the spinners
        firstSpinner = findViewById(R.id.firstSpinner);
        secondSpinner = findViewById(R.id.secondSpinner);

        // Initialize the map to store parent to child options
        parentToChildOptions = new HashMap<>();

        // Create an ArrayAdapter for the first spinner
        firstSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        firstSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstSpinner.setAdapter(firstSpinnerAdapter);

        // Create an ArrayAdapter for the second spinner
        secondSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        secondSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondSpinner.setAdapter(secondSpinnerAdapter);

        // Read the data from the "bus_data" node
        busDataRef = FirebaseDatabase.getInstance().getReference("Bus_Data");
        busDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot parentSnapshot : dataSnapshot.getChildren()) {
                    String parentOption = parentSnapshot.getKey();
                    firstSpinnerAdapter.add(parentOption);

                    List<String> childOptions = new ArrayList<>();
                    for (DataSnapshot childSnapshot : parentSnapshot.getChildren()) {
                        String childOption = childSnapshot.getKey();
                        childOptions.add(childOption);
                    }
                    parentToChildOptions.put(parentOption, childOptions);
                }
                if (firstSpinnerAdapter.getCount() > 0) {
                    firstSpinner.setSelection(0);
                    updateSecondSpinner();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Add listener for the first spinner selection
        firstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSecondSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateSecondSpinner() {
        String selectedParentOption = firstSpinner.getSelectedItem().toString();
        List<String> childOptions = parentToChildOptions.get(selectedParentOption);
        if (childOptions != null) {
            secondSpinnerAdapter.clear();
            secondSpinnerAdapter.addAll(childOptions);
        }
    }

    private void saveLoggedStatus(boolean hasLogged) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_HAS_LOGGED, hasLogged);
        editor.apply();
    }
}
