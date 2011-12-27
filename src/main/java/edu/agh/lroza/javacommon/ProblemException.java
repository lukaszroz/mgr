package edu.agh.lroza.javacommon;

public class ProblemException extends Exception {
    public ProblemException(String message) {
        super(message);
    }

    public ProblemException(String message, Throwable cause) {
        super(message, cause);
    }
}
