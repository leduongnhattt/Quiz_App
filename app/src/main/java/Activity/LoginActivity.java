package Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference; // Reference to Firebase Realtime Database
    private EditText loginEmail, loginPassword;
    private boolean passwordVisible = false; // Tracks visibility of the password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        setupUI();
        setupListeners();
    }

    private void setupUI() {

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loginEmail = findViewById(R.id.txtloginEmail);
        loginPassword = findViewById(R.id.txtLoginPassword);
        Button loginBtn = findViewById(R.id.btnLogin);
        ImageView btnGoogle = findViewById(R.id.btnLoginGg);
        TextView signupRedirectText = findViewById(R.id.signupRedirectText);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập toggle visibility cho trường password
        setupPasswordVisibilityToggle(loginPassword);
    }

    private void setupListeners() {
        findViewById(R.id.btnLogin).setOnClickListener(view -> loginUser());
        findViewById(R.id.signupRedirectText).setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (isEmailValid(email) && isPasswordValid(password)) {
            // Đăng nhập qua Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // Lấy thông tin người dùng từ Firebase Realtime Database
                                String userId = user.getUid();
                                getUserDetailsFromDatabase(userId);
                            }
                        } else {
                            showToast("Đăng nhập thất bại: " + task.getException().getMessage());
                        }
                    });
        }
    }
    private void getUserDetailsFromDatabase(String userId) {
        // Lấy thông tin người dùng từ Firebase Realtime Database
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    String userPicture = dataSnapshot.child("picture").getValue(String.class);
                    int userScore = dataSnapshot.child("score").getValue(Integer.class);

                    // Điều hướng đến MainActivity với thông tin người dùng
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity(userId, userName, userPicture, userScore);
                } else {
                    showToast("Không tìm thấy thông tin người dùng trong cơ sở dữ liệu");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Lỗi truy xuất dữ liệu: " + databaseError.getMessage());
            }
        });
    }

    private boolean isEmailValid(String email) {
        if (email.isEmpty()) {
            loginEmail.setError("Email không được để trống");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Vui lòng nhập Email hợp lệ");
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (password.isEmpty()) {
            loginPassword.setError("Mật khẩu không được để trống");
            return false;
        }
        return true;
    }

    private void setupPasswordVisibilityToggle(EditText passwordField) {
        passwordField.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility(passwordField);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText passwordField) {
        passwordVisible = !passwordVisible;
        passwordField.setInputType(passwordVisible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, passwordVisible ? R.drawable.baseline_visibility_24 : R.drawable.baseline_visibility_off_24, 0);
        passwordField.setSelection(passwordField.length());
    }

    private void navigateToMainActivity(String userId, String userName, String userPicture, int userScore) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("userPicture", userPicture);
        intent.putExtra("userScore", userScore);
        startActivity(intent);
        finish();
    }


    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
