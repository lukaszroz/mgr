package edu.agh.lroza.actors;

import static edu.agh.lroza.actors.java.NoticesActor.AddNotice;
import static edu.agh.lroza.actors.java.NoticesActor.ListNoticesIds;
import static edu.agh.lroza.common.UtilsJ.left;
import static edu.agh.lroza.common.UtilsJ.newProblem;
import static edu.agh.lroza.common.UtilsJ.some;

import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.dispatch.ActorCompletableFuture;
import akka.dispatch.Future;
import akka.japi.Creator;
import edu.agh.lroza.actors.java.LoginActor;
import edu.agh.lroza.actors.java.LoginActor.Login;
import edu.agh.lroza.actors.java.LoginActor.Logout;
import edu.agh.lroza.actors.java.NoticeActor.GetNotice;
import edu.agh.lroza.actors.java.NoticesActor;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.actors.scala.NoticeActor;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.common.Notice;
import edu.agh.lroza.common.NoticeBoardServer;
import edu.agh.lroza.common.Problem;
import edu.agh.lroza.common.UtilsS;
import scala.Either;
import scala.Option;
import scala.collection.Set;
import scala.reflect.Manifest;
import scala.reflect.Manifest$;

public class ActorServerJava implements NoticeBoardServer {
    ActorRef loginActor = Actors.actorOf(LoginActor.class);
    ActorRef noticesActor = Actors.actorOf(new Creator<Actor>() {
        public Actor create() {
            return new NoticesActor(loginActor);
        }
    });

    public ActorServerJava() {
        loginActor.start();
        noticesActor.start();
    }

    private static Manifest getManifest(Class<?> clazz) {
        return Manifest$.MODULE$.classType(clazz);
    }

    public Either<Problem, UUID> login(String username, String password) {
        Future response = loginActor.ask(new Login(username, password));
        Option<Either<Problem, UUID>> option = response.as(getManifest(Either.class));
        if (option.isDefined()) {
            return option.get();
        } else {
            return left(newProblem("Timeout occured"));
        }
    }

    public Option<Problem> logout(UUID token) {
        Future response = loginActor.ask(new Logout(token));
        Option<Option<Problem>> option = response.as(getManifest(Option.class));
        if (option.isDefined()) {
            return option.get();
        } else {
            return some(newProblem("Timeout occured"));
        }
    }

    public Either<Problem, Set<Id>> listNoticesIds(UUID token) {
        Future response = noticesActor.ask(new ListNoticesIds(token));
        Option<Either<Problem, Set<Id>>> option = response.as(getManifest(Either.class));
        if (option.isDefined()) {
            return option.get();
        } else {
            return left(newProblem("Timeout occured"));
        }
    }

    @Override
    public Either<Problem, Id> addNotice(UUID token, String title, String message) {
        Future response = noticesActor.ask(new AddNotice(token, title, message));
        Option<Either<Problem, Id>> option = response.as(getManifest(Either.class));
        if (option.isDefined()) {
            return option.get();
        } else {
            return left(newProblem("Timeout occured"));
        }
    }

    @Override
    public Either<Problem, Notice> getNotice(UUID token, Id id) {
        if (id instanceof ActorId) {
            ActorCompletableFuture response = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new GetNotice(token), response)) {
                Option<Either<Problem, Notice>> option = response.as(getManifest(Either.class));
                if (option.isDefined()) {
                    return option.get();
                } else {
                    return left(newProblem("Timeout occured"));
                }
            } else {
                return left(newProblem("There is no such notice '" + id + "'"));
            }
        } else {
            return left(newProblem("There is no such notice '" + id + "'"));
        }
    }

    @Override
    public Either<Problem, Id> updateNotice(UUID token, Id id, String title, String message) {
        if (id instanceof ActorId) {
            ActorCompletableFuture response = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new NoticeActor.UpdateNotice(token, title, message), response)) {
                Option<Either<Problem, Id>> option = response.as(getManifest(Either.class));
                if (option.isDefined()) {
                    return option.get();
                } else {
                    return left(newProblem("Timeout occured"));
                }
            } else {
                return left(newProblem("There is no such notice '" + id + "'"));
            }
        } else {
            return left(newProblem("There is no such notice '" + id + "'"));
        }
    }

    @Override
    public Option<Problem> deleteNotice(UUID token, Id id) {
        if (id instanceof ActorId) {
            ActorCompletableFuture response = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new NoticeActor.DeleteNotice(token), response)) {
                Option<Option<Problem>> option = response.as(getManifest(Option.class));
                if (option.isDefined()) {
                    return option.get();
                } else {
                    return some(newProblem("Timeout occured"));
                }
            } else {
                return some(newProblem("There is no such notice '" + id + "'"));
            }
        } else {
            return some(newProblem("There is no such notice '" + id + "'"));
        }
    }


}