package me.BrilZliaN.LongPollingBot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
    private static Logger logger;

    public static Logger get() {
        if (logger == null) {
            logger = Logger.getLogger("SchoolFunLog");
            try {
                FileHandler fh = new FileHandler("server.log", true);
                fh.setLevel(Level.ALL);
                fh.setFormatter(new Formatter(){

                    @Override
                    public String format(LogRecord record) {
                        SimpleDateFormat logTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(record.getMillis());
                        return String.valueOf(logTime.format(cal.getTime())) + " <" + record.getLevel().getName() + "> " + record.getMessage() + "\n";
                    }
                });
                logger.addHandler(fh);
            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }

}

