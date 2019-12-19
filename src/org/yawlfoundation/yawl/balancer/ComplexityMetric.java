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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.List;

/**
 * @author Michael Adams
 * @date 11/6/18
 */
public class ComplexityMetric {

    private static final Namespace YAWL_NAMESPACE = Namespace.getNamespace("yawl",
            "http://www.yawlfoundation.org/yawlschema");
    private static final Namespace XSD_NAMESPACE = Namespace.getNamespace("xs",
            "http://www.w3.org/2001/XMLSchema");



    public double calc(String specXML) {
        Document doc = JDOMUtil.stringToDocument(specXML);
        if (doc == null) return -1;
        List<Element> elementList = JDOMUtil.selectElementList(
                doc, "//yawl:task", YAWL_NAMESPACE);
        return getWeightedCarduso(elementList) + getWeightedTaskCardinality(elementList) +
                getWeightedUDTCount(doc) + getWeightedIOCount(elementList) +
                getWeightedResourceCount(doc);
    }


    public double getWeightedCarduso(List<Element> taskElements) {
        double weight = Config.getCardusoComplexityWeight();
        return taskElements == null || weight <= 0 ? 0 :
                getCardusoMetric(taskElements) * weight;
    }


    public double getWeightedTaskCardinality(List<Element> taskElements) {
        double weight = Config.getTaskCardinalityComplexityWeight();
        return taskElements == null || weight <= 0 ? 0 :
                taskElements.size() * weight;
    }


    public double getWeightedUDTCount(Document doc) {
        double weight = Config.getUDTComplexityWeight();
        if (doc == null || weight <= 0) return 0;
        Element root = doc.getRootElement();
        Element spec = root.getChild("specification", YAWL_NAMESPACE);
        Element dataSchema = spec.getChild("schema", XSD_NAMESPACE);
        return dataSchema.getChildren().size() * weight;
    }


    public double getWeightedIOCount(List<Element> taskElements) {
        double weight = Config.getDataMappingsComplexityWeight();
        if (taskElements == null || weight <= 0) return 0;
        int mappingCount = 0;
        for (Element te : taskElements) {
            mappingCount += te.getChildren("startingMappings", YAWL_NAMESPACE).size();
            mappingCount += te.getChildren("completedMappings", YAWL_NAMESPACE).size();
        }
        return mappingCount * weight;
    }


    public double getWeightedResourceCount(Document doc) {
        double weight = Config.getResourcingComplexityWeight();
        if (doc == null || weight <= 0) return 0;
        List<Element> elementList = JDOMUtil.selectElementList(
                doc, "//yawl:role", YAWL_NAMESPACE);
        return elementList.size() + weight;
    }


    private int getCardusoMetric(List<Element> taskElements) {
        int metric = 0;
        for (Element te : taskElements) {
            int successors = te.getChildren("flowsInto", YAWL_NAMESPACE).size();
            if (successors > 1) {
                String splitType = te.getChild("split", YAWL_NAMESPACE)
                        .getAttributeValue("code");
                if (splitType.equals("and")) {
                    metric++;
                }
                else if (splitType.equals("xor")) {
                    metric += successors;
                }
                else if (splitType.equals("or")) {
                    metric += Math.pow(2, successors) - 1;
                }
            }
        }
        return metric;
    }

}
