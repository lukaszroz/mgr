//package edu.agh.lroza.synchronize;
//
//import edu.agh.lroza.common.Problem;
//import edu.agh.lroza.common.Server;
//import scala.Either;
//import scala.Left;
//import scala.Option;
//import scala.Right;
//import scala.collection.Iterable;
//import scala.collection.JavaConversions;
//
//import java.util.*;
//
//public class SynchronizedServerJava implements Server {
//    private Map<UUID, String> loggedUsers = Collections.synchronizedMap(new HashMap<UUID, String>());
//
//
//    public Option<UUID> login(String username, String password) {
//        if (username.equals(password)) {
//            UUID token = UUID.randomUUID();
//            loggedUsers.put(token, username);
//            return scala.Some.apply(token);
//        } else {
//            return Option.empty();
//        }
//    }
//
//    public boolean logout(UUID token) {
//        String remove = loggedUsers.remove(token);
//        if (remove == null)
//            return false;
//        else
//            return true;
//    }
//
//    public Either<Problem, Iterable<String>> listTopics(UUID token) {
//        if (loggedUsers.containsKey(token)) {
//            Iterable<String> iterable = JavaConversions.iterableAsScalaIterable(new ArrayList<String>());
//            return new Right<Problem, Iterable<String>>(iterable);
//        } else {
//            return new Left<Problem, Iterable<String>>(new Problem("Please log in"));
//        }
//    }
//}
