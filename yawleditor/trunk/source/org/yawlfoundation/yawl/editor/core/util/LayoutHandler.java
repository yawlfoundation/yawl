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

package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * @author Michael Adams
 * @date 20/06/13
 */
public class LayoutHandler {

    private YLayout _layout;


    protected LayoutHandler(YSpecification specification) {
        _layout = new YLayout(specification);
    }


    protected YLayout getLayout() { return _layout; }

    protected void setLayout(YLayout layout) { _layout = layout; }


    protected String appendLayoutXML(String specXML) {
        if (! (_layout == null || specXML == null)) {

            // -1 offset sets the indent to the correct level for insertion
            String layoutXML = _layout.toXNode().toPrettyString(-1, 2);

            // remove last \n from layoutXML, then insert at end of specXML
            layoutXML = layoutXML.substring(0, layoutXML.length() - 1);
            return StringUtil.insert(specXML, layoutXML, specXML.lastIndexOf("</"));
        }
        return specXML;  // return unchanged
    }


    protected void parse(String xml) throws YLayoutParseException {
        XNode specNode = new XNodeParser().parse(xml);
        if (specNode == null) {
            throw new YLayoutParseException("Invalid layout data in specification");
        }
        _layout.parse(specNode.getChild("layout"));
    }


}
