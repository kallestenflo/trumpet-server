package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetConfig;
import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.boot.TrumpetService;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Properties;

public class ServerRunningRule implements TestRule {

    private final TrumpetService server;

    public ServerRunningRule() {
        this(0);
    }

    public ServerRunningRule(int port) {

        Properties props = new Properties();
        props.setProperty(TrumpetServerConfig.SERVER_HTTP_PORT, String.valueOf(port));
        props.setProperty(TrumpetDomainConfig.TRUMPETER_STALE_THRESHOLD, "1000000");
        props.setProperty(TrumpetDomainConfig.TRUMPETER_PURGE_INTERVAL, "1000000");

        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class, props);

        server = new TrumpetService(config);
    }

    public ServerRunningRule(Properties props) {
        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class, props);

        server = new TrumpetService(config);
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
