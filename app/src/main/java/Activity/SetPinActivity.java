package Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class SetPinActivity extends AppCompatActivity {

    private EditText etPin1, etPin2, etPin3, etPin4;
    private Button btnSavePin;
    private FirebaseAuth auth;
    private String userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setPinPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        etPin1 = findViewById(R.id.etPin1);
        etPin2 = findViewById(R.id.etPin2);
        etPin3 = findViewById(R.id.etPin3);
        etPin4 = findViewById(R.id.etPin4);
        btnSavePin = findViewById(R.id.btnSetPin);
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        backButton.setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();

        // Lấy mật khẩu từ Firebase Realtime Database
        loadUserPassword();

        // Thiết lập hành động khi nhấn nút lưu mã PIN
        btnSavePin.setOnClickListener(view -> {
            String pin = etPin1.getText().toString().trim() +
                    etPin2.getText().toString().trim() +
                    etPin3.getText().toString().trim() +
                    etPin4.getText().toString().trim();

            if (isPinValid(pin)) {
                showPasswordDialog();
            }
        });
        setupPinFieldNavigation();
        etPin1.requestFocus();
    }

    private void loadUserPassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Lấy mật khẩu của người dùng
                    userPassword = dataSnapshot.getValue(String.class);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(SetPinActivity.this, "Lỗi khi lấy mật khẩu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupPinFieldNavigation() {
        etPin1.addTextChangedListener(new PinTextWatcher(etPin1, etPin2));
        etPin2.addTextChangedListener(new PinTextWatcher(etPin2, etPin3));
        etPin3.addTextChangedListener(new PinTextWatcher(etPin3, etPin4));
        etPin4.addTextChangedListener(new PinTextWatcher(etPin4, null));
    }

    private class PinTextWatcher implements TextWatcher {
        private final EditText currentEditText;
        private final EditText nextEditText;

        PinTextWatcher(EditText current, EditText next) {
            this.currentEditText = current;
            this.nextEditText = next;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextEditText != null) {
                nextEditText.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mật khẩu");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            verifyPassword(password);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void verifyPassword(String password) {
        // Kiểm tra xem mật khẩu nhập vào có đúng với mật khẩu lấy từ Firebase không
        if (userPassword != null && userPassword.equals(password)) {
            savePin();
        } else {
            Toast.makeText(SetPinActivity.this, "Mật khẩu không đúng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetPinActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void savePin() {
        String pin = etPin1.getText().toString().trim() +
                etPin2.getText().toString().trim() +
                etPin3.getText().toString().trim() +
                etPin4.getText().toString().trim();

        // Kiểm tra tính hợp lệ của mã PIN
        if (!isPinValid(pin)) return;

        // Lưu mã PIN vào Firebase Realtime Database
        String userId = auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("pin")
                .setValue(pin)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SetPinActivity.this, "Mã PIN đã được lưu", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SetPinActivity.this, "Lưu mã PIN thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Kiểm tra tính hợp lệ của mã PIN
    private boolean isPinValid(String pin) {
        if (pin.length() < 4) {
            Toast.makeText(this, "Mã PIN phải gồm 4 chữ số", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Kiểm tra xem các ô nhập liệu có đầy đủ hay không
        if (etPin1.getText().toString().trim().isEmpty() ||
                etPin2.getText().toString().trim().isEmpty() ||
                etPin3.getText().toString().trim().isEmpty() ||
                etPin4.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Vui lòng nhập đầy đủ mã PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
