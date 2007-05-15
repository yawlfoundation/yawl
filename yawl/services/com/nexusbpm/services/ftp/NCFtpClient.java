package com.nexusbpm.services.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Implementation of the <tt>FtpClient</tt> interface which uses ORO, Inc.'s
 * NetComponents library.
 * 
 * @author     Dean Mao
 * @created    December 3, 2002
 * @see        "http://www.savarese.org/oro/"
 */
public class NCFtpClient implements FtpClient {

	private final static Log LOG = LogFactory.getLog( NCFtpClient.class );
	private final static char FTP_FILE_SEPARATOR_CHAR = '/';

	private boolean _connected = false;
	private boolean _loggedIn = false;
	private String _remoteHost = null;
	private FTPClient _ftpClient = null;

	/**
	 * Default constructor, to be called by the FTP client factory.
	 */
	protected NCFtpClient() {
		_ftpClient = new FTPClient();
	}

	/**
	 * Connects to the specified FTP host.
	 * @see FtpClient#connect(String)
	 */
	public boolean connect( String remoteHost ) throws IOException {
		_remoteHost = remoteHost;
		_connected = false;
		_loggedIn = false;

		try {
			_ftpClient.connect( remoteHost );

			if( FTPReply.isPositiveCompletion( _ftpClient.getReplyCode() ) ) {
				_connected = true;
			}
			else {
				disconnect();
			}
		}
		catch( Exception exception ) {
			throw new IOException( "Exception throw connecting to host " + _remoteHost);
		}

		return ( _connected );
	}

	/**
	 * Disconnects the FTP client.
	 * @see FtpClient#disconnect()
	 */
	public void disconnect() {
		_connected = false;
		_loggedIn = false;

		try {
			_ftpClient.disconnect();
		}
		catch( Exception e ) {
			LOG.error( "Error disconnecting.", e );
		}
	}

	/**
	 * @return whether the FTP client is connected.
	 * @see FtpClient#isConnected()
	 */
	public boolean isConnected() {
		return ( _connected );
	}

	/**
	 * Logs in the specified user with the given password.
	 * @see FtpClient#login(String, String)
	 */
	public boolean login( String userId, String password ) throws IOException {
		if( _connected ) {
			try {
				_loggedIn = false;
				if( _ftpClient.login( userId, password ) ) {
					_loggedIn = true;
				}
			}
			catch( Exception exception ) {
				throw new IOException( "Exception occurred trying to login" + userId);
			}
		}
		else {
			throw new IOException( "Not connected.  Unable to login." + userId );
		}

		return ( _loggedIn );
	}

	/**
	 * Logs out of the FTP server.
	 * @see FtpClient#logout()
	 */
	public void logout() {
		_loggedIn = false;

		try {
			_ftpClient.logout();
		}
		catch( Exception e ) {
			LOG.error( "Error logging out.", e );
		}
	}

	/**
	 * @return whether the FTP client is logged in.
	 * @see FtpClient#isLoggedIn()
	 */
	public boolean isLoggedIn() {
		return ( _loggedIn );
	}

	/**
	 * Transfers the specified file from the FTP server to the local machine.
	 * @see FtpClient#get(String, String, int, String)
	 */
	public boolean get( String localDir,
						String remoteDir,
						int fileType,
						String fileSpecification ) throws FtpException {
		return ( get( localDir, remoteDir, fileType, fileSpecification, fileSpecification, false, null ) );
	}

	/**
	 * Transfers the specified file from the FTP server to the local machine,
	 * possibly appended the contents of the remote file to the existing local
	 * file.
	 * @see FtpClient#get(String, String, int, String, boolean)
	 */
	public boolean get( String localDir,
						String remoteDir,
						int fileType,
						String fileSpecification,
						boolean appendIfExisting ) throws FtpException {
		return ( get( localDir, remoteDir, fileType, fileSpecification, fileSpecification, appendIfExisting, null ) );
	}

	/**
	 * Transfers the specified file from the FTP server to the local machine and
	 * gives it the specified name.
	 * @see FtpClient#get(String, String, int, String, String)
	 */
	public boolean get( String localDir,
						String remoteDir,
						int fileType,
						String localFileName,
						String remoteFileName ) throws FtpException {
		return ( get( localDir, remoteDir, fileType, localFileName, remoteFileName, false, null ) );
	}

	/**
	 * Transfers the specified file from the FTP server to the local machine
	 * giving it the specified name and possibly appending the contents of the
	 * remote file to the existing local file.
	 * @see FtpClient#get(String, String, int, String, String, boolean, FtpOperationListener)
	 */
	public boolean get( String localDir,
						String remoteDir,
						int fileType,
						String localFileName,
						String remoteFileName,
						boolean appendIfExisting,
						FtpOperationListener listener ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		boolean getOccurred = false;
		boolean removeFile = false;
		BufferedOutputStream outStream = null;

		remoteDir = getFTPPath( remoteDir );
		remoteFileName = getFTPPath( remoteFileName );
		String localFilePath = getLocalizedPath( localDir, localFileName );

		long startTime = System.currentTimeMillis();
		long processedBytes = 0;
		long totalBytes;
		try {
			this.cd( remoteDir );
			totalBytes = getBytesRecursive( remoteFileName );
		}
		catch( IOException e ) {
			throw new FtpException( e );
		}

		if( listener != null ) {
			listener.onStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		}

		try {
//			_ftpClient.setFileType( fileType == ASCII_FILE_TYPE ? FTP.ASCII_FILE_TYPE : FTP.IMAGE_FILE_TYPE );

			if( remoteFileName.indexOf( '*' ) >= 0 ) {
				String pattern = remoteFileName.replaceAll( "\\*", ".*" );
//				FTPFile[] filelist = _ftpClient.listFiles( remoteDir );
//				for( int i = 0; i < filelist.length; i++ ) {
//					FTPFile ftpfile = filelist[ i ];
//					String filename = ftpfile.getName();
//					if( filename.matches( pattern ) ) {
//						if( listener != null ) {
//							listener.onFileStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, filename ) );
//						}
//						localFilePath = getLocalizedPath( localDir, filename );
//						FileOutputStream fos = new FileOutputStream( localFilePath, appendIfExisting );
//						outStream = new FtpListeningBufferedOutputStream( fos, listener, processedBytes, totalBytes, startTime, filename );
//						if( _ftpClient.retrieveFile( filename, outStream ) ) {
//							getOccurred = true;
//						}
//						processedBytes += ftpfile.getSize();
//						if( listener != null ) {
//							listener.onFileComplete( new FtpOperationEvent( processedBytes, totalBytes, startTime, filename ) );
//						}
//					}
//				}
			}
			else {
				if( listener != null ) {
					listener.onFileStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, localFileName ) );
				}
				FileOutputStream fos = new FileOutputStream( localFilePath, appendIfExisting );
//				outStream = new FtpListeningBufferedOutputStream( fos, listener, processedBytes, totalBytes, startTime, localFileName );
//				if( _ftpClient.retrieveFile( remoteFileName, outStream ) ) {
//					getOccurred = true;
//				}
//				else {
//					removeFile = true;
//				}
				processedBytes = totalBytes;
				if( listener != null ) {
					listener.onFileComplete( new FtpOperationEvent( processedBytes, totalBytes, startTime, localFileName ) );
				}
			}
		}
		catch( Exception exception ) {
			throw new FtpException( "FTP Get operation failed with exception", exception );
		}
		finally {
			if( outStream != null ) {
				try {
//					Closer.close(outStream);
					if( removeFile ) {
						File file = new File( localFilePath );
						if( ( file.exists() ) && ( file.length() <= 0 ) ) {
							file.delete();
						}
					}
				}
				catch( Exception ex ) {
					LOG.error( "Error cleaning up.", ex );
				}
			}
		}

		if( listener != null ) {
			listener.onEnd( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		}

		return getOccurred;
	}

	/**
	 * Transfers the contents in the specified remote directory into the specified
	 * local directory recursively.
	 * @see FtpClient#getDirectory(String, String, FtpOperationListener)
	 */
	public boolean getDirectory( String remoteDir,
								 String localDir,
								 FtpOperationListener listener ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		remoteDir = getFTPPath( remoteDir );

		File f = new File( localDir );
		if( !f.exists() ) {
			f.mkdirs();
		}

		long startTime = System.currentTimeMillis();
		long processedBytes = 0;
		long totalBytes;
		try {
			this.cd( remoteDir );
			totalBytes = getBytesRecursive( "." );
		}
		catch( IOException e ) {
			throw new FtpException( e );
		}

		listener.onStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		boolean getOccurred = this.getDirectory( remoteDir, localDir, listener, processedBytes, totalBytes, startTime );
		listener.onEnd( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		return getOccurred;
	}

	/**
	 * Helper method for getDirectory() above.
	 */
	private boolean getDirectory( String remoteDir,
								  String localDir,
								  FtpOperationListener listener,
								  long processedBytes,
								  long totalBytes,
								  long startTime ) throws FtpException {

		Boolean getOccurred = null;

		// If the specified local directory does not exist, create it.
		String localDirLocalized = getLocalizedPath( localDir );
		File localDirFile = new File( localDirLocalized );
		if( !localDirFile.exists() ) {
			create( localDirFile, true );
		}

		// Change the remote directory.
		this.cd( remoteDir );

		try {
			// Binary transfer.
//			_ftpClient.setFileType( FTP.IMAGE_FILE_TYPE );
			// Go through all the files in the remote directory.
//			FTPFile[] filelist = _ftpClient.listFiles( remoteDir );
//			for( int i = 0; i < filelist.length; i++ ) {
//				// Fire the OnFileStart event.
//				FTPFile ftpfile = filelist[ i ];
//				String filename = ftpfile.getName();
//				listener.onFileStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, filename ) );
//				// Perform the transfer differently depending on whether it is a directory or not.
//				boolean success;
//				if( ftpfile.isFile() ) {
//					String localFileName = getLocalizedPath( localDir, filename );
//					File localFile = new File( localFileName );
//					if( localFile.exists() ) {
//						if( !localFile.delete() ) {
//							LOG.warn( "Unable to overwrite file '" + localFileName + "'." );
//							continue;
//						}
//					}
//					create( localFile, false );
//					FileOutputStream fos = new FileOutputStream( localFile, false );
//					FtpListeningBufferedOutputStream bos = new FtpListeningBufferedOutputStream( fos, listener, processedBytes, totalBytes, startTime, filename );
//					success = _ftpClient.retrieveFile( filename, bos );
//					if( !success ) {
//						LOG.warn( "Unable to retrieve file '" + filename + "'." );
//					}
//				}
//				else {
//					String r = remoteDir + FTP_FILE_SEPARATOR_CHAR + filename;
//					String l = localDir + File.separator + filename;
//					success = getDirectory( r, l, listener, processedBytes, totalBytes, startTime );
//					if( !success ) {
//						LOG.warn( "Unable to retrieve directory '" + r + "'." );
//					}
//				}
//				getOccurred = Boolean.valueOf( success && ( getOccurred != null ? getOccurred.booleanValue() : true ) );
//				long bytes = ftpfile.getSize();
//				processedBytes += bytes;
//				// Fire the OnFileComplete event.
//				listener.onFileComplete( new FtpOperationEvent( processedBytes, totalBytes, startTime, filename ) );
//			}
		}
		catch( Exception e ) {
			throw new FtpException( "Exception transferring directory contents.", e );
		}

		return ( getOccurred != null ? getOccurred.booleanValue() : false );

	}

	/**
	 * Creates the specified local file. If the file cannot be created for any reason,
	 * an <tt>FtpException</tt> is thrown.
	 */
	private boolean create( File f, boolean isDirectory ) throws FtpException {
		boolean created = false;
		try {
			if( isDirectory ) {
				created = f.mkdirs();
			}
			else {
				created = f.createNewFile();
			}
		}
		catch( IOException e ) {
			throw new FtpException( "Unable to create file '" + f.getAbsolutePath() + "'.", e );
		}
		if( !created ) { throw new FtpException( "Unable to create file '" + f.getAbsolutePath() + "'." ); }
		return created;
	}

	/**
	 * Gets the size of the current directory recursively, including subdirectories.
	 */
	private long getBytesRecursive( String path ) throws IOException {
		long totalBytes = 0;
//		FTPFile[] filelist = _ftpClient.listFiles( path );
//		if( filelist != null ) {
//			for( int i = 0; i < filelist.length; i++ ) {
//				FTPFile file = filelist[ i ];
//				if( file.isFile() ) {
//					totalBytes += file.getSize();
//				}
//				else if( file.isDirectory() ) {
//					totalBytes += getBytesRecursive( path + "/" + file.getName() );
//				}
//			}
//		}
		return totalBytes;
	}

	/**
	 * Transfers the specified file from the local machine to the FTP server.
	 * @see FtpClient#put(String, String, int, String)
	 */
	public boolean put( String localDir,
						String remoteDir,
						int fileType,
						String fileSpecification ) throws FtpException {
		return ( put( localDir, remoteDir, fileType, fileSpecification, fileSpecification, false, null ) );
	}

	/**
	 * Transfers the specified file from the local machine to the FTP server
	 * possibly appended the contents of the local file to the existing remote
	 * file.
	 * @see FtpClient#put(String, String, int, String, boolean)
	 */
	public boolean put( String localDir,
						String remoteDir,
						int fileType,
						String fileSpecification,
						boolean appendIfExisting ) throws FtpException {
		return ( put( localDir, remoteDir, fileType, fileSpecification, fileSpecification, appendIfExisting, null ) );
	}

	/**
	 * Transfers the specified file from the local machine to the FTP server and
	 * gives it the specified name.
	 * @see FtpClient#put(String, String, int, String, String)
	 */
	public boolean put( String localDir,
						String remoteDir,
						int fileType,
						String localFileName,
						String remoteFileName ) throws FtpException {
		return ( put( localDir, remoteDir, fileType, localFileName, remoteFileName, false, null ) );
	}

	/**
	 * Transfers the specified file from the local machine to the FTP server
	 * giving it the specified name and possibly appending the contents of the
	 * local file to the existing remote file.
	 * @see FtpClient#put(String, String, int, String, String, boolean, FtpOperationListener)
	 */
	public boolean put( String localDir,
						String remoteDir,
						int fileType,
						String localFileName,
						String remoteFileName,
						boolean appendIfExisting,
						FtpOperationListener listener ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		boolean putOccurred = false;
		File localFile = null;
		BufferedInputStream inStream = null;

		remoteDir = getFTPPath( remoteDir );
		localFile = new File( getLocalizedPath( localDir, localFileName ) );
		remoteFileName = getFTPPath( remoteFileName );

		long startTime = System.currentTimeMillis();
		long processedBytes = 0;
		long totalBytes = localFile.length();

		this.cd( remoteDir );

		if( listener != null ) {
			listener.onStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		}

		if( !localFile.exists() ) { throw new FtpException( localFile.getAbsolutePath() + " does not exist." ); }

		if( !localFile.isFile() ) { throw new FtpException( localFile.getAbsolutePath() + " is not a file." ); }

		if( !localFile.canRead() ) { throw new FtpException( localFile.getAbsolutePath() + " cannot be read." ); }

		if( listener != null ) {
			listener.onFileStart( new FtpOperationEvent( processedBytes, totalBytes, startTime, localFileName ) );
		}

		try {
//			_ftpClient.setFileType( fileType == ASCII_FILE_TYPE ? FTP.ASCII_FILE_TYPE : FTP.IMAGE_FILE_TYPE );
			FileInputStream fis = new FileInputStream( localFile );
//			inStream = new FtpListeningBufferedInputStream( fis, listener, processedBytes, totalBytes, startTime, localFileName );
//			if( appendIfExisting ) {
//				if( _ftpClient.appendFile( remoteFileName, inStream ) ) {
//					putOccurred = true;
//				}
//			}
//			else {
//				if( _ftpClient.storeFile( remoteFileName, inStream ) ) {
//					putOccurred = true;
//				}
//			}
		}
		catch( Exception exception ) {
			throw new FtpException( "FTP Put operation failed with " + "exception", exception );
		}
		finally {
//			Closer.close(inStream);
		}
		processedBytes = totalBytes;

		if( listener != null ) {
			listener.onFileComplete( new FtpOperationEvent( processedBytes, totalBytes, startTime, localFileName ) );
		}

		if( listener != null ) {
			listener.onEnd( new FtpOperationEvent( processedBytes, totalBytes, startTime, null ) );
		}

		return putOccurred;
	}
	
	/**
	 * Deletes the specified file from the FTP server.
     * @param remoteDir the remote directory of the file in question.
     * @param remoteFileName the name of the file to delete.
     * @return whether the file was deleted.
     * @throws FtpException if there is an error performing the operation.
     * @see FtpClient#deleteFile(String, String)
	 */
	public boolean deleteFile( String remoteDir, String remoteFileName )
		throws FtpException {
		
		this.assertConnectedAndLoggedIn();
		
		boolean deleted = false;
		
		// replace slashes in the filenames as appropriate
		remoteDir = getFTPPath( remoteDir );
		remoteFileName = getFTPPath( remoteFileName );
		
		this.cd( remoteDir );
		
		try {
			if( _ftpClient.deleteFile( remoteFileName ) ) {
				deleted = true;
			}
		}
		catch( Exception exception ) {
			throw new FtpException( "Exception occurred checking file existence", exception );
		}
		
		return deleted;
	}

	/**
	 * Determines whether the specified file exists on the FTP server.
	 * @see FtpClient#exists(String, String)
	 */
	public boolean exists( String remoteDir, String remoteFileName ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		boolean fileExists = false;
		String[] fileList = null;

		remoteDir = getFTPPath( remoteDir );
		remoteFileName = getFTPPath( remoteFileName );

		this.cd( remoteDir );

		try {
			fileList = _ftpClient.listNames( remoteFileName );
			if( ( fileList != null ) && ( fileList.length > 0 ) ) {
				if( fileList[ 0 ].indexOf( "No such file" ) < 0 ) {
					fileExists = true;
				}
			}
		}
		catch( Exception exception ) {
			throw new FtpException( "Exception occurred checking file existence", exception );
		}

		return fileExists;
	}

	/**
	 * Returns the names of the contents of the specified directory.
	 * @see FtpClient#list(String)
	 */
	public String[] list( String remoteDir ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		String remotePath = getFTPPath( remoteDir );
		this.cd( remotePath );

		try {
			String[] fileList = _ftpClient.listNames();
			return fileList;
		}
		catch( Exception exception ) {
			throw new FtpException( "Exception occurred listing files in directory '" + remoteDir + "'.", exception );
		}

	}

	/**
	 * Sends a site-specific command to the FTP server.
	 * @see FtpClient#sendSiteCommand(String)
	 */
	public boolean sendSiteCommand( String cmd ) throws FtpException {
		this.assertConnectedAndLoggedIn();
		try {
			boolean commandSent = _ftpClient.sendSiteCommand( cmd );
			return commandSent;
		}
		catch( Exception exception ) {
			throw new FtpException( "Exception occurred sending site specific command '" + cmd + "'.", exception );
		}
	}

	/**
	 * Gets the FTP path for the specified remote path.
	 * @param path the remote path to get an FTP path for.
	 * @return the FTP path for the specified remote path.
	 */
	public static String getFTPPath( String path ) {
		return ( getPath( path, '\\', FTP_FILE_SEPARATOR_CHAR ) );
	}

	/**
	 * Gets the FTP path for the specified remote file.
	 * @param directory the remote path of the directory containing the file.
	 * @param fileName the name of the file.
	 * @return the FTP path for the specified remote file.
	 */
	public static String getFTPPath( String directory, String fileName ) {
		return ( getPath( directory, fileName, '\\', FTP_FILE_SEPARATOR_CHAR ) );
	}

	/**
	 * Gets the system-dependent localized path for the specified local path.
	 * @param path the local path to turn into a system-appropriate path.
	 * @return the path appropriate for the local system.
	 */
	public static String getLocalizedPath( String path ) {
		return ( getPath( path, File.separatorChar == '/' ? '\\' : '/', File.separatorChar ) );
	}

	/**
	 * Gets the system-dependent localized path for the specified local file.
	 * @param directory the local path of the directory containing the file.
	 * @param fileName the name of the file.
	 * @return the path appropriate for the local system.
	 */
	public static String getLocalizedPath( String directory, String fileName ) {
		return ( getPath( directory, fileName, File.separatorChar == '/' ? '\\' : '/', File.separatorChar ) );
	}

	private static String getPath( String path, char searchForFileSeparator, char replacementFileSeparator ) {
		String newPath = null;

		if( path != null ) {
			if( searchForFileSeparator != replacementFileSeparator ) {
				newPath = path.replace( searchForFileSeparator, replacementFileSeparator );
			}
			else {
				newPath = path;
			}
		}

		return ( newPath );
	}

	private static String getPath( String directory, String fileName, char searchForFileSeparator, char replacementFileSeparator ) {
		String newPath = null;

		if( ( directory != null ) && ( fileName != null ) ) {
			directory = getPath( directory, searchForFileSeparator, replacementFileSeparator );
			fileName = getPath( fileName, searchForFileSeparator, replacementFileSeparator );

			if( directory.length() > 0 ) {
				if( directory.charAt( directory.length() - 1 ) != replacementFileSeparator ) {
					newPath = directory + replacementFileSeparator + fileName;
				}
				else {
					newPath = directory + fileName;
				}
			}
			else {
				newPath = fileName;
			}
		}
		else {
			if( directory != null ) {
				newPath = getPath( directory, searchForFileSeparator, replacementFileSeparator );
			}
			else {
				if( fileName != null ) {
					newPath = getPath( fileName, searchForFileSeparator, replacementFileSeparator );
				}
			}
		}

		return ( newPath );
	}

	/**
	 * Throws an exception if the client is not yet connected or the user is not yet logged in.
	 */
	private void assertConnectedAndLoggedIn() throws FtpException {
		if( !_connected ) throw new FtpException( "Not connected." );
		if( !_loggedIn ) throw new FtpException( "Not logged in." );
	}

	/**
	 * Changes directory on the server to the specified remote directory. If there is a problem
	 * changing the remote directory, an exception is thrown.
	 */
	private void cd( String remoteDir ) throws FtpException {

		this.assertConnectedAndLoggedIn();

		boolean directorySet = false;
		remoteDir = NCFtpClient.getFTPPath( remoteDir );
		if( remoteDir != null && remoteDir.length() > 0 ) {
			try {
				directorySet = _ftpClient.changeWorkingDirectory( remoteDir );
			}
			catch( Exception exception ) {
				throw new FtpException( "Exception occurred changing remote directory to " + remoteDir, exception );
			}
		}

		if( !directorySet ) { throw new FtpException( "Unable to change to remote directory " + remoteDir + "." ); }
	}

	/**
	 * Buffered output stream that triggers interval events every 1024 bytes.
	 */
//	private class FtpListeningBufferedOutputStream extends ListeningBufferedOutputStream {
//		private FtpOperationListener _listener;
//		private long _processedBytes;
//		private long _totalBytes;
//		private long _startTime;
//		private String _fileName;
//
//		/**
//		 * Default constructor.
//		 * @param out the output stream to buffer.
//		 * @param listener the FTP operation listener to send FTP operation
//		 *                 events to every 1024 bytes.
//		 * @param processedBytes unused.
//		 * @param totalBytes the total number of bytes of the operation; used in
//		 *                   the created FTP operation events.
//		 * @param startTime the start time of the operation; used in the created
//		 *                  FTP operation events.
//		 * @param fileName the name of the file being transferred; used in the
//		 *                 created FTP operation events.
//		 */
//		public FtpListeningBufferedOutputStream( OutputStream out,
//												 FtpOperationListener listener,
//												 long processedBytes,
//												 long totalBytes,
//												 long startTime,
//												 String fileName ) {
//			super( out, 1024 );
//			_listener = listener;
//			_totalBytes = totalBytes;
//			_startTime = startTime;
//			_fileName = fileName;
//		}
//
//		/**
//		 * Called every 1024 bytes.
//		 * @see ListeningBufferedOutputStream#onInterval(long)
//		 */
//		protected void onInterval( long written ) {
//			//LOG.info("" + (_processedBytes + written) + "/" + (_totalBytes) + " = " + (((double)(_processedBytes + written))/_totalBytes));
//			if( _listener != null ) _listener.onInterval( new FtpOperationEvent( _processedBytes + written, _totalBytes, _startTime, _fileName ) );
//		}
//	}
//
//	/**
//	 * Buffered input stream that triggers interval events every 1024 bytes.
//	 */
//	private class FtpListeningBufferedInputStream extends ListeningBufferedInputStream {
//		private FtpOperationListener _listener;
//		private long _processedBytes;
//		private long _totalBytes;
//		private long _startTime;
//		private String _fileName;
//
//		/**
//		 * Default constructor.
//		 * @param in the input stream to buffer.
//		 * @param listener the FTP operation listener to send FTP operation
//		 *                 events to every 1024 bytes.
//		 * @param processedBytes unused.
//		 * @param totalBytes the total number of bytes of the operation; used in
//		 *                   the created FTP operation events.
//		 * @param startTime the start time of the operation; used in the created
//		 *                  FTP operation events.
//		 * @param fileName the name of the file being transferred; used in the
//		 *                 created FTP operation events.
//		 */
//		public FtpListeningBufferedInputStream( InputStream in,
//												FtpOperationListener listener,
//												long processedBytes,
//												long totalBytes,
//												long startTime,
//												String fileName ) {
//			super( in, 1024 );
//			_listener = listener;
//			_totalBytes = totalBytes;
//			_startTime = startTime;
//			_fileName = fileName;
//		}
//
//		/**
//		 * Called every 1024 bytes.
//		 * @see ListeningBufferedInputStream#onInterval(long)
//		 */
//		protected void onInterval( long read ) {
//			//LOG.info("" + (_processedBytes + read) + "/" + (_totalBytes) + " = " + (((double)(_processedBytes + read))/_totalBytes));
//			if( _listener != null ) _listener.onInterval( new FtpOperationEvent( _processedBytes + read, _totalBytes, _startTime, _fileName ) );
//		}
//	}

}
