package net.simon987.musicgraph.entities;

public class WeightedTag extends Tag {

    public double weight;

    public WeightedTag(long id, String name, double weight) {
        super(id, name);
        this.weight = weight;
    }
}
