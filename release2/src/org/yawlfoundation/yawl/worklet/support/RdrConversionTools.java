/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.worklet.support;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RdrNode;
import org.yawlfoundation.yawl.worklet.rdr.RdrTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  This class contains some static methods that convert some objects to Strings and
 *  vice versa. It supports the stringifying of some objects for persistence purposes.

 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class RdrConversionTools {

    /******************************************************************************/

    /** builds an RdrNode from its String representation */
    public static RdrNode stringToNode(String s, RdrTree tree) {
        return xmlStringToNode(JDOMUtil.stringToElement(s), tree);
    }

    /******************************************************************************/

    /**
     * Builds an RdrNode from its JDOM Element representation
     * @param e - the Element represerntation of the RdrNode
     * @param tree - the tree that this node will become a member of
     * @return - the reconstructed RdrNode
     */
    public static RdrNode xmlStringToNode(Element e, RdrTree tree) {
        int id = Integer.parseInt(e.getChildText("id"));
        int parent = Integer.parseInt(e.getChildText("parent"));
        int tID = Integer.parseInt(e.getChildText("trueChild"));
        int fID = Integer.parseInt(e.getChildText("falseChild"));
        String condition = e.getChildText("condition");
        Element conclusion = e.getChild("conclusion");
        Element cornerstone = e.getChild("cornerstone");

        RdrNode pNode = tree.getNode(parent);
        RdrNode tNode = tree.getNode(tID);
        RdrNode fNode = tree.getNode(fID);

        return new RdrNode(id, pNode, tNode, fNode, condition, conclusion, cornerstone) ;
    }

    /******************************************************************************/

    /**
     * Builds a WorkItemRecord from its representation as an XML String
     * @param xmlStr
     * @return the reconstructed WorkItemRecord
     */
    public static WorkItemRecord xmlStringtoWIR(String xmlStr) {
        Element eWIR = JDOMUtil.stringToElement(xmlStr) ;    // reform as Element

        String status = eWIR.getChildText("status");
        String specID = eWIR.getChildText("specid");
//        String id = eWIR.getChildText("id");
//        String[] idSplit = id.split(":");                      // id = taskid:caseid

        String caseid = eWIR.getChildText("caseid");
        String taskid = eWIR.getChildText("taskid");

        String taskName = Library.getTaskNameFromId(taskid);

        // call the wir constructor
        WorkItemRecord wir = new WorkItemRecord( caseid, taskid, specID,
                              null, status);

        // add data list if non-parent item
        Element data = eWIR.getChild("data").getChild(taskName) ;
        if (data != null) {
            data = (Element) data.detach() ;
            wir.setDataList(data);
        }
        return wir;
    }

    /******************************************************************************/

    /**
     * Build a pair of RdrNodes from their string representation. (The searchPair is
     * returned after a tree search to denote the last node satisfied and the last node
     * tested).
     * @param s - the String representing the two RdrNodes
     * @param tree - the tree that contains these two nodes
     * @return  - the reconstructed pair of nodes
     */
    public static RdrNode[] stringToSearchPair(String s, RdrTree tree) {

        RdrNode[] result = null ;
        if ((s != null) && (s.length() > 0)) {
            String[] nodeStr = s.split(":::");             // ':::' separates the 2 nodes
            result = new RdrNode[2];
            result[0] = stringToNode(nodeStr[0], tree);    // convert each to a node
            result[1] = stringToNode(nodeStr[1], tree);
        }
        return result ;
    }

    /******************************************************************************/

    /**
     * Converts a String of csv's and returns them as a String List
     *  PRE: 's' is a series of substrings delimited by commas
     * @param s - the string containing the comma separated values
     * @return - the List of values
     */
    public static List StringToStringList(String s) {
        List result = new ArrayList() ;

        if (s != null) {
           String[] items = s.split(",");

           for (int i=0; i < items.length; i++) result.add(items[i]);
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }

    /******************************************************************************/

    public static String WIRListToString(List items) {
        if (items != null) {
            StringBuilder xml = new StringBuilder("<recordlist>");
            Iterator itr = items.iterator() ;
            while (itr.hasNext()) {

                // convert each WIR to XML
                xml.append(((WorkItemRecord) itr.next()).toXML());
            }
            xml.append("</recordlist>");
            return xml.toString();
        }
        return null ;
    }

    public static List<WorkItemRecord> xmlToWIRList(String xml) {
        Element wirElem = JDOMUtil.stringToElement(xml);
        if (wirElem != null) {
            List<WorkItemRecord> result = new ArrayList<WorkItemRecord>();
            List children = wirElem.getChildren();
            for (Object o : children) {
                Element child = (Element) o;
                result.add(Marshaller.unmarshalWorkItem(child));
            }
            return result;
        }
        return null;
    }

    /**
     * Converts a list of String values to a String of csv's
     * @param list - list of String values
     * @return the String of comma separated values
     */
    public static String StringListToString(List list) {
        if (list != null)
            return itrToString(list.iterator());
        else
           return null ;
    }

    /******************************************************************************/

    /**
     * Converts a list of a Map's keyset values to a String of csv's
     * @param map - the map to convert
     * @return the String of comma separated keyset values
     */
    public static String MapKeySetToString(Map map) {
        if (map != null)
            return itrToString(map.keySet().iterator());
        else
           return null ;
    }

    /******************************************************************************/

    /** iterates through a list to return a String of csv's */
    private static String itrToString(Iterator itr) {
        String result = "";
        while (itr.hasNext()) {
            if (result.length() > 0) result += ",";
            result += itr.next();
        }
        if (result.length() == 0) result = null ;
        return result ;
    }

    /******************************************************************************/

    /** returns the String value of a child of the xml string passed */
    public static String getChildValue(String xmlStr, String child) {
        Element e = JDOMUtil.stringToElement(xmlStr) ;    // reform as Element
        if (e != null)
           return e.getChildText(child);
        else
           return "null" ;
    }

    /******************************************************************************/
    /******************************************************************************/
}
