package org.yawlfoundation.yawl.resourcing.util;

import sun.misc.BASE64Encoder;

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

    private static PasswordEncryptor _me;

    private PasswordEncryptor() { }


    public static synchronized PasswordEncryptor getInstance() {
        if (_me == null) _me = new PasswordEncryptor();
        return _me;
    }


    public synchronized String encrypt(String text)
                        throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(text.getBytes("UTF-8"));
        byte raw[] = md.digest();
        return (new BASE64Encoder()).encode(raw);
    }

}
