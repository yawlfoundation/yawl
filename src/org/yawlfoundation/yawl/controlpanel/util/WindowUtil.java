/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
