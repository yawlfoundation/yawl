/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

public class URLPassword extends Frame {

  private TextField tf = new TextField();
  private TextArea  ta = new TextArea();

  public URLPassword() {

    super ("URL Password");

    // Install Authenticator
    Authenticator.setDefault (new MyAuthenticator ());

    // Setup screen
    add (tf, BorderLayout.NORTH);
    ta.setEditable(false);
    add (ta, BorderLayout.CENTER);
    tf.addActionListener (new ActionListener() {
      public void actionPerformed (ActionEvent e) {
        String s = tf.getText();
        if (s.length() != 0)
          ta.setText (fetchURL (s));
      }
    });
    addWindowListener (new WindowAdapter() {
      public void windowClosing (WindowEvent e) {
        dispose();
        System.exit(0);
      }
    });
  }

  private String fetchURL (String urlString) {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter(sw);

    try {
System.getProperties().put("http.proxyHost", "proxy.yawlfoundation.org");
System.getProperties().put("http.proxyPort", "3128");

      URL url = new URL (urlString);
      InputStream content = (InputStream)url.getContent();
      BufferedReader in   =
        new BufferedReader (new InputStreamReader (content));
      String line;
      while ((line = in.readLine()) != null) {
        pw.println (line);
      }
    } catch (MalformedURLException e) {
      pw.println ("Invalid URL");
    } catch (IOException e) {
      pw.println ("Error reading URL");
        e.printStackTrace();
    }
    return sw.toString();
  }


  public static void main (String args[]) {
    Frame f = new URLPassword();
    f.setSize(300, 300);
    f.setVisible (true);
  }

  class MyAuthenticator extends Authenticator {
    protected PasswordAuthentication getPasswordAuthentication() {
      final Dialog jd = new Dialog (URLPassword.this, "Enter password", true);
      jd.setLayout (new GridLayout (0, 1));
      Label jl = new Label (getRequestingPrompt());
      jd.add (jl);
      TextField username = new TextField();
      username.setBackground (Color.lightGray);
      jd.add (username);
      TextField password = new TextField();
      password.setEchoChar ('*');
      password.setBackground (Color.lightGray);
      jd.add (password);
      Button jb = new Button ("OK");
      jd.add (jb);
      jb.addActionListener (new ActionListener() {
        public void actionPerformed (ActionEvent e) {
          jd.dispose();
        }
      });
      jd.pack();
      jd.setVisible(true);
      return new PasswordAuthentication (username.getText(), password.getText().toCharArray());
    }
  }
}

