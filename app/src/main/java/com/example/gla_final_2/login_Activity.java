package com.example.gla_final_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class login_Activity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://busdata-358b6-default-rtdb.firebaseio.com/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final EditText uniroll = findViewById(R.id.uniroll);
        final EditText password = findViewById(R.id.password);
        final Button loginBtn = findViewById(R.id.loginbtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String unirolltxt = uniroll.getText().toString();
                final String passwordtxt = password.getText().toString();

                if (unirolltxt.isEmpty() || passwordtxt.isEmpty()) {
                    Toast.makeText(login_Activity.this, "Please enter the University Roll Number and Password", Toast.LENGTH_SHORT).show();
                }
                else {
                    databaseReference.child("student").addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(unirolltxt)) {
                                final String getpassword = snapshot.child(unirolltxt).child("password").getValue(String.class);


                                    if(getpassword.equals(passwordtxt)) {
                                        Toast.makeText(login_Activity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(login_Activity.this, selectionActivity.class));
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(login_Activity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                    }

                            }
                            else {
                                Toast.makeText(login_Activity.this, "Password not found", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(login_Activity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
