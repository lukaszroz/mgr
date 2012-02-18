package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ActorRef;

abstract class NoticeActorMessage {
    abstract void handleMessage(NoticeActor instance);

    void deletedHandleMessage(NoticeActor instance) {
        instance.getContext().reply(NoticeActor.NO_SUCH_NOTICE);
    }

    abstract UUID getToken();

    abstract ActorRef getActor();
}
