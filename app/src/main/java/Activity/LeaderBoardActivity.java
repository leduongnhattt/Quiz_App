package Activity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityLeaderBoardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.LeaderAdapter;
import Domain.UserModel;

public class LeaderBoardActivity extends AppCompatActivity {

    private ActivityLeaderBoardBinding binding;
    private final LeaderAdapter leaderAdapter = new LeaderAdapter();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    private TextView titleTop1Txt, titleTop2Txt, titleTop3Txt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        ConstraintLayout mainLayout = binding.leaderPage;
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        titleTop3Txt = findViewById(R.id.titleTop3Txt);
        titleTop2Txt = findViewById(R.id.titleTop2Txt);
        titleTop1Txt = findViewById(R.id.titleTop1Txt);
        titleTop1Txt.setTextColor(isNightMode ? WHITE : BLACK);
        titleTop2Txt.setTextColor(isNightMode ? WHITE : BLACK);
        titleTop3Txt.setTextColor(isNightMode ? WHITE : BLACK);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.leaderPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        setupButtons();
        updateLeaderBoard("Tuần");
        binding.bottomMenu.setItemSelected(R.id.board, true);
        binding.bottomMenu.setOnItemSelectedListener(it -> {
            Class<?> targetActivity;
            if (it == R.id.home) {
                targetActivity = MainActivity.class;
            } else if (it == R.id.profile) {
                targetActivity = ProfileActivity.class;
            } else {
                targetActivity = MyQuizActivity.class;
            }
            startActivity(new Intent(LeaderBoardActivity.this, targetActivity));
        });
    }

    private void setupButtons() {
        binding.btnWeek.setOnClickListener(v -> {
            setButtonColor(binding.btnWeek);
            updateLeaderBoard("Tuần");
        });

        binding.btnMonth.setOnClickListener(v -> {
            setButtonColor(binding.btnMonth);
            updateLeaderBoard("Tháng");
        });

        binding.btnAllTime.setOnClickListener(v -> {
            setButtonColor(binding.btnAllTime);
            updateLeaderBoard("Tất cả");
        });
    }

    private void setButtonColor(View selectedButton) {
        // Khôi phục màu nền và văn bản cho tất cả các nút
        resetButtonStyles();
        // Đặt màu và văn bản cho nút đang chọn
        selectedButton.setBackgroundResource(R.drawable.orange_background);
        ((AppCompatButton) selectedButton).setTextColor(getResources().getColor(R.color.white));
    }

    private void resetButtonStyles() {
        // Đặt lại màu cho tất cả các nút
        binding.btnWeek.setBackgroundResource(R.drawable.white_background);
        binding.btnWeek.setTextColor(getResources().getColor(R.color.navy_blue));

        binding.btnMonth.setBackgroundResource(R.drawable.white_background);
        binding.btnMonth.setTextColor(getResources().getColor(R.color.navy_blue));

        binding.btnAllTime.setBackgroundResource(R.drawable.white_background);
        binding.btnAllTime.setTextColor(getResources().getColor(R.color.navy_blue));
    }

    private void updateLeaderBoard(String period) {
        loadData(period); // Tải dữ liệu từ Firebase
    }

    private void loadData(String period) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        List<UserModel> users = new ArrayList<>();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    UserModel user = userSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                // Sắp xếp người dùng theo điểm số
                users.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

                // Lấy danh sách theo thời gian
                List<UserModel> filteredUsers = new ArrayList<>();
                switch (period) {
                    case "Tuần":
                        filteredUsers = users.subList(3, 9);
                        break;
                    case "Tháng":
                        filteredUsers = users.subList(2, 8);
                        break;
                    case "Tất cả":
                        filteredUsers = users;
                        break;
                    default:
                        return;
                }
                updateTopUsers(filteredUsers);

                // Cập nhật danh sách leaderboard
                if (filteredUsers.size() > 3) {
                    filteredUsers = filteredUsers.subList(3, filteredUsers.size());
                } else {
                    filteredUsers.clear();
                }
                leaderAdapter.differ.submitList(filteredUsers);
                binding.leaderView.setLayoutManager(new LinearLayoutManager(LeaderBoardActivity.this));
                binding.leaderView.setAdapter(leaderAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "Không lấy được dữ liệu");
            }
        });
    }

    private void updateTopUsers(List<UserModel> list) {
        if (list.size() > 0) {
            binding.scoreTop1Txt.setText(String.valueOf(list.get(0).getScore()));
            binding.titleTop1Txt.setText(list.get(0).getName());
            Glide.with(this).load(list.get(0).getPicture()).into(binding.pic1); // Tải ảnh từ URL
        }

        if (list.size() > 1) {
            binding.scoreTop2Txt.setText(String.valueOf(list.get(1).getScore()));
            binding.titleTop2Txt.setText(list.get(1).getName());
            Glide.with(this).load(list.get(1).getPicture()).into(binding.pic2); // Tải ảnh từ URL
        }

        if (list.size() > 2) {
            binding.scoreTop3Txt.setText(String.valueOf(list.get(2).getScore()));
            binding.titleTop3Txt.setText(list.get(2).getName());
            Glide.with(this).load(list.get(2).getPicture()).into(binding.pic3); // Tải ảnh từ URL
        }
    }
    private int getDrawableResourceId(String picture) {
        return binding.getRoot().getResources().getIdentifier(
                picture, "drawable", binding.getRoot().getContext().getPackageName()
        );
    }
}
