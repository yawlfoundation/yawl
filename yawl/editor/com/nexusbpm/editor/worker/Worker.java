package com.nexusbpm.editor.worker;

/**
 * @author HoY
 * @author Daniel Gredler
 */
public interface Worker {
	/**
	 * The implementation of whatever work this worker does.<br>
	 * Implemented by each subclass.
	 * @throws Throwable if the worker encounters an error.
	 */
	public void execute() throws Throwable;
	/**
	 * @return the name of this worker.
	 */
	public String getName();
}
