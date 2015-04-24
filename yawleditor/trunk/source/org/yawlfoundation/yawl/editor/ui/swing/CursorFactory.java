/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import java.awt.*;

public class CursorFactory {
  public static final int SELECTION               = 0;
  public static final int FLOW_RELATION           = 1;
  public static final int CONDITION               = 2;
  public static final int ATOMIC_TASK             = 3;
  public static final int COMPOSITE_TASK          = 4;
  public static final int MULTIPLE_ATOMIC_TASK    = 5;
  public static final int MULTIPLE_COMPOSITE_TASK = 6;
  public static final int HIDDEN                  = 7;


  private static final Point TOP_LEFT = new Point(0,0);
  private static final Point CENTRE = new Point(15,15);


  private static final Cursor[] cursors = {
          buildCustomCursor("Selection32",             TOP_LEFT, "Selection"),
          buildCustomCursor("FlowRelation32",          TOP_LEFT, "FLowRelation"),
          buildCustomCursor("Condition32",             TOP_LEFT, "Condition"),
          buildCustomCursor("AtomicTask32",            TOP_LEFT, "AtomicTask"),
          buildCustomCursor("CompositeTask32",         TOP_LEFT, "CompositeTask"),
          buildCustomCursor("MultipleAtomicTask32",    TOP_LEFT, "MultipleAtomicTask"),
          buildCustomCursor("MultipleCompositeTask32", TOP_LEFT, "MultipleAtomicTask"),
          buildCustomCursor("Hidden",                  TOP_LEFT, "Hidden"),
  };

  public static Cursor getCustomCursor(int cursorType) {
    return cursors[cursorType];
  }

  private static Cursor buildCustomCursor(String cursorFileName, Point hotspot, String name) {
      Image image = ResourceLoader.getCursorIcon(cursorFileName);
      return Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, name);
  }
}
