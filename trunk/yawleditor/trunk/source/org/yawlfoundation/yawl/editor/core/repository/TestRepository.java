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

package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
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
        YRepository repo = YRepository.getInstance();
        String xml = StringUtil.fileToString(
                "/Users/adamsmj/Documents/Subversion/distributions/orderfulfilment20.yawl");
//        String xml = StringUtil.fileToString("/Users/adamsmj/Documents/temp/_eaExample.yawl");

        try {
            YSpecification spec = YMarshal.unmarshalSpecifications(xml).get(0);

            // test task decomposition store
            TaskDecompositionRepository repoMap = repo.getTaskDecompositionRepository();
            for (YDecomposition gateway : spec.getDecompositions()) {
                if (gateway instanceof YAWLServiceGateway) {
                    repoMap.add(gateway.getID(), "some description", gateway);
                }
            }

            for (RepoDescriptor rd : repoMap.getDescriptors()) {
                System.out.println("Name: " + rd.getName() + ", desc: " + rd.getDescription());
            }

            YAWLServiceGateway gateway = repoMap.get("Carrier_TImeout");
            String newxml = gateway.toXML();
            System.out.println(newxml);

            // test external attributes store
            YSpecification spec1 = YMarshal.unmarshalSpecifications(xml).get(0);
            YDecomposition dec = spec1.getDecomposition("Answer");
            YParameter par = dec.getInputParameters().get("Answer");
            YAttributeMap map = par.getAttributes();
            ExtendedAttributesRepository repository = repo.getExtendedAttributesRepository();
            repository.add("Answer", "a desc", map);

            map = repository.get("Answer");
            System.out.println(map.toXMLElements());

            // test data type store
            String schema = spec.getDataSchema();
            XNode schemaNode = new XNodeParser().parse(schema);
            List<XNode> children = schemaNode.getChildren("xs:complexType");
            schemaNode.removeChildren();
            DataDefinitionRepository ddr = repo.getDataDefinitionRepository();
            for (XNode subnode : children) {
                schemaNode.addChild(subnode);
                ddr.add(subnode.getAttributeValue("name"), "X", schemaNode.toString());
                schemaNode.removeChild(subnode);
            }

            for (RepoDescriptor rd : ddr.getDescriptors()) {
                System.out.println("Name: " + rd.getName() + ", desc: " + rd.getDescription());
            }

            String def = ddr.get("YTimerType");
            System.out.println(def);
        }
        catch (YSyntaxException ioe) {
            //
        }
    }
}
