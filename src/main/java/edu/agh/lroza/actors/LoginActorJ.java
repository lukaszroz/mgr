package edu.agh.lroza.actors;

import akka.actor.UntypedActor;
import edu.agh.lroza.common.Login;
import edu.agh.lroza.common.Logout;
import scala.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActorJ extends UntypedActor {
    private Map<UUID, String> loggedUsers = new HashMap<UUID, String>();

    public void onReceive(Object message) throws Exception {
        if (message instanceof IsLogged) {
            IsLogged isLogged = (IsLogged) message;
            getContext().reply(loggedUsers.containsKey(isLogged.token()));
        } else if (message instanceof Login) {
            Login login = (Login) message;
            getContext().reply(login(login.username(), login.password()));
        } else if (message instanceof Logout) {
            Logout logout = (Logout) message;
            getContext().reply(logout(logout.token()));
        }
    }

    private boolean logout(UUID token) {
        String remove = loggedUsers.remove(token);
        if (remove == null)
            return false;
        else
            return true;
    }

    private Option<UUID> login(String username, String password) {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsers.put(token, username);
            return scala.Some.apply(token);
        } else {
            return Option.empty();
        }
    }
}
