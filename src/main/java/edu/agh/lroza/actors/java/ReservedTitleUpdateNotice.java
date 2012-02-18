package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.Channel;
import edu.agh.lroza.javacommon.Notice;

@SuppressWarnings({"unchecked", "rawtypes"})
class ReservedTitleUpdateNotice extends NoticeActorMessage {
    private final Channel originalSender;
    private final UpdateNotice updateNotice;

    ReservedTitleUpdateNotice(Channel originalSender, UpdateNotice updateNotice) {
        this.originalSender = originalSender;
        this.updateNotice = updateNotice;
    }

    @Override
    public void handleMessage(NoticeActor instance) {
        String oldTitle = instance.getNotice().getTitle();
        instance.setNotice(new Notice(updateNotice.getTitle(), updateNotice.getMessage()));
        instance.getNoticesActor().tell(new FreeTitle(oldTitle));
        originalSender.tell(new ActorId(instance.getContext()));
    }

    @Override
    public void deletedHandleMessage(NoticeActor instance) {
        instance.getNoticesActor().tell(new FreeTitle(instance.getNotice().getTitle()));
        originalSender.tell(NoticeActor.NO_SUCH_NOTICE);
    }

    @Override
    public UUID getToken() {
        throw new UnsupportedOperationException("Reserved title doesn't contain token");
    }

    @Override
    public ActorRef getActor() {
        throw new UnsupportedOperationException("Reserved title doesn't contain id");
    }
}
