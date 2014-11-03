package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetServer;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import com.jayway.trumpet.server.domain.subscriber.TrumpeteerRepository;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

import java.util.Properties;

public class ServerRunningRule implements TestRule {

    private final TrumpetServer server;

    public static ServerRunningRule remote(String host, int port) {
        return new ServerRunningRule(host, port);
    }

    public static ServerRunningRule local() {
        return local(0);
    }

    public static ServerRunningRule local(int port) {
        Properties props = new Properties();
        props.setProperty(TrumpetServerConfig.SERVER_HTTP_PORT, String.valueOf(port));

        return local(props);
    }

    @SuppressWarnings("unchecked")
    public static ServerRunningRule local(Pair<String, String> property, Pair<String, String>... properties) {
        Properties props = new Properties();
        props.put(property.getKey(), property.getValue());
        for (Pair<String, String> pair : properties) {
            props.put(pair.getKey(), pair.getValue());
        }
        return local(props);
    }

    public static ServerRunningRule local(Properties props) {

        if (!props.containsKey(SubscriberConfig.TRUMPETEER_STALE_THRESHOLD)) {
            props.setProperty(SubscriberConfig.TRUMPETEER_STALE_THRESHOLD, "2000");
        }
        if (!props.containsKey(SubscriberConfig.TRUMPETEER_PURGE_INTERVAL)) {
            props.setProperty(SubscriberConfig.TRUMPETEER_PURGE_INTERVAL, "1000");
        }
        if (!props.containsKey(SubscriberConfig.TRUMPETEER_PURGE_ENABLED)) {
            props.setProperty(SubscriberConfig.TRUMPETEER_PURGE_ENABLED, "false");
        }
        if (!props.containsKey(TrumpetServerConfig.SERVER_HTTP_PORT)) {
            props.setProperty(TrumpetServerConfig.SERVER_HTTP_PORT, String.valueOf(0));
        }
        if (!props.containsKey(TrumpetServerConfig.SERVER_HOST_NAME)) {
            props.setProperty(TrumpetServerConfig.SERVER_HOST_NAME, "localhost");
        }

        return new ServerRunningRule(props);
    }


    private ServerRunningRule(String host, int port) {
        server = Mockito.mock(TrumpetServer.class);
        Mockito.when(server.getHost()).thenReturn(host);
        Mockito.when(server.getPort()).thenReturn(port);
    }

    private ServerRunningRule(Properties props) {
        TrumpetServerConfig config = ConfigFactory.create(TrumpetServerConfig.class, props);

        server = new TrumpetServer(config);
    }

    public int port() {
        return server.getPort();
    }

    public String host() {
        return server.getHost();
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
                    server.getTrumpetService().clear();
                }
            }
        };
    }

    public TrumpeteerRepository trumpeteerRepository() {
        return server.getTrumpetService();
    }
}
