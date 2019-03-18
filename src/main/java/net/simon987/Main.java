package net.simon987;

import net.simon987.logging.LogManager;
import net.simon987.webapi.Index;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private final static Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {

        LOGGER.info("test");
        startHttpServer();
    }

    private static void startHttpServer() {

        ResourceConfig rc = new ResourceConfig();

        rc.registerClasses(Index.class);
        rc.registerClasses(JacksonFeature.class);

        try {
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                    new URI("http://localhost:8080/"),
                    rc);

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
