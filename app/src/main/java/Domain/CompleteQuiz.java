package Domain;

import java.util.List;

public class CompleteQuiz {
    private String quizId;
    private int score; // Điểm số cuối cùng
    private String dateCompleted; // Ngày hoàn thành


    public CompleteQuiz() {}

    public CompleteQuiz(String quizId, int score, String dateCompleted) {
        this.quizId = quizId;
        this.score = score;
        this.dateCompleted = dateCompleted;
    }

    // Getters and Setters
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getDateCompleted() { return dateCompleted; }
    public void setDateCompleted(String dateCompleted) { this.dateCompleted = dateCompleted; }


}
