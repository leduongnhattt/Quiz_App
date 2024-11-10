package Activity;

import static android.graphics.Color.BLACK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.KnowledgeAdapter;
import Adapter.QuizListAdapter;
import Domain.KnowledgeModel;

public class KnowledgeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private KnowledgeAdapter adapter;
    private List<KnowledgeModel> knowledgeList;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        knowledgeList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);

        setupRecyclerView();
        loadKnowledgeData();

        // Thiết lập màu Status Bar
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        // Night mode check
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        // Thiết lập background
        LinearLayout mainLayout = findViewById(R.id.knowledgePage);
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        TextView txtDanhmuc = findViewById(R.id.txtDanhmuc);
        // Sự kiện back về trang chủ
        backButton.setOnClickListener(v -> navigateToMain());

        // Thiết lập night mode và giao diện
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        setButtonColors(isNightMode, backButton, txtDanhmuc);
    }
    private void navigateToMain() {
        Intent intent = new Intent(KnowledgeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void setButtonColors(boolean isNightMode, ImageButton backButton, TextView txtDanhmuc) {
        int color = getResources().getColor(isNightMode ? R.color.white : R.color.black);
        backButton.setColorFilter(color);
        txtDanhmuc.setTextColor(color);
    }

    private void setupRecyclerView() {
        adapter = new KnowledgeAdapter(knowledgeList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadKnowledgeData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("knowledge_data");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                knowledgeList.clear(); // Clear the existing list to avoid duplication

                for (DataSnapshot knowledgeSnapshot : dataSnapshot.getChildren()) {
                    String title = knowledgeSnapshot.child("title").getValue(String.class);
                    String content = knowledgeSnapshot.child("content").getValue(String.class);
                    KnowledgeModel knowledgeModel = new KnowledgeModel(title, content);
                    knowledgeList.add(knowledgeModel);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }
}
