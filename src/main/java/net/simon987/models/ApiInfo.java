package net.simon987.models;

public class ApiInfo {

    private String name;
    private String version;

    public ApiInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
