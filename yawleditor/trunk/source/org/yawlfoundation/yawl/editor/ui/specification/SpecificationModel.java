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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

public class SpecificationModel {

    private static final YSpecificationHandler _specificationHandler = new YSpecificationHandler();
    private static final NetModelSet _nets = new NetModelSet();

    private SpecificationModel() { reset(); }


    public static NetGraph newSpecification() {
        try {
            reset();
            _specificationHandler.newSpecification();
            YNet net = _specificationHandler.getControlFlowHandler().getRootNet();
            NetGraph graph = new NetGraph(net);
            _nets.addRootNet(graph.getNetModel());
            return graph;
        }
        catch (YControlFlowHandlerException ycfhe) {
            // would only occur if we forgot to call handler.newSpecification first
        }
        catch (YSyntaxException yse) {
            // only thrown on bad data schema - which won't be the case here
        }
        return null;
    }


    public static void reset() {
        _nets.clear();
        YAWLEditor.getStatusBar().setText("Open or create a net to begin.");
    }


    public static YSpecificationHandler getHandler() { return _specificationHandler; }

    public static NetModelSet getNets() { return _nets; }

}
