package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetServer;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ServerRunningRule implements TestRule {

    private final TrumpetServer server;

    public ServerRunningRule() {
        this(0);
    }

    public ServerRunningRule(final int port) {

        TrumpetServerConfig config = new TrumpetServerConfig() {
            @Override
            public int port() {
                return port;
            }

            @Override
            public String hostname() {
                return "localhost";
            }
        };

        server = new TrumpetServer(config);
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
