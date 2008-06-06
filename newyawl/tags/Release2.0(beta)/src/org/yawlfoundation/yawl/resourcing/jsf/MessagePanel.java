package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 28/02/2008
 */
public class MessagePanel extends PanelLayout {

    public enum MsgType { error, info, warn, success }

    private static final String errorIconURL = "/resources/error.png" ;
    private static final String infoIconURL = "/resources/info.png" ;
    private static final String warnIconURL = "/resources/warn.png" ;
    private static final String successIconURL = "/resources/success.png" ;

    private List<String> _errorMessage;
    private List<String> _warnMessage;
    private List<String> _infoMessage;
    private List<String> _successMessage;
    private int idSuffix = 0 ;
    private String _style = "";
    private int msgTop = 0 ;

    private PanelLayout _pnlMessages;
    private ImageComponent _imgIcon;

    public MessagePanel() {
        this.setId("msgPanel001");
        this.setStyleClass("messagePanel") ;
        this.getChildren().add(constructImage());
        this.getChildren().add(constructMessagesPanel());
        this.setVisible(false);
    }

    
    public void error(String message) {
        _errorMessage = addMessage(_errorMessage, message);
    }

    public void warn(String message) {
        _warnMessage = addMessage(_warnMessage, message);
    }

    public void info(String message) {
        _infoMessage = addMessage(_infoMessage, message);
    }

    public void success(String message) {
        _successMessage = addMessage(_successMessage, message);
    }

    public void clear() {
        _errorMessage = null;
        _infoMessage = null;
        _warnMessage = null;
        _successMessage = null;
    }
    
    public boolean hasMessage() {
        return (_errorMessage != null) || (_infoMessage != null) ||
               (_warnMessage != null) || (_successMessage != null);
    }

    public void show(int top, int left, String position) {
        _style = String.format("top: %dpx; left: %dpx; position: %s;",
                                top, left, position);
        show();
    }

    public void show() {
        MsgType msgType = getDominantType() ;
        if (msgType != null) {                          // if msgs to show
            _pnlMessages.getChildren().clear();
            _imgIcon.setUrl(getImageURL(msgType));        
            setMessages();
            this.setVisible(true);
            clear();
        }
        else this.setVisible(false);
    }


    private String unwrap(String xml) {
        String result = null;
        if (xml != null) {
            int start = xml.indexOf(">") + 1;
            int finish = xml.lastIndexOf("<");
            if (start >= 0 && finish >= 0) {
                result = xml.substring(start, finish);
            }
        }
        return result;
    }


    public String format(String xml) {
        while ((xml != null) && xml.startsWith("<"))
            xml = unwrap(xml);
        return xml ;
    }


    private List<String> addMessage(List<String> list, String message) {
        if (list == null)  list = new ArrayList<String>();
        list.add(message + System.getProperty("line.separator"));
        return list ;
    }



    private String getStyle(int left, int top, int fontSize, MsgType msgType) {
        return getPosStyle(left, top) + getFontStyle(fontSize, msgType) ;
    }


    private String getPosStyle(int left, int top) {
        return String.format("position: absolute; left: %dpx; top: %dpx;", left, top);

    }


    private String getFontColor(MsgType msgType) {
        switch (msgType) {
            case error :  return "red" ;
            case warn : return "orange" ;
            case info : return "blue" ;
            case success : return "green" ;
        }
        return "black";          // default
    }


    private String getFontStyle(int size, MsgType msgType) {
        String fontSize = (size > 0) ? String.format("font-size: %dpx;", size) : "";
        return fontSize + String.format("color: %s;", getFontColor(msgType));
    }


    private ImageComponent constructImage() {
        _imgIcon = new ImageComponent() ;
        _imgIcon.setId("imgIcon001");
        _imgIcon.setUrl(errorIconURL);
        _imgIcon.setStyle(getPosStyle(10, 10) + "background-color: #f0f0f0; " +
                                                "height: 48px; width: 48px;") ;
        return _imgIcon ;
    }



    private String getImageURL(MsgType msgType) {
        switch (msgType) {
            case error : return errorIconURL ;
            case warn  : return warnIconURL ;
            case info  : return infoIconURL ;
            case success : return successIconURL ;
        }
        return null;          // default
    }


    private PanelLayout constructMessagesPanel() {
        _pnlMessages = new PanelLayout() ;
        _pnlMessages.setId("pnlMessages001");
        _pnlMessages.setStyle("background-color: #f0f0f0; width: 270px;" +
                               getPosStyle(70, 15)) ;
        _pnlMessages.setPanelLayout("flow");
        return _pnlMessages ;
    }


    private MsgType getDominantType() {
        if (_errorMessage != null) return MsgType.error ;
        else if (_warnMessage != null) return MsgType.warn ;
        else if (_infoMessage != null) return MsgType.info ;
        else if (_successMessage != null) return MsgType.success ;
        else return null ;
    }


    private void setMessages() {
        int lineCount = 0;
        lineCount += listMessages(_errorMessage, MsgType.error, lineCount);
        lineCount += listMessages(_warnMessage, MsgType.warn, lineCount);
        lineCount += listMessages(_infoMessage, MsgType.info, lineCount);
        lineCount += listMessages(_successMessage, MsgType.success, lineCount);
        setHeight(lineCount);
    }

    private int listMessages(List<String> list, MsgType msgType, int lineCount) {
        if (list != null) {
            for (String message : list) {
                lineCount += (message.length() / 45) + 1;     // approx. 45chrs per line
                listMessage(message, msgType, lineCount) ;
            }
            return lineCount;
        }
        return 0;
    }

    private void listMessage(String message, MsgType msgType, int lineCount) {
        StaticText sttMessage = new StaticText();
        sttMessage.setId("stt" + getNextIDSuffix()) ;
        sttMessage.setText(message);
        sttMessage.setStyle(getFontStyle(0, msgType)); // +
            //    "width: 270px; top: " + (lineCount * 15) + "px; position: absolute;");
        _pnlMessages.getChildren().add(sttMessage) ;
    }

    private int getNextIDSuffix() {
        return ++idSuffix ;
    }

    private void setHeight(int lineCount) {

        // rough est. - assume 45 chars/line, 15px per line height, min height 70 (for icon)
        long height = Math.round(Math.max(70, lineCount * 15));
        this.setStyle(String.format("%s height: %dpx", _style, height));
    }
}
