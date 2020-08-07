package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.ApiInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class Index {

    private static final ApiInfo INFO = new ApiInfo(
            "music-graph-api",
            "2.0",
            "/application.wadl",
            "https://github.com/simon987/music-graph-api"
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApiInfo get() {
        return INFO;
    }
}
