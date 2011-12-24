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

        @Override
        public int hashCode() {
            return actor.hashCode();
        }
    }

    private static interface NoticesActorMessage {
        void handleMessage(NoticesActor instance);
    }

    public static class ListNoticesIds implements NoticesActorMessage {
        private final UUID token;

        public ListNoticesIds(UUID token) {
            this.token = token;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            instance.loginActor.tell(new ValidateToken(token, instance.getContext().getChannel(), false,
                    new ValidatedListNoticesId(instance.getContext().getChannel())), instance.getContext());
        }
    }

    public static class AddNotice implements NoticesActorMessage {
        private final UUID token;
        private final String title;
        private final String message;

        public AddNotice(UUID token, String title, String message) {
            this.token = token;
            this.title = title;
            this.message = message;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            ActorRef context = instance.getContext();
            instance.loginActor.tell(new ValidateToken(token, context.getChannel(), false,
                    new ValidatedAddNotice(context.getChannel(), this)), context);
        }
    }

    private static class ValidatedListNoticesId implements NoticesActorMessage {
        private final UntypedChannel originalSender;

        public ValidatedListNoticesId(UntypedChannel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            scala.collection.Set<Id> scalaSet = JavaConversions.asScalaSet(ImmutableSet.<Id>copyOf(instance.ids));
            originalSender.tell(right(scalaSet));
        }
    }

    private static class ValidatedAddNotice implements NoticesActorMessage {
        private final UntypedChannel originalSender;
        private final AddNotice addNotice;

        public ValidatedAddNotice(UntypedChannel originalSender, AddNotice addNotice) {
            this.originalSender = originalSender;
            this.addNotice = addNotice;
        }

        @Override
        public void handleMessage(final NoticesActor instance) {
            if (instance.titles.contains(addNotice.title)) {
                originalSender.tell(left(newProblem("Topic with title '" + addNotice.title + "' already exists")));
            } else {
                instance.titles.add(addNotice.title);
                ActorRef actor = Actors.actorOf(new Creator<Actor>() {
                    public Actor create() {
                        return new NoticeActor(instance.getContext(), instance.loginActor, new NoticeJ(addNotice.title, addNotice.message));
                    }
                });
                actor.start();
                ActorId id = new ActorId(actor);
                instance.ids.add(id);
                originalSender.tell(UtilsJ.right(id));
            }
        }
    }

    static class ReserveTitle implements NoticesActorMessage {
        private final String title;
        private final UntypedChannel originalSender;
        private final Object message;

        public ReserveTitle(String title, UntypedChannel originalSender, Object message) {
            this.title = title;
            this.originalSender = originalSender;
            this.message = message;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            Set<String> titles = instance.titles;
            if (titles.contains(title)) {
                originalSender.tell(left(newProblem("Topic with title '" + title + "' already exists")));
            } else {
                titles.add(title);
                if (!instance.getContext().tryReply(message)) {
                    titles.remove(title);
                    originalSender.tell(left(newProblem("Notice has been deleted")));
                }
            }
        }
    }

    static class FreeTitle implements NoticesActorMessage {
        private final String title;

        public FreeTitle(String title) {
            this.title = title;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            instance.titles.remove(title);
        }
    }

    static class RemoveId implements NoticesActorMessage {
        private final ActorId id;

        RemoveId(ActorId id) {
            this.id = id;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            instance.ids.remove(id);
        }
    }

    public NoticesActor(ActorRef loginActor) {
        this.loginActor = loginActor;
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof NoticesActorMessage) {
            ((NoticesActorMessage) message).handleMessage(this);
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}