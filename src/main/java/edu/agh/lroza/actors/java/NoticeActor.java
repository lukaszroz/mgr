package edu.agh.lroza.actors.java;

import akka.actor.Channel;
import akka.actor.UntypedActor;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.ProblemException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NoticeActor extends UntypedActor {
    public static final ProblemException NO_SUCH_NOTICE = new ProblemException("There is no such notice");
    private final Channel noticesActor;
    private Notice notice;

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

    Notice getNotice() {
        return notice;
    }

    void setNotice(Notice notice) {
        this.notice = notice;
    }

    Channel getNoticesActor() {
        return noticesActor;
    }
}