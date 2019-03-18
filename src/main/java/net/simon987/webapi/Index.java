package net.simon987.webapi;

import net.simon987.models.ApiInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class Index {

    private static final ApiInfo INFO = new ApiInfo(
            "music-graph",
            "0.1"
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApiInfo get() {
        return INFO;
    }
}
