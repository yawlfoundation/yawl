/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.actions.net;

import au.edu.qut.yawl.editor.actions.YAWLBaseAction;
import au.edu.qut.yawl.editor.specification.SpecificaitonModelListener;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

class YAWLExistingNetAction extends YAWLBaseAction 
                            implements SpecificaitonModelListener {

  private final SpecificationModel specificationModel = 
    SpecificationModel.getInstance();

  {
    getSpecificationModel().subscribe(this);   
  }                                  

  public void updateState(int state) {
    switch(state) {
      case SpecificationModel.NO_NETS_EXIST: {
        setEnabled(false);     
        break;    
      }
      case SpecificationModel.NETS_EXIST: {
        setEnabled(true);
        break;    
      }
      case SpecificationModel.NO_NET_SELECTED: 
      case SpecificationModel.SOME_NET_SELECTED: {
        break;
      }
      default: {
        assert false : "Invalid state passed to updateState()";   
      }    
    }
  }

  public SpecificationModel getSpecificationModel() {
    return specificationModel;  
  }
}
