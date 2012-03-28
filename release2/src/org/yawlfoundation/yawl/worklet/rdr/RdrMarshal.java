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
 * Marshals and unmarshals Rdr objects and their XML string equivalents
 *
 * @author Michael Adams
 * @date 26/03/12
 */
public class RdrMarshal {

    /**
     * Creates a new RdrNode object
     * @param xml the XML string describing the node
     * @return the instantiated RdrNode
     */
    public RdrNode unmarshalNode(String xml) {
        return new RdrNode(xml); 
    }


    /**
     * Converts an RdrNode into its XML string equivalent
     * @param node the node to translate to XML
     * @return the XML string describing the node
     */
    public String marshalNode(RdrNode node) {
        return node.toXML();
    }
    
    
    /**
     * Creates a new RdrTree object
     * @param xml the XML string describing the tree
     * @return the instantiated RdrTree
     */
    public RdrTree unmarshalTree(String xml) {
        RdrTree tree = new RdrTree();
        tree.fromXML(xml);
        return tree;
    }


    /**
     * Converts an RdrTree into its XML string equivalent
     * @param tree the tree to translate to XML
     * @return the XML string describing the tree
     */
    public String marshalTree(RdrTree tree) {
        return tree.toXML();
    }
    
    
    /**
     * Creates a new RdrSet object
     * @param xml the XML string describing the set
     * @return the instantiated RdrSet
     */
    public RdrSet unmarshalSet(String xml) {
        RdrSet set = new RdrSet("");
        set.fromXML(xml);
        return set;
    }


    /**
     * Converts an RdrSet into its XML string equivalent
     * @param set the set to translate to XML
     * @return the XML string describing the set
     */
    public String marshalSet(RdrSet set) {
        return set.toXML();
    }
    

    /**
     * Creates a new RdrConclusion object
     * @param xml the XML string describing the conclusion
     * @return the instantiated RdrConclusion
     */
    public RdrConclusion unmarshalConclusion(String xml) {
        RdrConclusion conc = new RdrConclusion(null);
        conc.fromXML(xml);
        return conc;
    }


    /**
     * Converts an RdrConclusion into its XML string equivalent
     * @param conc the conclusion to translate to XML
     * @return the XML string describing the conclusion
     */
    public String marshalConclusion(RdrConclusion conc) {
        return conc.toXML();
    }

}
