/*
 * Created on 31/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.swing;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;

public class CursorFactory {
  public static final int SELECTION               = 0;
  public static final int FLOW_RELATION           = 1;
  public static final int CONDITION               = 2;
  public static final int ATOMIC_TASK             = 3;
  public static final int COMPOSITE_TASK          = 4;
  public static final int MULTIPLE_ATOMIC_TASK    = 5;
  public static final int MULTIPLE_COMPOSITE_TASK = 6;
 
  private static final Point topLeft = new Point(1,1);
 
  private static final Cursor[] cursors = {
    buildCustomCursor("Selection32",             topLeft, "Selection"),
    buildCustomCursor("FlowRelation32",          topLeft, "FLowRelation"),
    buildCustomCursor("Condition32",             topLeft, "Condition"),
    buildCustomCursor("AtomicTask32",            topLeft, "AtomicTask"),
    buildCustomCursor("CompositeTask32",         topLeft, "CompositeTask"),
    buildCustomCursor("MultipleAtomicTask32",    topLeft, "MultipleAtomicTask"),
    buildCustomCursor("MultipleCompositeTask32", topLeft, "MultipleAtomicTask"),
  };
  
  public static Cursor getCustomCursor(int cursorType) {
    return cursors[cursorType];    
  }

  private static Cursor buildCustomCursor(String cursorFileName, Point hotspot, String name) {
    Image image = 
      ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/cursors/" + 
      cursorFileName + ".gif").getImage();
    assert image != null : "Image is null!";
    return Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, name);  
  }
}
