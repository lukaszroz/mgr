package edu.agh.lroza.actors.java;

import java.util.UUID;

class FreeTitle implements NoticesActorMessage {
    private final String title;

    FreeTitle(String title) {
        this.title = title;
    }

    @Override
    public void handleMessage(NoticesActor instance) {
        instance.getTitles().remove(title);
    }

    @Override
    public UUID getToken() {
        throw new UnsupportedOperationException("Reserved title doesn't contain token");
    }
}
