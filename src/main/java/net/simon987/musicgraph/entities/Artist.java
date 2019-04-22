package net.simon987.musicgraph.entities;


import java.util.List;

public class Artist {

    public Long id;
    public String mbid;
    public String name;
    public List<String> labels;
    public int listeners;
    public int playCount;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Artist) {
            return ((Artist) obj).id.equals(id);
        }
        return false;
    }
}


