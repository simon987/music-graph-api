package net.simon987.musicgraph.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RelatedTags {
    public Set<Tag> tags;
    public List<Relation> relations;

    public RelatedTags() {
        tags = new HashSet<>();
        relations = new ArrayList<>();
    }
}
