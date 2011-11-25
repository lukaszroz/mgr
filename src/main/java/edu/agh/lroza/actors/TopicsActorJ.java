//package edu.agh.lroza.actors;
//
//import akka.actor.ActorRef;
//import akka.actor.Actors;
//import akka.actor.UntypedActor;
//import edu.agh.lroza.common.ListTopics;
//import edu.agh.lroza.common.Problem;
//import scala.Either;
//import scala.Left;
//import scala.Right;
//import scala.collection.Iterable;
//import scala.collection.JavaConversions;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class TopicsActorJ extends UntypedActor {
//    ActorRef loginActor = Actors.registry().actorsFor(LoginActorJ.class)[0];
//    List<String> topics = new ArrayList<String>();
//
//    public void onReceive(Object message) throws Exception {
//        if (message instanceof ListTopics) {
//            ListTopics listTopics = (ListTopics) message;
//            getContext().reply(listTopics(listTopics.token()));
//        }
//    }
//
//    private Either<Problem, Iterable<String>> listTopics(UUID token) {
//        if (isLogged(token)) {
//            Iterable<String> iterable = JavaConversions.iterableAsScalaIterable(topics);
//            return new Right<Problem, Iterable<String>>(iterable);
//        } else {
//            return new Left<Problem, Iterable<String>>(new Problem("Please log in"));
//        }
//    }
//
//    private boolean isLogged(UUID token) {
//        return (boolean) loginActor.ask(new IsLogged(token)).get();
//    }
//}
