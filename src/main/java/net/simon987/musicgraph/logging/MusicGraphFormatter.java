package net.simon987.musicgraph.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class MusicGraphFormatter extends Formatter {

    private static final DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {

        Date date = new Date();

        return String.format(
                "[%s] (MG) %s: %s\n",
                dateFormat.format(date), record.getLevel(), record.getMessage()
        );
    }
}
