package edu.agh.lroza.common;

import scala.Left;
import scala.Option;
import scala.Right;
import scala.Some;

public class UtilsJ {
    public static Problem newProblem(String message) {
        return new ProblemJ(message);
    }

    public static <Problem, U> Right<Problem, U> right(U right) {
        return new Right<>(right);
    }

    public static <Problem, U> Left<Problem, U> left(Problem left) {
        return new Left<>(left);
    }

    public static <T> Option<T> some(T t) {
        return new Some<>(t);
    }

    public static <T> Option<T> none() {
        return Option.empty();
    }
}
