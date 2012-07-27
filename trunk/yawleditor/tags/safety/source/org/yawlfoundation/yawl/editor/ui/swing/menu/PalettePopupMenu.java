/*
 * Created on 05/12/2003
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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.palette.*;

import javax.swing.JPopupMenu;

public class PalettePopupMenu extends JPopupMenu {

  private static final long serialVersionUID = 1L;

  public PalettePopupMenu() {
    super();
    addMenuItems();
  }
  
  private void addMenuItems() {
    add(new AtomicTaskAction(
            Palette.getInstance().getControlFlowPalette()    
       )
    );
    add(new CompositeTaskAction(
            Palette.getInstance().getControlFlowPalette()    
       )
    );
    add(new MultipleAtomicTaskAction(
            Palette.getInstance().getControlFlowPalette()    
       )
    );
    add(new MultipleCompositeTaskAction(
            Palette.getInstance().getControlFlowPalette()    
        )
    );
    add(new ConditionAction(
            Palette.getInstance().getControlFlowPalette()    
        )
    );
    add(new MarqueeAction(
            Palette.getInstance().getControlFlowPalette()    
        )
    );
    add(new NetDragAction(
            Palette.getInstance().getControlFlowPalette()    
        )
    );
  }
}
