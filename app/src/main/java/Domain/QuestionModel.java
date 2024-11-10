package Domain;

import java.util.List;

public class QuestionModel {
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    private String questionId;
    private String question;
    private List<String> options;
    private String correct;

    public QuestionModel() {}

    public QuestionModel( String questionId, String question, List<String> options, String correct) {
        this.questionId = questionId;
        this.question = question;
        this.options = options;
        this.correct = correct;
    }



    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrect() { return correct; }
    public void setCorrect(String correct) { this.correct = correct; }
}
