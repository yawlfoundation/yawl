package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.List;

/**
 * @author Michael Adams
 * @date 18/06/12
 */
public class TestRepository {

    public static void main(String args[]) {
        Repository repo = new Repository();
        String xml = StringUtil.fileToString(
                "/Users/adamsmj/Documents/Subversion/distributions/orderfulfilment20.yawl");
        String xml1 = StringUtil.fileToString("/Users/adamsmj/Documents/temp/_eaExample.yawl");

        try {
            YSpecification spec = YMarshal.unmarshalSpecifications(xml).get(0);
//
//            // test task decomposition store
//            for (YDecomposition gateway : spec.getDecompositions()) {
//                if (gateway instanceof YAWLServiceGateway) {
//                    repo.addTaskDecomposition(gateway.getID(), "some description",
//                            gateway);
//                }
//            }
//
//            for (RepoDescriptor rd : repo.getTaskDecompositionDescriptors()) {
//                System.out.println("Name: " + rd.getName() + ", desc: " + rd.getDescription());
//            }
//
//            YAWLServiceGateway gateway = repo.getTaskDecomposition("Carrier_Timeout");
//            String newxml = gateway.toXML();
//            System.out.println(newxml);
//
//            // test external attributes store
//            YSpecification spec1 = YMarshal.unmarshalSpecifications(xml1).get(0);
//            YDecomposition dec = spec1.getDecomposition("Answer");
//            YParameter par = dec.getInputParameters().get("Answer");
//            YAttributeMap map = par.getAttributes();
//            repo.addExtendedVariableMap("Answer", "a desc", map);
//
//            map = repo.getExtendedVariableMap("Answer");
//            System.out.println(map.toXMLElements());

            // test data type store
            String schema = spec.getDataSchema();
            XNode schemaNode = new XNodeParser().parse(schema);
            List<XNode> children = schemaNode.getChildren("xs:complexType");
            schemaNode.removeChildren();
            for (XNode subnode : children) {
                schemaNode.addChild(subnode);
                repo.addDataDefinition(subnode.getAttributeValue("name"), "X",
                        schemaNode.toString());
                schemaNode.removeChild(subnode);
            }

            for (RepoDescriptor rd : repo.getDataDefinitionDescriptors()) {
                System.out.println("Name: " + rd.getName() + ", desc: " + rd.getDescription());
            }

            String def = repo.getDataDefinition("YTimerType");
            System.out.println(def);
        }
        catch (YSyntaxException ioe) {
            //
        }
    }
}
