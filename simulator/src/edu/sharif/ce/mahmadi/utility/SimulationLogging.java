package edu.sharif.ce.mahmadi.utility;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class SimulationLogging {
    static Logger logger;
    public Handler fileHandler;
    Formatter plainText;

    private SimulationLogging() throws IOException {
        //instance the logger
        logger = Logger.getLogger(SimulationLogging.class.getName());
        //instance the filehandler
        String fileName = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss'.tsv'").format(new Date());
        fileHandler = new FileHandler(fileName+".txt", false);

        //instance formatter, set formatting, and handler
        plainText = new customFormatter();
        fileHandler.setFormatter(plainText);
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);

    }

    //returns the logger and use it normally
    public static Logger getLogger(){
        if(logger == null){
            try {
                new SimulationLogging();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }
}

class customFormatter extends Formatter {
    // Create a DateFormat to format the logger timestamp.
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(df.format(new Date(record.getMillis()))).append(" - ");
        builder.append("[").append(record.getSourceClassName()).append(".");
        builder.append(record.getSourceMethodName()).append("] - ");
        builder.append("[").append(record.getLevel()).append("] - ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
