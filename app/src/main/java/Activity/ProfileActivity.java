package Activity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    private static final String NOTIFICATION_KEY = "notification_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        boolean isNotificationEnabled = sharedPreferences.getBoolean(NOTIFICATION_KEY, true);

        setupView(isNightMode, isNotificationEnabled);

        // Khởi tạo Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            loadUserData(userId);
        }

        binding.logOutbtn.setOnClickListener(v -> {
            showLogoutDialog();
        });
        binding.securityBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SecurityActivity.class));
        });
        binding.messageBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
        });
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });
        binding.btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AboutUsActivity.class));
        });
        binding.switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(NIGHT_MODE_KEY, isChecked).apply();
            updateNightModeUI(isChecked);
        });

        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotificationPermissionDialog();
            } else {
                sharedPreferences.edit().putBoolean(NOTIFICATION_KEY, isChecked).apply();
            }
        });

        binding.bottomMenu.setItemSelected(R.id.profile, true);
        binding.bottomMenu.setOnItemSelectedListener(it -> {
            Class<?> targetActivity;
            if (it == R.id.home) {
                targetActivity = MainActivity.class;
            } else if (it == R.id.board) {
                targetActivity = LeaderBoardActivity.class;
            } else {
                targetActivity = MyQuizActivity.class;
            }
            startActivity(new Intent(ProfileActivity.this, targetActivity));
        });

        setupWindowInsets();
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
    }

    private void setupView(boolean isNightMode, boolean isNotificationEnabled) {
        binding.profilePage.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        binding.switchNightMode.setChecked(isNightMode);
        binding.switchNotification.setChecked(isNotificationEnabled);
        updateTextColor(isNightMode);
    }

    private void updateNightModeUI(boolean isChecked) {
        binding.profilePage.setBackgroundColor(isChecked ? BLACK : getResources().getColor(R.color.grey));
        updateTextColor(isChecked);
    }

    private void updateTextColor(boolean isNightMode) {
        int textColor = isNightMode ? WHITE : getResources().getColor(R.color.black);
        binding.txtEmailProfile.setTextColor(textColor);
        binding.txtName.setTextColor(textColor);
    }

    private void loadUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);

                    String imageUrl = dataSnapshot.child("picture").getValue(String.class);
                    if (imageUrl != null) {
                        Glide.with(ProfileActivity.this).load(imageUrl).into( binding.imageProfile);
                    } else {
                        binding.imageProfile.setImageResource(R.drawable.profile);
                    }
                    binding.txtEmailProfile.setText(email);
                    binding.txtName.setText(name);
                } else {
                    binding.txtEmailProfile.setText("Email không tìm thấy");
                    binding.txtName.setText("Tên không tìm thấy");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Không", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle("Bật Thông Báo")
                .setMessage("Bạn có muốn bật thông báo cho ứng dụng này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    binding.switchNotification.setChecked(false); // Turn off the switch
                    sharedPreferences.edit().putBoolean(NOTIFICATION_KEY, false).apply(); // Save the state
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.profilePage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
