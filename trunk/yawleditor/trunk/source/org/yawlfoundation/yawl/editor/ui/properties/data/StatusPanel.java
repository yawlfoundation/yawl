package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.swing.MoreDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 15/08/13
 */
public class StatusPanel extends JPanel {

    private JLabel _statusLabel;
    private JButton _btnMore;
    private java.util.List<String> _moreText;
    private Window _owner;

    public static final Color ERROR = Color.RED;
    public static final Color WARNING = Color.YELLOW;
    public static final Color INFO = Color.BLUE;
    public static final Color OK = Color.GREEN;


    public StatusPanel(Window owner) {
        super(new BorderLayout());
        _owner = owner;
        _statusLabel = new JLabel();
        add(_statusLabel, BorderLayout.WEST);
        add(createMoreButton(), BorderLayout.CENTER);
    }


    public void clear() {
        _statusLabel.setText(null);
        _moreText = null;
        _statusLabel.setVisible(false);
        _btnMore.setVisible(false);
    }

    public void set(String text) {
        set(text, ERROR);
    }

    public void set(String text, Color foreColor) {
        set(text, foreColor, null);
    }

    public void set(String text, java.util.List<String> moreText) {
        set(text, ERROR, moreText);
    }

    public void set(String text, Color foreColor, java.util.List<String> moreText) {
        _statusLabel.setForeground(foreColor);
        _statusLabel.setText(text);
        _statusLabel.setVisible(true);
        _moreText = moreText;
        if (moreText != null) {
            _btnMore.setVisible(true);
        }
    }


    private JButton createMoreButton() {
        _btnMore = new JButton();
        _btnMore.setText("<HTML><FONT color=\"#112DCD\"><U><I>more...</I></U></FONT></HTML >");
        _btnMore.setHorizontalAlignment(SwingConstants.LEFT);
        _btnMore.setBorderPainted(false);
        _btnMore.setOpaque(false);
        _btnMore.setFocusPainted(false);
        _btnMore.setActionCommand("more");
        _btnMore.setVisible(false);
        _btnMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                new MoreDialog(_owner, _moreText).setVisible(true);
            }
        });

        return _btnMore;
    }

}
