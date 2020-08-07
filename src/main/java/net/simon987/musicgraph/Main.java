package net.simon987.musicgraph;

import net.simon987.musicgraph.io.*;
import net.simon987.musicgraph.logging.LogManager;
import net.simon987.musicgraph.webapi.*;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        startHttpServer();
    }

    private static void startHttpServer() {

        ResourceConfig rc = new ResourceConfig();

        String pgUrl = System.getenv("PG_URL");
        String pgUsername = System.getenv("PG_USERNAME");
        String pgPassword = System.getenv("PG_PASSWORD");

        rc.registerInstances(new MusicDatabase());
        rc.registerInstances(new PostgreSQLCoverArtDatabase(pgUrl, pgUsername, pgPassword));
//        rc.registerInstances(new MagickChartBuilder("/dev/shm/im_chart/"));

        rc.registerClasses(Index.class);
        rc.registerClasses(ArtistController.class);
        rc.registerClasses(AutocompleteController.class);
        rc.registerClasses(CoverController.class);
        rc.registerClasses(TagController.class);
        rc.registerClasses(ReleaseController.class);
        rc.registerClasses(JacksonFeature.class);

        rc.registerClasses(MyExceptionMapper.class);

        try {
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                    new URI("http://0.0.0.0:3030/"),
                    rc);

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
