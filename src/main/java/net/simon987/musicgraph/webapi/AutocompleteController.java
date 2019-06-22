package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.io.MusicDatabase;
import net.simon987.musicgraph.logging.LogManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/")
public class AutocompleteController {

    private static Logger logger = LogManager.getLogger();

    @Inject
    private MusicDatabase db;

    public AutocompleteController() {
    }

    @GET
    @Path("/autocomplete/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    public AutoCompleteData autoComplete(@PathParam("prefix") String prefix) {

        prefix = prefix.replace('+', ' ');
        logger.info(String.format("Autocomplete for '%s'", prefix));

        return db.autoComplete(prefix);
    }
}
