//package edu.agh.lroza.actors;
//
//import akka.actor.ActorRef;
//import akka.actor.Actors;
//import edu.agh.lroza.common.*;
//import scala.Either;
//import scala.Option;
//
//import java.util.UUID;
//
//public class ActorServerJava implements Server {
//    ActorRef loginActor = Actors.actorOf(LoginActorJ.class).start();
//    ActorRef topicsActor = Actors.actorOf(TopicsActorJ.class).start();
//
//
//    public Option<UUID> login(String username, String password) {
//        return (Option<UUID>) loginActor.ask(new Login(username, password)).get();
//    }
//
//    public boolean logout(UUID token) {
//        return (boolean) loginActor.ask(new Logout(token)).get();
//    }
//
//    public Either<Problem, scala.collection.Iterable<String>> listTopics(UUID token) {
//        return (Either<Problem, scala.collection.Iterable<String>>) topicsActor.ask(new ListTopics(token)).get();
//    }
//}
