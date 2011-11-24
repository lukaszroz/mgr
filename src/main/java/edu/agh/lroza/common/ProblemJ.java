package edu.agh.lroza.common;

public class ProblemJ implements Problem {
    private final String message;

    public ProblemJ(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
