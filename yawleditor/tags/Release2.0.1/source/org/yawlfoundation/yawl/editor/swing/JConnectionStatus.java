package org.yawlfoundation.yawl.editor.swing;

import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;

import javax.swing.*;
import java.awt.*;

/**
 * Author: Michael Adams
 * Creation Date: 16/03/2009
 */
public class JConnectionStatus extends JPanel {

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/resources/";
    private static final ImageIcon onlineIcon = getIconByName("online") ;
    private static final ImageIcon offlineIcon = getIconByName("offline") ;
    private JIndicator engineIndicator;
    private JIndicator resourceIndicator;

    public JConnectionStatus() {
        super();
        setLayout(new BorderLayout());
        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)
            )
        );
        addIndicators();
    }

    private static ImageIcon getIconByName(String iconName) {
      return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    private void addIndicators() {
        engineIndicator = new JIndicator("Engine");
        resourceIndicator = new JIndicator("Resource Service");
        add(engineIndicator, BorderLayout.WEST);
        add(resourceIndicator, BorderLayout.EAST);
    }

    
    public void setStatusMode(String component, boolean online) {
        if (component.equals("engine")) {
            engineIndicator.setOnline(online);
        }
        else if (component.equals("resource")) {
            resourceIndicator.setOnline(online);
        }
    }

    /*******************************************/

    class JIndicator extends JPanel {

        private JLabel _indicator ;
        private boolean _online ;
        private String _indicatorFor;

        public JIndicator() {
            super();
            _indicator = new JLabel();
            setOnline(false);
            this.add(_indicator);
        }

        public JIndicator(String iFor) {
            this();
            setIndicatorFor(iFor) ;
        }


        public void setOnline(boolean online) {
            _online = online;
            _indicator.setIcon(online ? onlineIcon : offlineIcon) ;
            setTip();
        }

        public void setIndicatorFor(String iFor) {
            _indicatorFor = iFor;
            setTip();
        }

        private void setTip() {
            _indicator.setToolTipText(_indicatorFor + " is " + getStatus());
        }

        private String getStatus() {
            return _online ? "online" : "offline" ;
        }
    }
}
