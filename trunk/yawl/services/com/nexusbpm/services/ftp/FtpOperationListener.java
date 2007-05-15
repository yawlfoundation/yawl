package com.nexusbpm.services.ftp;

/**
 * Listens for events related to a specifc FTP operation. The <tt>onStart()</tt>
 * method gets called at the beginning of the operation, the <tt>onEnd()</tt>
 * method gets called at the end of the operation, the <tt>onFileStart()</tt>
 * method gets called each time the FTP operation is about to be applied to an
 * individual file, and the <tt>onFileComplete()</tt> method gets called each time
 * the FTP operation has been applied to an individual file.
 * 
 * @see     FtpOperationEvent
 * @author  Daniel Gredler
 * @created Aug 23 2004
 */
public class FtpOperationListener {
    /**
     * Creates a new FtpOperationListener.
     */
	public FtpOperationListener() {
	  // Empty constructor.
	}//FtpOperationListener()
    
    /**
     * Called at the beginning of the FTP operation.
     * @param event the event causing this method to be called.
     */
	public void onStart(FtpOperationEvent event) {
		// Empty implementation.
	}//onStart()
    
    /**
     * Called each time the FTP operation is about to be applied to an
     * individual file.
     * @param event the event causing this method to be called.
     */
	public void onFileStart(FtpOperationEvent event) {
		// Empty implementation.
	}//onFileStart()
    
    /**
     * Called at intervals during the FTP operation.
     * @param event the event causing this method to be called.
     */
	public void onInterval(FtpOperationEvent event) {
		// Empty implementation.
	}//onInterval()
    
    /**
     * Called each time the FTP operation has been applied to an individual
     * file.
     * @param event the event causing this method to be called.
     */
	public void onFileComplete(FtpOperationEvent event) {
		// Empty implementation.
	}//onFileComplete()
    
    /**
     * Called at the end of the FTP operation.
     * @param event the event causing this method to be called.
     */
	public void onEnd(FtpOperationEvent event) {
		// Empty implementation.
	}//onEnd()

}
