package Activity;

import static android.graphics.Color.BLACK;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;
import com.example.capstone_app.databinding.ActivityCreateQuizBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Domain.QuestionModel;
import Domain.QuizModel;

public class CreateQuizActivity extends AppCompatActivity {

    private ActivityCreateQuizBinding binding;
    private List<QuestionModel> questionList;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.imageButtonBack.setOnClickListener(v -> finish());
        // Khởi tạo Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("quizzes");
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        // Thiết lập background
        LinearLayout mainLayout = findViewById(R.id.createQuiz);
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));


        if (isNightMode) {
            binding.imageButtonBack.setColorFilter(getResources().getColor(R.color.white));
            setEditTextColors(R.color.white, R.color.white);
        } else {
            binding.imageButtonBack.setColorFilter(getResources().getColor(R.color.black));
            setEditTextColors(R.color.black, R.color.black);
        }

        // Khởi tạo Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Khởi tạo danh sách câu hỏi
        questionList = new ArrayList<>();

        // Áp dụng padding cho root layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.createQuiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.buttonSaveQuiz.setOnClickListener(v -> saveQuestions());
    }
    private void setEditTextColors(int textColor, int hintColor) {
        binding.editTextQuizTitle.setTextColor(getResources().getColor(textColor));
        binding.editTextQuizSubtitle.setTextColor(getResources().getColor(textColor));
        binding.editTextQuestion.setTextColor(getResources().getColor(textColor));
        binding.editTextCorrectAnswer.setTextColor(getResources().getColor(textColor));
        binding.editTextOption1.setTextColor(getResources().getColor(textColor));
        binding.editTextOption2.setTextColor(getResources().getColor(textColor));
        binding.editTextOption3.setTextColor(getResources().getColor(textColor));
        binding.editTextOption4.setTextColor(getResources().getColor(textColor));

        // Thiết lập màu cho hint
        binding.editTextQuizTitle.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextQuizSubtitle.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextQuestion.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextCorrectAnswer.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextOption1.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextOption2.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextOption3.setHintTextColor(getResources().getColor(hintColor));
        binding.editTextOption4.setHintTextColor(getResources().getColor(hintColor));
    }
    private void clearInputFields() {
        binding.editTextQuestion.setText("");
        binding.editTextCorrectAnswer.setText("");
        binding.editTextOption1.setText("");
        binding.editTextOption2.setText("");
        binding.editTextOption3.setText("");
        binding.editTextOption4.setText("");
    }

    private void saveQuestions() {
        String quizTitle = binding.editTextQuizTitle.getText().toString().trim();
        String quizSubtitle = binding.editTextQuizSubtitle.getText().toString().trim();
        String questionText = binding.editTextQuestion.getText().toString().trim();
        String correctAnswer = binding.editTextCorrectAnswer.getText().toString().trim();
        List<String> options = new ArrayList<>();
        options.add(binding.editTextOption1.getText().toString());
        options.add(binding.editTextOption2.getText().toString());
        options.add(binding.editTextOption3.getText().toString());
        options.add(binding.editTextOption4.getText().toString());
        if (!isInputValid(quizTitle, quizSubtitle, questionText, correctAnswer, options)) {
            return; // Nếu đầu vào không hợp lệ, dừng lại
        }
        String questionId = UUID.randomUUID().toString();
        QuestionModel question = new QuestionModel(questionId, questionText, options, correctAnswer);
        questionList.add(question);

        // Lấy userId của người dùng hiện tại đang đăng nhập
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            String quizId = ("my" + userId );

            QuizModel quiz = new QuizModel(quizId, quizTitle, quizSubtitle, "30", "MyQuiz", questionList);

            databaseReference.child(quizId).setValue(quiz)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateQuizActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateQuizActivity.this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(CreateQuizActivity.this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show();
        }
    }
    // Kiểm tra tính hợp lệ của các trường
    private boolean isInputValid(String quizTitle, String quizSubtitle, String questionText, String correctAnswer, List<String> options) {

        if (quizTitle.isEmpty()) {
            setError(binding.editTextQuizTitle, "Tiêu đề quiz không được để trống");
            return false;
        }
        if (quizSubtitle.isEmpty()) {
            setError(binding.editTextQuizSubtitle, "Phụ đề quiz không được để trống");
            return false;
        }
        if (questionText.isEmpty()) {
            setError(binding.editTextQuestion, "Câu hỏi không được để trống");
            return false;
        }
        if (correctAnswer.isEmpty()) {
            setError(binding.editTextCorrectAnswer, "Câu trả lời đúng không được để trống");
            return false;
        }
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).isEmpty()) {
                setError(getEditTextForOption(i), "Tùy chọn " + (i + 1) + " không được để trống");
                return false;
            }
        }
        return true;
    }

    private EditText getEditTextForOption(int index) {
        switch (index) {
            case 0: return binding.editTextOption1;
            case 1: return binding.editTextOption2;
            case 2: return binding.editTextOption3;
            case 3: return binding.editTextOption4;
            default: return null;
        }
    }
    // Thiết lập thông báo lỗi cho các trường đầu vào
    private void setError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }
}
