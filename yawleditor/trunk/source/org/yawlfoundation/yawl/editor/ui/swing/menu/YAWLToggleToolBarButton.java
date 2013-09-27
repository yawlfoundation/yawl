package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Author: Michael Adams
 * Creation Date: 11/05/2010
 */
public class YAWLToggleToolBarButton extends JToggleButton {

    private static final Insets margin = new Insets(4,4,4,4);

    public YAWLToggleToolBarButton(Action a) {
        super(a);
        setText(null);
        setMnemonic(0);
        setMargin(margin);
        setMaximumSize(getPreferredSize());
    }

    public Point getToolTipLocation(MouseEvent e) {
        return new Point(0,getSize().height);
    }

    public void setEnabled(boolean enabled) {
        if (getAction() instanceof TooltipTogglingWidget) {
            TooltipTogglingWidget action = (TooltipTogglingWidget) this.getAction();
            if (enabled) {
                setToolTipText(action.getEnabledTooltipText());
            } else {
                setToolTipText(action.getDisabledTooltipText());
            }
        }
        super.setEnabled(enabled);
    }
}



