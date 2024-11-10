package Domain;

import java.util.List;
import java.util.Map;

public class QuizModel {
    private String id;
    private String title;
    private String subtitle;
    private String time;
    private String category;
    private List <QuestionModel> questionList;

    public QuizModel(String id, String title, String subtitle, String time, String category, List <QuestionModel> questionList) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.time = time;
        this.category = category;
        this.questionList = questionList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List <QuestionModel> getQuestionList() { return questionList; }
    public void setQuestionList(List <QuestionModel> questionList) { this.questionList = questionList; }
}
