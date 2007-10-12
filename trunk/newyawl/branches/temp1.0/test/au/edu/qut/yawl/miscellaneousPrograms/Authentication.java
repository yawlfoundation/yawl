/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.miscellaneousPrograms;

import javax.swing.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 11/03/2004
 * Time: 17:23:11
 * 
 */
public class Authentication {
    public static void doIt() {
        System.getProperties().put("http.proxyHost", "proxy.qut.edu.au");
        System.getProperties().put("http.proxyPort", "3128");
        final String userName = JOptionPane.showInputDialog("Enter UserName");
        final String password = JOptionPane.showInputDialog("Enter Password");
        class MyAuthenticator extends Authenticator {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        userName, password.toCharArray());
            }
        }
        Authenticator.setDefault(new MyAuthenticator());
    }
}
