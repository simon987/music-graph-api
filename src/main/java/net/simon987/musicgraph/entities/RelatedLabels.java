package net.simon987.musicgraph.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RelatedLabels {
    public Set<Label> labels;
    public List<Relation> relations;

    public RelatedLabels() {
        labels = new HashSet<>();
        relations = new ArrayList<>();
    }
}
