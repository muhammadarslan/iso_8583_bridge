package com.avcora.iso8583.bridge.listener;

import com.avcora.iso8583.bridge.common.Constants;
import com.avcora.iso8583.bridge.common.MessageFactory;
import com.avcora.iso8583.bridge.sender.ConnectorSocket;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.jpos.iso.ISOMsg;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: daniel
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class);
    private static Map<Integer, ClientConfiguration> clients = new HashMap<Integer, ClientConfiguration>();

    public static void main(String[] args) {
        try {

            // Create the acceptor
            NioSocketAcceptor acceptor = new NioSocketAcceptor();

            // Add two filters : a logger and a codec
            // acceptor.getFilterChain().addLast("logger", new LoggingFilter());
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

            // Attach the business logic to the server
            acceptor.setHandler(new MessageListener());

            // Configurate the buffer size and the iddle time
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

            // And bind !
            Properties listeners = loadListenersConfig();
            int i = 0;
            while(1 == 1) {
                String port = listeners.getProperty("listener.port." + i);
                if (port == null)
                    break;
                String description = listeners.getProperty("listener.description." + i);
                clients.put(Integer.valueOf(port), new ClientConfiguration(Integer.valueOf(port), description));
                logger.info("start " + description + " on port " + port);
                acceptor.bind(new InetSocketAddress(Integer.valueOf(port)));
                ++i;

            }

            ConnectorSocket.getInstance().connect();

            ISOMsg echo = MessageFactory.createEchoISOMsg();
            ConnectorSocket.getInstance().sendMessage(echo);

        } catch(Throwable e) {
            logger.fatal("cannot start message listener", e);
        }
    }

    private static Properties loadListenersConfig() throws IOException {
        URL url = Loader.getResource("listeners.properties");
        InputStream in = url.openStream();
        Properties props = new Properties();
        props.load(in);
        in.close();
        return props;
    }

    /**
     *
     * @param port
     * @return ClientConfiguration or null;
     */
    public static ClientConfiguration getClientConfiguration(final Integer port) {
        return clients.get(port);
    }
}
