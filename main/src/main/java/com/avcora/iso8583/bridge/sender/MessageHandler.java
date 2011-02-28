package com.avcora.iso8583.bridge.sender;

import com.solab.iso8583.IsoMessage;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.io.DataInputStream;

/**
 *
 */
public class MessageHandler implements IoHandler {

    private static final Logger logger = Logger.getLogger(MessageHandler.class);

    @Override
    public void messageReceived(IoSession ioSession, Object o) throws Exception {
        logger.info("message received: " + o);
        //new com.solab.iso8583.MessageFactory().parseMessage()

    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        logger.info("message sent: ");
        if (o instanceof byte[]) {
            byte[] data = (byte[]) o;
            ByteInputStream byteIn = new ByteInputStream(data, data.length);
            DataInputStream dataIn = new DataInputStream(byteIn);
            String s = dataIn.readUTF();
            logger.info("\t" + s);
        } else {
            logger.info("\t" + o);
        }
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        logger.info("SESSION CREATED");
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
        logger.info("SESSION OPENED");
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        logger.info("SESSION CLOSED");
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {
        logger.info("IDLE");
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        logger.error("error in message handler", throwable);
    }
}
