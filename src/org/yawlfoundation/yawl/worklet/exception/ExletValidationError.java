package org.yawlfoundation.yawl.worklet.exception;

/**
 * @author Michael Adams
 * @date 27/08/15
 */
public class ExletValidationError {

    private int _index;
    private String _msg;

    public ExletValidationError() { }

    public ExletValidationError(int index, String msg) {
        _index = index;
        _msg = msg;
    }


    public int getIndex() { return _index; }

    public String getMessage() { return _msg; }

}
