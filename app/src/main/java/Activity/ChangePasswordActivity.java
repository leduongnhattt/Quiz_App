// ChangePasswordActivity.java
package Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstone_app.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private EditText oldPassword, newPassword, confirmNewPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnChangePassword.setOnClickListener(view -> changePassword());
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        backButton.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String oldPass = oldPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmNewPassword.getText().toString().trim();

        if (!isInputValid(oldPass, newPass, confirmPass)) return;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !TextUtils.isEmpty(oldPass)) {
            String email = user.getEmail();
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            updatePasswordInDatabase(user.getUid(), newPass); // Update password in Realtime Database
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangePasswordActivity.this, SecurityActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thất bại: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không chính xác", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Update user's password in Firebase Realtime Database
    private void updatePasswordInDatabase(String userId, String newPassword) {
        database.child(userId).child("password").setValue(newPassword).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(ChangePasswordActivity.this, "Cập nhật mật khẩu thất bại trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isInputValid(String oldPass, String newPass, String confirmPass) {
        if (oldPass.isEmpty()) {
            oldPassword.setError("Mật khẩu cũ không được để trống");
            oldPassword.requestFocus();
            return false;
        }

        if (newPass.isEmpty()) {
            newPassword.setError("Mật khẩu mới không được để trống");
            newPassword.requestFocus();
            return false;
        }

        if (newPass.length() < 6) {
            newPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            newPassword.requestFocus();
            return false;
        }

        if (!newPass.equals(confirmPass)) {
            confirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            confirmNewPassword.requestFocus();
            return false;
        }

        return true;
    }
}
