package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetServer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ServerRunningRule implements TestRule {

    private final TrumpetServer server;

    public ServerRunningRule() {
        this(0);
    }

    public ServerRunningRule(int port) {
        server = new TrumpetServer(port);
    }

    public int port(){
        return server.getPort();
    }

    @Override
    public Statement apply(Statement base, Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    server.start();

                    base.evaluate();
                } finally {
                    server.stop();
                }
            }
        };
    }
}
