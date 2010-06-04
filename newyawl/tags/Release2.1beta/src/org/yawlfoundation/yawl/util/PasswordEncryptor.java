package org.yawlfoundation.yawl.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides simple one-way encryption for passwords
 *
 * @author Michael Adams
 * Date: 9/06/2008
 */

public class PasswordEncryptor {

    private PasswordEncryptor() { }

    public static synchronized String encrypt(String text)
                        throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(text.getBytes("UTF-8"));
        byte raw[] = md.digest();
        return new Base64(-1).encodeToString(raw);            // -1 means no line breaks
    }

    
    public static synchronized String encrypt(String text, String defText) {
        if (defText == null) defText = text;
        try {
            return encrypt(text);
        }
        catch (Exception e) {
            return defText;
        }
    }

}
