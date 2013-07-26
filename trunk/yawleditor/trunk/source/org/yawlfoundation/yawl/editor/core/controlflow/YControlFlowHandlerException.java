package org.yawlfoundation.yawl.editor.core.controlflow;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class YControlFlowHandlerException extends Exception {

    public YControlFlowHandlerException() { }

    public YControlFlowHandlerException(String s) {
        super(s);
    }

    public YControlFlowHandlerException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
