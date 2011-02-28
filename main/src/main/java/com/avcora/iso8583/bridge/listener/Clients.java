package com.avcora.iso8583.bridge.listener;

import com.avcora.iso8583.bridge.common.MessageFactory;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: daniel
 */
public class Clients {

    private static final Logger logger = Logger.getLogger(Clients.class);

    private static Map<String, IoSession> clients = new HashMap<String, IoSession>();

    public static void sendResponse(String strMsg) throws ISOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);

            ISOPackager packager = new ISO93APackager();
            ISOMsg msg = new ISOMsg();
            msg.setPackager(packager);
            msg.unpack(strMsg.getBytes());
            msg.dump(ps, "");

            String response = new String(out.toByteArray());
            logger.info("response:\n\t" + response);
            IoSession session = clients.get(msg.getString(11));
            if (session == null) {
                logger.warn("no client waiting for response");
                return;
            }

            if (isEchoResponse(msg))
                sendSignOnOrKeyExchange(msg, session);
            else
                session.write(response);
        } catch (Throwable e) {
            logger.error("cannot respond to client", e);
        }
    }

    public static void addClient(ISOMsg msg, IoSession session) {
        clients.put(msg.getString(11), session);
    }

    public static void removeClient(IoSession session) {
        clients.values().remove(session);
    }

    private static boolean isEchoResponse(ISOMsg msg) throws ISOException {
        return String.valueOf(MessageFactory.ECHO_RESPONSE_MTI).equals(msg.getMTI());
    }

    private static void sendSignOnOrKeyExchange(ISOMsg msg, IoSession session) throws Exception {
        if (MessageFactory._24_ECHO.equals(msg.getString(24)))
            sendSignOn(msg, session);
        if (MessageFactory._24_SIGN_ON.equals(msg.getString(24)))
            sendKeyExchange(msg, session);
    }

    private static void sendSignOn(ISOMsg msg, IoSession session) throws Exception {
        ISOMsg signOn = MessageFactory.createSignOnISOMsg(msg);
        MessageListener.forwardToFinancialSwitch(signOn, session);
    }

    private static void sendKeyExchange(ISOMsg msg, IoSession session) throws Exception {
        ISOMsg keyEx = MessageFactory.createKeyExchangeISOMsg(msg);
        // TODO uncomment when MessageFactory.createKeyExchangeISOMsg() is implemented
        // MessageListener.forwardToFinancialSwitch(keyEx, session);
    }
}
