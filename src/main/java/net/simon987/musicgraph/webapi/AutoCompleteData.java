package net.simon987.musicgraph.webapi;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteData {
    public List<AutocompleteLine> lines;

    public AutoCompleteData() {
        lines = new ArrayList<>(30);
    }
}
