/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.mailSender;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class MailSender extends InterfaceBWebsideController {


    //private static String _sessionHandle = null;
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem){
    	
    }

    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord){
    }
    class MyAuthenticator extends Authenticator
    {  
    	String smtpUsername = null;
    	String smtpPassword = null;
     
    	public MyAuthenticator(String username, String password)
    	{
    		smtpUsername = username;
    		smtpPassword = password;
    	}
     
     
    	protected PasswordAuthentication getPasswordAuthentication()
    	{
    		return new PasswordAuthentication(smtpUsername,smtpPassword);
    	}
    }
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WebMail").forward(req, resp);
    }
    public void SendEmail(String SMTP, String Port, String Login, String password, String To, String Alias, String subject, String content, String filename)
    {
    	String _Pathway = System.getenv("CATALINA_HOME")+ "/webapps/mailSender/files/";
    	try{
			File file = new File(_Pathway, "SMTP.xml");
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        org.w3c.dom.Document doc = db.parse(file);
	        doc.getDocumentElement().normalize();

			System.out.println("Root element " + doc.getDocumentElement().getNodeName());
	        NodeList nodeLst = doc.getElementsByTagName("SMTP");
	        for (int s = 0; s < nodeLst.getLength(); s++) {
    	 
	            Node fstNode = nodeLst.item(s);
         
	            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

	              org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
        
	              NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("SMTP_Address");
	              org.w3c.dom.Element fstNmElmnt = (org.w3c.dom.Element) fstNmElmntLst.item(0);
	              NodeList fstNm = fstNmElmnt.getChildNodes();
	              System.out.println("SMTP_Address : "  + ((Node) fstNm.item(0)).getNodeValue());
				  SMTP = ((Node) fstNm.item(0)).getNodeValue();
				  System.out.println("SMTP_Address : " + SMTP);

	              NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("Port");
	              org.w3c.dom.Element lstNmElmnt = (org.w3c.dom.Element) lstNmElmntLst.item(0);
	              NodeList lstNm = lstNmElmnt.getChildNodes();
	              System.out.println("Port : " + ((Node) lstNm.item(0)).getNodeValue());
				  Port=((Node) lstNm.item(0)).getNodeValue();
				  System.out.println("Port : " + Port);
	            }
	      }
    	}catch  (Exception e){}

    	 Properties props = new Properties();
        // props.setProperty("mail.transport.protocol", "smtp");

         props.setProperty("mail.smtp.host", SMTP);
         props.setProperty("mail.smtp.port", Port);
         props.setProperty("mail.smtp.auth", "true");
         props.setProperty("mail.debug", "true");
         props.setProperty("mail.smtp.starttls.enable", "true");

        if(Port.compareTo("25") != 0 ){
         props.setProperty("mail.smtp.socketFactory.port", Port);
         props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.setProperty("mail.smtp.socketFactory.fallback", "false");
        }

         System.out.println("STEP passed");
     try{
         MyAuthenticator auth = new MyAuthenticator (Login,password);
         Session session;
         session = Session.getDefaultInstance(props, auth);
         System.out.println("STEP passed");
         session.setDebug(true);
         System.out.println("STEP passed");
         Message message;
         message = new MimeMessage(session);
         System.out.println("STEP passed");
         InternetAddress addressFrom = new InternetAddress(Alias);
         //InternetAddress(_Id);
         message.setFrom(addressFrom);
         message.setSentDate(new java.util.Date());
         System.out.println("STEP passed");
         InternetAddress addressTo = new InternetAddress(To);
         message.addRecipient(Message.RecipientType.TO, addressTo);
         message.setSubject(subject);
         //message.setText(_Message);
         System.out.println("STEP passed");
         // create and fill the first message part
         BodyPart MessageContent = new MimeBodyPart();
         MessageContent.setText(content);
        
         // set the message body of email 
         final Multipart mimemultipart = new MimeMultipart();
         mimemultipart.addBodyPart(MessageContent);
         if(filename != null) {
             MimeBodyPart mimebodypart1 = new MimeBodyPart();
             FileDataSource filedatasource = new FileDataSource(System.getenv("CATALINA_HOME") + "/webapps/mailSender/files/" + filename);
             mimebodypart1.setDataHandler(new DataHandler(filedatasource));
             mimebodypart1.setFileName(filename);
             mimebodypart1.setDisposition("inline");
             System.out.println(mimebodypart1.getEncoding());
             mimemultipart.addBodyPart(mimebodypart1);
         }
         message.setContent(mimemultipart);
        
         Transport.send(message);
     }catch (Exception e) {e.printStackTrace();}
    }
}