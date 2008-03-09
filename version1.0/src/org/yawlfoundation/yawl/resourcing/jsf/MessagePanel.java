package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

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

    public String format(String xml) {
        Element root = JDOMUtil.stringToElement(xml) ;
        if (root != null)
            return root.getText() ;
        else
            return null ;
    }

    private List<String> addMessage(List<String> list, String message) {
        if (list == null)  list = new ArrayList<String>();
        list.add(message);
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
            case warn : return "yellow" ;
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
        _pnlMessages.setStyle("background-color: #f0f0f0; width: 280px;" +
                               getPosStyle(60, 15)) ;
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
        listMessages(_errorMessage, MsgType.error);
        listMessages(_warnMessage, MsgType.warn);
        listMessages(_infoMessage, MsgType.info);
        listMessages(_successMessage, MsgType.success);
    }

    private void listMessages(List<String> list, MsgType msgType) {
        if (list != null) {
            for (String message : list) listMessage(message, msgType) ;
        }
    }

    private void listMessage(String message, MsgType msgType) {
        StaticText sttMessage = new StaticText();
        sttMessage.setId("stt" + getNextIDSuffix()) ;
        sttMessage.setText(message);
        sttMessage.setStyle(getFontStyle(0, msgType) + "width: 400px;");
        _pnlMessages.getChildren().add(sttMessage) ;
    }

    private int getNextIDSuffix() {
        return ++idSuffix ;
    }
}
