package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.SearchResult;
import net.simon987.musicgraph.io.MusicDatabase;
import net.simon987.musicgraph.logging.LogManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/tag")
public class TagController {

    private static Logger logger = LogManager.getLogger();

    @Inject
    private MusicDatabase db;

    public TagController() {
    }

    @GET
    @Path("related/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult getRelated(@PathParam("id") long id) {

        logger.info(String.format("Related tag for %d", id));
        return db.getRelatedByTag(id);
    }
}
