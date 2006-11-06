/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.services.email;

import java.util.Date;
import java.util.Properties;

import javax.jws.WebService;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.services.NexusService;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * @author Nathan Rose
 */
@WebService(endpointInterface="com.nexusbpm.services.NexusService", serviceName="EmailSenderService")
public class EmailSenderService implements NexusService {
	private static final Log LOG = LogFactory.getLog( EmailSenderService.class );
	
    public static final String MAILER = "NEXUS_EMAIL_SENDER";
    
	/**
	 * Creates a new EmailSender.
	 */
	public EmailSenderService() {
	}
    
	private Session getSession( String host ) {
		Properties properties = new Properties();
		properties.put( "mail.smtp.host", host );
		Session session = Session.getDefaultInstance( properties, null );
		session.setDebug( false );
		LOG.info( session.getProperties() );
        return session;
	}
	
	public NexusServiceData execute( NexusServiceData data ) {
		String to = null;
		String cc = null;
		String bcc = null;
        String from = null;
		String subject = null;
		String body = null;
        String host = null;
		StringBuffer b = new StringBuffer();
		
		try {
			to = data.getPlain( "toAddress" );
			cc = data.getPlain( "ccAddress" );
			bcc = data.getPlain( "bccAddress" );
            from = data.getPlain( "fromAddress" );
			subject = data.getPlain( "subject" );
			body = data.getPlain( "body" );
            host = data.getPlain( "host" );
			
			b.append( "to: " ).append( to ).append( "\n" )
				.append( "cc: " ).append( cc ).append( "\n" )
				.append( "bcc: " ).append( bcc ).append( "\n" )
                .append( "from: " ).append( from ).append( "\n" )
				.append( "subject: " ).append( subject ).append( "\n" )
                .append( "host: " ).append( host );
			
			LOG.debug( b.toString() );
			
			send( to, cc, bcc, from, subject, body, host );
		}
		catch( Exception e ) {
			LOG.error( "Error sending email!\n" + b.toString(), e.getCause() );
		}
		
		return data;
	}
	
	/**
	 * Sends an email with the given parameters.
	 * @param to      the address that the email is to.
	 * @param cc      the CC address for the email.
	 * @param bcc     the BCC address for the email.
	 * @param subject the subject of the email to send.
	 * @param body    the body of the email to send.
	 */
	public void send( String to, String cc, String bcc, String from, String subject, String body, String host )
        throws AddressException, MessagingException {
		Message message = null;
		message = new MimeMessage( getSession( host ) );
		message.setFrom( new InternetAddress( from ) );
		message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( to, false ) );
		if( cc != null ) {
			message.setRecipients( Message.RecipientType.CC, InternetAddress.parse( cc, false ) );
		}//if
		if( bcc != null ) {
			message.setRecipients( Message.RecipientType.BCC, InternetAddress.parse( bcc, false ) );
		}//if
		message.setSubject( subject );
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setText( body );
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart( bodyPart );
		message.setContent( multipart );
		message.setHeader( "X-Mailer", MAILER );
		message.setSentDate( new Date() );
		Transport.send( message );
		LOG.debug( "sent email to '" + to + "' with subject '" + subject + "'" );
	}
}
