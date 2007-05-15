package com.nexusbpm.services.ftp;

/**
 * Exception thrown if there is an error with an FTP operation.
 */
public class FtpException extends Exception
{
	/**
	 * Creates an FtpException with the given error message.
	 * @param message the error message.
	 * @see Exception#Exception(String)
	 */
    public FtpException( String message )
    { super( message ); }
    
    /**
     * Creates an FtpException with the specified cause.
     * @param cause the cause of the FtpException.
     * @see Exception#Exception(Throwable)
     */
    public FtpException( Throwable cause )
    { super( cause ); }
    
    /**
     * Creates an FtpException with the specified message and cause.
     * @param message the error message.
     * @param cause the cause of the FtpException.
     * @see Exception#Exception(String, Throwable)
     */
    public FtpException( String    message,
                         Throwable cause )
    { super( message, cause ); }
}