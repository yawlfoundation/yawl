/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Michael Adams
 * @date 27/06/2014
 */
public class LogPredicateStyledDocument extends DefaultStyledDocument
        implements DocumentListener {

    private LogPredicateScope container;
    private Style badParamStyle;
    private Style goodParamStyle;
    private Style plainStyle;

    private final java.util.List<Block> blocks;

    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    private static final String DECOMPOSITION_PREFIX = PREFIX + "decomp:";
    private static final String SPECIFICATION_PREFIX = PREFIX + "spec:";
    private static final String TASK_PREFIX = PREFIX + "task:";
    private static final String ITEM_PREFIX = PREFIX + "item:";
    private static final String EXPRESSION_PREFIX = PREFIX + "expression:";
    private static final String VARIABLE_PREFIX = PREFIX + "parameter:";
    private static final String PARTICIPANT_PREFIX = PREFIX + "particpant:";
    private static final String RESOURCE_PREFIX = PREFIX + "resource:";

    private static final java.util.List<String> DECOMPOSITION_PARAMETERS =
            Arrays.asList("name", "spec:name", "inputs", "outputs", "doco");

    private static final java.util.List<String> SPECIFICATION_PARAMETERS =
            Arrays.asList("name", "version", "key");

    private static final java.util.List<String> TASK_PARAMETERS =
            Arrays.asList("id", "name", "doco", "decomposition:name");

    private static final java.util.List<String> ITEM_PARAMETERS =
            Arrays.asList("id", "handlingservice:name", "handlingservice:uri",
                    "handlingservice:doco", "codelet", "customform", "enabledtime",
                    "firedtime", "startedtime", "status", "timer:status", "timer:expiry");

    private static final java.util.List<String> VARIABLE_PARAMETERS =
            Arrays.asList("name", "datatype", "namespace", "doco", "usage", "ordering",
                    "decomposition", "initialvalue", "defaultvalue");

    private static final java.util.List<String> PARTICIPANT_PARAMETERS =
            Arrays.asList("name", "userid", "offeredQueueSize", "allocatedQueueSize",
                    "startedQueueSize", "suspendedQueueSize");

    private static final java.util.List<String> RESOURCE_PARAMETERS =
            Arrays.asList("initiator:offer", "initiator:allocate", "initiator:start",
                    "offerset", "piler", "deallocators", "allocator", "roles",
                    "dynParams", "filters", "constraints");

    private static final java.util.List<String> TIME_PARAMETERS =
            Arrays.asList("now", "date", "time");


    public LogPredicateStyledDocument() {
        blocks = new ArrayList<Block>();
        badParamStyle = addStyle("invalidParam", null);
        badParamStyle.addAttribute(StyleConstants.Foreground, Color.RED);
        goodParamStyle = addStyle("validParam", null);
        goodParamStyle.addAttribute(StyleConstants.Foreground, new Color(0, 143, 41));
        plainStyle = addStyle("plain", null);
        plainStyle.addAttribute(StyleConstants.Foreground, Color.BLACK);
        addDocumentListener(this);
        container = LogPredicateScope.Net;    // default
    }

    public LogPredicateStyledDocument(LogPredicateScope pc) {
        this();
        setPredicateScope(pc);
    }


    public void setPredicateScope(LogPredicateScope pc) {
        container = pc;
    }


    public void insertUpdate(DocumentEvent documentEvent) { locate(); }

    public void removeUpdate(DocumentEvent documentEvent) { locate(); }

    public void changedUpdate(DocumentEvent documentEvent) { }


    private void locate() {
        try {
            blocks.clear();
            String text = getText(0, getLength());
            int offset = text.indexOf("${");
            while (offset > -1) {
                int end = text.indexOf("}", offset);
                if (end > -1) {
                    int len = end - offset + 1;
                    blocks.add(new Block(offset, len, getParamStyle(offset, len)));
                }
                offset = text.indexOf("${", offset + 1);
            }
            highlight(blocks);
        }
        catch (BadLocationException ble) {
            // should never happen
        }
    }


    private Style getParamStyle(int offset, int len) throws BadLocationException {
        return isValidParameter(offset, len) ? goodParamStyle :badParamStyle;
    }


    // invokeLater, because: 'the DocumentListener may not mutate the source of the event'
    private void highlight(final java.util.List<Block> blocks) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setCharacterAttributes(0, getLength(), plainStyle, false);
                    for (Block b : blocks) {
                        setCharacterAttributes(b.offset, b.len, b.style, true);
                    }
                }
        });
    }


    private boolean isValidParameter(int offset, int len) throws BadLocationException {
        String s = getText(offset, len);
        return isValidTimeParameter(s) || isValidParameterForContainer(s);
    }


    private boolean isValidParameterForContainer(String s) {
        switch (container) {
            case Net: return isValidNetParameter(s);
            case Task: return isValidTaskParameter(s);
            case Variable: return isValidVariableParameter(s);
        }
        return true;
    }


    private boolean isValidNetParameter(String s) {
        return isValidParameter(s, DECOMPOSITION_PREFIX, DECOMPOSITION_PARAMETERS) ||
               s.startsWith(DECOMPOSITION_PREFIX + "attribute:");
    }


    private boolean isValidTaskParameter(String s) {
        return isValidParameter(s, SPECIFICATION_PREFIX, SPECIFICATION_PARAMETERS) ||
               isValidParameter(s, TASK_PREFIX, TASK_PARAMETERS) ||
               isValidParameter(s, ITEM_PREFIX, ITEM_PARAMETERS) ||
               isValidParameter(s, PARTICIPANT_PREFIX, PARTICIPANT_PARAMETERS) ||
               isValidParameter(s, RESOURCE_PREFIX, RESOURCE_PARAMETERS) ||
               s.startsWith(EXPRESSION_PREFIX);
    }


    private boolean isValidVariableParameter(String s) {
        return isValidParameter(s, VARIABLE_PREFIX, VARIABLE_PARAMETERS) ||
               s.startsWith(VARIABLE_PREFIX + "attribute:");
    }


    private boolean isValidTimeParameter(String s) {
        return isValidParameter(s, PREFIX, TIME_PARAMETERS);
    }


    private boolean isValidParameter(String s, String prefix, List<String> parameters) {
        return s != null && s.startsWith(prefix) && s.endsWith(SUFFIX) &&
                parameters.contains(s.substring(prefix.length(), s.length() - 1));
    }


    /********************************************************************/

    class Block {
        int offset;
        int len;
        Style style;

        Block(int o, int l, Style s) {
            offset = o; len = l; style = s;
        }
    }

}
