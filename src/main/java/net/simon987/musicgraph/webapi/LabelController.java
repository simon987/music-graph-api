package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.RelatedLabels;
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

@Path("/label")
public class LabelController {

    private static final Logger logger = LogManager.getLogger();

    @Inject
    private MusicDatabase db;

    public LabelController() {
    }

    @GET
    @Path("related/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult getRelated(@PathParam("id") long id) {

        logger.info(String.format("Related artist for label %d", id));
        return db.getRelatedByLabel(id);
    }

    @GET
    @Path("label/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RelatedLabels getRelatedLabel(@PathParam("id") long id) {

        logger.info(String.format("Related labels for label %d", id));
        return db.getRelatedLabelByLabel(id);
    }
}
