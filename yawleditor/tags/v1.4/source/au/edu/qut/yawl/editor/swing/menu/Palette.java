/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package au.edu.qut.yawl.editor.swing.menu;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import au.edu.qut.yawl.editor.actions.palette.*;

import au.edu.qut.yawl.editor.specification.SpecificaitonModelListener;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.JStatusBar;

public class Palette extends YAWLToolBar implements SpecificaitonModelListener {

  public static final int ATOMIC_TASK             = 0;
  public static final int COMPOSITE_TASK          = 1;
  public static final int MULTIPLE_ATOMIC_TASK    = 2;
  public static final int MULTIPLE_COMPOSITE_TASK = 3;
  public static final int FLOW_RELATION           = 4;
  public static final int CONDITION               = 5;
  public static final int MARQUEE                 = 6;

  private static int selectedItem = MARQUEE;
  
  private boolean enabledState = true;

  private static YAWLPaletteButton[] buttons = {
    new YAWLPaletteButton(new AtomicTaskAction(),KeyEvent.VK_1),
    new YAWLPaletteButton(new CompositeTaskAction(),KeyEvent.VK_2),
    new YAWLPaletteButton(new MultipleAtomicTaskAction(),KeyEvent.VK_3),
    new YAWLPaletteButton(new MultipleCompositeTaskAction(),KeyEvent.VK_4),
    new YAWLPaletteButton(new FlowRelationAction(),KeyEvent.VK_5),
    new YAWLPaletteButton(new ConditionAction(),KeyEvent.VK_6),
    new YAWLPaletteButton(new MarqueeAction(),KeyEvent.VK_7),
  };

  private static final Palette INSTANCE = new Palette();

  public static Palette getInstance() {
    return INSTANCE;
  }  
    
  private Palette() {
    super("YAWLEditor Palette");
    SpecificationModel.getInstance().subscribe(this);   
  }  

	protected void buildInterface() {
    setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
    setMargin(new Insets(0,0,0,0));
    add(getButtons());
  }
  
  private JPanel getButtons() {
    JPanel buttonPanel = new JPanel(new GridLayout(4,2,0,0));  
    for (int i = 0; i < buttons.length; i++) {
      buttonPanel.add(buttons[i]);      
    }
    return buttonPanel;
  }
  
  public void setSelected(int item) {
    assert item >= CONDITION && item <= MARQUEE : "Invalid item selection made.";
    buttons[selectedItem].setSelected(false);
    selectedItem = item;
    buttons[selectedItem].setSelected(true);   
    JStatusBar.getInstance().setStatusText(getItemText(item));
  }
  
  private String getItemText(int item) {
    switch (item) {
      case MARQUEE: {
        return "Select a number of net elements to manipulate.";     
      }
      case CONDITION: {
        return getClickAnywhereText() + "condition.";
      }
      case ATOMIC_TASK: {
        return getClickAnywhereText() + "atomic task.";
      }
      case COMPOSITE_TASK: {
        return getClickAnywhereText() + "composite task.";
      }
      case FLOW_RELATION: {
        return "Draw a flow relation between two elements. The cursor will hilight where valid connections can be made.";
      }
      case MULTIPLE_ATOMIC_TASK: {
        return getClickAnywhereText() + "multiple atomic task.";
      }
      case MULTIPLE_COMPOSITE_TASK: {
        return getClickAnywhereText() + "multiple composite task.";
      }
      default: {
        return "You should never see this palette message!";
      }
    } 
  }
  
  private String getClickAnywhereText() {
    return "Click anywhere on the selected net to create a new ";    
  }
  
  public int getSelected() {
    return selectedItem;
  }
  
  public void setEnabled(boolean state) {
    if (enabledState == state) {
      return;
    }
    setVisible(false);
    for (int i = 0; i < buttons.length; i++) {
      buttons[i].setEnabled(state);      
    }
    if (state == true) {
      JStatusBar.getInstance().setStatusText(getItemText(getSelected()));
    }
    setVisible(true);
    enabledState = state;
  }
  
  public void updateState(int state) {
    switch(state) {
      case SpecificationModel.NO_NETS_EXIST: {
        setSelected(MARQUEE);
        setEnabled(false);
        JStatusBar.getInstance().setStatusText("Open or create a specification to begin.");     
        break;    
      }
      case SpecificationModel.NETS_EXIST: {
        JStatusBar.getInstance().setStatusText("Select a net to continue editing it.");     
        break;    
      }
      case SpecificationModel.NO_NET_SELECTED: {
        JStatusBar.getInstance().setStatusText("Select a net to continue editing it.");     
        setEnabled(false);
        break;
      }
      case SpecificationModel.SOME_NET_SELECTED: {
        JStatusBar.getInstance().setStatusText("Use the palette toolbar to edit the selected net.");     
        setEnabled(true);
        break;
      }
      default: {
        assert false : "Invalid state passed to updateState()";   
      }    
    }
  }
}
