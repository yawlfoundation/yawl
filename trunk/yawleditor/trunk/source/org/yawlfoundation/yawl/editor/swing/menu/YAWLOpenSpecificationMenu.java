/*
 * Created on 05/10/2003
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

package org.yawlfoundation.yawl.editor.swing.menu;


import javax.swing.JMenu;

import org.yawlfoundation.yawl.editor.specification.SpecificationFileModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModelListener;

abstract class YAWLOpenSpecificationMenu extends JMenu 
                                implements SpecificationFileModelListener {
    
  private static final SpecificationFileModel fileModel =  
    SpecificationFileModel.getInstance(); 
    
  public YAWLOpenSpecificationMenu(String title, int keyEventCode) {
    super(title);
    setMnemonic(keyEventCode);
    buildInterface();
    fileModel.subscribe(this);
  }
  
  protected abstract void buildInterface();
  
  public void specificationFileModelStateChanged(int state) {
    switch (state) {
      case SpecificationFileModel.IDLE: {
        setEnabled(false);  
        break;    
      }
      case SpecificationFileModel.EDITING: {
        setEnabled(true);
        break;   
      }
      default: {
         assert false: "Invalid state passed to updateState().";   
      }    
    }
  }
}
