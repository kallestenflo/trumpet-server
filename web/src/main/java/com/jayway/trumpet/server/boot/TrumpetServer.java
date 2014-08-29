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


public class TrumpetServer {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetServer.class);

    public static final int DEFAULT_PORT = 9191;

    private Server server;

    public TrumpetServer(int port) {
        this.server = configureServer(port);
    }

    public int getPort(){
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }

    public void start(){
        try {
            server.start();
            logger.info("TrumpetServer started on port: {}", getPort());
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

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                if(args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h") ){
                    docs();
                    System.exit(0);
                }

                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid usage! Arguments are <port> <maxDistance>. If no arguments are provided default values are used: <" + DEFAULT_PORT + ">");
            }
        }
        TrumpetServer trumpetServer = new TrumpetServer(port);
        trumpetServer.start();
    }

    private static void docs(){
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Commandline options");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("java -jar trumpet-server-1.0.0-shadow.jar <port>");


        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("ENTRY POINT");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl \"http://localhost:9191/api/?latitude=55.583985&longitude=12.957578\" ");
        System.out.println("");
        System.out.println("Respose 200 ");
        System.out.println("{");
        System.out.println("   \"_links\": {");
        System.out.println("        \"location\": \"http://localhost:9191/api/trumpeters/1/location\",");
        System.out.println("        \"subscribe\": \"http://localhost:9191/api/trumpeters/1/subscribe\",");
        System.out.println("        \"trumpet\": \"http://localhost:9191/api/trumpeters/1/trumpet\"");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("LOCATION");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X PUT --data \"latitude=55.583985&longitude=12.957578\" http://localhost:9191/api/trumpeters/1/location");
        System.out.println("Respose 200 (no content)");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("TRUMPET");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X POST --data \"msg=This is my first trumpet&distance=200\" http://localhost:9191/api/trumpeters/1/trumpet");
        System.out.println("The form parameter distance is optional");
        System.out.println("Respose 200 (no content)");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("SUBSCRIBE");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("1. Open EventSource to href");
        System.out.println("2. Message to subscribe to is 'trumpet'");
        System.out.println("3. Message format is: ");
        System.out.println("{");
        System.out.println("    \"msg\": \"This is noise from a trumpeter!\" ");
        System.out.println("}");
    }

    private Server configureServer(int port) {
        try {
            Server configServer = new Server();

            configServer.setConnectors(new Connector[]{createConnector(configServer, port)});

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/api");
            ServletHolder servletHolder = new ServletHolder(createJerseyServlet());
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

    private ServerConnector createConnector(Server s, int port) {
        ServerConnector connector = new ServerConnector(s);
        connector.setHost("0.0.0.0");
        connector.setPort(port);
        return connector;
    }

    private ServletContainer createJerseyServlet() throws IOException {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(SseFeature.class);
        resourceConfig.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        resourceConfig.register(new TrumpetResource());

        return new ServletContainer(resourceConfig);
    }
}
