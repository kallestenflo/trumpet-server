package com.jayway.trumpet.server.boot;

import com.jayway.trumpet.server.resource.TrumpetResource;
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
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class TrumpetServer {

    private static final Logger logger = LoggerFactory.getLogger(TrumpetServer.class);

    public static final int DEFAULT_PORT = 9191;
    public static final int DEFAULT_MAX_DISTANCE = 200;

    private Server server;

    public TrumpetServer(int port, int maxDistance) {
        try {
            server = new Server();

            server.setConnectors(new Connector[]{createConnector(server, port)});

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/api");
            ServletHolder servletHolder = new ServletHolder(createJerseyServlet(maxDistance));
            servletHolder.setInitOrder(1);
            context.addServlet(servletHolder, "/*");

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setServer(server);
            webAppContext.setContextPath("/");
            webAppContext.setResourceBase(getClass().getResource("/www").toExternalForm());

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{context, webAppContext});
            server.setHandler(handlers);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ServerConnector createConnector(Server s, int port) {
        ServerConnector connector = new ServerConnector(s);
        connector.setHost("localhost");
        connector.setPort(port);
        return connector;
    }

    private static ServletContainer createJerseyServlet(int maxDistance) throws IOException {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(SseFeature.class);

        resourceConfig.register(new TrumpetResource(maxDistance));

        return new ServletContainer(resourceConfig);
    }

    public int getPort(){
        return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
    }

    public void start(){
        try {
            server.start();
            logger.info("Server started on port: {}", getPort());
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
        int maxDistance = DEFAULT_MAX_DISTANCE;

        if (args.length > 0) {
            try {
                if(args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h") ){
                    docs();
                    System.exit(0);
                }

                port = Integer.parseInt(args[0]);
                if(args.length > 1){
                    maxDistance = Integer.parseInt(args[1]);
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid usage! Arguments are <port> <maxDistance>. If no arguments are provided default values are used: <" + DEFAULT_PORT + "> <" +DEFAULT_MAX_DISTANCE+ ">" );
            }
        }
        TrumpetServer trumpetServer = new TrumpetServer(port, maxDistance);
        trumpetServer.start();
    }

    private static void docs(){
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Commandline options");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("java -jar trumpet-server-1.0.0-shadow.jar <port> <maxDistance>");


        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("ENTRY POINT");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl \"http://localhost:9191/api/?latitude=55.583985&longitude=12.957578\" ");
        System.out.println("");
        System.out.println("Respose 200 ");
        System.out.println("{");
        System.out.println("   \"_links\": {");
        System.out.println("        \"location\": \"http://localhost:9191/api/clients/1/location\",");
        System.out.println("        \"subscribe\": \"http://localhost:9191/api/clients/1/subscribe\",");
        System.out.println("        \"trumpet\": \"http://localhost:9191/api/clients/1/trumpet\"");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("LOCATION");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X PUT --data \"latitude=55.583985&longitude=12.957578\" http://localhost:9191/api/clients/1/location");
        System.out.println("Respose 200 (no content)");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("TRUMPET");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X POST --data \"msg=This is my first trumpet\" http://localhost:9191/api/clients/1/trumpet");
        System.out.println("Respose 200 (no content)");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("SUBSCRIBE");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("1. Open EventSource to href");
        System.out.println("2. Message to subscribe to is 'trumpet'");
        System.out.println("3. Message format is: ");
        System.out.println("{");
        System.out.println("    \"msg\": \"this is the trumpet noice!\" ");
        System.out.println("}");

    }
}
