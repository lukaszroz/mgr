package edu.agh.lroza.actors.java;

import java.util.UUID;

class RemoveId implements NoticesActorMessage {
    private final ActorId id;

    RemoveId(ActorId id) {
        this.id = id;
    }

    @Override
    public void handleMessage(NoticesActor instance) {
        instance.getIds().remove(id);
    }

    @Override
    public UUID getToken() {
        throw new UnsupportedOperationException("Reserved title doesn't contain token");
    }
}
