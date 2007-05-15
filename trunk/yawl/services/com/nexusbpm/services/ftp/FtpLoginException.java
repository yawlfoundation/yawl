package com.nexusbpm.services.ftp;

/**
 * TODO unused?
 */
public class FtpLoginException extends Exception
{
    private String userId = null;
    
    
    public FtpLoginException( String userId,
                              String message )
    { this( userId, message, null ); }
    
    
    public FtpLoginException( String    userId,
                              String    message,
                              Throwable cause )
    { 
        super( message, cause );
        
        this.userId = userId;
    }
    
    
    public String getUserId()
    { return( userId ); }
    
    
    public String toString()
    { return( super.toString() + ":  Login User Id:  " + userId ); }
}
