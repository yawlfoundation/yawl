/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.component.Label;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A JSF component that is used to display messages to users
 *
 * Author: Michael Adams
 * Creation Date: 28/02/2008
 * Last Date: 17/04/2010
 */

public class MessagePanel extends PanelLayout {

    // types of messages
    public enum MsgType { error, warn, info, success }

    // icons corresponding to each message type
    private static final String errorIconURL = "/resources/error.png" ;
    private static final String infoIconURL = "/resources/info.png" ;
    private static final String warnIconURL = "/resources/warn.png" ;
    private static final String successIconURL = "/resources/success.png" ;

    private static final int TITLEBAR_HEIGHT = 18;
    private static final int BTNPANEL_HEIGHT = 30;
    private static final int MIN_MESSAGES_WIDTH = 270;
    private static final int MIN_PANEL_HEIGHT = 120;
    private static final int NON_MESSAGE_WIDTH = 80;
    private static final int PANEL_VSPACE = 5;
    private static final int MESSAGE_VSPACE = 6;
    private static final Font _msgFont = new Font("SansSerif", Font.PLAIN, 13);


    // list of messages for each type
    private Map<String, MsgType> _messages;

    private int idSuffix = 0 ;                         // used in creation of unique ids
    private String _style = "";
    private int _parentWidth;                          // width of parent container
    private String _titleText;
    private int _height;

    private PanelLayout _pnlMessages;
    private StaticText _title;
    private ImageComponent _imgIcon;
    private PanelLayout _btnPanel;

    //  MESSAGE PANEL LAYOUT:
    //
    //   |---------------------|
    //   | title panel         |
    //   |---------------------|
    //   |  image |   messages |
    //   |  panel |   panel    |
    //   |--------|            |
    //   |        |            |
    //   |---------------------|
    //   | button panel        |
    //   |---------------------|

    public MessagePanel() {
        _messages = new LinkedHashMap<String, MsgType>();    // keeps insertion order
        setId("msgPanel001");
        setStyleClass("messagePanel") ;
        composeContents();
        setVisible(false);                                   // hide it initially
    }


    /* adds an error message */
    public void error(String message) {
        _messages.put(format(message), MsgType.error);
    }

    public void error(List<String> msgList) {
        for (String message : msgList) error(message);
    }

    /* adds a warning message */
    public void warn(String message) {
        _messages.put(format(message), MsgType.warn);
    }

    public void warn(List<String> msgList) {
        for (String message : msgList) warn(message);
    }

    /* adds an info message */
    public void info(String message) {
        _messages.put(format(message), MsgType.info);
    }

    public void info(List<String> msgList) {
        for (String message : msgList) info(message);
    }

    /* adds a success message */
    public void success(String message) {
        _messages.put(format(message), MsgType.success);
    }

    public void success(List<String> msgList) {
        for (String message : msgList) success(message);
    }

    /* removes all messages */
    public void clear() {
        _messages.clear();
        _titleText = null;
    }

    public void setTitleText(String text) {
        _titleText = text;
    }
    
    public boolean hasMessage() {
        return ! _messages.isEmpty();
    }


    /* show the panel in the coords passed; position = absolute or relative */
    public void show(int top, int left, String position) {
        _style = String.format("top: %dpx; left: %dpx; position: %s;",
                                top, left, position);
        showPanel();
    }

    /* show the panel with default settings */
    public void show() {
        _style = "top:60px; position:absolute;";      // default style & posn
        showPanel();
    }

    /* show the panel centered inside its parent */
    public void show(int width) {
        _parentWidth = width;
        show();
    }

    /* removes any surrounding xml tags from text */
    public String format(String xml) {
        while ((xml != null) && xml.trim().startsWith("<"))
            xml = unwrap(xml);
        return xml ;
    }

    public int getHeight() { return _height; }


    /***************************************************************************/

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
        _pnlMessages.getChildren().clear();             // clear any previous msgs
        _imgIcon.setUrl(getImageURL(msgType));
        setTitle(msgType);
        listMessages();
        sizeAndPositionContent();
    }


    /* returns strings from inside xml tags */
    private String unwrap(String xml) {
        if (xml != null) {
            if (xml.endsWith("/>")) {
                return xml.substring(1, xml.length()-2);
            }
            String[] stripped = xml.split("^\\s*<.*?>|</[^<]*?>\\s*$");
            return stripped[stripped.length -1];
        }
        return null;
    }


    /* creates a style string for positioning */
    private String getPosStyle(int left, int top) {
        return String.format("position: absolute; left: %dpx; top: %dpx;", left, top);

    }


    private void composeContents() {
        getChildren().add(constructTitleBar());
        getChildren().add(constructImage());
        getChildren().add(constructMessagesPanel());
        getChildren().add(constructButtonPanel());
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
                           "width: 100%; text-align: left; font-weight: bold; " +
                           "height: " + TITLEBAR_HEIGHT + "px;") ;
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
        Button btnOK = new Button();
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
        _pnlMessages.setPanelLayout("flow");
        return _pnlMessages ;
    }


    /**
     * Each message goes inside its own panel, which in turn goes inside _pnlMessages
     * @return the child panel with a message in it
     */
    private PanelLayout constructInnerPanel() {
        PanelLayout inner = new PanelLayout() ;
        inner.setId("pnlInner" + getNextIDSuffix());
        inner.setStyleClass("msgPanelMessage");
        inner.setPanelLayout("flow");
        return inner ;
    }


    private MsgType getDominantType() {
        if (_messages.containsValue(MsgType.error)) return MsgType.error ;
        else if (_messages.containsValue(MsgType.warn)) return MsgType.warn ;
        else if (_messages.containsValue(MsgType.info)) return MsgType.info ;
        else if (_messages.containsValue(MsgType.success)) return MsgType.success ;
        else return null ; 
    }


    private String getTitle(MsgType msgType) {
        switch (msgType) {
            case error : return "Error";
            case warn : return "Warning";
            case info : return "Information";
            case success : return "Success";
            default: return "";  // should never be reached
        }
    }


    private void setTitle(MsgType msgType) {
        String text = getTitle(msgType);
        if (_titleText != null) text += ": " + _titleText;
        _title.setText(text);
    }


    private boolean hasMixedMessages() {
        if (hasMessage()) {
            MsgType dominantType = getDominantType();
            for (MsgType msgType : _messages.values()) {
                if (msgType != dominantType) {
                    return true;
                }
            }
        }
        return false;
    }

    
    private void listMessages() {
        if (hasMessage()) {
            boolean mixed = hasMixedMessages();
            for (String message : _messages.keySet()) {
                String msg = mixed ?
                             getTitle(_messages.get(message)) + ": " + message : message;
                listMessage(msg, _messages.get(message)) ;
                addVSpace();
            }
        }
    }


    private void listMessage(String message, MsgType msgType) {
        PanelLayout innerPanel = constructInnerPanel();
        StaticText sttMessage = new StaticText();
        sttMessage.setId("stt" + getNextIDSuffix()) ;
        sttMessage.setText(message);
        sttMessage.setStyle("font-family:verdana, sans-serif");
        innerPanel.getChildren().add(sttMessage) ;
        _pnlMessages.getChildren().add(innerPanel);
    }


    private void addVSpace() {
        Label label = new Label();
        label.setText("#####");                                  // won't be visible
        label.setId("vsp" + getNextIDSuffix());
        label.setStyle("color:#f0f0f0; font-size:6px;");    // same colour as background
        _pnlMessages.getChildren().add(label);
    }


    private int getNextIDSuffix() {
        return ++idSuffix ;
    }


    private void setButtonPanelStyle(int height) {
        _btnPanel.setStyle("background-color: #f0f0f0; width: 100%; height: " +
                BTNPANEL_HEIGHT + "px; position: absolute; top:" +
                (height - BTNPANEL_HEIGHT) + "px;") ;
    }


    private void setStyle(int width, int height) {
        String style = String.format("%s height: %dpx; width: %dpx;",
                _style, height, width);

        if (_parentWidth > 0) {
            style += String.format("left: %dpx;", (_parentWidth - width) / 2);
        }
        setStyle(style);
    }


    private String getLongestWord() {
        String longest = "";
        if (hasMessage()) {
            for (String message : _messages.keySet()) {
                String subLongest = getLongestWord(message);
                if (subLongest.length() > longest.length()) {
                    longest = subLongest;
                }
            }
        }
        return longest;
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


    private int getMessagesHeight(int width) {
        int height = 0;
        if (hasMessage()) {
            for (String message : _messages.keySet()) {
                Dimension bounds = FontUtil.getFontMetrics(message, _msgFont);
                int lines = (int) Math.ceil(bounds.getWidth() / (width + 30));
                height += lines * (bounds.getHeight() + (_msgFont.getSize() / 2) - 2);
            }
            height += MESSAGE_VSPACE * _messages.size();           // add vspace
        }
        return height;
    }


    private void sizeAndPositionContent() {
        int width = MIN_MESSAGES_WIDTH;
        _height = sizeAndPositionContent(width);

        // try and get it all on one screen (up to a max width of 900)
        while ((width <= 800) && (_height > width)) {
            width += 100;
            _height = sizeAndPositionContent(width);
        }
    }

    private int sizeAndPositionContent(int minWidth) {

        // set the width of the panels based on the width of the longest word
        Dimension bounds = FontUtil.getFontMetrics(getLongestWord(), _msgFont);
        int width = (int) Math.max(minWidth, bounds.getWidth());
        int outerWidth = width + NON_MESSAGE_WIDTH;

        // set the width of the inner messages panel
        _pnlMessages.setStyle(String.format("background-color: #f0f0f0; %s width: %dpx",
                getPosStyle(70, 35), width));

        // get total height of all panels
        int messagesHeight = getMessagesHeight(width) + PANEL_VSPACE;
        int totalHeight = TITLEBAR_HEIGHT + messagesHeight + BTNPANEL_HEIGHT;
        int outerHeight = Math.max(MIN_PANEL_HEIGHT, totalHeight);

        setButtonPanelStyle(outerHeight);
        setStyle(outerWidth, outerHeight);

        return outerHeight;
    }
}
