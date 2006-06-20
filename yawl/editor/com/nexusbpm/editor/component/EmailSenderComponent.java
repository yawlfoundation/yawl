package com.nexusbpm.editor.component;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;

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

/**
 * The email sender component sends an email and then allows flow
 * execution to continue. <br>
 * 
 * 
 * <b>Use case:</b><br>
 * <p>Sends an email message with an optional file attachment and optional html
 * attachment. It is able to send the email to multiple email addresses that are
 * separated by commas in the EmailSenderEditor.  It is also allowable to modify
 * the from address field that users should reply to.  If both a FileAttribute
 * and MarkupAttachment have values, the email will be sent with two MIME body
 * parts, the first with the file, and the second with the html attachment.
 * 
 * @author             Dean Mao
 * @created            February 19, 2003
 * @hibernate.subclass discriminator-value="11"
 * @javabean.class     name="EmailSenderComponent"
 *                     displayName="Email Sender Component"
 */
public class EmailSenderComponent extends Component {

	private final static long serialVersionUID = 1907269838393868190L;
	private final static Log LOG = LogFactory.getLog( EmailSenderComponent.class );
	private final static String ATTRIBUTE_FILE_ATTACHMENT = "email_file_attachment";

	private String _body = "";
	private String _subject = "";
	private String _toAddresses = "";
	private String _fromAddress = "capsela@ichotelsgroup.com";
	private String _markupAttachment = "";

	/**
	 * Creates a new empty <code>EmailSenderComponent</code>, needed for Hibernate.
	 */
	public EmailSenderComponent() {
		super();
	}//EmailSenderComponent()
	/**
	 * Creates a new <code>EmailSenderComponent</code> with mandatory attributes.
	 * @param name the <code>EmailSenderComponent</code>'s name
	 * @param folder the <code>EmailSenderComponent</code>'s parent folder
	 */
	public EmailSenderComponent( String name) {
		super( name);
	}//EmailSenderComponent()
	/**
	 * @see com.ichg.capsela.framework.domain.DomainObject#construct()
	 */
	protected void construct() {
		super.construct();
//		setTypeID( new Long( ComponentType.EMAIL_SENDER ) );
	}//construct()

	/**
	 * @see Component#clientValidate(Collection)
	 */
	public Collection<ValidationMessage> clientValidate( Collection<ValidationMessage> validationErrors ) {
//		validationErrors = super.clientValidate( validationErrors );
		// The email sender should have a to-address.
		if( _toAddresses == null || _toAddresses.length() == 0 ) {
//			String name = this.getName();
//			String msg = "The email sender component '" + name + "' is missing a 'to address'.";
//			validationErrors.add( new ValidationMessage( msg, this, ValidationMessage.WARNING ) );
		}//if
		return validationErrors;
	}//clientValidate()

	/**
	 * @see com.ichg.capsela.domain.component.Component#run()
	 */
	public void run( )  {

		String cc = null;
		String bcc = null;
		String mailer = "Capsela_Workflow_Engine";

//	    com.ichg.capsela.framework.util.Properties emailProps = ServiceLocator.instance().loadProperties( "/capsela.email.properties" );
//		String mailhost = emailProps.getProperty( "smtp.server" );

		// We need to throw an exception if the to-address is not set since the clientValidate() method
		// only displays the error as a warning (it might be set via data transfer).
		String toAddresses = getToAddresses();
		if( toAddresses == null || toAddresses.length() == 0 ) {
//			String msg = idString() + ": missing to Addresses";
//			throw new CapselaException( msg );
		}

		try {
			Properties props = System.getProperties();
//			if (mailhost != null) {
//				props.put("mail.smtp.host", mailhost);
//			}
			Session session = Session.getDefaultInstance(props, null);
//			LOG.debug( "Using SMTP host '" + props.get("mail.smtp.host") + "' to send email (set to '" + mailhost + "' in capsela.email.properties file)." );

			Message msg = null;
			try {
				msg = new MimeMessage(session);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (msg != null) {
				if (this.getFromAddress() != null) {
					msg.setFrom(new InternetAddress(this.getFromAddress()));
				} else {
					msg.setFrom(new InternetAddress("dean.mao@ichotelsgroup.com"));
				}

				msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.getToAddresses(), false));
				if (cc != null) {
					msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
				}
				if (bcc != null) {
					msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
				}

				String subject = ( this.getSubject() != null ? this.getSubject() : "" );
				msg.setSubject(subject);

				String body = ( this.getBody() != null ? this.getBody() : "" );
				MimeBodyPart bodyPart1 = new MimeBodyPart();
				bodyPart1.setText(body);
				Multipart mp = new MimeMultipart();
				mp.addBodyPart(bodyPart1);

//				FileAttribute attachment = this.getFileAttachment();
//				if(attachment != null) {
//					String filename = attachment.getDisplayedName();
//					File file = attachment.getFile();
//					attachment.makeSyntheticFileLocal();
//					if (! file.exists()) {
//						throw new CapselaException("File attachment doesn't exist on the file system!  I'm looking for this file: "+file.getAbsolutePath());
//					}
//					if(filename != null && filename.length() > 0) {
//						FileDataSource fds = new FileDataSource(file);
//						MimeBodyPart bodyPart2 = new MimeBodyPart();
//						bodyPart2.setDataHandler(new DataHandler(fds));
//						bodyPart2.setFileName(filename);
//						mp.addBodyPart(bodyPart2);
//					}
//				}

				String markup = ( this.getMarkupAttachment() != null ? this.getMarkupAttachment() : "");
				if ( markup.length() > 0 ) {
					MimeBodyPart bodyPart2 = new MimeBodyPart();
					bodyPart2.setContent(this.getMarkupAttachment(), "text/html");
					mp.addBodyPart(bodyPart2);
				}

				msg.setContent(mp);
				msg.setHeader("X-Mailer", mailer);
				msg.setSentDate(new Date());

				// send the thing off
				Transport.send(msg);
				LOG.debug( "Sent email message with subject '" + msg.getSubject() + "' successfully." );
//				if (attachment != null) {
//					attachment.deleteSyntheticLocalFile();
//				}
			}
		} catch (AddressException e) {
//			throw new CapselaException( e );
		} catch (MessagingException e) {
//			throw new CapselaException( e );
		}
	}

	/**
	 * Retrieves the email's file attachment, if any.
	 * 
	 * @return             the email's file attachment, if any
	 * @javabean.property  displayName="File Attachment"
	 *                     hidden="false"
	 */
//	public FileAttribute getFileAttachment()
//	{
//		return (FileAttribute) getComponentAttribute(ATTRIBUTE_FILE_ATTACHMENT);
//	}
	
	/**
	 * Sets the email's file attachment to the specified <code>FileAttribute</code>.
	 * @param file the <code>FileAttribute</code> to add.
	 * @throws CapselaException shouldn't be thrown.
	 */
//	public void setFileAttachment(FileAttribute file) throws CapselaException
//	{
//		assert file != null : "fileAttribute != null";
//		FileAttribute attribute = addFileAttribute(ATTRIBUTE_FILE_ATTACHMENT, file);
//		updateAttribute("fileAttachment", attribute);
//	}
	
  /**
   * Returns the email body.
   * 
   * @return             the email body
   * @hibernate.property column="EMAIL_SENDER_EMAIL_BODY"
   *                     length="65536"
   * @javabean.property  displayName="Body"
   *                     hidden="false"
   */
  public String getBody() {
    return this._body;
  }

  /**
   * @param  body  The new body value
   */
  public void setBody(String body) {
//		updateAttribute( "body", body );
    this._body = body;
  }

  /**
   * Returns the HTML markup text that is to be attached to the email.
   * 
   * @return             the HTML markup text that is to be attached to the email
   * @hibernate.property column="EMAIL_SENDER_MARKUP_ATTACHMENT"
   *                     length="10000000"
   * @javabean.property  displayName="Markup Attachment"
   *                     hidden="false"
   */
  public String getMarkupAttachment() {
    return this._markupAttachment;
  }

  /**
   *  Sets the markup attribute of the MarkupBean object
   *
   * @param  markup  The new markup value
   */
  public void setMarkupAttachment(String markupAttachment) {
//		updateAttribute( "markupAttachment", markupAttachment );
    this._markupAttachment = markupAttachment;
  }

  /**
   * Returns the email's subject.
   * 
   * @return             the email's subject
   * @hibernate.property column="EMAIL_SENDER_SUBJECT"
   * @javabean.property  displayName="Subject"
   *                     hidden="false"
   */
  public String getSubject() {
    return this._subject;
  }


  /**
   * @param  subject  The new subject value
   */
  public void setSubject(String subject) {
//		updateAttribute( "subject", subject );
    this._subject = subject;
  }


  /**
   * Returns the email's to-address.
   * 
   * @return             the email's to-address
   * @hibernate.property column="EMAIL_SENDER_TO_ADDRESSES" length="5000"
   * @javabean.property  displayName="To Address"
   *                     hidden="false"
   *                     not-null="true"
   */
  public String getToAddresses() {
    return this._toAddresses;
  }


  /**
   * @param  toAddresses  The new toAddresses value
   */
  public void setToAddresses(String toAddresses) {
//		updateAttribute( "toAddresses", toAddresses );
    this._toAddresses = toAddresses;
  }


  /**
   * Returns the email's from-address.
   * 
   * @return             the email's from-address
   * @hibernate.property column="EMAIL_SENDER_FROM_ADDRESS"
   * @javabean.property  displayName="From Address"
   *                     hidden="false"
   */
  public String getFromAddress() {
    return this._fromAddress;
  }


  /**
   * @param  fromAddress  The new fromAddress value
   */
  public void setFromAddress(String fromAddress) {
//		updateAttribute( "fromAddress", fromAddress );
    this._fromAddress = fromAddress;
  }

}