package edu.agh.lroza.actors.java;

import java.util.UUID;

interface NoticesActorMessage {
    void handleMessage(NoticesActor instance);

    UUID getToken();
}
