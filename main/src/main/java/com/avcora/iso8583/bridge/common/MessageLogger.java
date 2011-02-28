package com.avcora.iso8583.bridge.common;

import com.avcora.iso8583.bridge.listener.ClientConfiguration;
import com.avcora.iso8583.bridge.listener.Main;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: daniel
 */
public class MessageLogger {

    public static Logger getLogger(final Integer port) {
        Logger logger = Logger.getLogger(String.valueOf(port));
        Appender appender = logger.getAppender(String.valueOf(port));
        try {
            if (appender == null) {
                ClientConfiguration cc = Main.getClientConfiguration(port);
                if (cc == null)
                    cc = new ClientConfiguration(8000, "dummy application"); //TODO remove this line
                String path = new File(Constants.DATA_DIR + File.separator + "logs" + File.separator + cc.getDescription().replaceAll(" ", "_")
                        + File.separator + "outgoing" + File.separator,
                        cc.getDescription().replaceAll(" ", "_") + ".log").getAbsolutePath();
                appender = new DailyRollingFileAppender(new PatternLayout("[%d] [%t] %-5p %c %x - %m%n"), path, "'.'yyyy-MM-dd");

                appender.setName(String.valueOf(port));
                logger.addAppender(appender);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logger;
    }
}
