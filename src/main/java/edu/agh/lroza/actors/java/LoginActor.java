package edu.agh.lroza.actors.java;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import akka.actor.UntypedActor;
import akka.actor.UntypedChannel;
import edu.agh.lroza.common.Problem;
import edu.agh.lroza.common.UtilsJ;

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
        private final Boolean returnOption;
        private final Object returnMessage;

        ValidateToken(UUID token, UntypedChannel originalSender, Boolean returnOption, Object returnMessage) {
            this.token = token;
            this.originalSender = originalSender;
            this.returnOption = returnOption;
            this.returnMessage = returnMessage;
        }
    }

    private void returnProblem(ValidateToken validateToken, Problem problem) {
        if (validateToken.returnOption) {
            validateToken.originalSender.tell(UtilsJ.some(problem));
        } else {
            validateToken.originalSender.tell(UtilsJ.left(problem));
        }
    }

    public void onReceive(Object message) throws Exception {
        if (message instanceof ValidateToken) {
            ValidateToken validateToken = (ValidateToken) message;
            if (loggedUsers.contains(validateToken.token)) {
                boolean success = getContext().tryReply(validateToken.returnMessage);
                if (!success) {
                    returnProblem(validateToken, UtilsJ.newProblem("Notice has been deleted"));
                }
            } else {
                returnProblem(validateToken, UtilsJ.newProblem("Please log in"));
            }
        } else if (message instanceof Login) {
            Login login = (Login) message;
            if (login.username.equals(login.password)) {
                UUID token = UUID.randomUUID();
                loggedUsers.add(token);
                getContext().reply(UtilsJ.right(token));
            } else {
                getContext().reply(UtilsJ.left(UtilsJ.newProblem("Wrong password")));
            }
        } else if (message instanceof Logout) {
            Logout logout = (Logout) message;
            if (loggedUsers.contains(logout.token)) {
                loggedUsers.remove(logout.token);
                getContext().reply(UtilsJ.none());
            } else {
                getContext().reply(UtilsJ.some(UtilsJ.newProblem("Invalid token")));
            }
        } else {
            throw new IllegalArgumentException("Unknown message: " + message);
        }
    }
}
