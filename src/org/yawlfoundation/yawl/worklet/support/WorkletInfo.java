package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * An API transport for basic worklet specification descriptors
 *
 * @author Michael Adams
 * @date 2/12/2015
 */
public class WorkletInfo implements Comparable<WorkletInfo> {

    private YSpecificationID _specID;
    private String _description;


    public WorkletInfo(String xml) { fromXML(xml); }

    public WorkletInfo(XNode node) {fromXNode(node); }


    public WorkletInfo(YSpecificationID specID, String description) {
        _specID = specID;
        _description = description;
    }


    public WorkletInfo(WorkletSpecification wSpec) {
        _specID = wSpec.getSpecID();
        _description = extractDescription(wSpec.getXML());
    }


    public YSpecificationID getSpecID() { return _specID; }

    public String getDescription() { return _description; }


    public XNode toXNode() {
        XNode root = new XNode("worklet_info");
        root.addChild(_specID.toXNode());
        root.addChild("description", _description);
        return root;
    }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        if (node != null) {
            _specID = new YSpecificationID(node.getChild("specificationid"));
            _description = node.getChildText("description");
        }
    }


    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }


    public int compareTo(WorkletInfo other) {
        return this.toString().compareToIgnoreCase(other.toString());
    }


    public String toString() {
        return _specID.toString() + ": " + _description;
    }


    private String extractDescription(String xml) {
        String description = "No description provided";
        Document doc = JDOMUtil.stringToDocument(xml);
        if (doc != null) {
            Element root = doc.getRootElement();
            if (root != null) {
                Namespace ns = root.getNamespace();
                Element spec = root.getChild("specification", ns);
                if (spec != null) {
                    String content = spec.getChildText("documentation", ns);
                    if (content == null) {
                        Element metadata = spec.getChild("metaData", ns);
                        if (metadata != null) {
                            content = metadata.getChildText("description", ns);
                        }
                    }
                    if (content != null) {
                        description = content;
                    }
                }
            }
        }
        return description;
    }

}
