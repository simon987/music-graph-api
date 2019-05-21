package net.simon987.musicgraph.entities;

public class Release {

    public String mbid;
    public String name;
    public long year;

    public Release(String mbid, String name, long year) {
        this.mbid = mbid;
        this.name = name;
        this.year = year;
    }
}
