package org.yawlfoundation.yawl.editor.core.exception;

/**
 * @author Michael Adams
 * @date 18/08/13
 */
public class IllegalIdentifierException extends IllegalArgumentException {

    public IllegalIdentifierException() {
        super();
    }

    public IllegalIdentifierException(String s) {
        super(s);
    }

    public IllegalIdentifierException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
