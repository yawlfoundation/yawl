package org.yawlfoundation.yawl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public class CheckSummer {


    public String getMD5Hex(String fileName) throws IOException {
        return getMD5Hex(new File(fileName));
    }


    public String getMD5Hex(File file) throws IOException {
        return getMD5Hex(new FileInputStream(file));
    }


    public boolean compare(String fileName, String hexToCompare) throws IOException {
        return getMD5Hex(fileName).equals(hexToCompare);
    }


    public boolean compare(File file, String hexToCompare) throws IOException {
        return getMD5Hex(file).equals(hexToCompare);
    }


    public boolean compare(FileInputStream fis, String hexToCompare) throws IOException {
        return getMD5Hex(fis).equals(hexToCompare);
    }


    public static String getMD5Hex(FileInputStream fis) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            return String.format("%032x", new BigInteger(1, hash));
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new IOException(nsae.getMessage());
        }
    }

}
