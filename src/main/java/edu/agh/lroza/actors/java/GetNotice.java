package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ActorRef;
import edu.agh.lroza.common.Id;

public class GetNotice extends NoticeActorMessage {

    private final UUID token;
    private final Id id;

    public GetNotice(UUID token, Id id) {
        this.token = token;
        this.id = id;
    }

    @Override
    public void handleMessage(NoticeActor instance) {
        instance.getContext().reply(instance.getNotice());
    }

    @Override
    public UUID getToken() {
        return token;
    }

    @Override
    public ActorRef getActor() {
        if (id instanceof ActorId) {
            return ((ActorId) id).getActor();
        } else {
            return null;
        }
    }
}
