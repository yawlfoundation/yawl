package org.chiba.connectors.xmlrpc;


public class DocTransformerException extends Exception {

    /**
     * The internal id of this exception.
     */
    protected String id = "doctransformer-exception";

    /**
     * The cause for this exception.
     */
    protected Exception cause = null;

    /**
     * Creates a new doctransformer exception.
     *
     * @param message the error message.
     */
    public DocTransformerException(String message) {
        super(message);
    }

    /**
     * Creates a new doctransformer exception.
     *
     * @param cause the root cause.
     */
    public DocTransformerException(Exception cause) {
        super(cause);
    }

    /**
     * Creates a new doctransformer exception.
     *
     * @param message the error message.
     * @param cause   the root cause.
     */
    public DocTransformerException(String message, Exception cause) {
        super(message, cause);
    }
}
