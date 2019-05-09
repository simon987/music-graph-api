package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.io.ICoverArtDatabase;
import net.simon987.musicgraph.io.MusicDatabase;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("/chart")
public class ChartController {

    @Inject
    IChartBuilder chartBuilder;

    @Inject
    ICoverArtDatabase coverArtDatabase;

    @Inject
    private MusicDatabase musicDatabase;

    @GET
    @Produces("image/png")
    public byte[] makeChart() throws Exception {
        var opt = new ChartOptions();

        throw new NotFoundException();

//        List<String> releases = new ArrayList<>();
//
//        opt.cols = 4;
//        opt.rows = 5;
//        opt.covers = coverArtDatabase.getCovers(releases);
//        opt.backgroundColor = "blue";
//        opt.details = releases
//                .parallelStream() // Thread-safe?
//                .map(r -> musicDatabase.getReleaseDetails(r))
//                .collect(Collectors.toList());
//
//        return chartBuilder.makeChart(opt);
    }
}
