package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ReceiveTimeout$;
import akka.japi.Procedure;
import edu.agh.lroza.common.Id;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DeleteNotice extends NoticeActorMessage {
    private final UUID token;

    public DeleteNotice(UUID token, Id id) {
        super(id);
        this.token = token;
    }

    @Override
    public void handleMessage(final NoticeActor instance) {
        instance.getNoticesActor().tell(new RemoveId(new ActorId(instance.getContext())));
        instance.getNoticesActor().tell(new FreeTitle(instance.getNotice().getTitle()));
        instance.getContext().reply(null);
        instance.getContext().setReceiveTimeout(50L);
        instance.become(new Procedure<Object>() {
            public void apply(Object o) {
                if (o instanceof NoticeActorMessage) {
                    ((NoticeActorMessage) o).deletedHandleMessage(instance);
                } else if (o instanceof ReceiveTimeout$) {
                    instance.getContext().stop();
                } else {
                    throw new IllegalArgumentException("Unknown message: " + o.getClass());
                }
            }
        });

    }

    @Override
    public UUID getToken() {
        return token;
    }
}
