package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.ArtistDetails;
import net.simon987.musicgraph.io.MusicDatabase;
import net.simon987.musicgraph.entities.SearchResult;
import net.simon987.musicgraph.logging.LogManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/artist")
public class ArtistController {

    private static Logger logger = LogManager.getLogger();

    @Inject
    private MusicDatabase db;

    public ArtistController() {
    }

    @GET
    @Path("related/{mbid}")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult getRelated(@PathParam("mbid") String mbid) {

        logger.info(String.format("Searching for %s", mbid));
        return db.getRelated(mbid);
    }

    @GET
    @Path("details/{mbid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArtistDetails getDetails(@PathParam("mbid") String mbid) {

        logger.info(String.format("Details for %s", mbid));
        return db.getArtistDetails(mbid);
    }
}
