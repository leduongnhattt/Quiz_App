package Activity;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Domain.QuestionModel;
import Domain.QuizModel;

public class MainActivity extends AppCompatActivity {
    private List<QuizModel> quizModelList = new ArrayList<>();
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) displayUserInfo(currentUser);

        getDataFromFirebase();
        setupNightMode();

        setupBottomMenu();
        setupButtonListeners();
    }

    private void setupNightMode() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        int textColor = isNightMode ? WHITE : getResources().getColor(R.color.black);
        int backgroundColor = isNightMode ? BLACK : getResources().getColor(R.color.grey);

        binding.txtName.setTextColor(textColor);
        binding.txtCate.setTextColor(textColor);
        binding.main.setBackgroundColor(backgroundColor);

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBottomMenu() {
        binding.bottomMenu.setItemSelected(R.id.home, true);
        binding.bottomMenu.setOnItemSelectedListener(item -> {
            Class<?> targetActivity = null;
            if (item == R.id.board) {
                targetActivity = LeaderBoardActivity.class;
            } else if (item == R.id.profile) {
                targetActivity = ProfileActivity.class;
            } else if (item == R.id.myQuiz) {
                targetActivity = MyQuizActivity.class;
            }
            if (targetActivity != null) {
                startActivity(new Intent(MainActivity.this, targetActivity));
            }
        });
    }

    private void setupButtonListeners() {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        binding.btnBuyCoin.setOnClickListener(view -> {
            binding.btnBuyCoin.startAnimation(scaleUp);
            binding.btnBuyCoin.startAnimation(scaleDown);
            startActivity(new Intent(MainActivity.this, BuyCoinActivity.class));
        });

        binding.btnRandomQuiz.setOnClickListener(view -> {
            binding.btnRandomQuiz.startAnimation(scaleUp);
            binding.btnRandomQuiz.startAnimation(scaleDown);
            startRandomQuiz();
        });

        binding.btnKnowledge.setOnClickListener(view -> {
            binding.btnKnowledge.startAnimation(scaleUp);
            binding.btnKnowledge.startAnimation(scaleDown);
            startActivity(new Intent(MainActivity.this, KnowledgeActivity.class));
        });

        binding.btnCreateQuiz.setOnClickListener(view -> {
            binding.btnCreateQuiz.startAnimation(scaleUp);
            binding.btnCreateQuiz.startAnimation(scaleDown);
            startActivity(new Intent(MainActivity.this, CreateQuizActivity.class));
        });

        setupCategoryButton(R.id.linearScience, "Science", ScienceActivity.class, scaleUp, scaleDown);
        setupCategoryButton(R.id.linearHistory, "History", HistoryActivity.class, scaleUp, scaleDown);
        setupCategoryButton(R.id.linearSport, "Sport", SportActivity.class, scaleUp, scaleDown);
        setupCategoryButton(R.id.linearMath, "Math", MathActivity.class, scaleUp, scaleDown);
    }

    private void displayUserInfo(FirebaseUser user) {
        String userId = user.getUid();

        databaseReference.child(userId).child("name").get().addOnCompleteListener(task -> {
            String name = task.isSuccessful() ? task.getResult().getValue(String.class) : "User";
            binding.txtName.setText("Hi, " + (name != null ? name : "User"));
        });
        databaseReference.child(userId).child("picture").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String imageUrl = task.getResult().getValue(String.class);

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.imageView);
            }
        });
        databaseReference.child(userId).child("score").get().addOnCompleteListener(task -> {
            Integer score = task.isSuccessful() ? task.getResult().getValue(Integer.class) : 0;
            binding.scoreTxt.setText(String.valueOf(score != null ? score : 0));
        });
    }

    private void startRandomQuiz() {
        if (quizModelList.isEmpty()) {
            Toast.makeText(this, "Quiz list is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categories = {"Math", "Sport", "Science", "History"};
        String selectedCategory = categories[(int) (Math.random() * categories.length)];

        List<QuizModel> filteredQuizList = new ArrayList<>();
        for (QuizModel quiz : quizModelList) {
            if (quiz.getCategory().equals(selectedCategory)) filteredQuizList.add(quiz);
        }

        if (!filteredQuizList.isEmpty()) {
            QuizModel randomQuiz = filteredQuizList.get((int) (Math.random() * filteredQuizList.size()));
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            QuizActivity.questionModelList = randomQuiz.getQuestionList();
            QuizActivity.time = randomQuiz.getTime();
            intent.putExtra("quizCategory", randomQuiz.getCategory());
            intent.putExtra("quizId", randomQuiz.getId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No quiz available for the selected category!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCategoryButton(int layoutId, String category, Class<?> activityClass, Animation scaleUp, Animation scaleDown) {
        LinearLayout categoryLayout = findViewById(layoutId);
        categoryLayout.setOnClickListener(view -> {
            categoryLayout.startAnimation(scaleUp);
            categoryLayout.startAnimation(scaleDown);

            Intent intent = new Intent(MainActivity.this, activityClass);
            intent.putExtra("quizCategory", category);
            startActivity(intent);
        });
    }

    private void getDataFromFirebase() {
        DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizModelList.clear(); // Clear the list to prevent duplicates
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String title = quizSnapshot.child("title").getValue(String.class);
                    String subtitle = quizSnapshot.child("subtitle").getValue(String.class);
                    String time = quizSnapshot.child("time").getValue(String.class);
                    String category = quizSnapshot.child("category").getValue(String.class);

                    if (!"MyQuiz".equals(category)) {
                        List<QuestionModel> questionList = new ArrayList<>();
                        for (DataSnapshot questionSnapshot : quizSnapshot.child("questionList").getChildren()) {
                            String questionId = questionSnapshot.child("questionId").getValue(String.class);
                            String question = questionSnapshot.child("question").getValue(String.class);
                            String correctAnswer = questionSnapshot.child("correct").getValue(String.class);

                            List<String> options = new ArrayList<>();
                            for (DataSnapshot optionSnapshot : questionSnapshot.child("options").getChildren()) {
                                options.add(optionSnapshot.getValue(String.class));
                            }
                            questionList.add(new QuestionModel(questionId, question, options, correctAnswer));
                        }
                        quizModelList.add(new QuizModel(quizId, title, subtitle, time, category, questionList));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }
}
