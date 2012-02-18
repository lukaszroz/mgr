package edu.agh.lroza.actors.java;

import java.util.Set;
import java.util.UUID;

import akka.actor.Channel;
import edu.agh.lroza.javacommon.ProblemException;

@SuppressWarnings({"unchecked", "rawtypes"})
class ReserveTitle implements NoticesActorMessage {
    private final String title;
    private final Channel originalSender;
    private final Object message;

    ReserveTitle(String title, Channel originalSender, Object message) {
        this.title = title;
        this.originalSender = originalSender;
        this.message = message;
    }

    @Override
    public void handleMessage(NoticesActor instance) {
        Set<String> titles = instance.getTitles();
        if (titles.contains(title)) {
            originalSender.tell(new ProblemException("Topic with title '" + title + "' already exists"));
        } else {
            titles.add(title);
            if (!instance.getContext().tryReply(message)) {
                titles.remove(title);
                originalSender.tell(new ProblemException("Notice has been deleted"));
            }
        }
    }

    @Override
    public UUID getToken() {
        throw new UnsupportedOperationException("Reserved title doesn't contain token");
    }
}
