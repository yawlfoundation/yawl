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

package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FontDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 27/09/13
 */
public class FontPanel extends JPanel {

    private final ActionListener _listener;
    private Font _font;
    private JLabel _fontLabel;
    private boolean _changed;
    private Color _textColour;


    public FontPanel(ActionListener listener) {
        super();
        _listener = listener;
        _font = new Font(UserSettings.getFontFamily(), UserSettings.getFontStyle(),
                UserSettings.getFontSize());
        _textColour = UserSettings.getDefaultTextColour();
        addContent();
    }


    public void applyChanges() {
        if (_changed) {
            UserSettings.setFontFamily(_font.getFamily());
            UserSettings.setFontStyle(_font.getStyle());
            UserSettings.setFontSize(_font.getSize());
            UserSettings.setDefaultTextColour(_textColour);
            propagateChange();
        }
    }


    private void addContent() {
        setLayout(new BorderLayout());
        add(buildCaptionPanel(), BorderLayout.WEST);
        _fontLabel = new JLabel(getFontLabelText());
        _fontLabel.setForeground(_textColour);
        _fontLabel.setToolTipText("Current Font");
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(_fontLabel, BorderLayout.CENTER);
        panel.add(buildFontButton(), BorderLayout.EAST);
        add(panel, BorderLayout.EAST);
    }


    private JPanel buildCaptionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Default Font:"));
        return panel;
    }


    private String getFontLabelText() {
        StringBuilder s = new StringBuilder();
        s.append('[');
        s.append(_font.getFamily()).append(", ");
        s.append(intToFontStyle(_font.getStyle())).append(", ");
        s.append(_font.getSize());
        s.append(']');
        return s.toString();
    }

    private JButton buildFontButton() {
        JButton button = new JButton("...");
        button.setPreferredSize(new Dimension(25, 25));
        button.setToolTipText(" Select Font ");

        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                FontDialog dialog = new FontDialog(YAWLEditor.getInstance(), _font);
                dialog.setColour(_textColour);
                Font newFont = dialog.showDialog();
                if (! (newFont == null || newFont.equals(_font))) {
                    _font = newFont;
                    _changed = true;
                    announceChange();
                }
                Color colour = dialog.getColour();
                if (! (colour == null || colour.equals(_textColour))) {
                    _textColour = colour;
                    _changed = true;
                    announceChange();
                }
                _fontLabel.setForeground(_textColour);
                _fontLabel.setText(getFontLabelText());
            }
        });
        return button;
    }


    private void announceChange() {
        _listener.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    private String intToFontStyle(int style) {
        switch (style) {
            case Font.BOLD : return "Bold";
            case Font.ITALIC : return "Italic";
            case Font.BOLD | Font.ITALIC : return "Bold,Italic";
            default : return "Plain";
        }
    }


    private void propagateChange() {
        SpecificationModel.getNets().propagateGlobalFontChange(_font);
    }

}
