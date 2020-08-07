package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.io.ICoverArtDatabase;
import net.simon987.musicgraph.logging.LogManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.logging.Logger;

@Path("/cover/{mbid}")
public class CoverController {

    private static final Logger logger = LogManager.getLogger();

    @Inject
    private ICoverArtDatabase db;

    public CoverController() {
    }

    @GET
    @Produces("image/jpg")
    public byte[] getByReleaseId(@PathParam("mbid") String mbid) throws Exception {

        if (mbid == null) {
            throw new BadRequestException();
        }

        logger.info(String.format("Cover for %s", mbid));

        byte[] cover = db.getCover(mbid);

        if (cover == null) {
            throw new NotFoundException();
        }

        return cover;
    }
}
