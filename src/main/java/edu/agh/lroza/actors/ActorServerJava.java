package edu.agh.lroza.actors;

import java.util.Set;
import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.dispatch.ActorCompletableFuture;
import akka.japi.Creator;
import edu.agh.lroza.actors.java.LoginActor;
import edu.agh.lroza.actors.java.LoginActor.Login;
import edu.agh.lroza.actors.java.LoginActor.Logout;
import edu.agh.lroza.actors.java.NoticeActor.DeleteNotice;
import edu.agh.lroza.actors.java.NoticeActor.GetNotice;
import edu.agh.lroza.actors.java.NoticeActor.UpdateNotice;
import edu.agh.lroza.actors.java.NoticesActor;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.actors.java.NoticesActor.AddNotice;
import edu.agh.lroza.actors.java.NoticesActor.ListNoticesIds;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.common.UtilsS;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;
import scala.reflect.Manifest;
import scala.reflect.Manifest$;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ActorServerJava implements NoticeBoardServerJava {
    private ActorRef loginActor = Actors.actorOf(LoginActor.class);
    private ActorRef noticesActor = Actors.actorOf(new Creator<Actor>() {
        public Actor create() {
            return (Actor) new NoticesActor(loginActor);
        }
    });

    public ActorServerJava() {
        loginActor.start();
        noticesActor.start();
    }

    private static Manifest getManifest(Class<?> clazz) {
        return Manifest$.MODULE$.classType(clazz);
    }

    public UUID login(String username, String password) throws ProblemException {
        Object response = loginActor.ask(new Login(username, password)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        } else {
            return (UUID) response;
        }
    }

    public void logout(UUID token) throws ProblemException {
        Object response = loginActor.ask(new Logout(token)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        }
    }

    @Override
    public Set<Id> listNoticesIds(UUID token) throws ProblemException {
        Object response = noticesActor.ask(new ListNoticesIds(token)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        } else {
            return (Set<Id>) response;
        }
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        Object response = noticesActor.ask(new AddNotice(token, title, message)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        } else {
            return (Id) response;
        }
    }

    @Override
    public Notice getNotice(UUID token, Id id) throws ProblemException {
        if (id instanceof ActorId) {
            ActorCompletableFuture responseChannel = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new GetNotice(token), responseChannel)) {
                Object response = responseChannel.get();
                if (response instanceof ProblemException) {
                    throw (ProblemException) response;
                } else {
                    return (Notice) response;
                }
            } else {
                throw new ProblemException("There is no such notice '" + id + "'");
            }
        } else {
            throw new ProblemException("There is no such notice '" + id + "'");
        }
    }

    @Override
    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException {
        if (id instanceof ActorId) {
            ActorCompletableFuture responseChannel = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new UpdateNotice(token, title, message), responseChannel)) {
                Object response = responseChannel.get();
                if (response instanceof ProblemException) {
                    throw (ProblemException) response;
                } else {
                    return (Id) response;
                }
            } else {
                throw new ProblemException("There is no such notice '" + id + "'");
            }
        } else {
            throw new ProblemException("There is no such notice '" + id + "'");
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        if (id instanceof ActorId) {
            ActorCompletableFuture responseChannel = UtilsS.getFuture();
            if (((ActorId) id).getActor().tryTell(new DeleteNotice(token), responseChannel)) {
                Object response = responseChannel.get();
                if (response instanceof ProblemException) {
                    throw (ProblemException) response;
                }
            } else {
                throw new ProblemException("There is no such notice '" + id + "'");
            }
        } else {
            throw new ProblemException("There is no such notice '" + id + "'");
        }
    }
}