/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
