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
