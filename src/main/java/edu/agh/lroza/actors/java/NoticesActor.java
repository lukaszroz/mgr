package edu.agh.lroza.actors.java;

import static edu.agh.lroza.common.UtilsJ.left;
import static edu.agh.lroza.common.UtilsJ.newProblem;
import static edu.agh.lroza.common.UtilsJ.right;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import akka.japi.Creator;
import edu.agh.lroza.actors.java.LoginActor.ValidateToken;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.common.NoticeJ;
import edu.agh.lroza.common.UtilsJ;
import scala.collection.JavaConversions;

import com.google.common.collect.ImmutableSet;

public class NoticesActor extends UntypedActor {
    private ActorRef loginActor;
    private Set<String> titles = new HashSet<>();
    private Set<Id> ids = new HashSet<>();

    public static class ActorId implements Id {
        private final ActorRef actor;

        ActorId(ActorRef actor) {
            this.actor = actor;
        }

        public ActorRef getActor() {
            return actor;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ActorId) {
                ActorId actorId = (ActorId) obj;
                return this.actor.equals(actorId.actor);
            } else {
                return false;
            }
        }
    }

    public static class ListNoticesIds {
        private final UUID token;

        public ListNoticesIds(UUID token) {
            this.token = token;
        }
    }

    public static class AddNotice {
        private final UUID token;
        private final String title;
        private final String message;

        public AddNotice(UUID token, String title, String message) {
            this.token = token;
            this.title = title;
            this.message = message;
        }
    }

    private static class ValidatedListNoticesId {
        private final UntypedChannel originalSender;

        public ValidatedListNoticesId(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }
    }

    private static class ValidatedAddNotice {
        private final UntypedChannel originalSender;
        private final AddNotice addNotice;

        public ValidatedAddNotice(UntypedChannel originalSender, AddNotice addNotice) {
            this.originalSender = originalSender;
            this.addNotice = addNotice;
        }
    }

    static class ReserveTitle {
        private final String title;
        private final UntypedChannel originalSender;
        private final Object message;

        public ReserveTitle(String title, UntypedChannel originalSender, Object message) {
            this.title = title;
            this.originalSender = originalSender;
            this.message = message;
        }
    }

    static class FreeTitle {
        private final String title;

        public FreeTitle(String title) {
            this.title = title;
        }
    }

    public NoticesActor(ActorRef loginActor) {
        this.loginActor = loginActor;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof ListNoticesIds) {
            ListNoticesIds listNoticesIds = (ListNoticesIds) message;
            loginActor.tell(new ValidateToken(listNoticesIds.token, getContext().getChannel(), false,
                    new ValidatedListNoticesId(getContext().getChannel())), getContext());
        } else if (message instanceof ValidatedListNoticesId) {
            scala.collection.Set<Id> scalaSet = JavaConversions.asScalaSet(ImmutableSet.<Id>copyOf(ids));
            ((ValidatedListNoticesId) message).originalSender.tell(right(scalaSet));
        } else if (message instanceof AddNotice) {
            AddNotice addNotice = (AddNotice) message;
            loginActor.tell(new ValidateToken(addNotice.token, getContext().getChannel(), false,
                    new ValidatedAddNotice(getContext().getChannel(), addNotice)), getContext());
        } else if (message instanceof ValidatedAddNotice) {
            final ValidatedAddNotice validatedAddNotice = (ValidatedAddNotice) message;
            if (titles.contains(validatedAddNotice.addNotice.title)) {
                validatedAddNotice.originalSender.tell(left(newProblem("Topic with title '" + validatedAddNotice.addNotice.title + "' already exists")));
            } else {
                titles.add(validatedAddNotice.addNotice.title);
                ActorRef actor = Actors.actorOf(new Creator<Actor>() {
                    public Actor create() {
                        return new NoticeActor(getContext(), loginActor, new NoticeJ(validatedAddNotice.addNotice.title, validatedAddNotice.addNotice.message));
                    }
                });
                actor.start();
                ActorId id = new ActorId(actor);
                ids.add(id);
                validatedAddNotice.originalSender.tell(UtilsJ.right(id));
            }
        } else if (message instanceof ReserveTitle) {
            ReserveTitle reserveTitle = (ReserveTitle) message;
            if (titles.contains(reserveTitle.title)) {
                reserveTitle.originalSender.tell(left(newProblem("Topic with title '" + reserveTitle.title + "' already exists")));
            } else {
                titles.add(reserveTitle.title);
                if (!getContext().tryReply(reserveTitle.message)) {
                    titles.remove(reserveTitle.title);
                    reserveTitle.originalSender.tell(left(newProblem("Notice has been deleted")));
                }
            }
        } else if (message instanceof FreeTitle) {
            FreeTitle freeTitle = (FreeTitle) message;
            titles.remove(freeTitle.title);
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}
