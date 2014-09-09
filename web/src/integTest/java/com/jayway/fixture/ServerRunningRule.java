package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetConfig;
import com.jayway.trumpet.server.boot.TrumpetDomainConfig;
import com.jayway.trumpet.server.boot.TrumpetServer;
import com.jayway.trumpet.server.boot.TrumpetServerConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

import java.util.Properties;

public class ServerRunningRule implements TestRule {

    private final TrumpetServer server;

    public static ServerRunningRule remote(String host, int port){
        return new ServerRunningRule(host, port);
    }

    public static ServerRunningRule local(){
        return new ServerRunningRule(0);
    }

    public static ServerRunningRule local(int port){
        return new ServerRunningRule(port);
    }

    private ServerRunningRule(int port) {

        Properties props = new Properties();
        props.setProperty(TrumpetServerConfig.SERVER_HTTP_PORT, String.valueOf(port));
        props.setProperty(TrumpetDomainConfig.TRUMPETEER_STALE_THRESHOLD, "1000000");
        props.setProperty(TrumpetDomainConfig.TRUMPETEER_PURGE_INTERVAL, "1000000");

        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class, props);

        server = new TrumpetServer(config);
    }

    private ServerRunningRule(String host, int port) {
        server = Mockito.mock(TrumpetServer.class);
        Mockito.when(server.getHost()).thenReturn(host);
        Mockito.when(server.getPort()).thenReturn(port);
    }

    public ServerRunningRule(Properties props) {
        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class, props);

        server = new TrumpetServer(config);
    }

    public int port(){
        return server.getPort();
    }

    public String host(){
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
                }
            }
        };
    }
}
