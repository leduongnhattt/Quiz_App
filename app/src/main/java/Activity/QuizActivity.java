package Activity;

import static android.graphics.Color.BLACK;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityQuizBinding;
import com.example.capstone_app.databinding.ScoreDialogBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Domain.QuestionModel;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    public static List<QuestionModel> questionModelList;
    public static String time = "";
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    private ActivityQuizBinding binding;
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private int score = 0;
    private String quizCategory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.quizPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        // Thiết lập các view sau khi đã khởi tạo binding
        LinearLayout mainLayout = binding.quizPage;
        CardView npcLayout = binding.cardPage;

        // Thiết lập background dựa trên trạng thái Night Mode
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.white));
        npcLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.white));
        if (isNightMode) {
            binding.nextBtn.setBackgroundColor(getResources().getColor(R.color.white));
            binding.nextBtn.setTextColor(Color.BLACK);
        } else {
            binding.nextBtn.setTextColor(Color.BLACK);
        }

        // Thiết lập onClickListeners cho buttons
        binding.btn0.setOnClickListener(this);
        binding.btn1.setOnClickListener(this);
        binding.btn2.setOnClickListener(this);
        binding.btn3.setOnClickListener(this);
        binding.nextBtn.setOnClickListener(this);

        loadQuestions(); // Load questions
        startTimer(); // Start countdown timer
    }

    private void startTimer() {
        long totalTimeMillis = Integer.parseInt(time) * 60 * 1000L;

        new CountDownTimer(totalTimeMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                binding.timerIndicatorTextview.setText(String.format("%02d:%02d", minutes, remainingSeconds));
            }

            @Override
            public void onFinish() {
                binding.timerIndicatorTextview.setText("00:00");
                finishQuiz();
            }
        }.start();
    }

    private void loadQuestions() {
        selectedAnswer = ""; // Reset selected answer

        // Reset background color of buttons to grey
        binding.btn0.setBackgroundColor(getColor(R.color.grey));
        binding.btn1.setBackgroundColor(getColor(R.color.grey));
        binding.btn2.setBackgroundColor(getColor(R.color.grey));
        binding.btn3.setBackgroundColor(getColor(R.color.grey));

        // Kiểm tra tất cả các câu hỏi đã trả lời hết chưa
        if (currentQuestionIndex == questionModelList.size()) {
            finishQuiz();
            return;
        }

        // Update question information
        binding.questionIndicatorTextview.setText("Câu hỏi " + (currentQuestionIndex + 1) + "/" + questionModelList.size());
        binding.questionProgressIndicator.setProgress((int) ((currentQuestionIndex * 1.0 / questionModelList.size()) * 100));

        // Load current question
        QuestionModel currentQuestion = questionModelList.get(currentQuestionIndex);
        binding.questionTextview.setText(currentQuestion.getQuestion());

        // Set answer options
        binding.btn0.setText(currentQuestion.getOptions().get(0));
        binding.btn1.setText(currentQuestion.getOptions().get(1));
        binding.btn2.setText(currentQuestion.getOptions().get(2));
        binding.btn3.setText(currentQuestion.getOptions().get(3));
        binding.nextBtn.setEnabled(false);
        binding.nextBtn.setBackgroundColor(getColor(R.color.grey));
    }

    @Override
    public void onClick(View view) {
        // Reset background color of buttons
        binding.btn0.setBackgroundColor(getColor(R.color.grey));
        binding.btn1.setBackgroundColor(getColor(R.color.grey));
        binding.btn2.setBackgroundColor(getColor(R.color.grey));
        binding.btn3.setBackgroundColor(getColor(R.color.grey));

        Button clickedBtn = (Button) view;

        if (clickedBtn.getId() == R.id.next_btn) {
            // Kiểm tra liệu câu trả lời có đúng
            if (selectedAnswer.equals(questionModelList.get(currentQuestionIndex).getCorrect())) {
                score++;
                Log.i("Score of quiz", String.valueOf(score));
                highlightCorrectAnswer(R.color.green);
            } else {
                highlightWrongAnswer(R.color.red);
                highlightCorrectAnswer(R.color.green);
            }

            // Cho trễ 1s trước khi load câu hỏi tiếp theo
            binding.nextBtn.postDelayed(() -> {
                currentQuestionIndex++;
                loadQuestions();
            }, 1500);
        } else {
            selectedAnswer = clickedBtn.getText().toString();
            clickedBtn.setBackgroundColor(getColor(R.color.orange));
            binding.nextBtn.setEnabled(true);
            if (sharedPreferences.getBoolean(NIGHT_MODE_KEY, false)) {
                binding.nextBtn.setBackgroundColor(getColor(R.color.orange));
            }else {
                binding.nextBtn.setBackgroundColor(getColor(R.color.orange));
            }
        }
    }

    private void highlightCorrectAnswer(int color) {
        String correctAnswer = questionModelList.get(currentQuestionIndex).getCorrect();
        if (correctAnswer.equals(binding.btn0.getText().toString())) {
            binding.btn0.setBackgroundColor(getColor(color));
        } else if (correctAnswer.equals(binding.btn1.getText().toString())) {
            binding.btn1.setBackgroundColor(getColor(color));
        } else if (correctAnswer.equals(binding.btn2.getText().toString())) {
            binding.btn2.setBackgroundColor(getColor(color));
        } else if (correctAnswer.equals(binding.btn3.getText().toString())) {
            binding.btn3.setBackgroundColor(getColor(color));
        }
    }

    private void highlightWrongAnswer(int color) {
        if (selectedAnswer.equals(binding.btn0.getText().toString())) {
            binding.btn0.setBackgroundColor(getColor(color));
        } else if (selectedAnswer.equals(binding.btn1.getText().toString())) {
            binding.btn1.setBackgroundColor(getColor(color));
        } else if (selectedAnswer.equals(binding.btn2.getText().toString())) {
            binding.btn2.setBackgroundColor(getColor(color));
        } else if (selectedAnswer.equals(binding.btn3.getText().toString())) {
            binding.btn3.setBackgroundColor(getColor(color));
        }
    }

    private void finishQuiz() {
        quizCategory = getIntent().getStringExtra("quizCategory");
        int totalQuestions = questionModelList.size();
        int percentage = (int) ((float) score / totalQuestions * 100);

        // Khởi tạo và cấu hình dialog view cho score
        ScoreDialogBinding dialogBinding = ScoreDialogBinding.inflate(getLayoutInflater());
        dialogBinding.scoreProgressIndicator.setProgress(percentage);
        dialogBinding.scoreProgressText.setText(percentage + "%");

        if (percentage > 60) {
            dialogBinding.scoreTitle.setText("Chúc mứng! Bạn đã vượt qua");
            dialogBinding.scoreTitle.setTextColor(Color.BLUE);
        } else {
            dialogBinding.scoreTitle.setText("Thật đáng tiếc! Bạn đã trượt");
            dialogBinding.scoreTitle.setTextColor(Color.RED);
        }
        dialogBinding.scoreSubtitle.setText("Bạn đã làm đúng " + score +"/" + totalQuestions + " câu hỏi");
        dialogBinding.finishBtn.setOnClickListener(v -> finish());

        // Hiển thị và cập nhật điểm ở Firebase
        new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .show();
         if(!("MyQuiz".equals(quizCategory))) {
            updateQuizScoreInFirebase(score * 10, totalQuestions);
        }

    }
    private void updateQuizScoreInFirebase(int quizScore, int totalQuestions) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users");

        // Lấy ID của người dùng hiện tại
        String userId = auth.getCurrentUser().getUid();
        String quizId = getIntent().getStringExtra("quizId");

        String dateCompleted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Dữ liệu của bài quiz
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("dateCompleted", dateCompleted);
        quizData.put("score", quizScore);

        // Cập nhật thông tin completedQuizzes cho userId và lưu bài quiz mới
        userRef.child(userId).child("completedQuizzes").child(quizId).setValue(quizData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Quiz data updated successfully!");
                    // Sau khi lưu completedQuiz thành công, lấy tổng score hiện tại của user
                    userRef.child(userId).child("score").get().addOnSuccessListener(dataSnapshot -> {
                        int currentTotalScore = 0;
                        if (dataSnapshot.exists()) {
                            currentTotalScore = dataSnapshot.getValue(Integer.class);
                        }
                        // Cộng thêm quizScore vào tổng score hiện tại
                        int newTotalScore = currentTotalScore + quizScore;

                        // Cập nhật lại tổng score của user trong Firebase
                        userRef.child(userId).child("score").setValue(newTotalScore)
                                .addOnSuccessListener(aVoid1 -> Log.d("Firebase", "Total score updated successfully!"))
                                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update total score", e));
                    }).addOnFailureListener(e -> Log.e("Firebase", "Failed to get current total score", e));
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update quiz data", e));
    }
}
