package com.example.trackinggapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        DocumentReference usageRef = firestore.collection("UsageData")
                .document(userId)
                .collection("daily")
                .document(todayDate);

        DocumentReference goalRef = firestore.collection("users")
                .document(userId)
                .collection("goals")
                .document("data");

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

                                StringBuilder suggestion = new StringBuilder("Suggestions:\n\n\n\n\n");

                                boolean exceeded = false;

                                if (waterUsed > waterGoal) {
                                    exceeded = true;
                                    suggestion.append("     "+"🚰 Turn off the tap while brushing your teeth.\n\n");
                                    suggestion.append("     "+"🚿 Use a bucket instead of a shower to save water.\n\n");
                                    suggestion.append("     "+"🧼 Wash clothes in full loads.\n\n");
                                    suggestion.append("     "+"🪣 Fix any leaking taps immediately.\n\n");
                                    suggestion.append("     "+"🌧 Collect rainwater for gardening.\n\n");
                                    NotificationHelper.sendGoalExceededNotification(this, "⚠ High Water Usage! Try reducing water consumption.");
                                }

                                if (electricityUsed > electricityGoal) {
                                    exceeded = true;
                                    suggestion.append("     "+"💡 Turn off lights when not needed.\n\n");
                                    suggestion.append("     "+"🔌 Unplug devices when not in use.\n\n");
                                    suggestion.append("     "+"🌀 Use fans instead of air conditioners when possible.\n\n");
                                    suggestion.append("     "+"📺 Limit screen time to save electricity.\n\n");
                                    suggestion.append("     "+"🌤 Use natural sunlight during the day.\n\n");
                                    NotificationHelper.sendGoalExceededNotification(this, "⚠ High Electricity Usage! Reduce your power consumption.");
                                }

                                if (!exceeded) {
                                    suggestion.append("     "+"✅ Great job! You're within your usage limits.\n\n");
                                    suggestion.append("     "+"✨ Keep following energy-efficient and water-saving habits!\n\n");
                                    suggestion.append("     "+"🎉 Excellent work! Your efforts to save resources are paying off!\n\n");
                                    suggestion.append("     "+"🌱 Keep up your eco-friendly habits for a greener future.\n\n");
                                    suggestion.append("     "+"💧 Smart water usage makes a big difference — keep it up!\n\n");
                                    suggestion.append("     "+"⚡ Energy saved today is a better tomorrow — great job!\n\n");
                                    suggestion.append("     "+"🏆 You’re setting a great example for others!\n\n");
                                    suggestion.append("     "+"🌍 Small savings every day create a big impact — continue the good work!\n\n");
                                }

                                tvNudgeMessage.setText(suggestion.toString());

                            } else {
                                Toast.makeText(this, "No goal data found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to fetch goals", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(this, "No usage data for today!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to fetch usage data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
