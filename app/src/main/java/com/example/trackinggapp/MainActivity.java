package com.example.trackinggapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView waterUsageTextView, electricityUsageTextView;
    private FirebaseFirestore db;
    private Button btnStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing views
        waterUsageTextView = findViewById(R.id.waterUsageTextView);
        electricityUsageTextView = findViewById(R.id.electricityUsageTextView);
        btnStatistics = findViewById(R.id.btnStatistics);

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Fetch real-time usage data
        fetchUsageData();

        // Open StatisticsActivity when button is clicked
        btnStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUsageData() {
        DocumentReference docRef = db.collection("users").document("usage");

        // Listen for real-time updates
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long waterUsage = documentSnapshot.getLong("waterUsage");
                Long electricityUsage = documentSnapshot.getLong("electricityUsage");

                // Set default values if null
                String waterText = "Water Usage: " + (waterUsage != null ? waterUsage + " liters" : "N/A");
                String electricityText = "Electricity Usage: " + (electricityUsage != null ? electricityUsage + " kWh" : "N/A");

                // Display the values in TextViews
                waterUsageTextView.setText(waterText);
                electricityUsageTextView.setText(electricityText);
            } else {
                Toast.makeText(MainActivity.this, "No real-time data available!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
