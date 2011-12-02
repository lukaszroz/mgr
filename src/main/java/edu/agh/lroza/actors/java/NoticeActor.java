package edu.agh.lroza.actors.java;

import static edu.agh.lroza.actors.java.NoticesActor.FreeTitle;
import static edu.agh.lroza.actors.java.NoticesActor.RemoveId;
import static edu.agh.lroza.actors.java.NoticesActor.ReserveTitle;
import static edu.agh.lroza.common.UtilsJ.none;
import static edu.agh.lroza.common.UtilsJ.right;

import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import edu.agh.lroza.actors.java.LoginActor.ValidateToken;
import edu.agh.lroza.actors.java.NoticesActor.ActorId;
import edu.agh.lroza.common.Notice;
import edu.agh.lroza.common.NoticeJ;

public class NoticeActor extends UntypedActor {
    private final ActorRef noticesActor;
    private final ActorRef loginActor;
    private Notice notice;

    public static class GetNotice {
        private final UUID token;

        public GetNotice(UUID token) {
            this.token = token;
        }
    }

    public static class ValidatedGetNotice {
        private final UntypedChannel originalSender;

        public ValidatedGetNotice(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }
    }

    public static class UpdateNotice {
        private final UUID token;
        private final String title;
        private final String message;

        public UpdateNotice(UUID token, String title, String message) {
            this.token = token;
            this.title = title;
            this.message = message;
        }
    }

    private static class ValidatedTokenUpdateNotice {
        private final UntypedChannel originalSender;
        private final UpdateNotice updateNotice;

        public ValidatedTokenUpdateNotice(UntypedChannel originalSender, UpdateNotice updateNotice) {
            this.originalSender = originalSender;
            this.updateNotice = updateNotice;
        }
    }

    private static class ReservedTitleUpdateNotice {
        private final UntypedChannel originalSender;
        private final UpdateNotice updateNotice;

        public ReservedTitleUpdateNotice(ValidatedTokenUpdateNotice validatedTokenUpdateNotice) {
            this.originalSender = validatedTokenUpdateNotice.originalSender;
            this.updateNotice = validatedTokenUpdateNotice.updateNotice;
        }
    }

    public static class DeleteNotice {
        private final UUID token;

        public DeleteNotice(UUID token) {
            this.token = token;
        }
    }

    private static class ValidatedDeleteNotice {
        private final UntypedChannel originalSender;

        public ValidatedDeleteNotice(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }
    }

    public NoticeActor(ActorRef noticesActor, ActorRef loginActor, Notice notice) {
        this.noticesActor = noticesActor;
        this.loginActor = loginActor;
        this.notice = notice;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof GetNotice) {
            GetNotice getNotice = (GetNotice) message;
            loginActor.tell(new ValidateToken(getNotice.token, getContext().channel(), false,
                    new ValidatedGetNotice(getContext().channel())), getContext());
        } else if (message instanceof ValidatedGetNotice) {
            ValidatedGetNotice validatedGetNotice = (ValidatedGetNotice) message;
            validatedGetNotice.originalSender.tell(right(notice));
        } else if (message instanceof UpdateNotice) {
            UpdateNotice updateNotice = (UpdateNotice) message;
            loginActor.tell(new ValidateToken(updateNotice.token, getContext().channel(), false,
                    new ValidatedTokenUpdateNotice(getContext().channel(), updateNotice)), getContext());
        } else if (message instanceof ValidatedTokenUpdateNotice) {
            ValidatedTokenUpdateNotice validatedTokenUpdateNotice = (ValidatedTokenUpdateNotice) message;
            UpdateNotice updateNotice = validatedTokenUpdateNotice.updateNotice;
            if (updateNotice.title.equals(notice.title())) {
                notice = new NoticeJ(updateNotice.title, updateNotice.message);
                validatedTokenUpdateNotice.originalSender.tell(right(new ActorId(getContext())));
            } else {
                noticesActor.tell(new ReserveTitle(updateNotice.title, validatedTokenUpdateNotice.originalSender,
                        new ReservedTitleUpdateNotice(validatedTokenUpdateNotice)), getContext());
            }
        } else if (message instanceof ReservedTitleUpdateNotice) {
            ReservedTitleUpdateNotice reservedTitleUpdateNotice = (ReservedTitleUpdateNotice) message;
            UpdateNotice updateNotice = reservedTitleUpdateNotice.updateNotice;
            String oldTitle = notice.title();
            notice = new NoticeJ(updateNotice.title, updateNotice.message);
            noticesActor.tell(new FreeTitle(oldTitle));
            reservedTitleUpdateNotice.originalSender.tell(right(new ActorId(getContext())));
        } else if (message instanceof DeleteNotice) {
            DeleteNotice deleteNotice = (DeleteNotice) message;
            loginActor.tell(new ValidateToken(deleteNotice.token, getContext().channel(), true,
                    new ValidatedDeleteNotice(getContext().channel())), getContext());
        } else if (message instanceof ValidatedDeleteNotice) {
            ValidatedDeleteNotice validatedDeleteNotice = (ValidatedDeleteNotice) message;
            noticesActor.tell(new RemoveId(new ActorId(getContext())));
            noticesActor.tell(new FreeTitle(notice.title()));
            validatedDeleteNotice.originalSender.tell(none());
            getContext().stop();
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}
