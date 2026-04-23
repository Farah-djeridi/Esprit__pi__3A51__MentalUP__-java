package models;

public class TipData {
    private String title;
    private String text;
    private String exercise;
    private String type;
    private String source;
    private String url;
    private String keywordUsed;

    public TipData() {
    }

    public TipData(String title, String text, String exercise, String type,
                   String source, String url, String keywordUsed) {
        this.title = title;
        this.text = text;
        this.exercise = exercise;
        this.type = type;
        this.source = source;
        this.url = url;
        this.keywordUsed = keywordUsed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKeywordUsed() {
        return keywordUsed;
    }

    public void setKeywordUsed(String keywordUsed) {
        this.keywordUsed = keywordUsed;
    }
}
