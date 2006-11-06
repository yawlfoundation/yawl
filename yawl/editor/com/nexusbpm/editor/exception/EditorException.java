/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.exception;


public class EditorException extends Exception {
    public EditorException(Throwable cause) {
        super(cause);
    }
	public EditorException(String message) {
		super(message);
	}
    public EditorException(String message, Throwable cause) {
        super(message, cause);
    }
}
