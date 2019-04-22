package net.simon987.musicgraph.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogManager {

    private static Logger logger;

    static {
        logger = Logger.getLogger("music-graph");

        Handler handler = new StdOutHandler();

        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public static Logger getLogger() {
        return logger;
    }
}
