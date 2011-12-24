package edu.agh.lroza.javacommon;

public class Notice {
    private final String title;
    private final String message;

    public Notice(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NoticeJ(" + title + ", " + message + ")";
    }
}
