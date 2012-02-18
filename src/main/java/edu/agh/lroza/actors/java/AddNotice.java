package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.japi.Creator;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.ProblemException;

public class AddNotice implements NoticesActorMessage {
    private final UUID token;
    private final String title;
    private final String message;

    public AddNotice(UUID token, String title, String message) {
        this.token = token;
        this.title = title;
        this.message = message;
    }

    @Override
    public void handleMessage(final NoticesActor instance) {
        if (instance.getTitles().contains(title)) {
            instance.getContext().reply(new ProblemException("Topic with title '" + title + "' already exists"));
        } else {
            instance.getTitles().add(title);
            ActorRef actor = Actors.actorOf(new Creator<Actor>() {
                public Actor create() {
                    return (Actor) new NoticeActor(instance.getContext(), new Notice(title, message));
                }
            });
            actor.start();
            ActorId id = new ActorId(actor);
            instance.getIds().add(id);
            instance.getContext().reply(id);
        }
    }

    @Override
    public UUID getToken() {
        return token;
    }
}
