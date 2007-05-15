package com.nexusbpm.services.ftp;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The FTP component allows uploading and downloading of files to and from an
 * arbitrary FTP server.
 * 
 * <b>Use case:</b><br>
 * <p>When run, the component uploads or downloads a file from a remote location.
 * The file that is uploaded or downloaded to, is a FileAttribute type. The user
 * must make use of the FileComponent or another component that produces a file 
 * that the ftp component can make use of.</p>
 * 
 * @author             Dean Mao
 * @created            February 19, 2003
 * @hibernate.subclass discriminator-value="14"
 * @javabean.class     name="FtpComponent"
 *                     displayName="FTP Component"
 */
public class FtpComponent{

	private static final long serialVersionUID = 8962447152170872142L;
	private static final Log LOG = LogFactory.getLog( FtpComponent.class );

	/** Constant indicating the file exists. */
	public final static String EXISTENCE_STATUS_EXISTENT = "Exists";
	/** Constant indicating the file does not exist. */
	public final static String EXISTENCE_STATUS_NONEXISTENT = "Does not exist";

	/** Constant indicating the FTP operation is a <tt>get</tt>. */
	public final static String OPERATION_GET = "GET";
	/** Constant indicating the FTP operation is a <tt>put</tt>. */
	public final static String OPERATION_PUT = "PUT";
	/** Constant indicating the FTP operation is to check for existence. */
	public final static String OPERATION_EXISTS = "EXISTS";

	/** Constant indicating the file should be treated as binary. */
	public final static String FILE_TYPE_BINARY = "BINARY";
	/** Constant indicating the file should be treated as an image. */
	public final static String FILE_TYPE_IMAGE = "IMAGE";
	/** Constant indicating the file should be treated as an ASCII file. */
	public final static String FILE_TYPE_ASCII = "ASCII";
	/** Constant indicating the file should be treated as text. */
	public final static String FILE_TYPE_TEXT = "TEXT";

	/**
	 * Creates a new empty <code>FtpComponent</code>, needed for Hibernate.
	 */
	public FtpComponent() {
		super();
	}//StartComponent()

	/**
	 * @see Component#run()
	 */
	public void run() throws Exception {

//		FileAttribute fa = this.getLocalFile();
//		if( fa == null ) {
//			throw new Exception( "A file attribute has not been specified for this FTP component!" );
//		}//if

//		fa.makeSyntheticFileLocal();

		int fileType = FtpClient.BINARY_FILE_TYPE;
		boolean append = false;
		String operation = this.getOperation();
		String remoteHost = this.getRemoteHost();
		String userId = this.getUserId();
		String userPassword = this.getUserPassword();
		String remoteDir = this.getRemoteDir();
		//Properties properties = ServiceLocator.instance().deploymentProperties();
		String localDir = System.getProperty("java.io.tmpdir");
		String fileTypeString = this.getFileType();
		String appendString = this.getAppend();
		String remoteFileSpec = this.getRemoteFileSpec();
		String localFileSpec = "";//fa.getSyntheticName();

		FtpClient ftpClient = null;

		if( operation == null || operation.length() == 0 ) {
			throw new Exception( "Operation not specified." );
		}

		if( remoteHost == null || remoteHost.length() == 0 ) {
			throw new Exception( "Remote host not specified." );
		}

		if( fileTypeString != null ) {
			if((fileTypeString.equalsIgnoreCase(FILE_TYPE_ASCII)) ) {
				fileType = FtpClient.ASCII_FILE_TYPE;
			}//if
		}//if

		if( (appendString != null) && ((appendString.equalsIgnoreCase("TRUE") || (appendString.equalsIgnoreCase("YES")))) ) {
			append = true;
		}//if

		try {
			ftpClient = FtpClientFactory.getInstance().create();
			if( ! ftpClient.connect( remoteHost ) ) {
				throw new Exception( "Unable to connect to '" + remoteHost + "'.");
			}//if
			if( ! ftpClient.login( userId, userPassword ) ) {
				throw new Exception( "User '" + userId + "' unable to log into '" + remoteHost + "'." );
			}//if
			if( operation.equalsIgnoreCase(OPERATION_GET) ) {
				if( ftpClient.get(localDir, remoteDir, fileType, localFileSpec, remoteFileSpec, append, null) ) {
					LOG.info( "FTP GET operation was successful." );
				}//if
				else {
					throw new Exception( "FTP GET operation failed. "
							+ ";fileType=" + fileType + ";append=" + append
							+ ";localFileSpec=" + localFileSpec	+ ";localDir=" + localDir
							+ ";remoteFileSpec=" + remoteFileSpec + ";remoteDir=" + remoteDir);
				}//else
			}//if
			else if( operation.equalsIgnoreCase(OPERATION_PUT) ) {
				if( ftpClient.put(localDir, remoteDir, fileType, localFileSpec, remoteFileSpec, append, null) ) {
					LOG.info( "FTP PUT operation was successful." );
				}//if
				else {
					throw new Exception( "FTP PUT operation failed. "
							+ ";fileType=" + fileType + ";append=" + append
							+ ";localFileSpec=" + localFileSpec	+ ";localDir=" + localDir
							+ ";remoteFileSpec=" + remoteFileSpec + ";remoteDir=" + remoteDir);
				}//else
			}//else if
			else if( operation.equalsIgnoreCase(OPERATION_EXISTS) ) {
				if( ftpClient.exists(remoteDir, remoteFileSpec) ) {
					setExistsStatus(EXISTENCE_STATUS_EXISTENT);
				}//if
				else {
					setExistsStatus(EXISTENCE_STATUS_NONEXISTENT);
				}//else
			}//else if
			else {
				throw new Exception( "Unknown operation: '" + operation + "'.");
			}//else
		}//try
		catch( IOException e ) {
			throw e;
		}//catch
		catch( Exception e ) {
			throw new Exception( e );
		}//catch
		finally {
			if( ftpClient != null ) {
				try {
					ftpClient.disconnect();
				}//try
				catch( Exception e ) {
					LOG.warn("Unable to disconnect from FTP client.", e);
				}//catch
			}//if
		}//finally

//		fa.makeSyntheticFileRemote();

	}//run()

	private String _operation;
	/**
	 * Gets the operation performed by this FTP component.
	 * 
	 * @return             the operation performed
	 * @hibernate.property column="FTP_OPERATION"
	 * @javabean.property  displayName="Operation"
	 *                     hidden="false"
	 */
	public String getOperation() {
		return _operation;
	}//getOperation()
	/**
	 * Sets the operation to be performed by this FTP component.
	 * @param operation the operation to be performed.
	 */
	public void setOperation(String operation) {
		assert operation == null ||
			OPERATION_GET.equals( operation ) ||
			OPERATION_PUT.equals( operation ) ||
			OPERATION_EXISTS.equals( operation ) :
			"Invalid operation: " + operation;
//		updateAttribute("operation", operation);
		_operation = operation;
	}//setOperation()

	private String _remoteHost;
	/**
	 * Gets the remote host
	 * 
	 * @return             the remote host
	 * @hibernate.property column="FTP_REMOTE_HOST"
	 * @javabean.property  displayName="Remote Host"
	 *                     hidden="false"
	 */
	public String getRemoteHost() {
		return _remoteHost;
	}//getRemoteHost()
	/**
	 * Sets the remote host.
	 * @param remoteHost the remote host.
	 */
	public void setRemoteHost(String remoteHost) {
		if( remoteHost != null ) {
			remoteHost = remoteHost.trim();
		}
//		updateAttribute("remoteHost", remoteHost);
		_remoteHost = remoteHost;
	}//setRemoteHost()

	private String _userId;
	/**
	 * Gets the username used to login.
	 * 
	 * @return             the username used to login.
	 * @hibernate.property column="FTP_USERNAME"
	 * @javabean.property  displayName="User ID"
	 *                     hidden="false"
	 */
	public String getUserId() {
		return _userId;
	}//getUserId()
	/**
	 * Sets the username to be used to login.
	 * @param userId the username to be used to login.
	 */
	public void setUserId(String userId) {
		if( userId != null ) {
			userId = userId.trim();
		}
//		updateAttribute("userId", userId);
		_userId = userId;
	}//setUserId()

	private String _userPassword;
	/**
	 * Gets the password used to login.
	 * 
	 * @return             the password used to login.
	 * @hibernate.property column="FTP_PASSWORD"
	 * @javabean.property  displayName="User Password"
	 *                     hidden="false"
	 */
	public String getUserPassword() {
		return _userPassword;
	}//getUserPassword()
	/**
	 * Sets the password to be used to login.
	 * @param userPassword the password to be used to login.
	 */
	public void setUserPassword(String userPassword) {
		if( userPassword != null ) {
			userPassword = userPassword.trim();
		}
//		updateAttribute("userPassword", userPassword);
		_userPassword = userPassword;
	}//setUserPassword()

	private String _remoteDir;
	/**
	 * Gets the remote directory.
	 * 
	 * @return             the remote directory.
	 * @hibernate.property column="FTP_REMOTE_DIRECTORY"
	 * @javabean.property  displayName="Remote Directory"
	 *                     hidden="false"
	 */
	public String getRemoteDir() {
		return _remoteDir;
	}//getRemoteDir()
	/**
	 * Sets the remote directory.
	 * @param remoteDir the remote directory.
	 */
	public void setRemoteDir(String remoteDir) {
		if( remoteDir != null ) {
			remoteDir = remoteDir.trim();
		}
//		updateAttribute("remoteDir", remoteDir);
		_remoteDir = remoteDir;
	}//setRemoteDir()

	private final static String ATTRIBUTE_FILE = "ftp_file_attribute";
	/**
	 * Retrieves the local file.
	 * 
	 * @return             the local file
	 * @javabean.property  displayName="Local File"
	 *                     hidden="false"
	 */
//	public FileAttribute getLocalFile() {
//		return (FileAttribute) getComponentAttribute(ATTRIBUTE_FILE);
//	}//getLocalFile()
//	/**
//	 * Sets the local file.
//	 * @param file the local file.
//	 * @throws Exception shouldn't ever be thrown.
//	 */
//	public void setLocalFile(FileAttribute file) throws Exception {
//		FileAttribute fa = addFileAttribute(ATTRIBUTE_FILE, file);
//		updateAttribute(ATTRIBUTE_FILE, fa);
//	}//setLocalFile()

	private String _fileType;
	/**
	 * Gets the type of file being put/get.
	 * 
	 * @return             the fileType value
	 * @hibernate.property column="FTP_FILE_TYPE"
	 * @javabean.property  displayName="File Type"
	 *                     hidden="false"
	 */
	public String getFileType() {
		return _fileType;
	}//getFileType()
	/**
	 * Sets the type of file being put/get.
	 * @param fileType the type of file being put/get.
	 * @see #FILE_TYPE_ASCII
	 * @see #FILE_TYPE_BINARY
	 * @see #FILE_TYPE_IMAGE
	 * @see #FILE_TYPE_TEXT
	 */
	public void setFileType(String fileType) {
		assert fileType == null ||
			FILE_TYPE_BINARY.equals( fileType ) ||
			FILE_TYPE_IMAGE.equals( fileType ) ||
			FILE_TYPE_TEXT.equals( fileType ) ||
			FILE_TYPE_ASCII.equals( fileType ) :
			"Invalid file type: " + fileType;
//		updateAttribute("fileType", fileType);
		_fileType = fileType;
	}//setFileType()

	private String _append;
	/**
	 * Gets whether the file will be appended if one already exists.
	 * 
	 * @return             whether to append the file.
	 * @hibernate.property column="FTP_APPEND"
	 * @javabean.property  displayName="Append"
	 *                     hidden="false"
	 */
	public String getAppend() {
		return _append;
	}//getAppend()
	/**
	 * Sets whether the file will be appended if one already exists. The file
	 * will be appended if the string is either "true" or "yes" (ignoring case).
	 * @param append whether to append the file.
	 */
	public void setAppend(String append) {
		if( append != null ) {
			append = append.trim();
		}
//		updateAttribute("append", append);
		_append = append;
	}//setAppend()

	private String _remoteFileSpec;
	/**
	 * Gets the remote file name.
	 * 
	 * @return             the remote file name
	 * @hibernate.property column="FTP_REMOTE_FILE_SPEC"
	 * @javabean.property  displayName="Remote File Spec"
	 *                     hidden="false"
	 */
	public String getRemoteFileSpec() {
		return _remoteFileSpec;
	}//getRemoteFileSpec()
	/**
	 * Sets the remote file name.
	 * @param remoteFileSpec the remote file name.
	 */
	public void setRemoteFileSpec(String remoteFileSpec) {
		if( remoteFileSpec != null ) {
			remoteFileSpec = remoteFileSpec.trim();
		}
//		updateAttribute("remoteFileSpec", remoteFileSpec);
		_remoteFileSpec = remoteFileSpec;
	}//setRemoteFileSpec()

	private String _existsStatus;
	/**
	 * Gets the existence status of the remote file.
	 * 
	 * @return             the existsStatus value
	 * @hibernate.property column="FTP_EXISTS_STATUS"
	 * @javabean.property  displayName="Exists Status"
	 *                     hidden="false"
	 */
	public String getExistsStatus() {
		return _existsStatus;
	}//getExistsStatus()
	/**
	 * Sets what the existence status of the remote file is.
	 * @param existsStatus the existence status of the remote file.
	 * @see #EXISTENCE_STATUS_EXISTENT
	 * @see #EXISTENCE_STATUS_NONEXISTENT
	 */
	public void setExistsStatus(String existsStatus) {
		assert existsStatus == null ||
			EXISTENCE_STATUS_EXISTENT.equals( existsStatus ) ||
			EXISTENCE_STATUS_NONEXISTENT.equals( existsStatus ) :
			"Invalid existence status: " + existsStatus;
//		updateAttribute("existsStatus", existsStatus);
		_existsStatus = existsStatus;
	}//setExistsStatus()

}
