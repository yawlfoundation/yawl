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

    private ActionListener _listener;
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
        setPreferredSize(new Dimension(350, 25));
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
        add(buildCaptionPanel("Default Font:"), BorderLayout.WEST);
        _fontLabel = new JLabel(getFontLabelText());
        _fontLabel.setForeground(_textColour);
        _fontLabel.setToolTipText("Current Font");
        add(_fontLabel, BorderLayout.CENTER);
        add(buildFontButton(), BorderLayout.EAST);
        setPreferredSize(new Dimension(450, 25));
    }


    private JPanel buildCaptionPanel(String caption) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(caption));
        panel.setPreferredSize(new Dimension(100, 25));
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
        SpecificationModel.getInstance().getNets().propagateGlobalFontChange(_font);
    }

}
