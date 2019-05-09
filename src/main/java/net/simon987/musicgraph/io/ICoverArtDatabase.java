package net.simon987.musicgraph.io;

import java.util.HashMap;
import java.util.List;

public interface ICoverArtDatabase {

    /**
     * @param mbid MusicBrainz id
     * @return null if not found
     * @throws Exception if unexpected error
     */
    byte[] getCover(String mbid) throws Exception;

    HashMap<String, byte[]> getCovers(List<String> mbids) throws Exception;
}
