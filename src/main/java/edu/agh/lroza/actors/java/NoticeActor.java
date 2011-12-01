package edu.agh.lroza.actors.java;

import java.util.UUID;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import edu.agh.lroza.actors.java.LoginActor.ValidateToken;
import edu.agh.lroza.common.Notice;
import edu.agh.lroza.common.UtilsJ;

public class NoticeActor extends UntypedActor {
    private final ActorRef noticesActor;
    private final ActorRef loginActor;
    private final Notice notice;

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
            validatedGetNotice.originalSender.tell(UtilsJ.right(notice));
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}
