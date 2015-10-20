package org.yawlfoundation.yawl.controlpanel.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Michael Adams
 * @date 13/10/2015
 */
public class WebPageLauncher {

    public JButton createLink(String text, final String url) {
        JButton button = new JButton();
        button.setText(showAsLink(text));
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    openWebPage(url);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            e.getMessage(), "Error browsing to page",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return button;
    }


    public void openWebPage(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
                return;
            }
        }
        throw new IOException("Cannot launch browser from control panel on this platform");
    }


    private String showAsLink(String text) {
            StringBuilder s = new StringBuilder(50);
            s.append("<HTML><FONT color=\"#000099\"><U>");
            s.append(text);
            s.append("</U></FONT></HTML >");
            return s.toString();
        }

}
