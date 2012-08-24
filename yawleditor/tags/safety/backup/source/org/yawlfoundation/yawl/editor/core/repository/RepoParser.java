package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.Map;

/**
 * Parses the XML of an object in the repository to its corresponding object.
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoParser {

    protected RepoParser() { }

    /**
     * Creates a task decomposition from its XML description
     * @param xml the XML to parse
     * @return the populated task decomposition
     */
    protected YAWLServiceGateway parseTaskDecomposition(String xml) {
        XNode decompNode = parse(xml);
        if (decompNode == null) return null;

        String id = decompNode.getAttributeValue("id");
        YAWLServiceGateway decomposition = new YAWLServiceGateway(id, new YSpecification());
        setCommonDecompositionMembers(decompNode, decomposition);
        setCodelet(decompNode, decomposition);
        setInteraction(decompNode, decomposition);
        setService(decompNode, decomposition);
        return decomposition;
    }


    /**
     * Creates a net decomposition from its XML description
     * @param xml the XML to parse
     * @return the populated net decomposition
     */
    protected YNet parseNetDecomposition(String xml)  {
        XNode decompNode = parse(xml);
        if (decompNode == null) return null;

        String id = decompNode.getAttributeValue("id");
        YNet net = new YNet(id, new YSpecification());
        setCommonDecompositionMembers(decompNode, net);
        // todo: much parsing of net specific xml
        return net;
    }


    /**
     * Creates a map of extended attributes from its XML description
     * @param xml the XML to parse
     * @return the populated extended attribute map
     */
    protected YAttributeMap parseAttributeMap(String xml) {
        YAttributeMap map = new YAttributeMap();
        if (xml != null) {
            XNode node = parse(xml);
            if (node != null) {
                for (XNode attNode : node.getChildren()) {
                    map.put(attNode.getName(), attNode.getText());
                }
            }
        }
        return map;
    }


    /*****************************************************************************/

    // parses an XML String into an XNode object
    private XNode parse(String xml) {
        return new XNodeParser().parse(xml);
    }


    // sets the common decomposition (task and net) members
    private void setCommonDecompositionMembers(XNode node, YDecomposition decomposition) {
        setName(node, decomposition);
        setDocumentation(node, decomposition);
        setDecompositionAttributes(node, decomposition);
        setInputParameters(node, decomposition);
        setOutputParameters(node, decomposition);
        setLogPredicate(node, decomposition);
    }


    // sets all the input parameters for a decomposition
    private void setInputParameters(XNode decompNode, YDecomposition decomposition) {
        for (XNode node : decompNode.getChildren("inputParam")) {
            YParameter parameter = parseParameter(node, decomposition,
                    YParameter._INPUT_PARAM_TYPE);
             decomposition.addInputParameter(parameter);
        }
    }


    // sets all the output parameters for a decomposition
    private void setOutputParameters(XNode decompNode, YDecomposition decomposition) {
        for (XNode node : decompNode.getChildren("outputParam")) {
            YParameter parameter = parseParameter(node, decomposition,
                    YParameter._OUTPUT_PARAM_TYPE);
             decomposition.addOutputParameter(parameter);
        }
    }


    // creates a YParameter and populates it with the contents of the XNode passed
    private YParameter parseParameter(XNode node, YDecomposition decomposition, int type) {
        YParameter parameter = new YParameter(decomposition, type);
        parameter.setDataTypeAndName(node.getChildText("type"), node.getChildText("name"),
                node.getChildText("namespace"));
        parameter.setAttributes(node.getAttributes());
        setIndex(node, parameter);
        setDocumentation(node, parameter);
        setValues(node, parameter);
        setLogPredicate(node, parameter);
        setMandatory(node, parameter);
        setBypassesStateSpace(node, parameter);
        return parameter;
    }


    // sets the codelet for a task decomposition
    private void setCodelet(XNode decompNode, YAWLServiceGateway decomposition) {
        String codeletName = decompNode.getChildText("codelet");
        if (codeletName != null) {
            decomposition.setCodelet(codeletName);
        }
    }


    // sets the external interaction for a task decomposition
    private void setInteraction(XNode decompNode, YAWLServiceGateway decomposition) {
        String interactionMode = decompNode.getChildText("externalInteraction");
        decomposition.setExternalInteraction(interactionMode.equals("manual"));
    }


    // sets the yawl service for a task decomposition
    private void setService(XNode decompNode, YAWLServiceGateway decomposition) {
        XNode serviceNode = decompNode.getChild("yawlService");
        if (serviceNode != null) {
            String uri = serviceNode.getAttributeValue("id");
            decomposition.setYawlService(new YAWLServiceReference(uri, decomposition));
        }
    }


    // sets the extended attributes for a decomposition
    private void setDecompositionAttributes(XNode decompNode, YDecomposition decomposition) {
        if (decompNode.getAttributeCount() > 2) {     // more than id & xsi:type
            Map<String, String> attributes = decompNode.getAttributes();
            attributes.remove("id");
            attributes.remove("xsi:type");
            decomposition.setAttributes(attributes);
        }
    }


    // sets the name of a decomposition
    private void setName(XNode decompNode, YDecomposition decomposition) {
        String name = decompNode.getChildText("name");
        if (name != null) {
            decomposition.setName(name);
        }
    }


    // sets the documentation of a decomposition
    private void setDocumentation(XNode decompNode, YDecomposition decomposition) {
        String documentation = decompNode.getChildText("documentation");
        if (documentation != null) {
            decomposition.setDocumentation(documentation);
        }
    }


    // sets the documentation of a parameter
    private void setDocumentation(XNode paramNode, YParameter parameter) {
        String documentation = paramNode.getChildText("documentation");
        if (documentation != null) {
            parameter.setDocumentation(documentation);
        }
    }


    // sets the log predicate of a decomposition
    private void setLogPredicate(XNode decompNode, YDecomposition decomposition) {
        YLogPredicate logPredicate = parseLogPredicate(decompNode);
        if (logPredicate != null) {
            decomposition.setLogPredicate(logPredicate);
        }
    }


    // sets the log predicate of a parameter
    private void setLogPredicate(XNode paramNode, YParameter parameter) {
        YLogPredicate logPredicate = parseLogPredicate(paramNode);
        if (logPredicate != null) {
            parameter.setLogPredicate(logPredicate);
        }
    }


    // creates a LogPredicate object from values in the XNode passed
    private YLogPredicate parseLogPredicate(XNode node) {
        XNode logPredicateNode = node.getChild("logPredicate");
        YLogPredicate logPredicate = null;
        if (logPredicateNode != null) {
            logPredicate = new YLogPredicate();
            logPredicate.setStartPredicate(logPredicateNode.getChildText("start", true));
            logPredicate.setCompletionPredicate(logPredicateNode.getChildText(
                    "completion", true));
        }
        return logPredicate;
    }


    // sets the mandatory field of a parameter
    private void setMandatory(XNode paramNode, YParameter parameter) {
        parameter.setMandatory(paramNode.hasChild("mandatory"));
    }


    // sets the state space bypass field of a parameter
    private void setBypassesStateSpace(XNode paramNode, YParameter parameter) {
        if (paramNode.hasChild("bypassesStatespaceForDecomposition")) {
            parameter.setIsCutThroughParam(true);
        }
    }


    // sets the index field of a parameter
    private void setIndex(XNode paramNode, YParameter parameter) {
        String indexStr = paramNode.getChildText("index");
        if (indexStr != null) {
            int index = StringUtil.strToInt(indexStr, -1);
            if (index > -1) {
                parameter.setOrdering(index);
            }
        }
    }


    // sets the initial and default value fields of a parameter
    private void setValues(XNode paramNode, YParameter parameter) {
        parameter.setInitialValue(paramNode.getChildText("initialValue", true));
        parameter.setDefaultValue(paramNode.getChildText("defaultValue", true));
    }

}
