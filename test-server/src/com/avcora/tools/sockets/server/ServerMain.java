package com.avcora.tools.sockets.server;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 *
 */
public class ServerMain {

    private static final Logger logger = Logger.getLogger(ServerMain.class);

    public static void main(String[] args) throws IOException {
        // Create the acceptor
        IoAcceptor acceptor = new NioSocketAcceptor();

        // Add two filters : a logger and a codec
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

        // Attach the business logic to the server
        acceptor.setHandler(new ServerHandler());

        // Configurate the buffer size and the iddle time
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        // And bind !
        logger.info("binding");
        acceptor.bind(new InetSocketAddress(5814));
    }
}
