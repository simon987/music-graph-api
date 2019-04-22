package net.simon987.musicgraph.io;

public interface ICoverArtDatabase {

    /**
     * @param mbid MusicBrainz id
     * @return null if not found
     * @throws Exception if unexpected error
     */
    byte[] getCover(String mbid) throws Exception;
}
