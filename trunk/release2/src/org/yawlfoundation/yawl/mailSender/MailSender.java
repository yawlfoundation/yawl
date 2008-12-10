package org.yawlfoundation.yawl.mailSender;

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
    public void SendEmail(String SMTP, String Login, String password, String To, String Alias, String subject, String content, String filename)
    {
    	
    	 String _SMTP = SMTP;
    	 String _Login = Login;
    	 String _Password = password;
    	 
         String _Message = content;
         String _Address = To;
         String _Subject = subject;
         
         
         String _Id = Alias;
        
    	 Properties props = new Properties();
         props.put("mail.smtp.host", _SMTP);
         props.put("mail.smtp.auth", "true");
         props.put("mail.debug", "true");
     try{
         MyAuthenticator auth = new MyAuthenticator (_Login,_Password);	                      
         Session session;
         session = Session.getDefaultInstance(props, auth);

         session.setDebug(true);

         Message message;
         message = new MimeMessage(session);
         
         InternetAddress addressFrom = new InternetAddress(_Id);
         //InternetAddress(_Id);
         message.setFrom(addressFrom);
         message.setSentDate(new java.util.Date());
         
         InternetAddress addressTo = new InternetAddress(_Address); 
         message.addRecipient(Message.RecipientType.TO, addressTo);
         message.setSubject(_Subject);
         //message.setText(_Message);
         
         // create and fill the first message part
         BodyPart MessageContent = new MimeBodyPart();
         MessageContent.setText(_Message);
        
         // set the message body of email 
         final Multipart mimemultipart = new MimeMultipart();
         mimemultipart.addBodyPart(MessageContent);
         if(filename == null);
         else
         {
             MimeBodyPart mimebodypart1 = null;
             mimebodypart1 = new MimeBodyPart();
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