package Activity;

import static android.graphics.Color.BLACK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityCreateQuizBinding;
import com.example.capstone_app.databinding.ActivityMyQuizBinding;
import com.example.capstone_app.databinding.ActivityProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.QuizListAdapter;
import Domain.QuestionModel;
import Domain.QuizModel;

public class MyQuizActivity extends AppCompatActivity {

    private ActivityMyQuizBinding binding;
    private List<QuizModel> quizModelList;
    private QuizListAdapter adapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.myQuizPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        quizModelList = new ArrayList<>();
        getDataFromFirebase();
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);
        ConstraintLayout mainLayout = binding.myQuizPage;
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));

        binding.bottomMenu.setItemSelected(R.id.myQuiz, true);
        binding.bottomMenu.setOnItemSelectedListener(it -> {
            Class<?> targetActivity;
            if (it == R.id.home) {
                targetActivity = MainActivity.class;
            } else if (it == R.id.board) {
                targetActivity = LeaderBoardActivity.class;
            } else {
                targetActivity = ProfileActivity.class;
            }
            startActivity(new Intent(MyQuizActivity.this, targetActivity));
        });
        setupRecyclerView();
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

                    if ("MyQuiz".equals(category)) {
                        List<QuestionModel> questionList = new ArrayList<>();
                        for (DataSnapshot questionSnapshot : quizSnapshot.child("questionList").getChildren()) {
                            String questionId = questionSnapshot.child("questionId").getValue(String.class);
                            String question = questionSnapshot.child("question").getValue(String.class);
                            String correctAnswer = questionSnapshot.child("correct").getValue(String.class);

                            // Lấy các tùy chọn
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

                // Thông báo cho adapter về danh sách đã được cập nhật
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý các lỗi có thể xảy ra
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }

}
