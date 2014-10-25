package com.jayway.trumpet.server.boot;

import com.jayway.trumpet.server.domain.trumpeteer.TrumpeteerNotificationService;
import com.jayway.trumpet.server.infrastructure.event.GuavaTrumpetEventBus;
import com.jayway.trumpet.server.infrastructure.subscription.gcm.GCMBroadcaster;
import com.jayway.trumpet.server.infrastructure.trumpeteer.TrumpetServiceImpl;
import com.jayway.trumpet.server.infrastructure.trumpeteer.TrumpeteerNotificationServiceImpl;
import com.jayway.trumpet.server.rest.LoggingFilter;
import com.jayway.trumpet.server.rest.TrumpetResource;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
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

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.EnumSet;


public class TrumpetServer {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetServer.class);

    private Server server;

    private final TrumpetServiceImpl trumpetService;

    public TrumpetServer(TrumpetServerConfig config) {
        this.trumpetService = new TrumpetServiceImpl(new GuavaTrumpetEventBus(), config, config);
        this.server = configureServer(config);
    }

    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public String getHost() {
        return ((ServerConnector) server.getConnectors()[0]).getHost();
    }

    public void start() {
        try {
            server.start();
            logger.info("Trumpet server running on port: {}", getPort());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            if (server.isRunning()) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Server configureServer(TrumpetServerConfig config) {
        try {
            Server configServer = new Server();

            configServer.setConnectors(new Connector[]{createConnector(configServer, config)});

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/api");
            ServletHolder servletHolder = new ServletHolder(createJerseyServlet(config));
            servletHolder.setInitOrder(1);
            context.addServlet(servletHolder, "/*");

            FilterHolder filterHolder = new FilterHolder(new LoggingFilter(true, LoggerFactory.getLogger("API")));
            context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
            //context.addFilter(LoggingFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setServer(configServer);
            webAppContext.setContextPath("/");
            if (config.resourceBase().startsWith("classpath")) {
                webAppContext.setResourceBase(getClass().getResource("/webapp").toExternalForm());
            } else {
                webAppContext.setResourceBase(config.resourceBase());
            }


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

    private ServletContainer createJerseyServlet(TrumpetServerConfig config) throws IOException {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(SseFeature.class);
        resourceConfig.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        GCMBroadcaster gcmBroadcaster = new GCMBroadcaster(config);
        TrumpeteerNotificationService trumpeteerNotificationService = new TrumpeteerNotificationServiceImpl(trumpetService, trumpetService, config);
        resourceConfig.register(new TrumpetResource(config, trumpetService, gcmBroadcaster, trumpetService, trumpetService, trumpeteerNotificationService));

        return new ServletContainer(resourceConfig);
    }

    public TrumpetServiceImpl getTrumpetService() {
        return trumpetService;
    }
}
