package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.UntypedChannel;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;

@SuppressWarnings({"unchecked", "rawtypes"})
public class UpdateNotice extends NoticeActorMessage {
    private final UUID token;
    private final String title;
    private final String message;

    public UpdateNotice(UUID token, Id id, String title, String message) {
        super(id);
        this.token = token;
        this.title = title;
        this.message = message;
    }

    @Override
    public void handleMessage(NoticeActor instance) {
        if (title.equals(instance.getNotice().getTitle())) {
            instance.setNotice(new Notice(title, message));
            instance.getContext().reply(new ActorId(instance.getContext()));
        } else {
            UntypedChannel originalSender = instance.getContext().getChannel();
            instance.getNoticesActor().tell(new ReserveTitle(title, originalSender,
                    new TitleReservedUpdateNotice(originalSender, this)), instance.getContext());
        }
    }

    @Override
    public UUID getToken() {
        return token;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
