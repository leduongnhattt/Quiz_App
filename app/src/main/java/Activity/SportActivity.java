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

import Adapter.QuizListAdapter;
import Domain.QuestionModel;
import Domain.QuizModel;

public class SportActivity extends AppCompatActivity {

    private List<QuizModel> quizModelList;
    private QuizListAdapter adapter;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String NIGHT_MODE_KEY = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);

        quizModelList = new ArrayList<>();
        getDataFromFirebase();

        // Thiết lập màu Status Bar
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = sharedPreferences.getBoolean(NIGHT_MODE_KEY, false);

        // Thiết lập background
        LinearLayout mainLayout = findViewById(R.id.sportPage);

        ImageButton backButton = findViewById(R.id.imageButtonBack);
        // Thiết lập night mode và giao diện
        TextView txtDanhmuc = findViewById(R.id.txtDanhmuc);
        mainLayout.setBackgroundColor(isNightMode ? BLACK : getResources().getColor(R.color.grey));
        setButtonColors(isNightMode, backButton, txtDanhmuc);
        backButton.setOnClickListener(v -> navigateToMain());

        setupRecyclerView();
    }
    private void navigateToMain() {
        Intent intent = new Intent(SportActivity.this, MainActivity.class);
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
                quizModelList.clear(); // Clear the existing list to avoid duplication

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey(); // Get the quiz ID
                    String title = quizSnapshot.child("title").getValue(String.class);
                    String subtitle = quizSnapshot.child("subtitle").getValue(String.class);
                    String time = quizSnapshot.child("time").getValue(String.class);
                    String category = quizSnapshot.child("category").getValue(String.class);

                    // Kiểm tra xem category có phải là "Sport" không
                    if ("Sport".equals(category)) {
                        List<QuestionModel> questionList = new ArrayList<>();
                        for (DataSnapshot questionSnapshot : quizSnapshot.child("questionList").getChildren()) {
                            String questionId = questionSnapshot.child("questionId").getValue(String.class);
                            String question = questionSnapshot.child("question").getValue(String.class);
                            String correctAnswer = questionSnapshot.child("correct").getValue(String.class);

                            // Get options
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

                // Notify the adapter about the updated list
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
