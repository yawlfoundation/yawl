/*
 * Created on 9/10/2003
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

package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModelListener;

/**
 * This class is an abstract class that supplies a basic Action for concrete Action classes
 * to build from. It becomes enabled only when there are no specifications loaded, and disabled
 * when there is a specification loaded.
 * 
 * @author Lindsay Bradford
 */

public abstract class YAWLNoOpenSpecificationAction extends YAWLBaseAction 
                                                    implements SpecificationFileModelListener {

  private final SpecificationFileModel fileModel =
    SpecificationFileModel.getInstance();

  {
    getFileModel().subscribe(this);   
  }                                  

  public void specificationFileModelStateChanged(int state) {
    switch(state) {
      case SpecificationFileModel.IDLE: {
        setEnabled(true);
        break;
      }
      case SpecificationFileModel.EDITING: {
        setEnabled(false);
        break;
      }
      case SpecificationFileModel.BUSY: {
        setEnabled(false);
        break;
      }
    }
  }

  public SpecificationFileModel getFileModel() {
    return fileModel;  
  }
}
