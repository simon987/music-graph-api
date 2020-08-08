package net.simon987.musicgraph.entities;

import java.util.ArrayList;
import java.util.List;

public class ArtistDetails {

    public String name;
    public String comment;
    public long year;
    public List<Release> releases;
    public List<WeightedTag> tags;
    public List<Label> labels;
    public List<SpotifyPreviewUrl> tracks;

    public ArtistDetails() {
        releases = new ArrayList<>();
        tags = new ArrayList<>();
        labels = new ArrayList<>();
        tracks = new ArrayList<>();
    }
}
