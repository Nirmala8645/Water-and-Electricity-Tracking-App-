package com.example.trackinggapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NudgesActivity extends AppCompatActivity {

    private TextView tvNudgeMessage;
    private FirebaseFirestore firestore;
    private String userId = "user123"; // Replace with actual user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nudges);

        tvNudgeMessage = findViewById(R.id.tvNudgeMessage);
        firestore = FirebaseFirestore.getInstance();

        fetchUsageAndSuggest();
    }

    private void fetchUsageAndSuggest() {
        DocumentReference usageRef = firestore.collection("UsageData").document(userId);
        DocumentReference goalRef = firestore.collection("users").document(userId).collection("goals").document("data");

        usageRef.get().addOnCompleteListener(usageTask -> {
            if (usageTask.isSuccessful()) {
                DocumentSnapshot usageDoc = usageTask.getResult();
                if (usageDoc.exists()) {
                    long waterUsed = usageDoc.getLong("waterUsed") != null ? usageDoc.getLong("waterUsed") : 0;
                    long electricityUsed = usageDoc.getLong("electricityUsed") != null ? usageDoc.getLong("electricityUsed") : 0;

                    goalRef.get().addOnCompleteListener(goalTask -> {
                        if (goalTask.isSuccessful()) {
                            DocumentSnapshot goalDoc = goalTask.getResult();
                            if (goalDoc.exists()) {
                                long waterGoal = goalDoc.getLong("waterGoal") != null ? goalDoc.getLong("waterGoal") : 100;
                                long electricityGoal = goalDoc.getLong("electricityGoal") != null ? goalDoc.getLong("electricityGoal") : 50;

                                StringBuilder suggestion = new StringBuilder("Suggestions:\n\n\n\n");

                                if (waterUsed > waterGoal) {
                                    suggestion.append("🚰 Reduce water usage! Try turning off taps while brushing.\n\n");
                                    NotificationHelper.sendGoalExceededNotification(this, "⚠ High Water Usage! Consider reducing your consumption.");
                                }
                                if (electricityUsed > electricityGoal) {
                                    suggestion.append("💡 Reduce electricity usage! Turn off lights when not in use.\n\n");
                                    NotificationHelper.sendGoalExceededNotification(this, "⚠ High Electricity Usage! Consider saving power.");
                                }
                                if (waterUsed <= waterGoal && electricityUsed <= electricityGoal) {
                                    suggestion.append("✅ Great job! You're within your usage limits.");
                                }

                                tvNudgeMessage.setText(suggestion.toString());
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "No usage data found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}