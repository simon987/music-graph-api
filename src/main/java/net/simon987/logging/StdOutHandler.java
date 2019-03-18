package net.simon987.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class StdOutHandler extends StreamHandler {

    StdOutHandler() {
        super(System.out, new MusicGraphFormatter());

        this.setLevel(Level.ALL);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(record);
        flush();
    }
}
