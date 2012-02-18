package edu.agh.lroza.actors.java;

import java.util.HashSet;
import java.util.Set;

import akka.actor.UntypedActor;
import edu.agh.lroza.common.Id;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NoticesActor extends UntypedActor {
    private final Set<String> titles = new HashSet<>();
    private final Set<Id> ids = new HashSet<>();

    public void onReceive(Object message) {
        if (message instanceof NoticesActorMessage) {
            ((NoticesActorMessage) message).handleMessage(this);
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }

    Set<String> getTitles() {
        return titles;
    }

    Set<Id> getIds() {
        return ids;
    }
}