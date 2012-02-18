package edu.agh.lroza.actors.java;

import akka.actor.ActorRef;
import edu.agh.lroza.common.Id;

class ActorId implements Id {
    private final ActorRef actor;

    ActorId(ActorRef actor) {
        this.actor = actor;
    }

    ActorRef getActor() {
        return actor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActorId) {
            ActorId actorId = (ActorId) obj;
            return this.actor.equals(actorId.actor);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return actor.hashCode();
    }
}
