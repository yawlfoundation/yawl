package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * Represents a single record in the repository
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoRecord {

    private String name;
    private String description;
    private String value;

    protected RepoRecord() {  }

    /**
     * Constructs a new RepoRecord
     * @param name a name to reference the record
     * @param description a description of the record
     * @param value the record's value
     */
    protected RepoRecord(String name, String description, String value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }


    /**
     * Constructs a new RepoRecord
     * @param node an XNode containing the record's name, description and value
     */
    protected RepoRecord(XNode node) {
        fromXNode(node);
    }


    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }


    /**
     * Creates an XNode representation of this record
     * @return the populated XNode
     */
    protected XNode toXNode() {
        XNode node = new XNode("record");
        node.addChild("name", name);
        node.addChild("description", description);
        XNode valueNode = node.addChild("value");
        valueNode.addContent(value);
        return node;
    }


    /**
     * Creates an XML representation of this record
     * @return the populated XML String
     */
    protected String toXML() {
        return toXNode().toString();
    }


    /**
     * Populate this record with values in an XNode
     * @param node the populated XNode
     */
    protected void fromXNode(XNode node) {
        setName(node.getChildText("name"));
        setDescription(node.getChildText("description"));
        setValue(StringUtil.unwrap(node.getChild("value").toString()));
    }


    /**
     * Populate this record with values in an XML String
     * @param xml the populated XML String
     */
    protected void fromXML(String xml) {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) fromXNode(node);
    }
}
