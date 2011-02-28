package com.avcora.iso8583.bridge.sender;

import com.avcora.iso8583.bridge.common.Field;
import com.avcora.iso8583.bridge.common.MessageFactory;
import com.solab.iso8583.IsoMessage;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: daniel
 */
public class TestIsoMessage {

    private static final Logger logger = Logger.getLogger(TestIsoMessage.class);

    private static DocumentBuilder builder = getDocumentBuilder();


    public static void main(String[] args) throws ParseException, IOException, ISOException {
        String str = "<isomsg><field id=\"0\" value=\"1804\"/><field id=\"7\" value=\"0331094941\"/><field id=\"11\" value=\"304317\"/><field id=\"12\" value=\"105406\"/><field id=\"24\" value=\"831\"/></isomsg>";
        Document document = createDocument(str);
        List<Field> fields = parseFields(document);
        ISOPackager packager = new ISO93APackager();
        ISOMsg isoMessage = MessageFactory.createJPOSMessageFromFields(fields);
        isoMessage.setPackager(packager);
        //ISOMsg msg = new ISOMsg();
        String s = new String(isoMessage.pack());
        System.out.println(s);
        //isoMessage.dump(new PrintStream(System.out), "");
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        dataOut.writeUTF(s);
        dataOut.flush();
        dataOut.close();
        byteOut.close();
        System.out.println(new String(byteOut.toByteArray()));

    }

    private static List<Field> parseFields(Document document) {
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

    private static Document createDocument(String str) {
        DocumentBuilder builder = getDocumentBuilder();
        Document document = null;
        try {
            document = builder.parse(IOUtils.toInputStream(str));
        } catch (Throwable e) {
            logger.error("cannot parse xml string", e);
        }
        return document;
    }

    private static DocumentBuilder getDocumentBuilder() {
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
