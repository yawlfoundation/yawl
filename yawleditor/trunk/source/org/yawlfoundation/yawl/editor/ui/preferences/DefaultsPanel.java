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

import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 27/09/13
 */
public class DefaultsPanel extends JPanel implements PreferencePanel {

    private SwatchPanel _netSwatch;
    private SwatchPanel _elementSwatch;
    private FontPanel _fontPanel;
    private DescriptionTogglePanel _descriptionTogglePanel;
    private final ActionListener _listener;


    public DefaultsPanel(ActionListener listener) {
        super();
        _listener = listener;
        addContent();
        setPreferredSize(new Dimension(500, 400));
    }

    public void applyChanges() {
        UserSettings.setNetBackgroundColour(_netSwatch.getBackground());
        UserSettings.setVertexBackgroundColour(_elementSwatch.getBackground());
        _fontPanel.applyChanges();
        _descriptionTogglePanel.applyChanges();
    }


    private void addContent() {
        JPanel content = new JPanel(new GridLayout(0,1,15,15));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        _netSwatch = new SwatchPanel(UserSettings.getNetBackgroundColour());
        content.add(buildColourPanel(_netSwatch, "Default Net Background Colour"));
        _elementSwatch = new SwatchPanel(UserSettings.getVertexBackgroundColour());
        content.add(buildColourPanel(_elementSwatch, "Default Element Background Colour"));
        _fontPanel = new FontPanel(_listener);
        content.add(_fontPanel);
        _descriptionTogglePanel = new DescriptionTogglePanel(_listener);
        content.add(_descriptionTogglePanel);
        add(content);
    }


    private JPanel buildColourPanel(SwatchPanel swatchPanel, String caption) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.add(buildCaptionPanel(caption), BorderLayout.WEST);
        panel.add(swatchPanel, BorderLayout.CENTER);
        panel.add(buildColourButton(caption, swatchPanel), BorderLayout.EAST);
        panel.setPreferredSize(new Dimension(350, 25));
        return panel;
    }


    private JPanel buildCaptionPanel(String caption) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(caption + ":"));
        panel.setPreferredSize(new Dimension(250, 25));
        return panel;
    }


    private JButton buildColourButton(final String title, final SwatchPanel swatchPanel) {
        JButton button = new JButton("...");
        button.setPreferredSize(new Dimension(25, 25));
        button.setToolTipText(" Select Colour ");

        final JPanel thisPanel = this;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColour = JColorChooser.showDialog(
                        thisPanel, title, swatchPanel.getBackground());
                if (newColour != null) {
                    swatchPanel.setBackground(newColour);

                    // tell the parent dialog there's been a change
                    _listener.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
        return button;
    }


    /**************************************************************************/

    class SwatchPanel extends JPanel {

        SwatchPanel(Color colour) {
            super();
            setBorder(new LineBorder(Color.BLACK));
            setBackground(colour);
            setPreferredSize(new Dimension(25,25));
            setToolTipText(" Current Colour ");
        }

    }

}
