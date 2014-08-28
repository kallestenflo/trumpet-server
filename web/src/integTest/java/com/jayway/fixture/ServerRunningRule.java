package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetServer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerRunningRule implements TestRule {

    private final AtomicInteger port = new AtomicInteger();

    public ServerRunningRule() {
        this.port.set(0);
    }

    public ServerRunningRule(int port) {
        this.port.set(port);
    }

    public int port(){
        return this.port.intValue();
    }

    @Override
    public Statement apply(Statement base, Description description) {

        TrumpetServer server = new TrumpetServer(port.intValue(), 200);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    server.start();

                    port.set(server.getPort());

                    base.evaluate();
                } finally {
                    server.stop();
                }
            }
        };
    }
}
