package Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import Activity.LoginActivity;
import Domain.CompleteQuiz;
import Domain.UserModel;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference database;
    private EditText signupEmail, signUpPassword, signUpRePassword;
    private Button signupBtn;
    private TextView loginRedirectText;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo FirebaseAuth và FirebaseDatabase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        signupEmail = findViewById(R.id.txtEmail);
        signUpPassword = findViewById(R.id.txtPassword);
        signUpRePassword = findViewById(R.id.txtRePassword);
        signupBtn = findViewById(R.id.btnSignUp);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Thiết lập listener cho nút đăng ký và redirect đến màn hình đăng nhập
        signupBtn.setOnClickListener(view -> createUser());
        loginRedirectText.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

        // Thiết lập chức năng chuyển đổi hiển thị mật khẩu
        setupPasswordVisibilityToggle(signUpPassword);
        setupPasswordVisibilityToggle(signUpRePassword);
    }

    // Tạo người dùng mới trong Firebase Authentication
    private void createUser() {
        String user = signupEmail.getText().toString().trim();
        String pass = signUpPassword.getText().toString().trim();
        String rePass = signUpRePassword.getText().toString().trim();

        // Kiểm tra tính hợp lệ của đầu vào
        if (!isInputValid(user, pass, rePass)) return;

        // Tạo người dùng mới trong Firebase
        auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Lưu thông tin người dùng vào Firebase Database
                String userId = auth.getCurrentUser().getUid();
                saveUserToDatabase(userId, user, pass); // Lưu email và password

                Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            } else {
                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lưu thông tin người dùng vào Firebase Realtime Database
    private void saveUserToDatabase(String userId, String email, String pass) {
        Map<String, CompleteQuiz> completedQuizzes = new HashMap<>();
        UserModel userModel = new UserModel(userId, "User", email, pass, "https://firebasestorage.googleapis.com/v0/b/capstone-5716b.appspot.com/o/profile.png?alt=media&token=63a8e04a-e2ba-4494-9c83-9c8495ee892f", 0, completedQuizzes);
        database.child(userId).setValue(userModel);
    }

    // Kiểm tra tính hợp lệ của đầu vào người dùng
    private boolean isInputValid(String user, String pass, String rePass) {
        if (user.isEmpty()) {
            setError(signupEmail, "Email không được để trống");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            setError(signupEmail, "Email không hợp lệ");
            return false;
        }
        if (pass.isEmpty()) {
            setError(signUpPassword, "Mật khẩu không được để trống");
            return false;
        }
        if (pass.length() < 6) {
            setError(signUpPassword, "Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        if (rePass.isEmpty()) {
            setError(signUpRePassword, "Vui lòng nhập lại mật khẩu");
            return false;
        }
        if (!pass.equals(rePass)) {
            setError(signUpRePassword, "Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    // Thiết lập thông báo lỗi cho các trường đầu vào
    private void setError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    // Thiết lập chức năng chuyển đổi hiển thị mật khẩu
    private void setupPasswordVisibilityToggle(EditText passwordField) {
        passwordField.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && isDrawableAreaTapped(passwordField, event)) {
                togglePasswordVisibility(passwordField);
                return true;
            }
            return false;
        });
    }

    // Kiểm tra xem có chạm vào khu vực hiển thị không
    private boolean isDrawableAreaTapped(EditText passwordField, MotionEvent event) {
        return event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[2].getBounds().width());
    }

    // Chuyển đổi hiển thị mật khẩu
    private void togglePasswordVisibility(EditText passwordField) {
        if (passwordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
        } else {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
        }
        passwordField.setSelection(passwordField.length());
        passwordVisible = !passwordVisible;
    }
}
