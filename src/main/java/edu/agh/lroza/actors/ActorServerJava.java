package edu.agh.lroza.actors;

import java.util.Set;
import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.dispatch.ActorCompletableFuture;
import akka.dispatch.FutureTimeoutException;
import akka.japi.Creator;
import edu.agh.lroza.actors.java.AddNotice;
import edu.agh.lroza.actors.java.DeleteNotice;
import edu.agh.lroza.actors.java.GetNotice;
import edu.agh.lroza.actors.java.ListNoticesIds;
import edu.agh.lroza.actors.java.Login;
import edu.agh.lroza.actors.java.LoginActor;
import edu.agh.lroza.actors.java.Logout;
import edu.agh.lroza.actors.java.NoticesActor;
import edu.agh.lroza.actors.java.UpdateNotice;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.common.UtilsS;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ActorServerJava implements NoticeBoardServerJava {
    private ActorRef noticesActor = Actors.actorOf((Class<? extends Actor>) NoticesActor.class);
    private ActorRef loginActor = Actors.actorOf(new Creator<Actor>() {
        public Actor create() {
            return (Actor) new LoginActor(noticesActor);
        }
    });

    public ActorServerJava() {
        loginActor.start();
        noticesActor.start();
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
        Object response = loginActor.ask(new ListNoticesIds(token)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        } else {
            return (Set<Id>) response;
        }
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        Object response = loginActor.ask(new AddNotice(token, title, message)).get();
        if (response instanceof ProblemException) {
            throw (ProblemException) response;
        } else {
            return (Id) response;
        }
    }

    @Override
    public Notice getNotice(UUID token, Id id) throws ProblemException {
        ActorCompletableFuture responseChannel = UtilsS.getFuture();
        loginActor.tell(new GetNotice(token, id), responseChannel);
        try {
            Object response = responseChannel.get();
            if (response instanceof ProblemException) {
                throw (ProblemException) response;
            } else {
                return (Notice) response;
            }
        } catch (FutureTimeoutException e) {
            throw new ProblemException("Timeout occurred", e);
        }
    }

    @Override
    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException {
        ActorCompletableFuture responseChannel = UtilsS.getFuture();
        loginActor.tell(new UpdateNotice(token, id, title, message), responseChannel);
        try {
            Object response = responseChannel.get();
            if (response instanceof ProblemException) {
                throw (ProblemException) response;
            } else {
                return (Id) response;
            }
        } catch (FutureTimeoutException e) {
            throw new ProblemException("Timeout occurred", e);
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        ActorCompletableFuture responseChannel = UtilsS.getFuture();
        loginActor.tell(new DeleteNotice(token, id), responseChannel);
        try {
            Object response = responseChannel.get();
            if (response instanceof ProblemException) {
                throw (ProblemException) response;
            } else {
                return;
            }
        } catch (FutureTimeoutException e) {
            throw new ProblemException("Timeout occurred", e);
        }
    }
}