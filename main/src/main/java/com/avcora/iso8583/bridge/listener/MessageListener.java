package com.avcora.iso8583.bridge.listener;

import com.avcora.iso8583.bridge.common.DesUtils;
import com.avcora.iso8583.bridge.common.Field;
import com.avcora.iso8583.bridge.common.MessageFactory;
import com.avcora.iso8583.bridge.common.MessageLogger;
import com.avcora.iso8583.bridge.sender.ConnectorSocket;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import sun.security.provider.PolicySpiFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: daniel
 */
public class MessageListener extends IoHandlerAdapter {

    private static final Logger logger = Logger.getLogger(MessageListener.class);

    private DocumentBuilder builder = getDocumentBuilder();

    public static final Integer PAN_KEY = 2;
    public static final Integer PIN_KEY = 52;

    @Override
    public void sessionOpened(IoSession session) throws java.lang.Exception {
        logger.info("new client connection on local address " + session.getLocalAddress());
        /*ISOMsg echo = MessageFactory.createEchoISOMsg();
        forwardToFinancialSwitch(echo, session);*/
    }

    @Override
    public void sessionClosed(IoSession session) throws java.lang.Exception {
        Clients.removeClient(session);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.error("error in message listener", cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String str = message.toString();

        logger.info("received new message:");
        logger.info(str);
        logger.info("--------------------------");

        //add in log files
        try {
            Integer port = ((InetSocketAddress) session.getLocalAddress()).getPort();
            Logger messageLogger = MessageLogger.getLogger(port);
            messageLogger.info("Message sent at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
            messageLogger.info(str);
        } catch (Exception e) {
            logger.error("", e);
        }


        Document document = createDocument(str);
        List<Field> fields = parseFields(document);

        ISOMsg isoMessage = MessageFactory.createJPOSMessageFromFields(fields);

        if (isoMessage.getString(PAN_KEY) != null && isoMessage.getString(PAN_KEY).trim().length() > 0)
            transformPANMessage(isoMessage, session);
        else
            forwardToFinancialSwitch(isoMessage, session);
    }

    private void transformPANMessage(ISOMsg isoMessage, IoSession session) throws Exception {
        String panNumber = isoMessage.getString(PAN_KEY);
        String pinNumber = isoMessage.getString(PIN_KEY);

        String s1 = panNumber.substring(panNumber.length() - 12);
        s1 = "0000" + s1;

        String s2 = "04" + pinNumber + "FFFFFFFFFF";

        String result = xor(s1, s2);

        String encryptionKey = DesUtils.decrypt(Clients.KEY_EX_ENCODER_CODED);
        result = DesUtils.encrypt(result, encryptionKey);

        isoMessage.set(PIN_KEY, result);
        isoMessage.unset(PAN_KEY);

        forwardToFinancialSwitch(isoMessage, session);

    }

    public String xor(String str, String key) {
        String result = null;
        byte[] strBuf = str.getBytes();
        byte[] keyBuf = key.getBytes();
        int c = 0;
        int z = keyBuf.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(strBuf.length);
        for (int i = 0; i < strBuf.length; i++) {
            byte bS = strBuf[i];
            byte bK = keyBuf[c];
            byte bO = (byte)(bS ^ bK);
            if (c < z - 1) {
                c++;
            } else {
                c = 0;
            }
            baos.write(bO);
        }
        try {
            baos.flush();
            result = baos.toString();
            baos.close();
            baos = null;
        } catch (IOException ioex) {
        }
        return result;
    }

    public static void forwardToFinancialSwitch(ISOMsg isoMessage, IoSession session) throws Exception {
        Clients.addClient(isoMessage, session);
        ConnectorSocket.getInstance().sendMessage(isoMessage);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        logger.info("IDLE " + session.getIdleCount(status));
    }

    private List<Field> parseFields(Document document) {
        NodeList nodes = document.getElementsByTagName("field");
        int count = nodes.getLength();
        List<Field> fields = new ArrayList<Field>(count);
        for (int i = 0; i < count; ++i) {
            NamedNodeMap attributes = nodes.item(i).getAttributes();
            String id = attributes.getNamedItem("id").getTextContent();
            String value = attributes.getNamedItem("value").getTextContent();
            fields.add(new Field(Integer.valueOf(id), value));
        }
        return fields;
    }

    private Document createDocument(String str) {
        DocumentBuilder builder = getDocumentBuilder();
        Document document = null;
        try {
            document = builder.parse(IOUtils.toInputStream(str));
        } catch (Throwable e) {
            logger.error("cannot parse xml string", e);
        }
        return document;
    }

    private DocumentBuilder getDocumentBuilder() {
        if (builder != null)
            return builder;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (Throwable e) {
            logger.error("cannot create document builder", e);
        }
        return builder;
    }

}
