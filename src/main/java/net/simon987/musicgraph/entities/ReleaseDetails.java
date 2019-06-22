package net.simon987.musicgraph.entities;

import java.util.ArrayList;
import java.util.List;

public class ReleaseDetails {

    public String name;
    public String mbid;
    public String artist;
    public long year;
    public List<WeightedTag> tags;

    public ReleaseDetails() {
        this.tags = new ArrayList<>();
    }
}
