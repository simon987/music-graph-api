package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.ReleaseDetails;
import net.simon987.musicgraph.io.MusicDatabase;
import net.simon987.musicgraph.logging.LogManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/release")
public class ReleaseController {

    private static Logger logger = LogManager.getLogger();

    @Inject
    private MusicDatabase db;

    public ReleaseController() {
    }

    @GET
    @Path("details/{mbid}")
    @Produces(MediaType.APPLICATION_JSON)

    //TODO Should I make an endpoint only for the tags?
    public ReleaseDetails getDetails(@PathParam("mbid") String mbid) {

        logger.info(String.format("Release details for %s", mbid));
        return db.getReleaseDetails(mbid);
    }
}
