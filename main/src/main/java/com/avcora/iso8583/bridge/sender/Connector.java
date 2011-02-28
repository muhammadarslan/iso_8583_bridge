package com.avcora.iso8583.bridge.sender;

import com.avcora.iso8583.bridge.common.Constants;
import com.avcora.iso8583.bridge.common.MessageFactory;
import com.solab.iso8583.IsoMessage;
import org.apache.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 *
 */
public class Connector {

    private static final Logger logger = Logger.getLogger(Connector.class);

    private static final Connector INSTANCE = new Connector();

    public static Connector getInstance() {
        return INSTANCE;
    }

    private IoSession session;
    private NioSocketConnector connector;

    private Connector() {

    }

    public void connect() throws Exception {
        connector = new NioSocketConnector();
        InetSocketAddress remote = new InetSocketAddress(Constants.SERVER_IP, Constants.SERVER_PORT);

        // Configure the service.
        connector.setConnectTimeoutMillis(Constants.CONNECTION_TIMEOUT * 1000);
        //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        ObjectSerializationCodecFactory codec = new ObjectSerializationCodecFactory();
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codec));
        //connector.getFilterChain().addLast("logger", new LoggingFilter());

        connector.setHandler(new MessageHandler());

        logger.info("connecting to " + Constants.SERVER_IP + ":" + Constants.SERVER_PORT);
        int i = 1;
        for (; ;) {
            try {
                logger.info("connection attempt " + i++);
                ConnectFuture future = connector.connect(remote);
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                logger.error("connection failed", e);
                Thread.sleep(5000);
            }
        }
    }

    public void close() {
        if (session == null)
            return;
        session.getCloseFuture().awaitUninterruptibly();
        logger.info("connection closed");
        connector.dispose();
    }

    public void sendMessage(IsoMessage msg) {
        if (session == null)
            throw new RuntimeException("not connected");
        session.write(msg.writeData());
    }

    public void sendMessage(ISOMsg msg) throws ISOException, IOException {
        if (session == null)
            throw new RuntimeException("not connected");
        ISOPackager packager = new ISO93APackager();
        msg.setPackager(packager);
        byte[] data = msg.pack();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeUTF(new String(data));
        dataOut.flush();
        dataOut.close();
        byteOut.close();
        logger.info("Send ISOMsg\t" + new String(byteOut.toByteArray()));
        session.write(new String(byteOut.toByteArray()));
    }

    public void sendMessage(String msg) {
        if (session == null)
            throw new RuntimeException("not connected");
        session.write(msg);
    }
}
