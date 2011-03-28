package com.avcora.iso8583.bridge.listener;

import com.avcora.iso8583.bridge.common.MessageFactory;
import com.avcora.iso8583.bridge.common.MessageLogger;
import com.avcora.iso8583.bridge.sender.ConnectorSocket;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;
import sun.security.provider.PolicySpiFile;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: daniel
 */
public class Clients {

    private static final Logger logger = Logger.getLogger(Clients.class);

    private static Map<String, IoSession> clients = new HashMap<String, IoSession>();

    public static String KEY_EX_ENCODER_CODED;
    public static String KEY_EX_ENCODER_NOT_CODED;

    public static final String ENCRYPTION_KEY = "B16A8DC5127765C0B2127E77A9C01774";

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

            if (isEchoResponse(msg))
                sendSignOnOrKeyExchange(msg);
            else if (isKeyExMessage(msg))
                sendKeyExSecondMessage(msg, strMsg);
            else {
                IoSession session = clients.get(msg.getString(11));
                if (session == null) {
                    logger.warn("no client waiting for response");
                    return;
                }

                //add in log files
                try {
                    Integer port = ((InetSocketAddress) session.getLocalAddress()).getPort();
                    Logger messageLogger = MessageLogger.getLogger(port);
                    messageLogger.info("Response received at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    messageLogger.info(response);
                } catch (Exception e) {
                    logger.error("", e);
                }
                session.write(response);
            }


        } catch (Throwable e) {
            logger.error("cannot respond to client", e);
        }
    }

    private static void sendKeyExSecondMessage(ISOMsg msg, String strMsg) throws Exception {

        ISOMsg newMsg = MessageFactory.createKeyExSecondMessage(msg, strMsg.substring(70));

        KEY_EX_ENCODER_CODED = strMsg.substring(88, 88 + 32);

        PrintStream ps = new PrintStream(System.out);

        ISOPackager packager = new ISO93APackager();
        newMsg.setPackager(packager);
        newMsg.dump(ps, "");

        //MessageListener.forwardToFinancialSwitch(msg, session);
        ConnectorSocket.getInstance().sendMessage(newMsg);

    }

    private static boolean isKeyExMessage(ISOMsg msg) throws ISOException {
        System.out.println(String.valueOf(MessageFactory.ECHO_MTI).equals(msg.getMTI()));
        try {
        System.out.println(msg.getValue(96) != null);
        System.out.println(msg.getValue(96).toString().trim().length() > 0);
        } catch (Exception e) {}
        return  String.valueOf(MessageFactory.ECHO_MTI).equals(msg.getMTI()) && msg.getValue(96) != null
                && msg.getValue(96).toString().trim().length() > 0;
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

    private static void sendSignOnOrKeyExchange(ISOMsg msg) throws Exception {
        if (MessageFactory._24_ECHO.equals(msg.getString(24)))
            sendSignOn(msg);
        if (MessageFactory._24_SIGN_ON.equals(msg.getString(24)))
            sendKeyExInitMessage(msg);
    }

    private static void sendSignOn(ISOMsg msg) throws Exception {
        ISOMsg signOn = MessageFactory.createSignOnISOMsg(msg);
        //MessageListener.forwardToFinancialSwitch(signOn, session);
        ConnectorSocket.getInstance().sendMessage(signOn);
    }
    private static void sendKeyExInitMessage(ISOMsg msg) throws Exception {
        ISOMsg keyExInitMessage = MessageFactory.createKeyExInitMessage(msg);

        PrintStream ps = new PrintStream(System.out);

        ISOPackager packager = new ISO93APackager();
        keyExInitMessage.setPackager(packager);
        keyExInitMessage.dump(ps, "");

        //MessageListener.forwardToFinancialSwitch(keyExInitMessage, session);
        ConnectorSocket.getInstance().sendMessage(keyExInitMessage);
    }
}
