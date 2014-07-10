/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.document.AbstractXMLStyledDocument;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.List;

public class ValidityEditorPane extends JEditorPane
        implements XMLStyledDocumentValidityListener {

    public static final Color VALID_COLOR = Color.GREEN.darker().darker();
    public static final Color INVALID_COLOR = Color.RED.darker();
    public static final Color UNCERTAIN_COLOR = Color.ORANGE.darker();

    private static final Color DISABLED_BACKGROUND = Color.LIGHT_GRAY;

    private final Color enabledBackground;


    public ValidityEditorPane() {
        configure();
        enabledBackground = this.getBackground();
    }

    public void setEnabled(boolean enabled) {
        setBackground(enabled ? enabledBackground : DISABLED_BACKGROUND);
        super.setEnabled(enabled);
    }

    public void setDocument(AbstractXMLStyledDocument document) {
        super.setDocument(document);
        subscribeForValidityEvents();

        // Set the tab size
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
    }

    public boolean isContentValid() {
        return getXMLStyledDocument().isContentValid();
    }

    public void validateContent() {
        getXMLStyledDocument().publishValidity();
    }

    public boolean checkValidity() {
        getXMLStyledDocument().checkValidity();
        return isContentValid();
    }

    protected AbstractXMLStyledDocument getXMLStyledDocument() {
        return (AbstractXMLStyledDocument) getDocument();
    }

    protected void subscribeForValidityEvents() {
        acceptValiditySubscription(this);
    }

    public void acceptValiditySubscription(XMLStyledDocumentValidityListener subscriber) {
        getXMLStyledDocument().subscribe(subscriber);
    }

    public void setText(String text) {
        super.setText(text);
        setSize(150, 15);
    }

    public List<String> getProblemList() {
        return getXMLStyledDocument().getProblemList();
    }

    public void setTargetVariableName(String targetVariableName) {
        getXMLStyledDocument().setPreAndPostEditorText(
                "<" + targetVariableName + ">",
                "</" + targetVariableName + ">"
        );
    }

    public void documentValidityChanged(Validity documentValid) {
        switch (documentValid) {
            case VALID: setForeground(VALID_COLOR); break;
            case INVALID: setForeground(INVALID_COLOR); break;
            default: setForeground(UNCERTAIN_COLOR);
        }
    }

    public void configure() {
        Color element = new Color(9, 9, 155);
        Color attribute = new Color(23, 23, 240);
        Color value = new Color(0, 143, 41);
        Color comment = new Color(160, 160, 160);

        XMLEditorKit kit = new XMLEditorKit();
        kit.setAutoIndentation(true);
        kit.setTagCompletion(true);
        kit.setStyle(XMLStyleConstants.ELEMENT_PREFIX, element, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ELEMENT_NAME, element, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ELEMENT_VALUE, value, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ATTRIBUTE_PREFIX, attribute, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, attribute, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.ATTRIBUTE_VALUE, value, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_PREFIX, attribute, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_NAME, attribute, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.NAMESPACE_VALUE, value, Font.PLAIN);
        kit.setStyle(XMLStyleConstants.COMMENT, comment, Font.ITALIC);
        setEditorKit(kit);

        // Set the tab size & error highlighting
        getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);

        setBackground(new Color(252,252,252));
    }


    // override to add anti-aliasing to text
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.setRenderingHints(rh);
        super.paint(g);
    }
}