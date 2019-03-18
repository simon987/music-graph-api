package net.simon987.logging;

import java.util.logging.*;

public class LogManager {

    private static Logger logger;

    static {
        logger = Logger.getLogger("music-graph");

        Handler handler = new StdOutHandler();

        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    public static Logger getLogger() {
        return logger;
    }
}
