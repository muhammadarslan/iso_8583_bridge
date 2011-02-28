package com.avcora.iso8583.bridge.sender;

import org.apache.log4j.Logger;

/**
 *
 */
public class Main {

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        try {
            Connector.getInstance().connect();
            //IsoMessage msg = MessageFactory.createRequestMessage();
            String msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<isomsg>" +
                    "    <field id=\"0\" value=\"0800\"/>" +
                    "    <field id=\"11\" value=\"000001\"/>" +
                    "    <field id=\"43\" value=\"YOURCOMPANYNAME\"/>" +
                    "    <field id=\"70\" value=\"301\"/>" +
                    "</isomsg>";
            Connector.getInstance().sendMessage(msg);
        } catch(Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {
            Connector.getInstance().close();
        }
    }
}
