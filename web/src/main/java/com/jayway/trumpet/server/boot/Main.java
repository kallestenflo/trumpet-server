package com.jayway.trumpet.server.boot;

import org.aeonbits.owner.ConfigFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        TrumpetConfig config = ConfigFactory.create(TrumpetConfig.class,
                System.getProperties(),
                System.getenv());

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h")) {
                docs();
                System.exit(0);
            }
        }
        TrumpetServer trumpetServer = new TrumpetServer(config);
        trumpetServer.start();
    }

    private static void docs() {
        try {
            InputStream inputStream = new URL("https://raw.githubusercontent.com/kallestenflo/trumpet-server/master/README.md").openStream();

            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            System.out.println(textBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
