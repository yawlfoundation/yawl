package com.nexusbpm.services.ftp;

/**
 * TODO unused?
 */
public class FtpConnectionException extends Exception
{
    private String host = null;
    
    
    public FtpConnectionException( String host,
                                   String message )
    { this( host, message, null ); }
    
    
    public FtpConnectionException( String    host,
                                   String    message,
                                   Throwable cause )
    {
        super( message, cause );
        
        this.host = host;
    }
    
    
    public String getHost()
    { return( host ); }
    
    
    public String toString()
    { return( super.toString() + ":  Host:  " + host ); }
}
