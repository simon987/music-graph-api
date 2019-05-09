package net.simon987.musicgraph.webapi;

import net.simon987.musicgraph.entities.ReleaseDetails;

import java.util.HashMap;
import java.util.List;

public class ChartOptions {

    public HashMap<String, byte[]> covers;
    public List<ReleaseDetails> details;
    public int rows;
    public int cols;
    public String backgroundColor;
    public String textColor = "white";
    public String font = "Hack-Regular";
    public String borderColor = "white";
}
