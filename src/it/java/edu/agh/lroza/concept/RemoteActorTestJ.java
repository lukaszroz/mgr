package edu.agh.lroza.concept;

import akka.dispatch.Future;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Callable;

import static akka.dispatch.Futures.future;

public class RemoteActorTestJ {

    @BeforeClass
    public static void init() {
        ServerImplJ server = new ServerImplJ();
        System.err.println("Starting server....");
        Utils.start(server);
    }

    @Test (expected = ConcurrentModificationException.class)
    public void shouldCauseException() {
        final ServerClient client = Utils.getClient();
        future(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Future<Object> future = null;
                for (int i = 0; i < 11; i++) {
                    future = future(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            return client.remove();
                        }
                    });
                }
                assert future != null;
                return future.get();
            }
        });
        client.iterate();
    }

    @AfterClass
    public static void cleanUp() {
        Utils.stop();
    }
}
