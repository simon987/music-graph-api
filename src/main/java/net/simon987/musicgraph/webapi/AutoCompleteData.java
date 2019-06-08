package net.simon987.musicgraph.webapi;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteData {
    public List<ArtistOverview> artists;

    public AutoCompleteData() {
        artists = new ArrayList<>(30);
    }
}
