package edu.agh.lroza.actors.java;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import edu.agh.lroza.javacommon.ProblemException;

public class LoginActor extends UntypedActor {
    private Set<UUID> loggedUsers = new HashSet<>();

    public static class Login {
        private final String username;
        private final String password;

        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class Logout {
        private final UUID token;

        public Logout(UUID token) {
            this.token = token;
        }
    }

    static class ValidateToken {
        private final UUID token;
        private final UntypedChannel originalSender;
        private final Object returnMessage;

        ValidateToken(UUID token, UntypedChannel originalSender, Object returnMessage) {
            this.token = token;
            this.originalSender = originalSender;
            this.returnMessage = returnMessage;
        }
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof ValidateToken) {
            ValidateToken validateToken = (ValidateToken) message;
            if (loggedUsers.contains(validateToken.token)) {
                boolean success = getContext().tryReply(validateToken.returnMessage);
                if (!success) {
                    validateToken.originalSender.tell(new ProblemException("Notice has been deleted"));
                }
            } else {
                validateToken.originalSender.tell(new ProblemException("Please log in"));
            }
        } else if (message instanceof Login) {
            Login login = (Login) message;
            if (login.username.equals(login.password)) {
                UUID token = UUID.randomUUID();
                loggedUsers.add(token);
                getContext().reply(token);
            } else {
                getContext().reply(new ProblemException("Wrong password"));
            }
        } else if (message instanceof Logout) {
            Logout logout = (Logout) message;
            if (loggedUsers.contains(logout.token)) {
                loggedUsers.remove(logout.token);
                getContext().reply(null);
            } else {
                getContext().reply(new ProblemException("Invalid token"));
            }
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}