package edu.agh.lroza.actors.java;

import static edu.agh.lroza.actors.java.NoticesActor.FreeTitle;
import static edu.agh.lroza.actors.java.NoticesActor.RemoveId;
import static edu.agh.lroza.actors.java.NoticesActor.ReserveTitle;

import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import edu.agh.lroza.actors.java.LoginActor.ValidateToken;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.javacommon.Notice;

public class NoticeActor extends UntypedActor {
    private final ActorRef noticesActor;
    private final ActorRef loginActor;
    private Notice notice;

    private static interface NoticeActorMessage {
        void handleMessage(NoticeActor instance);
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
    }

    public static class ValidatedGetNotice implements NoticeActorMessage {
        private final UntypedChannel originalSender;

        public ValidatedGetNotice(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            originalSender.tell(instance.notice);
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
    }

    private static class ValidatedTokenUpdateNotice implements NoticeActorMessage {
        private final UntypedChannel originalSender;
        private final UpdateNotice updateNotice;

        public ValidatedTokenUpdateNotice(UntypedChannel originalSender, UpdateNotice updateNotice) {
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
    }

    private static class ReservedTitleUpdateNotice implements NoticeActorMessage {
        private final UntypedChannel originalSender;
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
    }

    private static class ValidatedDeleteNotice implements NoticeActorMessage {
        private final UntypedChannel originalSender;

        public ValidatedDeleteNotice(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(NoticeActor instance) {
            instance.noticesActor.tell(new RemoveId(new ActorId(instance.getContext())));
            instance.noticesActor.tell(new FreeTitle(instance.notice.getTitle()));
            originalSender.tell(null);
            instance.getContext().stop();
        }
    }

    public NoticeActor(ActorRef noticesActor, ActorRef loginActor, Notice notice) {
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