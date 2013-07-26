package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class FontDialog extends JDialog
        implements ActionListener, ListSelectionListener, MouseListener {

    private Font _font;
    private JList _fontNames;
    private JComboBox _fontSizes;
    private JList _fontStyles;
    private JLabel _previewPane;
    private JLabel _colourPane;
    private Color _colour;

    public FontDialog(Component parent, Font font) {
        super();
        _font = font;
        _colour = Color.BLACK;
        setLocationRelativeTo(parent);
        setSize(new Dimension(360, 370));
        add(getContent());
        setModal(true);
        setTitle("Font Picker");
        setResizable(false);
        _fontNames.ensureIndexIsVisible(_fontNames.getSelectedIndex());
    }


    public Font showDialog() {
        setVisible(true);
        return _font;
    }

    public Color getColour() { return _colour; }

    public void setColour(Color color) { _colour = color; }


    private JPanel getContent() {
        _fontNames = new JList(getFontNames());
        _fontSizes = new JComboBox(getFontSizes());
        _fontStyles = new JList(getFontStyles());
        _previewPane = new JLabel();

        _colourPane = new JLabel(".") {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintIcon(g);
            }
        };

        _fontSizes.setBorder(BorderFactory.createTitledBorder("Size"));
        _fontSizes.setSize(new Dimension(120, 30));
        _colourPane.setBorder(BorderFactory.createTitledBorder("Colour"));
        _colourPane.setForeground(_colourPane.getBackground());
        _previewPane.setBorder(BorderFactory.createTitledBorder("Preview"));
        _previewPane.setPreferredSize(new Dimension(350, 100));

        if (_font != null) {
            _fontNames.setSelectedValue(_font.getFamily(), true);
            _fontSizes.setSelectedItem(_font.getSize());
            _fontStyles.setSelectedIndex(_font.getStyle());
            setPreviewText();
        }

        _fontSizes.addActionListener(this);
        _fontNames.addListSelectionListener(this);
        _fontStyles.addListSelectionListener(this);
        _colourPane.addMouseListener(this);

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
        fontNamesPane.setPreferredSize(new Dimension(220, 140));
        fontNamesPane.setBorder(BorderFactory.createTitledBorder("Family"));

        JScrollPane fontStylesPane = new JScrollPane();
        fontStylesPane.setViewportView(_fontStyles);
        fontStylesPane.setPreferredSize(new Dimension(120, 100));
        fontStylesPane.setBorder(BorderFactory.createTitledBorder("Style"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnCancel);
        btnPanel.add(btnOK);

        JPanel sizeStylePanel = new JPanel(new BorderLayout());
        sizeStylePanel.add(fontStylesPane, BorderLayout.NORTH);
        sizeStylePanel.add(_colourPane, BorderLayout.SOUTH);
        sizeStylePanel.add(_fontSizes, BorderLayout.CENTER);

        JPanel subContent = new JPanel(new BorderLayout());
        subContent.add(fontNamesPane, BorderLayout.WEST);
        subContent.add(sizeStylePanel, BorderLayout.EAST);
        subContent.add(_previewPane, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 12));
        content.add(subContent, BorderLayout.NORTH);
        content.add(btnPanel, BorderLayout.SOUTH);

        content.setPreferredSize(new Dimension(360, 370));

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
            setPreviewText();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }

    public void mouseClicked(MouseEvent e) {
        Color newColour = JColorChooser.showDialog(this, "Choose a Colour", _colour);
        if (!( newColour == null || newColour.equals(_colour))) {
            _colour = newColour;
            _colourPane.repaint();
            setPreviewText();
        }
    }


    private int getSelectedStyle() {
        return _fontStyles.getSelectedIndex();
    }

    private Object[] getFontSizes() {
        return new Object[] {8,9,10,11,12,13,14,15,16,18,24,36,48,64,72,96,144};
    }

    private String[] getFontStyles() {
        return new String[] {"Regular", "Bold", "Italic", "Bold+Italic"};
    }

    private String[] getFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    private void setPreviewText() {
        _previewPane.setForeground(_colour);
        _previewPane.setFont(_font);
        String text = String.format(" %s, %d, %s", _font.getFamily(), _font.getSize(),
                getFontStyles()[_font.getStyle()]);
        _previewPane.setText(text);
    }



    private void paintIcon(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Paint oldPaint = g2d.getPaint();

        if (_colour != null) {
            g2d.setPaint(_colour);
            g.fillRect(20, 20, 80, 10);
        }

        g.setColor(UIManager.getColor("controlDkShadow"));
        g.drawRect(20, 20, 80, 10);
        g2d.setPaint(oldPaint);
    }

}
