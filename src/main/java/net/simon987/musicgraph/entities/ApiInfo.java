package net.simon987.musicgraph.entities;

public class ApiInfo {

    private String name;
    private String version;
    private String wadl;
    private String source;

    public ApiInfo(String name, String version, String wadl, String source) {
        this.name = name;
        this.version = version;
        this.wadl = wadl;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getWadl() {
        return wadl;
    }

    public String getSource() {
        return source;
    }
}
