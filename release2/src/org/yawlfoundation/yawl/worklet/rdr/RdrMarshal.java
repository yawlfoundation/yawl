/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.rdr;

/**
 * @author Michael Adams
 * @date 26/03/12
 */
public class RdrMarshal {
    
    public RdrNode unmarshalNode(String xml) {
        return new RdrNode(xml); 
    }
    
    public String marshalNode(RdrNode node) {
        return node.toXML();
    }
    
    
    public RdrTree unmarshalTree(String xml) {
        RdrTree tree = new RdrTree();
        tree.fromXML(xml);
        return tree;
    }
    
    public String marshalTree(RdrTree tree) {
        return tree.toXML();
    }
    
    
    public RdrSet unmarshalSet(String xml) {
        RdrSet set = new RdrSet("");
        set.fromXML(xml);
        return set;
    }
    
    public String marshalSet(RdrSet set) {
        return set.toXML();
    }
    
    
    public RdrConclusion unmarshalConclusion(String xml) {
        RdrConclusion conc = new RdrConclusion(null);
        conc.fromXML(xml);
        return conc;
    }
    
    public String marshalConclusion(RdrConclusion conc) {
        return conc.toXML();
    }

}
