package org.yawlfoundation.yawl.analyser;

/**
 * @author Michael Adams
 * @date 15/05/12
 */
public class YAnalyserEvent {

    private YAnalyserEventType _eventType;
    private String _source;
    private String _message;

    public YAnalyserEvent(YAnalyserEventType eventType, String source, String message) {
        _eventType = eventType;
        _source = source;
        _message = message;
    }

    public String getSource() { return _source; }

    public YAnalyserEventType getEventType() { return _eventType; }

    public String getMessage() { return _message; }

}
