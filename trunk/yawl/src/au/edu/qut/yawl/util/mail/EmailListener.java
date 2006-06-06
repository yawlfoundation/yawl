package au.edu.qut.yawl.util.mail;

import java.security.Security;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.exceptions.YAWLException;

/**
 * @author Felix Mayer
 * @version $Revision: 1.21 $
 */
public abstract class EmailListener implements Runnable {

	static private final Log LOG = LogFactory.getLog( EmailListener.class );

	/**
	 * The location of the email properties file.
	 * TODO update properties file name.
	 */
	static public final String EMAIL_PROPERTIES_FILE = "/capsela.email.properties";

	private final static int DELAY_STANDARD = 500;
	private final static int DELAY_FOR_CAPSELA_EXCEPTION = 5000;
	private final static int DELAY_FOR_THROWABLE = 180000;
	
	/**
	 * Default MS Exchange ports from
	 * http://support.microsoft.com/support/kb/articles/q150/5/43.asp
	 */
	public static final int PORT_MS_EXCHANGE_IMAP = 143;
	/**
	 * Default MS Exchange IMAP SSL port
	 */
	public static final int PORT_MS_EXCHANGE_IMAP_SSL = 993;
	/**
	 * Default MS Exchange LDAP port
	 */
	public static final int PORT_MS_EXCHANGE_LDAP = 389;
	/**
	 * Default MS Exchange LDAP SSL port
	 */
	public static final int PORT_MS_EXCHANGE_LDAP_SSL = 636;
	/**
	 * Default MS Exchange POP3 port
	 */
	public static final int PORT_MS_EXCHANGE_POP3 = 110;
	/**
	 * Default MS Exchange POP3 SSL port
	 */
	public static final int PORT_MS_EXCHANGE_POP3_SSL = 995;
	/**
	 * Default MS Exchange SMTP port
	 */
	public static final int PORT_MS_EXCHANGE_SMTP = 25;
	/**
	 * Default MS Exchange NNTP port
	 */
	public static final int PORT_MS_EXCHANGE_NNTP = 119;
	/**
	 * Default MS Exchange NNTP SSL port
	 */
	public static final int PORT_MS_EXCHANGE_NNTP_SSL = 563;
	
	/**
	 * Protocal name constant for the IMAP protocol.
	 */
	static public final String PROTOCOL_IMAP = "imap";
	/**
	 * Protocal name constant for the POP3 protocol.
	 */
	static public final String PROTOCOL_POP3 = "pop3";
	/**
	 * Protocal name constant for the NNTP protocol.
	 */
	static public final String PROTOCOL_NNTP = "nntp";
	
	/**
	 * http://java.sun.com/products/javamail/NOTES.txt:
	 * Q: I'm having trouble logging into my Microsoft Exchange server, even
	 * though I'm sure I'm using the correct username and password, what could
	 * I be doing wrong?
	 * A: When logging in to Exchange you need to use a username that's more
	 * than your simple login name. For example, if your email address is
	 * "J.User@server.com", your Windows NT login name is "juser", your NT domain
	 * name is "dom", and your Exchange mailbox name is "Joe User", then you
	 * would need to use a username of "dom\juser\J.User" when logging in using
	 * JavaMail.
	 *
	 * http://java.sun.com/products/javamail/NOTES.txt:
	 * 4. We've received reports of IMAP authentication failures on the Microsoft
	 * Exchange Server 5.5, enterprise edition. This is due to a bug in the
	 * Microsoft server and the "Service Pack 1 for Exchange Server 5.5"
	 * apparently fixes this server bug. The service pack can be downloaded
	 * from the Microsoft website.
	 *
	 * @see <a href="http://www.javaworld.com/javatips/jw-javatip115.html">JavaWorld SSL Tips</a>
	 * @see <a href="http://www.jguru.com/faq/view.jsp?EID=329193">JGuru IMAP + SSL Q&A</a>
	 * @see <a href="http://www.jguru.com/faq/view.jsp?EID=26996">JGuru JavaMail & Attachments</a>
	 * @see <a href="http://java.sun.com/products/javamail/JavaMail-1.2.pdf">The JavaMail Spec</a>
	 */
	public EmailListener() {
		this( EMAIL_PROPERTIES_FILE );
	}//EmailListener()
	/**
	 * Creates a new EmailListener with properties loaded from the given
	 * file name.
	 *
	 * @param fileName the name of the file to load the properties from
	 */
	public EmailListener( String fileName ) {
		// Configure email account and server properties.
		Properties emailProps = new Properties();
		// TODO set up properties
//		Properties emailProps = ServiceLocator.instance().loadProperties( fileName );
		setHost( emailProps.getProperty( "listen.host" ) );
		setPort( Integer.parseInt( emailProps.getProperty( "listen.port" ) ) );
		setMailbox( emailProps.getProperty( "listen.mailbox" ) );
		setMailboxExclusive( Boolean.valueOf( emailProps.getProperty( "listen.mailbox.isExclusive" ) ).booleanValue() );
		setUsername( emailProps.getProperty( "listen.username" ) );
		setPassword( emailProps.getProperty( "listen.password" ) );
		setProtocol( emailProps.getProperty( "listen.protocol" ) );
		setUseSSL( Boolean.valueOf( emailProps.getProperty( "listen.use.ssl" ) ) );
		initSession();
	}//EmailListener()
	/**
	 * Creates a new EmailListener with the given properties.
	 *
	 * @param host               the name of the email host
	 * @param port               the port number of the email host
	 * @param mailbox            the mailbox in which to look for emails
	 * @param isMailboxExclusive whether the mailbox is exclusive
	 * @param username           the username used to login to the email host
	 * @param password           the password used to login to the email host
	 * @param protocol           the email protocol to use with the host
	 * @param useSSL             whether a secure (SSL) connection should be made
	 */
	public EmailListener( String host, int port, String mailbox, boolean isMailboxExclusive, String username,
			String password, String protocol, boolean useSSL ) {
		// Configure email account and server properties.
		setHost( host );
		setPort( port );
		setMailbox( mailbox );
		setMailboxExclusive( isMailboxExclusive );
		setUsername( username );
		setPassword( password );
		setProtocol( protocol );
		setUseSSL( Boolean.valueOf( useSSL ) );
		initSession();
	}//EmailListener()
	/**
	 *
	 */
	private void initSession() {
		Properties properties = new Properties();
		properties.put( "mail.host", getHost() );
		properties.put( "mail.store.protocol", getProtocol() );
		properties.put( "mail.user", getUsername() );
		if( getUseSSL().booleanValue() ) {
			Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
			// Got this line from http://www.jguru.com/faq/view.jsp?EID=329193
			System.setProperty( "java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol" );
			properties.put( "mail." + _protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
			properties.put( "mail." + _protocol + ".socketFactory.fallback", "false" );
			properties.put( "mail." + _protocol + ".socketFactory.port", String.valueOf( _port ) );
			properties.put( "mail." + _protocol + ".port", String.valueOf( _port ) );
			// TODO make the timeout configurable
			properties.put( "mail." + _protocol + ".timeout", "5000" );
		}//if
		_session = Session.getInstance( properties );
		_session.setDebug( false );
		LOG.info( _session.getProperties() );
	}//initSession()

	private boolean _isRunning = false;
	/**
	 * Returns whether the EmailListener is currently running.
	 *
	 * @return whether the EmailListener is currently running.
	 */
	public final boolean isRunning() {
		return _isRunning;
	}//isRunning()
	/**
	 * Sets the running state of the EmailListener.
	 *
	 * @param isRunning whether the EmailListener is running or not.
	 */
	public final void setRunning( boolean isRunning ) {
		_isRunning = isRunning;
	}//setRunning()

	private Session _session = null;
	private Store _store = null;
	private Folder _folder = null;
	/**
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		setRunning( true );
		try {
			int throwables = 0;
			while( isRunning() ) {
				int delay = DELAY_STANDARD;
				try {
					connect();
					processMessages( _folder.getMessages() );
					// If we were able to connect and process messages without any problems, reset the throwable count.
					// Otherwise, a server that stays up a long time but has occassional timeouts/errors will end up
					// waiting longer and longer due to occasional glitches and will eventually cause this service to
					// be down for hours at a time.
					throwables = 0;
				}//try
				catch( YAWLException e ) {
					LOG.error( "Error processing messages in Email Listener", e );
					delay = DELAY_FOR_CAPSELA_EXCEPTION;
				}//catch
				catch( Throwable t ) {
					// Wait longer and longer before trying to connect again.
					LOG.error( t.getMessage(), t );
					delay = DELAY_FOR_THROWABLE + (2 * throwables * DELAY_FOR_THROWABLE);
					LOG.warn( "Error communicating with the mail server: will wait " + delay + " millis before trying again." );
					throwables++;
				}//catch
				finally {
					// Disconnect from Store so that processed messages get deleted.
					// Initially the interfaces MessageCountListener and ConnectionListener
					// were implemented, but we have to disconnect from the message store
					// in order to delete messages, so using these interfaces didn't make
					// much sense.
					try { disconnect(); }
					catch( Throwable t ) { LOG.error( "Error disconnecting: " + t.getMessage(), t ); }
				}//finally
				Thread.sleep( delay );
			}//while
		}//try
		catch( InterruptedException e ) {
			LOG.error( "Email Listener was interrupted!", e );
		}//catch
		finally {
			LOG.info( "stopped running" );
		}//finally
	}//run()

	private void connect() throws MessagingException, YAWLException {
		_store = _session.getStore();
		_store.connect( _host, _port, _username, _password );
		_folder = _store.getDefaultFolder().getFolder( _mailbox );
		if( !_folder.exists() ) {
			throw new YAWLException( "The specified mailbox ("
					+ _mailbox + ") does not exist." );
		}//if
		_folder.open( Folder.READ_WRITE );
	}//connect()
	private void disconnect() throws MessagingException {
		if( _folder != null ) {
			// Close mailbox and remove all messages marked for deletion.
			if( _folder.exists() ) {
				_folder.close( true );
				_folder = null;
			}//if
		}//if
		if( _store != null ) {
			_store.close();
			_store = null;
		}//if
	}//disconnect()

	/**
	 * @param messages
	 * @throws YAWLException
	 */
	private final void processMessages( Message[] messages )
			throws YAWLException {
		if( messages.length == 0 ) return;
		// TODO handle persistence
//		PersistenceManager persistenceManager = PersistenceManager.instance();
//		persistenceManager.openTransientSession();
		try {
			for( int i = 0; i < messages.length; i++ ) {
				Message message = messages[ i ];
				if( !_folder.isOpen() ) _folder.open( Folder.READ_WRITE );
				if( !message.isExpunged() ) {
					boolean processed = false;
					try {
						processed = processMessage( message );
					}//try
					catch( YAWLException e ) {
						LOG.error( "Error processing message in Email Listener", e );
						// Continue processing other emails even when an exception ocurred.
					}//catch
					if( processed ) {
						message.setFlag( javax.mail.Flags.Flag.DELETED, true );
						LOG.debug( "message '" + message.getSubject() + "' processed." );
					}//if
					else {
						if( this.isMailboxExclusive() ) {
							message.setFlag( javax.mail.Flags.Flag.DELETED, true );
							LOG.debug( "message '" + message.getSubject() + "' ignored and deleted." );
						}//if
						else {
							LOG.debug( "message '" + message.getSubject() + "' ignored." );
						}//else
					}//else
				}//if
			}//for
		}//try
		catch( MessagingException e ) {
			throw new YAWLException( e );
		}//catch
		finally {
			// TODO handle persistence
//			persistenceManager.closeTransientSession();
		}//finally
	}//hasMessageArrived()
	/**
	 * @param message
	 * @return true if the Message should be deleted after processing it.
	 * @throws YAWLException
	 */
	protected abstract boolean processMessage( Message message ) throws YAWLException;

	private String _host = null;
	/**
	 * Gets the name of the email host.
	 *
	 * @return the name of the email host
	 * @hibernate.property column="EMAIL_LISTENER_HOST"
	 * @javabean.property displayName="Host"
	 * hidden="false"
	 */
	public String getHost() {
		return _host;
	}//getHost()
	/**
	 * Sets the name of the email host.
	 *
	 * @param host the name of the email host.
	 */
	public void setHost( String host ) {
		_host = host;
	}//setHost()

	private String _mailbox = null;
	/**
	 * Gets the mailbox in which to look for emails.
	 *
	 * @return the mailbox in which to look for emails
	 * @hibernate.property column="EMAIL_LISTENER_MAILBOX_NAME"
	 * @javabean.property displayName="Mailbox"
	 * hidden="false"
	 */
	public String getMailbox() {
		return _mailbox;
	}//getMailbox()
	/**
	 * Sets the mailbox in which to look for emails.
	 *
	 * @param mailbox the mailbox in which to look for emails.
	 */
	public void setMailbox( String mailbox ) {
		_mailbox = mailbox;
	}//setMailbox()

	private boolean _isMailboxExclusive = false;
	/**
	 * Returns whether the mailbox is exclusive or not.
	 *
	 * @return whether the mailbox is exclusive or not.
	 */
	public boolean isMailboxExclusive() {
		return _isMailboxExclusive;
	}
	/**
	 * Sets whether the mailbox is exclusive or not.
	 *
	 * @param isMailboxExclusive whether the mailbox is exclusive or not.
	 */
	public void setMailboxExclusive( boolean isMailboxExclusive ) {
		_isMailboxExclusive = isMailboxExclusive;
	}

	private int _port = 0;
	/**
	 * Gets the port to connect to on the email host.
	 *
	 * @return the port to connect to to on the email host
	 * @hibernate.property column="EMAIL_LISTENER_PORT"
	 * @javabean.property displayName="Port"
	 * hidden="false"
	 */
	public int getPort() {
		return _port;
	}//getPort()
	/**
	 * Sets the port to connect to on the email host.
	 *
	 * @param port the port to connect to on the email host.
	 */
	public void setPort( int port ) {
		_port = port;
	}//setPort()
	
	private String _protocol = null;
	/**
	 * Gets the email protocol to use with the host.
	 *
	 * @return the email protocol to use with the host
	 * @hibernate.property column="EMAIL_LISTENER_PROTOCOL"
	 */
	public String getProtocol() {
		return _protocol;
	}//getProtocol()
	/**
	 * Sets the email protocol to use with the host. Must be one of the protocol
	 * constants in this class.
	 *
	 * @param protocol the protocol to use.
	 * @see #PROTOCOL_IMAP
	 * @see #PROTOCOL_NNTP
	 * @see #PROTOCOL_POP3
	 */
	public void setProtocol( String protocol ) {
		assert PROTOCOL_IMAP.equals( protocol ) || PROTOCOL_POP3.equals( protocol )
				|| PROTOCOL_NNTP.equals( protocol ) : "invalid protocol " + protocol;
		_protocol = protocol;
	}//setProtocol()

	private String _username = null;
	/**
	 * Gets the username to use to login to the email host.
	 *
	 * @return the username to use to login to the email host
	 * @hibernate.property column="EMAIL_LISTENER_USERNAME"
	 * @javabean.property displayName="Username"
	 * hidden="false"
	 */
	public String getUsername() {
		return _username;
	}//getUsername()
	/**
	 * Sets the username to use to login to the email host.
	 *
	 * @param username the username to use to login to the email host.
	 */
	public void setUsername( String username ) {
		_username = username;
	}//setUsername()

	private String _password = null;
	/**
	 * Returns the password used to login to the email host.
	 *
	 * @return the password used to login to the email host
	 * @hibernate.property column="EMAIL_LISTENER_PASSWORD"
	 * @javabean.property displayName="Password"
	 * hidden="false"
	 */
	public String getPassword() {
		return _password;
	}//getPassword()
	/**
	 * Sets the password used to login to the email host.
	 *
	 * @param password the password to use to login to the email host.
	 */
	public void setPassword( String password ) {
		_password = password;
	}//setPassword()

	private Boolean _useSSL = null;
	/**
	 * Returns <code>true</code> if a secure (SSL) connection should be created
	 * with the host.
	 *
	 * @return true if a secure (SSL) connection should be used
	 * @hibernate.property column="EMAIL_LISTENER_USE_SSL"
	 * @javabean.property displayName="Use SSL" hidden="true"
	 */
	public Boolean getUseSSL() {
		return _useSSL;
	}//getUseSSL()
	/**
	 * Set whether a secure (SSL) connection should be created with the host.
	 *
	 * @param useSSL whether a secure (SSL) connection should be used.
	 */
	public void setUseSSL( Boolean useSSL ) {
		_useSSL = useSSL;
	}//setUseSSL()

}//EmailListener
