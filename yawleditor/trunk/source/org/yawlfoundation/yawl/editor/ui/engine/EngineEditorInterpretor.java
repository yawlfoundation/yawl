/*
 * Created on 10/02/2006
 * YAWLEditor v1.4 
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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YNet;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an abstract class, supplying a base environment for concrete subclasses that must
 * interpret engine objects to editor objects or vice versa.
 * @see SpecificationExporter
 * @see SpecificationLoader
 * @author Lindsay Bradford
 */

public abstract class EngineEditorInterpretor {

    protected static Map<YAWLCell, Object> editorToEngineElementMap;
    protected static Map<NetGraphModel, YNet> editorToEngineNetMap;
    protected static Map<YAWLFlowRelation, YCondition> editorFlowEngineConditionMap;
    protected static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";

    public static void initialise() {
        editorToEngineElementMap = new HashMap<YAWLCell, Object>();
        editorToEngineNetMap = new HashMap<NetGraphModel, YNet>();
        editorFlowEngineConditionMap = new HashMap<YAWLFlowRelation, YCondition>();
        for (YInternalType type : YInternalType.values()) {
            type.setUsed(false);
        }
    }

    public static void reset() { initialise(); }
}
