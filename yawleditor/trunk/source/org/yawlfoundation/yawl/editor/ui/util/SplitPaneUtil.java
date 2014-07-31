/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * @author Michael Adams
 * @date 31/07/2014
 */
public class SplitPaneUtil {

    /**
     * Sets some divider options and removes its border
     * @param splitPane the pane to setup
     */
    public void setupDivider(JSplitPane splitPane) {
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0);
        splitPane.setOneTouchExpandable(true);
        splitPane.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    public void setBorder(Border b) { }
                };
            }
        });
        splitPane.setBorder(null);
    }


    /**
     * A workaround to ensure the divider location is set, even if the split pane
     * has not yet completed its construction (as required by the Swing API)
     * @param splitter the splitter
     * @param proportion where to put the divider
     * @return the splitter
     */
    public JSplitPane setDividerLocation(final JSplitPane splitter,
                                                final double proportion) {
        if (splitter.isShowing()) {
            if(splitter.getWidth() > 0 && splitter.getHeight() > 0) {
                splitter.setDividerLocation(proportion);
            }
            else {
                splitter.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent ce) {
                        splitter.removeComponentListener(this);
                        setDividerLocation(splitter, proportion);
                    }
                });
            }
        }
        else {
            splitter.addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 &&
                            splitter.isShowing()) {
                        splitter.removeHierarchyListener(this);
                        BasicSplitPaneUI ui = (BasicSplitPaneUI) splitter.getUI();
                        BasicSplitPaneDivider divider = ui.getDivider();
                        JButton button = (JButton) divider.getComponent(1);
                        button.doClick();              }
                }
            });
        }
        return splitter;
    }
}
