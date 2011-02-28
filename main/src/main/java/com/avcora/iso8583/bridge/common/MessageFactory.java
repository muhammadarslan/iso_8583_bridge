package com.avcora.iso8583.bridge.common;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO93APackager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class MessageFactory {

    private static final Logger logger = Logger.getLogger(MessageFactory.class);

    private static final String SIMPLE_DATE_FORMAT = "MMddhhmmss";
    private static final String FULL_DATE_FORMAT = "yyMMddhhmmss";
    private static final SimpleDateFormat SIMPLE_FORMATTER = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
    private static final SimpleDateFormat FULL_FORMATTER = new SimpleDateFormat(FULL_DATE_FORMAT);

    public static final Integer ECHO_MTI = 1804;
    public static final Integer ECHO_RESPONSE_MTI = 1814;
    public static final String _24_ECHO = "831";
    public static final String _24_SIGN_ON = "801";

    private static final String PATH_TO_STAN_PROPERTIES = Constants.DATA_DIR + File.separator + "stan.properties";
    private static final Integer STAN_MIN = 1;
    private static final Integer STAN_MAX = 999999;
    private static final String STAN_PROPERTY = "stan.current.value";

    public static IsoMessage createMessageFromFields(List<Field> fields) {
        Field type = (Field) CollectionUtils.find(fields, PredicateUtils.equalPredicate(new Field(0, "")));

        IsoMessage msg = new com.solab.iso8583.MessageFactory().newMessage(Integer.valueOf(type.getValue()));
        for (Field next : fields) {
            if (next.getId().equals(type.getId()))
                continue;
            String value = next.getValue();
            IsoValue<String> isoValue = new IsoValue<String>(IsoType.ALPHA, value, ((String) value).length());
            msg.setField(next.getId(), isoValue);
        }

        logger.info("iso message of type " + type.getValue() + " ready");

        return msg;
    }

    public static ISOMsg createJPOSMessageFromFields(List<Field> fields) throws ISOException {
        Field type = (Field) CollectionUtils.find(fields, PredicateUtils.equalPredicate(new Field(0, "")));

        ISOMsg msg = new ISOMsg(Integer.valueOf(type.getValue()));
        for (Field next : fields) {
            /*if (next.getId().equals(type.getId()))
                continue;*/
            String value = next.getValue();
            msg.set(next.getId(), value);
        }

        logger.info("ISOMsg message of type " + type.getValue() + " ready");

        return msg;
    }

    public static ISOMsg createEchoISOMsg() throws ISOException, IOException {
        ISOMsg msg = new ISOMsg(ECHO_MTI);
        msg.set(0, String.valueOf(ECHO_MTI));
        msg.set(7, getCurrentDateTime(SIMPLE_FORMATTER));
        msg.set(11, getSTAN());
        msg.set(12, getCurrentDateTime(FULL_FORMATTER));
        msg.set(24, _24_ECHO);
        //msg.set() TODO add field id 59 value 476274343
        return msg;
    }

    public static ISOMsg createSignOnISOMsg(ISOMsg response) throws ISOException, IOException {
        ISOMsg msg = new ISOMsg(ECHO_MTI);
        msg.set(0, String.valueOf(ECHO_MTI));
        msg.set(7, getCurrentDateTime(SIMPLE_FORMATTER));
        msg.set(11, response.getString(11));
        msg.set(12, getCurrentDateTime(FULL_FORMATTER));
        msg.set(24, _24_SIGN_ON);
        return msg;
    }

    public static ISOMsg createKeyExchangeISOMsg(ISOMsg response) throws ISOException, IOException {
        // TODO create key exchange message
        return null;
    }

    private static String getCurrentDateTime(SimpleDateFormat format) {
        return format.format(new Date());
    }

    private static String getSTAN() throws IOException {
        String value = readSTAN();
        Integer current = value == null ? null : Integer.valueOf(value);
        if (current == null || STAN_MAX.equals(current))
            current = STAN_MIN;
        else
            ++current;
        saveSTAN(current);
        return pad(current);
    }

    private static String pad(Integer value) {
        String strValue = String.valueOf(value);
        int length = strValue.length();
        int pads = 6 - length;
        StringBuffer padded = new StringBuffer(strValue);
        for (int i = 0; i < pads; ++i)
            padded.insert(0, "0");
        return padded.toString();
    }

    private static String readSTAN() throws IOException {
        Properties props = new Properties();
        InputStream in = new FileInputStream(PATH_TO_STAN_PROPERTIES);
        props.load(in);
        in.close();

        return props.getProperty(STAN_PROPERTY);
    }

    private static void saveSTAN(Integer value) throws IOException {
        OutputStream out = new FileOutputStream(PATH_TO_STAN_PROPERTIES);
        Properties props = new Properties();
        props.setProperty(STAN_PROPERTY, value + "");
        props.store(out, "");
        out.close();
    }
}
