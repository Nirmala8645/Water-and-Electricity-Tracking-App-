package com.example.trackinggapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tvWaterUsage, tvElectricityUsage, tvGoal;
    private FirebaseFirestore firestore;
    private String userId = "user123"; // Replace with actual user ID

    private long waterUsage = 0, electricityUsage = 0, waterGoal = 100, electricityGoal = 50;

    private PieChart waterChart, electricityChart; // Circular Charts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvWaterUsage = findViewById(R.id.tvWaterUsage);
        tvElectricityUsage = findViewById(R.id.tvElectricityUsage);
        tvGoal = findViewById(R.id.tvGoal);
        waterChart = findViewById(R.id.waterChart);
        electricityChart = findViewById(R.id.electricityChart);

        firestore = FirebaseFirestore.getInstance();

        fetchGoalData();   // Fetch goals first
        fetchUsageData();  // Fetch usage after goal
    }

    private void fetchGoalData() {
        DocumentReference docRef = firestore.collection("users").document(userId).collection("goals").document("data");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                waterGoal = document.getLong("waterGoal") != null ? document.getLong("waterGoal") : 100;
                electricityGoal = document.getLong("electricityGoal") != null ? document.getLong("electricityGoal") : 50;

                tvGoal.setText("Goal: Water " + waterGoal + "L, Electricity " + electricityGoal + " kWh");

                fetchUsageData();
            } else {
                showError("No goal data found!");
            }
        });
    }

    private void fetchUsageData() {
        DocumentReference docRef = firestore.collection("UsageData").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                waterUsage = document.getLong("waterUsed") != null ? document.getLong("waterUsed") : 0;
                electricityUsage = document.getLong("electricityUsed") != null ? document.getLong("electricityUsed") : 0;

                tvWaterUsage.setText("Water Usage: " + waterUsage + "L");
                tvElectricityUsage.setText("Electricity Usage: " + electricityUsage + " kWh");

                checkAndSendNotifications(); // Send notification if limit exceeded
                updateCharts(); // Update circular charts
            } else {
                showError("No usage data found!");
            }
        });
    }

    private void checkAndSendNotifications() {
        if (waterUsage > waterGoal) {
            NotificationHelper.sendGoalExceededNotification(this, "Water usage exceeded! (" + waterUsage + "L)");
        }
        if (electricityUsage > electricityGoal) {
            NotificationHelper.sendGoalExceededNotification(this, "Electricity usage exceeded! (" + electricityUsage + " kWh)");
        }
    }

    private void updateCharts() {
        updateChart(waterChart, "Water Usage", waterUsage, waterGoal, Color.CYAN);
        updateChart(electricityChart, "Electricity Usage", electricityUsage, electricityGoal, Color.BLUE);
    }

    private void updateChart(PieChart chart, String label, long usage, long goal, int color) {
        List<PieEntry> entries = new ArrayList<>();
        float percentage = (float) usage / goal * 100;
        entries.add(new PieEntry(percentage, label));
        entries.add(new PieEntry(100 - percentage, "Remaining"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(color, Color.LTGRAY);
        dataSet.setValueTextSize(14f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(75f);
        chart.setTransparentCircleRadius(80f);
        chart.setCenterText(String.format("%.0f%%", percentage));
        chart.setCenterTextSize(16f);
        chart.setEntryLabelTextSize(12f);
        chart.getLegend().setEnabled(false);

        chart.invalidate();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("StatisticsActivity", message);
    }
}