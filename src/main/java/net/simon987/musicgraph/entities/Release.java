package net.simon987.musicgraph.entities;

import java.util.List;

public class Release {

    public String mbid;
    public List<String> labels;
    public String name;
    public long year;
    public long id;

    public Release(List<String> labels, String mbid, String name, long year) {
        this.labels = labels;
        this.mbid = mbid;
        this.name = name;
        this.year = year;
    }

    public Release(List<String> labels, String mbid, String name, long year, long id) {
        this.labels = labels;
        this.mbid = mbid;
        this.name = name;
        this.year = year;
        this.id = id;
    }
}
