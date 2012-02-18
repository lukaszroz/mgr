package edu.agh.lroza.actors.java;

import java.util.UUID;

public class Logout {
    private final UUID token;

    public Logout(UUID token) {
        this.token = token;
    }

    public UUID getToken() {
        return token;
    }
}
