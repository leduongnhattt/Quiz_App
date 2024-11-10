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

import Domain.QuestionModel;
import Adapter.QuizListAdapter;
import Domain.QuizModel;
import com.example.capstone_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private List<QuizModel> quizModelList;
    private QuizListAdapter adapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        quizModelList = new ArrayList<>();
        getDataFromFirebase();

        // Thiết lập màu cho StatusBar
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        // Thiết lập background
        LinearLayout mainLayout = findViewById(R.id.historyPage);
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));

        ImageButton backButton = findViewById(R.id.imageButtonBack);
        TextView txtDanhmuc = findViewById(R.id.txtDanhmuc);

        backButton.setOnClickListener(v -> navigateToMain());

        // Thay đổi màu khi chế độ tối được bật và tắt
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        setButtonColors(isNightMode, backButton, txtDanhmuc);
        setupRecyclerView();
    }
    private void navigateToMain() {
        Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void setButtonColors(boolean isNightMode, ImageButton backButton, TextView txtDanhmuc) {
        int color = getResources().getColor(isNightMode ? R.color.white : R.color.black);
        backButton.setColorFilter(color);
        txtDanhmuc.setTextColor(color);
    }
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new QuizListAdapter(quizModelList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void getDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("quizzes");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizModelList.clear();

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String title = quizSnapshot.child("title").getValue(String.class);
                    String subtitle = quizSnapshot.child("subtitle").getValue(String.class);
                    String time = quizSnapshot.child("time").getValue(String.class);
                    String category = quizSnapshot.child("category").getValue(String.class);

                    // Kiểm tra xem category có phải là "History " không
                    if ("History".equals(category)) {
                        List<QuestionModel> questionList = new ArrayList<>();
                        for (DataSnapshot questionSnapshot : quizSnapshot.child("questionList").getChildren()) {
                            String questionId = questionSnapshot.child("questionId").getValue(String.class);
                            String question = questionSnapshot.child("question").getValue(String.class);
                            String correctAnswer = questionSnapshot.child("correct").getValue(String.class);

                            List<String> options = new ArrayList<>();
                            for (DataSnapshot optionSnapshot : questionSnapshot.child("options").getChildren()) {
                                options.add(optionSnapshot.getValue(String.class));
                            }
                            QuestionModel questionModel = new QuestionModel(questionId, question, options, correctAnswer);
                            questionList.add(questionModel);
                        }
                        QuizModel quizModel = new QuizModel(quizId, title, subtitle, time, category, questionList);
                        quizModelList.add(quizModel);
                    }
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
