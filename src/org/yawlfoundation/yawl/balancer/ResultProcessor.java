/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.balancer;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 22/9/17
 */
public class ResultProcessor {

    private final XNodeParser _parser = new XNodeParser();


    public ResultProcessor() {
        _parser.suppressMessages(true);
    }


    public String process(Set<String> resultSet) {

        // short-circuits
        if (resultSet == null) return null;
        resultSet.remove("null");                    // if any
        if (resultSet.isEmpty()) return "";
        if (resultSet.size() == 1) return resultSet.iterator().next();

        XNode resNode = null;
        String failMsg = null;
        for (String result : resultSet) {
            if (StringUtil.unwrap(result).isEmpty()) {
                continue;
            }
            if (failMsg == null && result.startsWith("<fail")) {
                failMsg = result;
                continue;
            }

            XNode node = parse(result);
            if (! hasParentTag(node)) {
                result = StringUtil.wrap(result, "temp");
                node = parse(result);
            }
            if (node != null) {
                if (resNode == null) {
                    resNode = node;
                }
                else {
                    resNode.addChildren(node.getChildren());
                }
            }
        }
        if (resNode != null) {
            resNode.removeDuplicateChildren();
            String out =  resNode.toString();
            if (out.startsWith("<temp>")) {
                out = StringUtil.unwrap(out);
            }
            return out;
        }
        else if (failMsg != null) {
            return failMsg;
        }
        else return "";
    }


    public XNode parse(String result) {
        return _parser.parse(result);
    }


    private boolean hasParentTag(XNode node) {
        if (node == null) return false;               // no parent tag, multi-elements
        if (! node.hasChildren()) return true;        // no child, single element
        if (node.getChildCount() == 1) return true;   // one child, thus parent tag

        // if children have different names, is a single element
        String name = null;
        for (XNode child : node.getChildren()) {
            if (name == null) name = child.getName();
            else if (! name.equals(child.getName())) return false;
        }
        return true;    // all children same name
    }

}
