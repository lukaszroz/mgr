package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ActorRef;
import edu.agh.lroza.common.Id;

abstract class NoticeActorMessage {
    private final Id id;

    protected NoticeActorMessage(Id id) {
        this.id = id;
    }

    void deletedHandleMessage(NoticeActor instance) {
        instance.getContext().reply(NoticeActor.NO_SUCH_NOTICE);
    }

    public ActorRef getActor() {
        if (id instanceof ActorId) {
            return ((ActorId) id).getActor();
        } else {
            return null;
        }
    }

    abstract void handleMessage(NoticeActor instance);

    abstract UUID getToken();
}
