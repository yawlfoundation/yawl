package com.nexusbpm.services.ftp;

/**
 * An FTP operation event gets generated at the beginning of an FTP operation,
 * at the end of an FTP operation, and during certain intervals while the FTP
 * operation is executing.
 * 
 * @see     FtpOperationListener
 * @author  Daniel Gredler
 * @created Aug 23 2004
 */
public class FtpOperationEvent {

	/**
	 * Creates a new <tt>FtpOperationEvent</tt> according to the specified
	 * parameters.
     * @param processedBytes the number of bytes currently processed.
     * @param totalBytes the number of bytes that must be processed before the
     *        FTP operation which is the source of this event will be complete
     * @param operationStartTime the system time at which the operation started
     * @param fileName the name of the file for this event 
	 */
	public FtpOperationEvent(
		long processedBytes,
		long totalBytes,
		long operationStartTime,
		String fileName) {
		_processedBytes = processedBytes;
		_totalBytes = totalBytes;
		_operationStartTime = operationStartTime;
		_operationEventTime = System.currentTimeMillis();
		_fileName = fileName;
	}//FtpOperationEvent()


	private long _processedBytes;
	/**
	 * Retrieves the number of bytes currently processed by the FTP operation
	 * which is the source of this event.
     * @return the number of bytes currently processed.
	 */
	public long getProcessedBytes() {
		return _processedBytes;
	}//getProcessedBytes()
	/**
	 * Sets the number of bytes currently processed by the FTP operation
	 * which is the source of this event.
     * @param bytes the number of bytes currently processed.
	 */
	protected void setProcessedBytes(long bytes) {
		_processedBytes = bytes;
	}//setProcessedBytes()


	private long _totalBytes;
	/**
	 * Retrieves the number of bytes that must be processed before the FTP
	 * operation which is the source of this event will be complete.
     * @return the number of bytes that must be processed before the FTP
     *         operation which is the source of this event will be complete.
	 */
	public long getTotalBytes() {
		return _totalBytes;
	}//getTotalBytes()
	/**
	 * Sets the number of bytes that must be processed before the FTP
	 * operation which is the source of this event will be complete.
     * @param bytes the number of bytes that must be processed before the FTP
     *        operation which is the source of this event will be complete.
	 */
	protected void setTotalBytes(long bytes) {
		_totalBytes = bytes;
	}//setTotalBytes()


	private long _operationStartTime;
	/**
	 * Retrieves the system time at which the FTP operation which is the source
	 * of this event started.
     * @return the system time at which the operation started.
	 */
	public long getOperationStartTime() {
		return _operationStartTime;
	}//getOperationStartTime()
	/**
	 * Sets the system time at which the FTP operation which is the source
	 * of this event started.
     * @param startTime the system time at which the operation started.
	 */
	protected void setOperationStartTime(long startTime) {
		_operationStartTime = startTime;
	}//setOperationStartTime()


	private long _operationEventTime;
	/**
	 * Retrieves the system time at which this event was generated.
     * @return the system time at which this event was generated.
	 */
	public long getOperationEventTime() {
		return _operationEventTime;
	}//getOperationEventTime()
	/**
	 * Sets the system time at which this event was generated.
     * @param eventTime the system time at which this event was generated.
	 */
	protected void setOperationEventTime(long eventTime) {
		_operationEventTime = eventTime;
	}//setOperationEventTime()


	private String _fileName;
	/**
	 * Retrieves the name of the file for which this event was generated.
	 * May be <tt>null</tt>.
     * @return the name of the file for which this event was generated.
	 */
	public String getFileName() {
		return _fileName;
	}//getFileName()
	/**
	 * Sets the name of the file for which this event was generated.
     * @param fileName the name of the file for which this event was generated.
	 */
	protected void setFileName(String fileName) {
	  _fileName = fileName;
	}//setFileName()
}
