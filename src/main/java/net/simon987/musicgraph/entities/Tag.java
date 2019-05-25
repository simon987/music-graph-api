package net.simon987.musicgraph.entities;

public class Tag {
    public String name;
    public double weight;
    public long id;


    public Tag(long id, String name, double weight) {
        this.id = id;
        this.name = name;
        this.weight = weight;
    }
}
