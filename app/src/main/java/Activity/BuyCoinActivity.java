package Activity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.capstone_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BuyCoinActivity extends AppCompatActivity {

    private EditText editTextPoints;
    private TextView textViewAmount;
    private TextView pricingHeader, coinPrice10, coinPrice50, coinPrice100, coinPrice500;
    private Button confirmButton, okButton;
    private int points;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_coin);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buyCoinPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        ImageButton backBtn = findViewById(R.id.imageButtonBack);
        ConstraintLayout miniLayout = findViewById(R.id.buyCoinPage);
        miniLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        pricingHeader = findViewById(R.id.pricingHeader);
        coinPrice10 = findViewById(R.id.coinPrice10);
        coinPrice50 = findViewById(R.id.coinPrice50);
        coinPrice100 = findViewById(R.id.coinPrice100);
        coinPrice500 = findViewById(R.id.coinPrice500);
        editTextPoints = findViewById(R.id.editTextPoints);
        textViewAmount = findViewById(R.id.textViewAmount);
        setButtonColors(isNightMode, backBtn);
        setTextColors(isNightMode);
        backBtn.setOnClickListener(v -> navigateToMain());
        confirmButton = findViewById(R.id.confirmButton);
        okButton = findViewById(R.id.okButton);

        // Set the click listener for the confirm button
        confirmButton.setOnClickListener(v -> processCoinPurchase());

        // Set the click listener for the OK button
        okButton.setOnClickListener(v -> showAmount());
    }
    private void setTextColors(boolean isNightMode) {
        int textColor = isNightMode ? WHITE : BLACK;
        pricingHeader.setTextColor(textColor);
        coinPrice10.setTextColor(textColor);
        coinPrice50.setTextColor(textColor);
        coinPrice100.setTextColor(textColor);
        coinPrice500.setTextColor(textColor);
        editTextPoints.setHintTextColor(textColor);
        textViewAmount.setTextColor(textColor);
    }
    private void navigateToMain() {
        Intent intent = new Intent(BuyCoinActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void setButtonColors(boolean isNightMode, ImageButton backButton) {
        int color = getResources().getColor(isNightMode ? R.color.white : R.color.black);
        backButton.setColorFilter(color);
    }
    private void processCoinPurchase() {
        String pointsString = editTextPoints.getText().toString();

        if (pointsString.isEmpty()) {
            showDialog("Error", "Please enter a valid number of points.");
            return;
        }

        points = Integer.parseInt(pointsString); // Cập nhật điểm số

        // Update the quiz score in Firebase
        updateQuizScoreInFirebase(points);
    }

    private void showAmount() {
        String pointsString = editTextPoints.getText().toString();

        if (pointsString.isEmpty()) {
            // Notify the user to enter points
            textViewAmount.setText("Please enter a valid number of points.");
            return;
        }

        points = Integer.parseInt(pointsString);
        int amount = (points / 10) * 20000;
        textViewAmount.setText("Amount: " + amount + " VND");
    }

    private void updateQuizScoreInFirebase(int quizScore) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        // Get the current user's ID
        String userId = auth.getCurrentUser().getUid();

        // Fetch the current score of the user
        userRef.child(userId).child("score").get().addOnSuccessListener(dataSnapshot -> {
            int currentTotalScore = 0;
            if (dataSnapshot.exists()) {
                currentTotalScore = dataSnapshot.getValue(Integer.class);
            }

            // Calculate the new total score
            int newTotalScore = currentTotalScore + quizScore;

            // Update the user's total score in Firebase
            userRef.child(userId).child("score").setValue(newTotalScore)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firebase", "Total score updated successfully!");
                        showDialog("Success", "Coins purchased successfully!"); // Hiển thị dialog thành công
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Failed to update total score", e);
                        showDialog("Error", "Failed to update total score."); // Hiển thị dialog lỗi
                    });

        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to get current total score", e);
            showDialog("Error", "Failed to get current total score."); // Hiển thị dialog lỗi
        });
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); // Đóng dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
