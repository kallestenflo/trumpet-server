package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.ConfigFactory;

public class Main {

    public static void main(String[] args) {
        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class,
                System.getProperties(),
                System.getenv());

        if (args.length > 0) {
            if(args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h") ){
                docs();
                System.exit(0);
            }
        }
        TrumpetServer trumpetServer = new TrumpetServer(config);
        trumpetServer.start();
    }

    private static void docs(){

        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Configuration");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("When bootstrapping tha application will scan for configurations in the order described below.");
        System.out.println("Properties defined in one file has precedence over properties defined later in the list of sources.");
        System.out.println("");
        System.out.println("    file:trumpet.config");
        System.out.println("    file:~/.trumpet.config");
        System.out.println("    file:/etc/trumpet.config");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Commandline options");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("Default port: 9191 ");
        System.out.println("Default host: 0.0.0.0");
        System.out.println("");
        System.out.println("java -Dserver.http.port=9999 -Dserver.http.host=localhost -jar trumpet-server-1.0.0-shadow.jar");


        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("ENTRY POINT");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl \"http://localhost:9191/api/?latitude=55.583985&longitude=12.957578&accuracy=100\" ");
        System.out.println("");
        System.out.println("Response: 200 ");
        System.out.println("{");
        System.out.println("   \"trumpeteerId\" : \"1\",");
        System.out.println("   \"_links\": {");
        System.out.println("        \"me\": { \"href\" : \"http://localhost:9191/api/trumpeteers/1\" }, ");
        System.out.println("        \"subscribe\": { \"href\" : \"http://localhost:9191/api/trumpeteers/1/subscription\" },");
        System.out.println("        \"location\": { \"href\" : \"http://localhost:9191/api/trumpeteers/1/location\" },");
        System.out.println("        \"trumpet\": { \"href\" : \"http://localhost:9191/api/trumpeteers/1/trumpet\" }, ");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("ME");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X GET \"Accept: application/json\" http://localhost:9191/api/trumpeteers/1?distance=200");
        System.out.println("Response: ");
        System.out.println("{");
        System.out.println("    \"trumpeteersInRange\": 12 ");
        System.out.println("}");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("LOCATION");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X PUT -H --data \"latitude=55.583985&longitude=12.957578&accuracy=10\" http://localhost:9191/api/trumpeteers/1/location");
        System.out.println("Response: 200 no content");
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("TRUMPET");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("");
        System.out.println("curl -X POST --data \"message=This is my first trumpet&distance=200&channel=foo\" http://localhost:9191/api/trumpeteers/1/trumpet");
        System.out.println("The form parameter distance is optional");
        System.out.println("Response: Content-type: application/json");
        System.out.println("{");
        System.out.println("    \"trumpetId\": \"121212121\" ");
        System.out.println("}");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("SUBSCRIBE");
        System.out.println("-----------------------------------------------------------------------------------------------");
        System.out.println("1. Open EventSource to entry point link with rel 'subscribe' ");
        System.out.println("2. Message to subscribe to is 'trumpet'");
        System.out.println("3. Message format is: ");
        System.out.println("{");
        System.out.println("   \"id\": \"1\", ");
        System.out.println("   \"timestamp\": 121212122, ");
        System.out.println("   \"message\": \"foo\", ");
        System.out.println("   \"channel\": \"bar\", ");
        System.out.println("   \"distanceFromSource\": 240,");
        System.out.println("   \"accuracy\": 10");
        System.out.println("   \"_links\": {");
        System.out.println("        \"echo\": { \"href\" : \"http://localhost:9191/api/trumpeteers/1/echo?trumpedId=xxxx&message=foo&distance=200&channel=foo\" },");
        System.out.println("    }");
        System.out.println("}");
    }
}
