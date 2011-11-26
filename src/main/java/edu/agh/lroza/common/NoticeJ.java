package edu.agh.lroza.common;

public class NoticeJ implements Notice {
    private final String title;
    private final String message;

    public NoticeJ(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "NoticeJ(" + title + ", " + message + ")";
    }
}
