package Domain;

import java.util.List;
import java.util.Map;

public class UserModel {
    private String id;
    private String name;
    private String email;
    private String password;
    private String picture;
    private int score;
    private Map<String, CompleteQuiz> completedQuizzes; // danh sách các quiz đã hoàn thành

    // Constructor (No-argument required for Firebase)
    public UserModel() {}

    public UserModel(String id, String name, String email, String password, String picture, int score, Map<String, CompleteQuiz> completedQuizzes) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.picture = picture;
        this.score = score;
        this.completedQuizzes = completedQuizzes;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Map<String, CompleteQuiz> getCompletedQuizzes() { return completedQuizzes; }
    public void setCompletedQuizzes(Map<String, CompleteQuiz> completedQuizzes) { this.completedQuizzes = completedQuizzes; }
}
