package edu.agh.lroza.actors.java;

import static edu.agh.lroza.actors.java.NoticesActor.FreeTitle;
import static edu.agh.lroza.actors.java.NoticesActor.RemoveId;
import static edu.agh.lroza.actors.java.NoticesActor.ReserveTitle;

import akka.actor.ActorRef;
import akka.actor.Channel;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import edu.agh.lroza.actors.java.LoginActor.ValidateToken;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.ProblemException;

import com.eaio.uuid.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NoticeActor extends UntypedActor {
    private static final ProblemException NO_SUCH_NOTICE = new ProblemException("There is no such notice");
    private final Channel noticesActor;
    private final Channel loginActor;
    private Notice notice;

    private static interface NoticeActorMessage {
        void handleMessage(NoticeActor instance);

        void deletedHandleMessage(NoticeActor instance);
    }

    public static class GetNotice implements NoticeActorMessage {

        private final UUID token;

        public GetNotice(UUID token) {
            this.token = token;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            ActorRef context = instance.getContext();
            instance.loginActor.tell(new ValidateToken(token, context.channel(),
                    new ValidatedGetNotice(context.channel())), context);
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            instance.getContext().reply(NO_SUCH_NOTICE);
        }
    }

    public static class ValidatedGetNotice implements NoticeActorMessage {
        private final Channel originalSender;

        public ValidatedGetNotice(Channel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            originalSender.tell(instance.notice);
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            originalSender.tell(NO_SUCH_NOTICE);
        }
    }

    public static class UpdateNotice implements NoticeActorMessage {
        private final UUID token;
        private final String title;
        private final String message;

        public UpdateNotice(UUID token, String title, String message) {
            this.token = token;
            this.title = title;
            this.message = message;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            ActorRef context = instance.getContext();
            instance.loginActor.tell(new ValidateToken(token, context.channel(),
                    new ValidatedTokenUpdateNotice(context.channel(), this)), context);
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            instance.getContext().reply(NO_SUCH_NOTICE);
        }
    }

    private static class ValidatedTokenUpdateNotice implements NoticeActorMessage {
        private final Channel originalSender;
        private final UpdateNotice updateNotice;

        public ValidatedTokenUpdateNotice(Channel originalSender, UpdateNotice updateNotice) {
            this.originalSender = originalSender;
            this.updateNotice = updateNotice;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            if (updateNotice.title.equals(instance.notice.getTitle())) {
                instance.notice = new Notice(updateNotice.title, updateNotice.message);
                originalSender.tell(new ActorId(instance.getContext()));
            } else {
                instance.noticesActor.tell(new ReserveTitle(updateNotice.title, originalSender,
                        new ReservedTitleUpdateNotice(this)), instance.getContext());
            }
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            originalSender.tell(NO_SUCH_NOTICE);
        }
    }

    private static class ReservedTitleUpdateNotice implements NoticeActorMessage {
        private final Channel originalSender;
        private final UpdateNotice updateNotice;

        public ReservedTitleUpdateNotice(ValidatedTokenUpdateNotice validatedTokenUpdateNotice) {
            this.originalSender = validatedTokenUpdateNotice.originalSender;
            this.updateNotice = validatedTokenUpdateNotice.updateNotice;
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
    }

    public static class DeleteNotice implements NoticeActorMessage {
        private final UUID token;

        public DeleteNotice(UUID token) {
            this.token = token;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            ActorRef context = instance.getContext();
            instance.loginActor.tell(new ValidateToken(token, context.channel(),
                    new ValidatedDeleteNotice(context.channel())), context);

        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            instance.getContext().reply(new ProblemException("There is no such notice"));
        }
    }

    private static class ValidatedDeleteNotice implements NoticeActorMessage {
        private final Channel originalSender;

        public ValidatedDeleteNotice(Channel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(final NoticeActor instance) {
            instance.noticesActor.tell(new RemoveId(new ActorId(instance.getContext())));
            instance.noticesActor.tell(new FreeTitle(instance.notice.getTitle()));
            originalSender.tell(null);
            instance.getContext().setReceiveTimeout(50L);
            instance.become(new Procedure<Object>() {
                public void apply(Object o) {
                    if (o instanceof NoticeActorMessage) {
                        ((NoticeActorMessage) o).deletedHandleMessage(instance);
                    } else if (o instanceof ReceiveTimeout) {
                        instance.getContext().stop();
                    } else {
                        throw new IllegalArgumentException("Unknown message: " + o);
                    }
                }
            });
        }

        @Override
        public void deletedHandleMessage(NoticeActor instance) {
            originalSender.tell(NO_SUCH_NOTICE);
        }
    }

    public NoticeActor(Channel noticesActor, Channel loginActor, Notice notice) {
        this.noticesActor = noticesActor;
        this.loginActor = loginActor;
        this.notice = notice;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof NoticeActorMessage) {
            ((NoticeActorMessage) message).handleMessage(this);
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}