package com.avcora.iso8583.bridge.sender;

import com.avcora.iso8583.bridge.common.Constants;
import com.avcora.iso8583.bridge.listener.Clients;
import com.avcora.iso8583.bridge.listener.MessageListener;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;

import java.io.*;
import java.net.Socket;

/**
 * @author: daniel
 */
public class ConnectorSocket {

    private static final Logger logger = Logger.getLogger(ConnectorSocket.class);

    private Socket socket;
    private DataOutputStream out;

    private static final ConnectorSocket INSTANCE = new ConnectorSocket();

    public static ConnectorSocket getInstance() {
        return INSTANCE;
    }

    private ConnectorSocket() {

    }

    public void connect() throws Exception {
        logger.info("connecting to " + Constants.SERVER_IP + ":" + Constants.SERVER_PORT);
        int i = 1;
        for (; ;) {
            try {
                logger.info("connection attempt " + i++);
                socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
                //socket.setReceiveBufferSize(260000);
                //socket.setSendBufferSize(260000);
                out = new DataOutputStream(socket.getOutputStream());

                startListening();
                break;
            } catch (Throwable e) {
                logger.error("connection failed", e);
                Thread.sleep(5000);
            }
        }
    }

    public void sendMessage(ISOMsg msg) throws Exception {
        if (!socket.isConnected() || socket.isClosed())
            connect();

        ISOPackager packager = new ISO93APackager();
        msg.setPackager(packager);
        byte[] data = msg.pack();
        logger.info("Send ISOMsg\t" + new String(data));
        out.writeUTF(new String(data));
        out.flush();
    }

    public void close() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed())
            return;
        socket.close();

    }

    private void startListening() {
        Thread listener = new Listener(socket);
        listener.start();
    }

    private static class Listener extends Thread {

        private Socket socket;

        private DataInputStream in;

        public Listener(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                if (socket != null && socket.isConnected()) {
                    while (true) {
                        try {
                            logger.info("waiting for message ...");
                            String msg = in.readUTF();
                            if (msg != null) {
                                logger.info("received message:");
                                logger.info("\t" + msg);
                                Clients.sendResponse(msg);
                            } else {
                                Thread.sleep(1000);
                            }
                        } catch(Throwable e) {
                            logger.error("error reading financial switch message", e);
                        }
                    }
                }
            } catch (Throwable e) {
                if (socket == null || socket.isClosed())
                    logger.warn("socket is closed");
                else
                    logger.error("cannot start listener", e);
            }
        }
    }
}