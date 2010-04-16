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

import javax.faces.el.MethodBinding;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.awt.*;
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

    private static final int MIN_PANEL_HEIGHT = 120;
    private static final Font _msgFont = new Font("Helvetica", Font.PLAIN, 13);


    // list of messages for each type
    private List<String> _errorMessage;
    private List<String> _warnMessage;
    private List<String> _infoMessage;
    private List<String> _successMessage;

    private int idSuffix = 0 ;                         // used in creation of unique ids
    private String _style = "";
    private int _outerWidth = 350;                     // default width of panel
    private int _parentWidth;                          // width of parent container

    private PanelLayout _pnlMessages;
    private StaticText _title;
    private ImageComponent _imgIcon;
    private PanelLayout _btnPanel;

    public MessagePanel() {
        setId("msgPanel001");
        setStyleClass("messagePanel") ;
        getChildren().add(constructTitleBar());
        getChildren().add(constructImage());
        getChildren().add(constructMessagesPanel());
        getChildren().add(constructButtonPanel());
        setVisible(false);                                   // hide it initially
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
        _style = "top:60px; position:absolute;";      // default style & posn
        showPanel();
    }

    public void show(int width) {
        _parentWidth = width;
        show();
    }

    private void showPanel() {
        MsgType msgType = getDominantType() ;
        if (msgType != null) {                         // if msgs to show
            setupPanel(msgType);
            setVisible(true);                          // show the panel
            clear();                                   // remove msgs from next rendering
        }
        else setVisible(false);
    }

    private void setupPanel(MsgType msgType) {
        _pnlMessages.getChildren().clear();
        _imgIcon.setUrl(getImageURL(msgType));
        setTitle(msgType);
        int lines = setMessages();
        int height = getHeight(lines);
        setButtonPanelStyle(height);
        setStyle(height);
    }


    /* returns strings from inside xml tags */
    private String unwrap(String xml) {
        if (xml != null) {
            String[] stripped = xml.split("^\\s*<.*?>|</[^<]*?>\\s*$");
            return stripped[stripped.length -1];
        }
        return null;
    }


    public String format(String xml) {
        while ((xml != null) && xml.trim().startsWith("<"))
            xml = unwrap(xml);
        return xml ;
    }

    /* adds a message to a particular list */
    private List<String> addMessage(List<String> list, String message) {
        if (message != null) {
            if (list == null) list = new ArrayList<String>();
            list.add(format(message));
        }    
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
        _imgIcon.setStyle(getPosStyle(10, 30) + "background-color: #f0f0f0; " +
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


    private PanelLayout constructTitleBar() {
        PanelLayout titleBar = new PanelLayout() ;
        titleBar.setId("pnlTitleBar001");
        titleBar.setStyle("background-color: #b0b0b0; border-bottom: 1px solid black;" +
                           "width: 100%; height: 18px; text-align:left; font-weight:bold") ;
        titleBar.setPanelLayout("flow");
        _title = new StaticText();
        _title.setId("sttTitleText001");
        _title.setText("Example Title Bar Text");
        titleBar.getChildren().add(_title);
        return titleBar ;
    }


    private PanelLayout constructButtonPanel() {
        _btnPanel = new PanelLayout() ;
        _btnPanel.setId("pnlButton001");
        _btnPanel.setPanelLayout("flow");
        _btnPanel.getChildren().add(constructOKButton());
        return _btnPanel ;
    }


    private com.sun.rave.web.ui.component.Button constructOKButton() {
        com.sun.rave.web.ui.component.Button btnOK = new com.sun.rave.web.ui.component.Button();
        btnOK.setId("btnOK001");
        btnOK.setText("OK");
        btnOK.setActionListener(bindButtonListener());
        return btnOK;
    }

    private MethodBinding bindButtonListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{SessionBean.messagePanelOKBtnAction}",
                                         new Class[]{ActionEvent.class});
    }



    private PanelLayout constructMessagesPanel() {
        _pnlMessages = new PanelLayout() ;
        _pnlMessages.setId("pnlMessages001");
        _pnlMessages.setStyle("background-color: #f0f0f0; " +
                               getPosStyle(70, 35) + "width: 270px;") ;
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


    private void setTitle(MsgType msgType) {
        String text = "";
        switch (msgType) {
            case error : text = "Error"; break;
            case warn : text = "Warning"; break;
            case info : text = "Information"; break;
            case success : text = "Success"; break;
        }
        _title.setText(text);
    }


    private int setMessages() {
        int lineCount = 0;
        lineCount += listMessages(_errorMessage, MsgType.error, lineCount);
        lineCount += listMessages(_warnMessage, MsgType.warn, lineCount);
        lineCount += listMessages(_infoMessage, MsgType.info, lineCount);
        lineCount += listMessages(_successMessage, MsgType.success, lineCount);
        return lineCount;
    }

    private int listMessages(List<String> list, MsgType msgType, int lineCount) {
        if (list != null) {
            for (String message : list) {
                Dimension bounds = FontUtil.getFontMetrics(message, _msgFont);
                lineCount += (int) Math.ceil(bounds.getWidth() / _outerWidth);
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
        adjustOuterSize(message);
    }

    private int getNextIDSuffix() {
        return ++idSuffix ;
    }

    private int getHeight(int lineCount) {
        double lineHeight = FontUtil.getFontMetrics("dummyText", _msgFont).getHeight() +
                            (_msgFont.getSize() / 2);
        return (int) Math.max(MIN_PANEL_HEIGHT, lineCount * lineHeight);
    }


    private void setButtonPanelStyle(int height) {
        _btnPanel.setStyle("background-color: #f0f0f0; width: 100%; height: 30px;" +
                           "position: absolute; top:" + (height - 30) + "px;") ;

    }

    private void setStyle(int height) {
        String style = String.format("%s height: %dpx; width: %dpx;",
                _style, height, _outerWidth);

        if (_parentWidth > 0) {
            style += String.format("left: %dpx;", (_parentWidth - _outerWidth) / 2);
        }
        setStyle(style);
    }

    private void adjustOuterSize(String msg) {
        Dimension bounds = FontUtil.getFontMetrics(getLongestWord(msg), _msgFont);
        _outerWidth = (int) Math.max(_outerWidth, bounds.getWidth() + 80);
    }

    private String getLongestWord(String msg) {
        if ((msg == null) || msg.length() == 0) return " ";
        String[] words = msg.split("\\s+");
        String longest = "";
        for (String word : words) {
            if (word.length() > longest.length()) {
                longest = word;
            }
        }
        return longest;
    }
}
