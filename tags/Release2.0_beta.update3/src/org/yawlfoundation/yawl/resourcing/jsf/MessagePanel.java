/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSF component that is used to display messages to users
 *
 * Author: Michael Adams
 * Creation Date: 28/02/2008
 * Last Date: 26/08/2008
 */

public class MessagePanel extends PanelLayout {

    // types of messages
    public enum MsgType { error, info, warn, success }

    // icons corresponding to each message type
    private static final String errorIconURL = "/resources/error.png" ;
    private static final String infoIconURL = "/resources/info.png" ;
    private static final String warnIconURL = "/resources/warn.png" ;
    private static final String successIconURL = "/resources/success.png" ;

    // panel width / font width: 268 / 6
    private static final int CHARS_PER_LINE = 45;


    // list of messages for each type
    private List<String> _errorMessage;
    private List<String> _warnMessage;
    private List<String> _infoMessage;
    private List<String> _successMessage;

    private int idSuffix = 0 ;                         // used in creation of unique ids
    private String _style = "";


    private PanelLayout _pnlMessages;
    private ImageComponent _imgIcon;

    public MessagePanel() {
        this.setId("msgPanel001");
        this.setStyleClass("messagePanel") ;
        this.getChildren().add(constructImage());
        this.getChildren().add(constructMessagesPanel());
        this.setVisible(false);                                   // hide it initially
    }

    /* adds an error message */
    public void error(String message) {
        _errorMessage = addMessage(_errorMessage, message);
    }

    /* adds a warning message */
    public void warn(String message) {
        _warnMessage = addMessage(_warnMessage, message);
    }

    /* adds an info message */
    public void info(String message) {
        _infoMessage = addMessage(_infoMessage, message);
    }

    /* adds a success message */
    public void success(String message) {
        _successMessage = addMessage(_successMessage, message);
    }

    /* removes all messages */
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


    /* show the panel in the coords passed - position = absolute or relative */
    public void show(int top, int left, String position) {
        _style = String.format("top: %dpx; left: %dpx; position: %s;",
                                top, left, position);
        showPanel();
    }

    /* show the panel */
    public void show() {
        _style = "top:70px; left:0px; position: relative;";      // default style & posn
        showPanel();
    }

    private void showPanel() {
        MsgType msgType = getDominantType() ;
        if (msgType != null) {                          // if msgs to show
            _pnlMessages.getChildren().clear();
            _imgIcon.setUrl(getImageURL(msgType));        
            setMessages();
            this.setVisible(true);                     // show the form
            clear();                                   // remove msgs from next rendering
        }
        else this.setVisible(false);
    }


    /* returns strings from inside xml tags */
    private String unwrap(String xml) {
        String result = null;
        if (xml != null) {
            int start = xml.indexOf(">") + 1;
            int finish = xml.lastIndexOf("<");
            if (start >= 0 && finish >= 0 && finish > start) {
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

    /* adds a message to a particular list */
    private List<String> addMessage(List<String> list, String message) {
        if (list == null)  list = new ArrayList<String>();
        list.add(message);
        return list ;
    }


    /* not currently used */
    private String getStyle(int left, int top, int fontSize, MsgType msgType) {
        return getPosStyle(left, top) + getFontStyle(fontSize, msgType) ;
    }


    /* creates a style string for positioning */
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
        _imgIcon.setHeight(48);
        _imgIcon.setWidth(48);
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


    private PanelLayout constructInnerPanel() {
        PanelLayout inner = new PanelLayout() ;
        inner.setId("pnlInner" + getNextIDSuffix());
        inner.setStyle("background-color: #f0f0f0; width: 268px") ;
        inner.setPanelLayout("flow");
        return inner ;
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
                lineCount += (message.length() / CHARS_PER_LINE) + 1;
                listMessage(message, msgType) ;
            }
            return lineCount;
        }
        return 0;
    }

    private void listMessage(String message, MsgType msgType) {
        PanelLayout innerPanel = constructInnerPanel();
        StaticText sttMessage = new StaticText();
        sttMessage.setId("stt" + getNextIDSuffix()) ;
        sttMessage.setText(message);
        sttMessage.setStyle(getFontStyle(0, msgType));
        innerPanel.getChildren().add(sttMessage) ;
        _pnlMessages.getChildren().add(innerPanel);
    }

    private int getNextIDSuffix() {
        return ++idSuffix ;
    }

    private void setHeight(int lineCount) {

        // estimated constants
        double minHeight = 70.0;
        double lineHeight = 15.5;

        double height = Math.round(Math.max(minHeight, lineCount * lineHeight));
        this.setStyle(String.format("%s height: %.0fpx", _style, height));
    }
}
