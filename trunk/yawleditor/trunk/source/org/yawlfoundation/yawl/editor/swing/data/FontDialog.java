package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class FontDialog extends JDialog implements ActionListener, ListSelectionListener {

    private Font _font;
    private JList _fontNames;
    private JComboBox _fontSizes;
    private JList _fontStyles;
    private JLabel _previewPane;

    public FontDialog(Component parent, Font font) {
        super();
        _font = font;
        setLocationRelativeTo(parent);
        setSize(new Dimension(360, 350));
        add(getContent());
        setModal(true);
        setTitle("Font Picker");
        setResizable(false);
    }


    public Font showDialog() {
        setVisible(true);
        return _font;
    }


    private JPanel getContent() {
        _fontNames = new JList(getFontNames());
        _fontSizes = new JComboBox(getFontSizes());
        _fontStyles = new JList(getFontStyles());
        _previewPane = new JLabel();

        _fontSizes.setBorder(BorderFactory.createTitledBorder("Size"));
        _fontSizes.setSize(new Dimension(120, 30));
        _previewPane.setBorder(BorderFactory.createTitledBorder("Preview"));
        _previewPane.setPreferredSize(new Dimension(350, 100));

        if (_font != null) {
            _fontNames.setSelectedValue(_font.getName(), true);
            _fontSizes.setSelectedItem(_font.getSize());
            _fontStyles.setSelectedIndex(_font.getStyle());
            setPreviewText();
        }

        _fontSizes.addActionListener(this);
        _fontNames.addListSelectionListener(this);
        _fontStyles.addListSelectionListener(this);

        JButton btnOK = new JButton("  OK  ");
        btnOK.setActionCommand("OK");
        btnOK.addActionListener(this);
        btnOK.setSize(80, 20);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setActionCommand("Cancel");
        btnCancel.addActionListener(this);
        btnCancel.setSize(80, 20);

        JScrollPane fontNamesPane = new JScrollPane();
        fontNamesPane.setViewportView(_fontNames);
        fontNamesPane.setBorder(BorderFactory.createTitledBorder("Family"));

        JScrollPane fontStylesPane = new JScrollPane();
        fontStylesPane.setViewportView(_fontStyles);
        fontStylesPane.setPreferredSize(new Dimension(120, 100));
        fontStylesPane.setBorder(BorderFactory.createTitledBorder("Style"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnOK);
        btnPanel.add(btnCancel);

        JPanel sizeStylePanel = new JPanel(new BorderLayout());
        sizeStylePanel.add(fontStylesPane, BorderLayout.NORTH);
        sizeStylePanel.add(_fontSizes, BorderLayout.CENTER);

        JPanel subContent = new JPanel(new BorderLayout());
        subContent.add(fontNamesPane, BorderLayout.WEST);
        subContent.add(sizeStylePanel, BorderLayout.EAST);
        subContent.add(_previewPane, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 12));
        content.add(subContent, BorderLayout.NORTH);
        content.add(btnPanel, BorderLayout.SOUTH);

        content.setPreferredSize(new Dimension(360, 350));

        return content;
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            setVisible(false);
        }
        else if (event.getActionCommand().equals("Cancel")) {
            _font = null;
            setVisible(false);
        }
        else fontChanged();     // size change

    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        fontChanged();         // name or style change
    }

    private void fontChanged() {
        try {
            int size = (Integer) _fontSizes.getSelectedItem();
            String name = _fontNames.getSelectedValue().toString();
            int style = getSelectedStyle();
            _font = new Font(name, style, size);
            _previewPane.setFont(_font);
            setPreviewText();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private int getSelectedStyle() {
        return _fontStyles.getSelectedIndex();
    }

    private Object[] getFontSizes() {
        return new Object[] {8,9,10,11,12,13,14,16,18,24,36,48,64,72,96,144};
    }

    private String[] getFontStyles() {
        return new String[] {"Regular", "Bold", "Italic", "Bold+Italic"};
    }

    private String[] getFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    private void setPreviewText() {
        String text = String.format(" %s, %d, %s", _font.getName(), _font.getSize(),
                getFontStyles()[_font.getStyle()]);
        _previewPane.setText(text);
    }
}
