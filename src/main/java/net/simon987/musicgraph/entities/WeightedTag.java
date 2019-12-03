package net.simon987.musicgraph.entities;

public class WeightedTag extends Tag {

    public double weight;

    public WeightedTag(long id, long tagid, String name, double weight) {
        super(id, tagid, name);
        this.weight = weight;
    }
}
