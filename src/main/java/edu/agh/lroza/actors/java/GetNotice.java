package edu.agh.lroza.actors.java;

import java.util.UUID;

import edu.agh.lroza.common.Id;

public class GetNotice extends NoticeActorMessage {

    private final UUID token;

    public GetNotice(UUID token, Id id) {
        super(id);
        this.token = token;
    }

    @Override
    public void handleMessage(NoticeActor instance) {
        instance.getContext().reply(instance.getNotice());
    }

    @Override
    public UUID getToken() {
        return token;
    }
}
