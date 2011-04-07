package com.avcora.iso8583.bridge.listener;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * @author: daniel
 */
public class MessageListenerTest {

    private MessageListener messageListener;

    private static final String PAN_NUMBER = "5021520000000004012";
    private static final String PIN_NUMBER = "1234";
    @Before
    public void setUp() throws Exception {
        messageListener = new MessageListener();
    }

    @Test
    public void transformPanMessageTest() {

    }

    @Test
    public void createPANBlock() {
        String s = messageListener.createPANBlock(PAN_NUMBER, PIN_NUMBER);
        System.out.println("s - " + s);
        assertNotNull(messageListener.createPANBlock(PAN_NUMBER, PIN_NUMBER));
    }


}
