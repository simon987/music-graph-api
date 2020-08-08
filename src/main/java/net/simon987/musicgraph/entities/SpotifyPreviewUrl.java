package net.simon987.musicgraph.entities;

public class SpotifyPreviewUrl {
    public String url;
    public String release;
    public String name;

    public SpotifyPreviewUrl(String release, String name, String url) {
        this.url = url;
        this.release = release;
        this.name = name;
    }
}
