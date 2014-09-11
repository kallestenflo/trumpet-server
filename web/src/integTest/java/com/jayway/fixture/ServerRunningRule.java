package com.jayway.fixture;

import com.jayway.trumpet.server.boot.TrumpetConfig;
import com.jayway.trumpet.server.boot.TrumpetServer;
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
        return local(0);
    }

    public static ServerRunningRule local(int port){
        Properties props = new Properties();
        props.setProperty(TrumpetConfig.SERVER_HTTP_PORT, String.valueOf(port));

        return local(props);
    }

    public static ServerRunningRule local(Properties props){

        if(!props.containsKey(TrumpetConfig.TRUMPETEER_STALE_THRESHOLD)) {
            props.setProperty(TrumpetConfig.TRUMPETEER_STALE_THRESHOLD, "2000");
        }
        if(!props.containsKey(TrumpetConfig.TRUMPETEER_PURGE_INTERVAL)) {
            props.setProperty(TrumpetConfig.TRUMPETEER_PURGE_INTERVAL, "1000");
        }
        if(!props.containsKey(TrumpetConfig.SERVER_HTTP_PORT)) {
            props.setProperty(TrumpetConfig.SERVER_HTTP_PORT, String.valueOf(0));
        }
        if(!props.containsKey(TrumpetConfig.SERVER_HOST_NAME)) {
            props.setProperty(TrumpetConfig.SERVER_HOST_NAME, "localhost");
        }

        return new ServerRunningRule(props);
    }



    private ServerRunningRule(String host, int port) {
        server = Mockito.mock(TrumpetServer.class);
        Mockito.when(server.getHost()).thenReturn(host);
        Mockito.when(server.getPort()).thenReturn(port);
    }

    private ServerRunningRule(Properties props) {
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
