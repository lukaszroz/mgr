package edu.agh.lroza.actors.java;

import static edu.agh.lroza.actors.java.LoginActor.ValidateToken;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.Channel;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.ProblemException;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NoticesActor extends UntypedActor {
    private Channel loginActor;
    private Set<String> titles = new HashSet<>();
    private Set<Id> ids = new HashSet<>();

    //
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
            instance.loginActor.tell(new ValidateToken(token, instance.getContext().getChannel(),
                    new ValidatedListNoticesId(instance.getContext().getChannel())), instance.getContext());
        }
    }

    private static class ValidatedListNoticesId implements NoticesActorMessage {
        private final Channel originalSender;

        public ValidatedListNoticesId(Channel originalSender) {
            this.originalSender = originalSender;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            originalSender.tell(ImmutableSet.<Id>copyOf(instance.ids));
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
            instance.loginActor.tell(new ValidateToken(token, context.getChannel(),
                    new ValidatedAddNotice(context.getChannel(), this)), context);
        }
    }

    private static class ValidatedAddNotice implements NoticesActorMessage {
        private final Channel originalSender;
        private final AddNotice addNotice;

        public ValidatedAddNotice(Channel originalSender, AddNotice addNotice) {
            this.originalSender = originalSender;
            this.addNotice = addNotice;
        }

        @Override
        public void handleMessage(final NoticesActor instance) {
            if (instance.titles.contains(addNotice.title)) {
                originalSender.tell(new ProblemException("Topic with title '" + addNotice.title + "' already exists"));
            } else {
                instance.titles.add(addNotice.title);
                ActorRef actor = Actors.actorOf(new Creator<Actor>() {
                    public Actor create() {
                        return (Actor) new NoticeActor(instance.getContext(), instance.loginActor, new Notice(addNotice.title, addNotice.message));
                    }
                });
                actor.start();
                ActorId id = new ActorId(actor);
                instance.ids.add(id);
                originalSender.tell(id);
            }
        }
    }

    static class ReserveTitle implements NoticesActorMessage {
        private final String title;
        private final Channel originalSender;
        private final Object message;

        public ReserveTitle(String title, Channel originalSender, Object message) {
            this.title = title;
            this.originalSender = originalSender;
            this.message = message;
        }

        @Override
        public void handleMessage(NoticesActor instance) {
            Set<String> titles = instance.titles;
            if (titles.contains(title)) {
                originalSender.tell(new ProblemException("Topic with title '" + title + "' already exists"));
            } else {
                titles.add(title);
                if (!instance.getContext().tryReply(message)) {
                    titles.remove(title);
                    originalSender.tell(new ProblemException("Notice has been deleted"));
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