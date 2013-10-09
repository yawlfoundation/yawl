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

package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * @author Michael Adams
 * @date 21/06/12
 */
public class TestLayout {

    public static void main(String args[]) {
        String path = "/Users/adamsmj/Documents/Subversion/distributions/orderfulfilment20.yawl";
        String specXML = StringUtil.fileToString(path);
        try {
            YSpecification spec = YMarshal.unmarshalSpecifications(specXML).get(0);
            YLayout layout = new YLayout(spec);
            XNode node = new XNodeParser().parse(specXML);
            String layoutStr = node.getChild("layout").toString();
            layout.parse(layoutStr);
            p(layout.toXML());
        }
        catch (Exception yse) {
            yse.printStackTrace();
        }
    }

    private static void p(String s) { System.out.println(s); }
}
