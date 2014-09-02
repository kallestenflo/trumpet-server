package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetServer;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import com.jayway.trumpet.server.domain.TrumpeterRepository;
import org.aeonbits.owner.ConfigFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Properties;

public class ServerRunningRule implements TestRule {

    private final TrumpetServer server;

    public ServerRunningRule() {
        this(0);
    }

    public ServerRunningRule(int port) {

        Properties props = new Properties();
        props.setProperty(TrumpetServerConfig.SERVER_HTTP_PORT, String.valueOf(port));
        props.setProperty(TrumpetServerConfig.TRUMPETER_STALE_THRESHOLD, "1000000");
        props.setProperty(TrumpetServerConfig.TRUMPETER_PURGE_INTERVAL, "1000000");

        TrumpetServerConfig config = ConfigFactory.create(TrumpetServerConfig.class, props);

        server = new TrumpetServer(config);
    }

    public ServerRunningRule(Properties props) {
        TrumpetServerConfig config = ConfigFactory.create(TrumpetServerConfig.class, props);

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
