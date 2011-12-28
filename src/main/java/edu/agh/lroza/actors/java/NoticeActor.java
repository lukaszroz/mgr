package edu.agh.lroza.actors.java;

import static edu.agh.lroza.actors.java.NoticesActor.FreeTitle;
import static edu.agh.lroza.actors.java.NoticesActor.RemoveId;
import static edu.agh.lroza.actors.java.NoticesActor.ReserveTitle;

import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.Channel;
import akka.actor.ReceiveTimeout$;
import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import akka.japi.Procedure;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.ProblemException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NoticeActor extends UntypedActor {
    private static final ProblemException NO_SUCH_NOTICE = new ProblemException("There is no such notice");
    private final Channel noticesActor;
    private Notice notice;

    static abstract class NoticeActorMessage {
        abstract void handleMessage(NoticeActor instance);

        public void deletedHandleMessage(NoticeActor instance) {
            instance.getContext().reply(NO_SUCH_NOTICE);
        }

        abstract UUID getToken();

        abstract ActorRef getActor();
    }

    public static class GetNotice extends NoticeActorMessage {

        private final UUID token;
        private final Id id;

        public GetNotice(UUID token, Id id) {
            this.token = token;
            this.id = id;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            instance.getContext().reply(instance.notice);
        }

        @Override
        public UUID getToken() {
            return token;
        }

        @Override
        public ActorRef getActor() {
            if (id instanceof ActorId) {
                return ((ActorId) id).getActor();
            } else {
                return null;
            }
        }
    }

    public static class UpdateNotice extends NoticeActorMessage {
        private final UUID token;
        private final String title;
        private final String message;
        private final Id id;

        public UpdateNotice(UUID token, Id id, String title, String message) {
            this.token = token;
            this.title = title;
            this.message = message;
            this.id = id;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            if (title.equals(instance.notice.getTitle())) {
                instance.notice = new Notice(title, message);
                instance.getContext().reply(new ActorId(instance.getContext()));
            } else {
                UntypedChannel originalSender = instance.getContext().getChannel();
                instance.noticesActor.tell(new ReserveTitle(title, originalSender,
                        new ReservedTitleUpdateNotice(originalSender, this)), instance.getContext());
            }
        }

        @Override
        public UUID getToken() {
            return token;
        }

        @Override
        public ActorRef getActor() {
            if (id instanceof ActorId) {
                return ((ActorId) id).getActor();
            } else {
                return null;
            }
        }
    }

    private static class ReservedTitleUpdateNotice extends NoticeActorMessage {
        private final Channel originalSender;
        private final UpdateNotice updateNotice;

        public ReservedTitleUpdateNotice(Channel originalSender, UpdateNotice updateNotice) {
            this.originalSender = originalSender;
            this.updateNotice = updateNotice;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            String oldTitle = instance.notice.getTitle();
            instance.notice = new Notice(updateNotice.title, updateNotice.message);
            instance.noticesActor.tell(new FreeTitle(oldTitle));
            originalSender.tell(new ActorId(instance.getContext()));
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            instance.noticesActor.tell(new FreeTitle(instance.notice.getTitle()));
            originalSender.tell(NO_SUCH_NOTICE);
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

    public static class DeleteNotice extends NoticeActorMessage {
        private final UUID token;
        private final Id id;

        public DeleteNotice(UUID token, Id id) {
            this.token = token;
            this.id = id;
        }

        @Override
        public void handleMessage(final NoticeActor instance) {
            instance.noticesActor.tell(new RemoveId(new ActorId(instance.getContext())));
            instance.noticesActor.tell(new FreeTitle(instance.notice.getTitle()));
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

        @Override
        public ActorRef getActor() {
            if (id instanceof ActorId) {
                return ((ActorId) id).getActor();
            } else {
                return null;
            }
        }
    }

    public NoticeActor(Channel noticesActor, Notice notice) {
        this.noticesActor = noticesActor;
        this.notice = notice;
    }

    public void onReceive(Object message) {
        if (message instanceof NoticeActorMessage) {
            ((NoticeActorMessage) message).handleMessage(this);
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}