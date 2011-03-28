package com.avcora.iso8583.bridge.common;

import java.security.spec.*;
import javax.crypto.SecretKey.*;
import javax.crypto.*;
import java.io.*;
import javax.crypto.spec.*;


public class DesUtils {

    public static String decrypt(String msg) {
        byte[] theMsg = hexToBytes(msg); //Put your key here
        byte[] theKey = null;

        String key = "B16A8DC5127765C0B2127E77A9C01774";
        byte[] theCph = null;
        try {

            if (key.length() == 16) {
                theKey = hexToBytes(key);
                KeySpec ks = new DESKeySpec(theKey);
                SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
                SecretKey ky = kf.generateSecret(ks);
                Cipher cf = Cipher.getInstance("DES/ECB/NoPadding");
                cf.init(Cipher.DECRYPT_MODE, ky);
                theCph = cf.doFinal(theMsg);

            } else if (key.length() == 32) {

                String p = key;
                String padding = new String(p.substring(0, 16));
                theKey = hexToBytes(key + padding);
                DESedeKeySpec ks = new DESedeKeySpec(theKey);
                SecretKeyFactory kf = SecretKeyFactory.getInstance("DESede");
                SecretKey ky = kf.generateSecret(ks);
                Cipher cf = Cipher.getInstance("DESede/ECB/NoPadding");
                cf.init(Cipher.DECRYPT_MODE, ky);
                theCph = cf.doFinal(theMsg);
            } else {
                System.out.println("The length of the message is not correct!");
                return null;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytesToHex(theCph);
    }

    public static String encrypt(String msg, String key) {
        byte[] theMsg = hexToBytes(msg);
        byte[] theKey = null;
        byte[] theCph = null;
        try {

            theKey = hexToBytes(key);
            KeySpec ks = new DESKeySpec(theKey);
            SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
            SecretKey ky = kf.generateSecret(ks);
            Cipher cf = Cipher.getInstance("DES/ECB/NoPadding");
            cf.init(Cipher.ENCRYPT_MODE, ky);
            theCph = cf.doFinal(theMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytesToHex(theCph);
    }

    public static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    public static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        } else {
            int len = data.length;
            String str = "";
            for (int i = 0; i < len; i++) {
                if ((data[i] & 0xFF) < 16) str = str + "0"
                        + java.lang.Integer.toHexString(data[i] & 0xFF);
                else str = str
                        + java.lang.Integer.toHexString(data[i] & 0xFF);
            }
            return str.toUpperCase();
        }
    }
}