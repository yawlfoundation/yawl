package com.nexusbpm.services.ftp;

/**
 * Factory for creating FTP clients.
 */
public class FtpClientFactory
{
    private static FtpClientFactory ftpClientFactory = new FtpClientFactory();
    
    /**
     * Private constructor to enforce a singleton instance of the factory.
     */
    private FtpClientFactory()
    {}
    
    /**
     * @return the singleton instance of the FTP client factory.
     */
    public static FtpClientFactory getInstance()
    { return( ftpClientFactory ); }
    
    /**
     * Creates an FTP client.
     * @return the created FTP client.
     */
    public FtpClient create()
    { return( new NCFtpClient() ); }
}