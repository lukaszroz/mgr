package edu.agh.lroza.actors.java;

import java.util.UUID;

import edu.agh.lroza.common.Id;

import com.google.common.collect.ImmutableSet;

public class ListNoticesIds implements NoticesActorMessage {
    private final UUID token;

    public ListNoticesIds(UUID token) {
        this.token = token;
    }

    @Override
    public void handleMessage(NoticesActor instance) {
        instance.getContext().reply(ImmutableSet.<Id>copyOf(instance.getIds()));
    }

    @Override
    public UUID getToken() {
        return token;
    }
}
