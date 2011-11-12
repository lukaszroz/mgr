package edu.agh.lroza;

import org.junit.Test;

public class ClientTestJava {

    @Test
    public void clientPingServerTest() throws InterruptedException {
        System.out.println("*** clientPingServerTest ***");
        PingServer pingServer = new PingServer();
        new Client().run();
        pingServer.shutdown();
    }
}
