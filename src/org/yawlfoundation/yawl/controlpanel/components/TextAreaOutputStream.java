/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Michael Adams
 * @date 20/10/2015
 */
public class TextAreaOutputStream extends OutputStream {

    private final JTextPane _textPane;

    private static final Color DEFAULT_COLOR = new Color(50, 50, 50);
    private static final Color ERROR_COLOR = new Color(197, 0, 0);
    private static final Color WARN_COLOR = new Color(255, 102, 96);
    private static final Color SUCCESS_COLOR = new Color(0, 128, 0);


    public TextAreaOutputStream(JTextPane textPane) {
        super();
        _textPane = textPane;
    }


    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        handleSpecialCases(text);
        SwingUtilities.invokeLater(new Runnable () {
            @Override
            public void run() {
                append(text);
            }
        });
    }


    @Override
    public void write(int b) throws IOException {
        write (new byte[] {(byte)b}, 0, 1);
    }


    // these output lines denote special conditions that have to be announced throughout
    private void handleSpecialCases(String text) {
        if (text.startsWith("ERROR: transport error 202")) {
            Publisher.abortStarting();
        }
    }


    private void append(String text) {
        if (mayIgnore(text)) {                  // ignore this warning
            return;
        }
        Color color = DEFAULT_COLOR;
        if (text.contains("ERROR") || text.contains("SEVERE")) {
            color = ERROR_COLOR;
        }
        if (text.contains("WARN")) {
            color = WARN_COLOR;
        }
        else if (text.contains("Server startup ")) {
            color = SUCCESS_COLOR;
        }
        else if (text.startsWith("INFO: Shutdown successfully")) {
            color = SUCCESS_COLOR;
        }
        append(text, color);
    }


    private void append(String text, Color color) {
        StyleContext context = StyleContext.getDefaultStyleContext();
        AttributeSet attributeSet = context.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, color);

        _textPane.setCaretPosition(_textPane.getDocument().getLength());
        _textPane.setCharacterAttributes(attributeSet, false);
        _textPane.setEditable(true);
        _textPane.replaceSelection(text);
        _textPane.setEditable(false);
    }


    // eh cache waring are for info only and can be ignored
    // on shutdown, memory leak warning can be ignored
    private boolean mayIgnore(String text) {
        return text.contains("ehcache") || text.contains("CacheManager") ||
                text.contains("memory leak") || text.contains("java.base@16/") ||
                text.contains("com.mchange.v2.async.ThreadPoolAsynchronousRunner$") ||
                text.contains("org.h2");
    }

}




