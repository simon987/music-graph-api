package net.simon987.musicgraph.entities;

public class Tag {

    public String name;
    public long id;
    public long tagid;

    public Tag(long id, long tagid, String name) {
        this.id = id;
        this.tagid = tagid;
        this.name = name;
    }
}
