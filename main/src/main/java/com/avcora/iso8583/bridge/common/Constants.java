package com.avcora.iso8583.bridge.common;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 *
 */
public class Constants {

    private static final Logger logger = Logger.getLogger(Constants.class);

    private static final Properties props = init();

    public static final String SERVER_IP = getString("connection.server.ip");

    public static final Integer SERVER_PORT = getInteger("connection.server.port");

    public static final Integer LISTENER_PORT = getInteger("incoming.listener.port");

    public static final Integer CONNECTION_TIMEOUT = getInteger("connection.timeout");

    public static final String DATA_DIR = getString("data.dir");

    public static final String ENCRYPTION_KEY = getString("encryption.key");


    private static String getString(String key) {
        return props.getProperty(key);
    }

    private static Integer getInteger(String key) {
        return Integer.valueOf(getString(key));
    }

    private static Properties init() {
        Properties props = new Properties();
        try {
            URL resource = Loader.getResource("constants.properties");
            InputStream in = resource.openStream();
            props.load(in);
        } catch (Throwable e) {
            logger.error("cannot init constants", e);
        }
        return props;
    }
}
