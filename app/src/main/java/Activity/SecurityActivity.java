package Activity;

import static android.graphics.Color.BLACK;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Controller.SendEmailTask;

public class SecurityActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_2FA_ENABLED = "2FAEnabled";
    private static final String NIGHT_MODE_KEY = "night_mode";
    private SwitchCompat switch2FA;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout securityPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setupUI();
        setupBackButton();
        setupListeners();
    }

    private void setupUI() {
        securityPage = findViewById(R.id.activity_privacy_security);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        securityPage.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.white));

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        ViewCompat.setOnApplyWindowInsetsListener(securityPage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        int backButtonColor = isNightMode ? R.color.white : R.color.black;
        backButton.setColorFilter(getResources().getColor(backButtonColor));
        backButton.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        findViewById(R.id.changePasswordPage).setOnClickListener(v -> openActivity(ChangePasswordActivity.class));
        findViewById(R.id.setPinPage).setOnClickListener(v -> openActivity(SetPinActivity.class));
        findViewById(R.id.privacyPolicyPage).setOnClickListener(v -> openActivity(PrivacyPolicyActivity.class));

        switch2FA = findViewById(R.id.switch2FA);
        switch2FA.setChecked(sharedPreferences.getBoolean(KEY_2FA_ENABLED, false));
        switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableTwoFactorAuthentication();
            } else {
                disableTwoFactorAuthentication();
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(SecurityActivity.this, activityClass));
    }

    private void enableTwoFactorAuthentication() {
        sharedPreferences.edit().putBoolean(KEY_2FA_ENABLED, true).apply();
        send2FACode();
        Toast.makeText(this, "Xác thực hai yếu tố đã được bật", Toast.LENGTH_SHORT).show();
    }

    private void disableTwoFactorAuthentication() {
        sharedPreferences.edit().putBoolean(KEY_2FA_ENABLED, false).apply();
        Toast.makeText(this, "Xác thực hai yếu tố đã bị tắt", Toast.LENGTH_SHORT).show();
    }

    private void send2FACode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String verificationCode = generateRandomCode();
            String userEmail = currentUser.getEmail();

            // Send verification code via email
            new SendEmailTask(this, userEmail, "your_email@example.com", "your_email_password", verificationCode).execute();
            showVerificationDialog(verificationCode);
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng hiện tại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVerificationDialog(String verificationCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Xác minh", (dialog, which) -> verifyCode(input.getText().toString(), verificationCode));
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.cancel();
            switch2FA.setChecked(false);
        });

        builder.show();
    }

    private void verifyCode(String userInputCode, String verificationCode) {
        if (userInputCode.equals(verificationCode)) {
            Toast.makeText(this, "Xác minh thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mã không hợp lệ. Tắt xác thực hai yếu tố.", Toast.LENGTH_SHORT).show();
            switch2FA.setChecked(false);
            sharedPreferences.edit().putBoolean(KEY_2FA_ENABLED, false).apply();
        }
    }

    private String generateRandomCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000); // Random mã code 6 số
    }
}
