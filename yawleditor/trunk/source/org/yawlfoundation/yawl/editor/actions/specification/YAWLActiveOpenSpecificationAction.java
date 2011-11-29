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

import org.yawlfoundation.yawl.editor.specification.SpecificationFileModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModelListener;

/**
 * This class is an abstract class that supplies a basic Action for concrete Action classes
 * to build from. It becomes enabled only when there is a specification loaded, and some net
 * within the specification is active. It is disabled otherwise.
 *
 * @author Lindsay Bradford
 */

public class YAWLActiveOpenSpecificationAction extends YAWLOpenSpecificationAction
        implements SpecificationFileModelListener {

    {
        getFileModel().subscribe(this);
    }

    public void specificationFileModelStateChanged(int state) {
        switch (state) {
            case SpecificationFileModel.IDLE: {
                setEnabled(false);
                break;
            }
            case SpecificationFileModel.EDITING: {
                break;
            }
            default: {
                assert false : "Invalid state passed to updateState()";
            }
        }
    }
}
