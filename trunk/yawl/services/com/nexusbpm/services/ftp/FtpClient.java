package com.nexusbpm.services.ftp;

import java.io.IOException;

/**
 * Interface for an FTP client that will connect and login to an FTP server
 * to put files from the local machine to the FTP server and to get files from
 * the FTP server to the local machine.
 */
public interface FtpClient
{
	/** Constant denoting an ASCII file type for file transfers. */
    public static final int ASCII_FILE_TYPE  = 1;
    /** Constant denoting a binary file type for file transfers. */
    public static final int BINARY_FILE_TYPE = 2;

    /**
     * Attempts to connect to the specified remote host and returns whether it
     * was successful.
     * @param remoteHost the name of the remote host to connect to.
     * @return whether the FTP client connected successfully.
     * @throws ConnectionException if there is an error attempting to connect.
     */
    public boolean connect( String remoteHost )
        throws IOException;

    /**
     * Disconnects the FTP client.
     */
    public void disconnect();
    
    /**
     * @return whether the FTP client is currently connected.
     */
    public boolean isConnected();

    /**
     * Logs in the specified user using the given password.
     * @param userId the name of the user to log in.
     * @param password the password of the specified user.
     * @return whether the FTP client was able to login the specified user.
     * @throws LoginException if there is an error attempting to login.
     */
    public boolean login( String userId,
                          String password )
        throws IOException;
    
    /**
     * Causes the FTP client to logout of the FTP server.
     */
    public void logout();
    
    /**
     * @return whether the FTP client is logged in to the FTP server.
     */
    public boolean isLoggedIn();

    /**
     * Transfers the specified file from the FTP server to the local machine.
     * @param remoteDir the path of the remote directory where the file exists.
     * @param localDir the path of the local directory where the file will be
     *                 copied to.
     * @param fileType the type of file transfer to perform.
     * @param fileSpecification the name of the file to transfer.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean get( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  fileSpecification )
        throws FtpException;

    /**
     * Transfers the specified file from the FTP server to the local machine,
     * possibly appending the contents of the remote file to the end of the
     * existing local file.
     * @param remoteDir the path of the remote directory where the file exists.
     * @param localDir the path of the local directory where the file will be
     *                 copied to.
     * @param fileType the type of file transfer to perform.
     * @param fileSpecification the name of the file to transfer.
     * @param appendIfExisting whether the contents of the remote file should be
     *                         appended to the existing local file.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean get( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  fileSpecification,
                        boolean appendIfExisting )
        throws FtpException;

    /**
     * Transfers the specified file from the FTP server to the local machine and
     * gives it the specified name.
     * @param remoteDir the path of the remote directory where the file exists.
     * @param localDir the path of the local directory where the file will be
     *                 copied to.
     * @param fileType the type of file transfer to perform.
     * @param localFileName the name to give the file on the local machine.
     * @param remoteFileName the name of the file on the remote machine.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean get( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  localFileName,
                        String  remoteFileName )
        throws FtpException;

    /**
     * Transfers the specified file from the FTP server to the local machine
     * giving it the specified name and possibly appending the contents of the
     * remote file to the end of the existing local file.
     * @param remoteDir the path of the remote directory where the file exists.
     * @param localDir the path of the local directory where the file will be
     *                 copied to.
     * @param fileType the type of file transfer to perform.
     * @param localFileName the name to give the file on the local machine.
     * @param remoteFileName the name of the file on the remote machine.
     * @param appendIfExisting whether the contents of the remote file should be
     *                         appended to the existing local file.
     * @param listener a listener that will be notified of the status of the
     *                 operation as it proceeds.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean get( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  localFileName,
                        String  remoteFileName,
                        boolean appendIfExisting,
                        FtpOperationListener listener )
        throws FtpException;

    /**
     * Transfers the contents of the specified remote directory into the
     * specified local directory.
     * @param remoteDir the path of the remote directory.
     * @param localDir the path of the local directory.
     * @param listener a listener that will be notified of the status of the
     *                 operation as it proceeds.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean getDirectory( String remoteDir,
                                 String localDir,
                                 FtpOperationListener listener)
        throws FtpException;

    /**
     * Transfers the specified file from the local machine to the FTP server.
     * @param remoteDir the path of the remote directory where the file will be
     *                  copied to.
     * @param localDir the path of the local directory where the file exists.
     * @param fileType the type of file transfer to perform.
     * @param fileSpecification the name of the file to transfer.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean put( String remoteDir,
                        String localDir,
                        int    fileType,
                        String fileSpecification )
        throws FtpException;

    /**
     * Transfers the specified file from the local machine to the FTP server,
     * possibly appending the contents of the local file to the end of the
     * existing remote file.
     * @param remoteDir the path of the remote directory where the file will be
     *                  copied to.
     * @param localDir the path of the local directory where the file exists.
     * @param fileType the type of file transfer to perform.
     * @param fileSpecification the name of the file to transfer.
     * @param appendIfExisting whether the contents of the local file should be
     *                         appended to the existing remote file.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean put( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  fileSpecification,
                        boolean appendIfExisting )
        throws FtpException;

    /**
     * Transfers the specified file from the local machine to the FTP server and
     * gives it the specified name.
     * @param remoteDir the path of the remote directory where the file will be
     *                  copied to.
     * @param localDir the path of the local directory where the file exists.
     * @param fileType the type of file transfer to perform.
     * @param localFileName the name of the file on the local machine.
     * @param remoteFileName the name to give the file on the remote machine.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean put( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  localFileName,
                        String  remoteFileName )
        throws FtpException;

    /**
     * Transfers the specified file from the local machine to the FTP server
     * giving it the specified name and possibly appending the contents of the
     * local file to the end of the existing remote file.
     * @param remoteDir the path of the remote directory where the file will be
     *                  copied to.
     * @param localDir the path of the local directory where the file exists.
     * @param fileType the type of file transfer to perform.
     * @param localFileName the name of the file on the local machine.
     * @param remoteFileName the name to give the file on the remote machine.
     * @param appendIfExisting whether the contents of the local file should be
     *                         appended to the existing remote file.
     * @param listener a listener that will be notified of the status of the
     *                 operation as it proceeds.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean put( String  remoteDir,
                        String  localDir,
                        int     fileType,
                        String  localFileName,
                        String  remoteFileName,
                        boolean appendIfExisting,
                        FtpOperationListener listener )
        throws FtpException;
    
    /**
     * Determines whether the specified file exists on the FTP server.
     * @param remoteDir the remote directory of the file in question.
     * @param remoteFileName the name of the file to query.
     * @return whether the specified file exists.
     * @throws FtpException if there is an error performing the operation.
     * @see java.io.File#exists()
     */
    public boolean exists( String remoteDir,
                           String remoteFileName )
        throws FtpException;
    
    /**
     * Deletes the specified file from the FTP server.
     * @param remoteDir the remote directory of the file in question.
     * @param remoteFileName the name of the file to delete.
     * @return whether the file was deleted.
     * @throws FtpException if there is an error performing the operation.
     */
    public boolean deleteFile( String remoteDir, String remoteFileName )
    	throws FtpException;
    
    /**
     * Returns the names of the contents of the specified directory.
     * @param remoteDir the path specifying the remote directory.
     * @return a <tt>String[]</tt> of the names of the specified remote
     *         directory's contents.
     * @throws FtpException if there is an error performing the operation.
     * @see java.io.File#list()
     */
    public String[] list( String remoteDir )
        throws FtpException;
    
    /**
     * Sends a site-specified command to the FTP server.
     * @param command the site-specific command and its arguments.
     * @return whether the operation was successful.
     * @throws FtpException if there is an error sending the command.
     */
    public boolean sendSiteCommand( String command )
        throws FtpException;
}