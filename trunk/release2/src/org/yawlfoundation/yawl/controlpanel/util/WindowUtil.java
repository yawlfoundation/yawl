package org.yawlfoundation.yawl.controlpanel.util;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 19/08/2014
 */
public class WindowUtil {

    private static final int SCREEN_WIDTH =
            (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private static final int SCREEN_HEIGHT =
            (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();


    public static Point calcLocation(JFrame main, JDialog dialog) {
        int mainWidth = main.getWidth();
        int mainX = main.getX();
        int dialogWidth = dialog.getWidth();
        int x;
        if ((mainX + mainWidth + dialogWidth) > SCREEN_WIDTH) {
            x = mainX - dialogWidth - 50;
        }
        else {
            x = mainX + mainWidth + 50;
        }
        return new Point(x, 50);
    }
}
