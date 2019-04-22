package net.simon987.musicgraph.entities;

import java.util.ArrayList;
import java.util.List;

public class ArtistDetails {

    public ArtistDetails() {
        releases = new ArrayList<>();
        tags = new ArrayList<>();
    }

    public String name;
    public List<String> releases;
    public List<Tag> tags;
}
