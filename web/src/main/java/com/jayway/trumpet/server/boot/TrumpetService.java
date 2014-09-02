package com.jayway.trumpet.server.boot;

import com.jayway.trumpet.server.rest.TrumpetResource;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class TrumpetService {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetService.class);

    private Server server;

    public TrumpetService(TrumpetConfig config) {
        this.server = configureServer(config);
    }

    public int getPort(){
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }

    public void start(){
        try {
            server.start();
            logger.info("TrumpetServer running on port: {}", getPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop(){
        try {
            if(server.isRunning()){
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Server configureServer(TrumpetConfig config) {
        try {
            Server configServer = new Server();

            configServer.setConnectors(new Connector[]{createConnector(configServer, config)});

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/api");
            ServletHolder servletHolder = new ServletHolder(createJerseyServlet(config));
            servletHolder.setInitOrder(1);
            context.addServlet(servletHolder, "/*");

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setServer(configServer);
            webAppContext.setContextPath("/");
            webAppContext.setResourceBase(getClass().getResource("/www").toExternalForm());

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{context, webAppContext});
            configServer.setHandler(handlers);

            return configServer;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ServerConnector createConnector(Server s, TrumpetServerConfig config) {
        ServerConnector connector = new ServerConnector(s);
        connector.setHost(config.hostname());
        connector.setPort(config.port());
        return connector;
    }

    private ServletContainer createJerseyServlet(TrumpetDomainConfig config) throws IOException {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(SseFeature.class);
        resourceConfig.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        resourceConfig.register(new TrumpetResource(config));

        return new ServletContainer(resourceConfig);
    }
}
