package net.simon987.musicgraph.entities;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResult {

    public Set<Artist> artists;
    public List<Relation> relations;

    public SearchResult() {
        artists = new HashSet<>();
        relations = new ArrayList<>();
    }
}
