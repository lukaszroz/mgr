package edu.agh.lroza.common;

public class NoticeJ implements Notice {
    private final String title;
    private final String message;
    private final String author;

    public NoticeJ(String title, String message, String author) {
        this.title = title;
        this.message = message;
        this.author = author;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    public String author() {
        return author;
    }
}
