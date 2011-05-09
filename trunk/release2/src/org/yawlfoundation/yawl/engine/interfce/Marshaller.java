/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;


/**
 *
 * @author Lachlan Aldred
 * Date: 16/02/2004
 * Time: 18:41:17
 *
 */
public class Marshaller {

    public static String getOutputParamsInXML(YParametersSchema paramSchema,
                                              String dataSpaceRootElementNm) {
        StringBuilder result = new StringBuilder();

        result.append("<").append(dataSpaceRootElementNm).append(">");
        if (paramSchema != null) {
            List<YParameter> params = paramSchema.getOutputParams();
            for (YParameter param : params) {
                result.append(presentParam(param));
            }
        }
        result.append("</").append(dataSpaceRootElementNm).append(">");
        return result.toString();
    }


    public static String presentParam(YParameter param) {
        StringBuilder result = new StringBuilder();
        result.append("\n  <!--");
        if (param.getDataTypeName() != null) {
            result.append("Data Type:     ").append(param.getDataTypeName());
        }
        result.append("\n      Is Mandatory:  ").append(param.isMandatory());
        if (param.getDocumentation() != null) {
            result.append("\n      Documentation: ").append(param.getDocumentation());
        }
        result.append("-->\n  ");
        String name = param.getPreferredName();
        result.append(String.format("<%s></%s>", name, name));
        return result.toString();
    }


    public static TaskInformation unmarshalTaskInformation(String taskInfoAsXML) {
        YParametersSchema paramsForTaskNCase = new YParametersSchema();
        YAttributeMap attributemap = new YAttributeMap();

        Element taskInfo = JDOMUtil.stringToElement(taskInfoAsXML);

        // if the string was encased in response tags, go down one level
        if (taskInfoAsXML.startsWith("<response>")) {
            taskInfo = taskInfo.getChild("taskInfo");
        }
        String taskID = taskInfo.getChildText("taskID");
        String taskName = taskInfo.getChildText("taskName");
        String taskDocumentation = taskInfo.getChildText("taskDocumentation");
        String decompositionID = taskInfo.getChildText("decompositionID");
        Element yawlService = taskInfo.getChild("yawlService");
    
        Element specElem = taskInfo.getChild("specification");
        String specIdentifier = specElem.getChildText("identifier");
        String specVersion = specElem.getChildText("version");
        String specURI = specElem.getChildText("uri");
        YSpecificationID specificationID = new YSpecificationID(specIdentifier, specVersion, specURI);
    
        Element attributes = taskInfo.getChild("attributes");
        if (attributes != null) {
            List attributelist = attributes.getChildren();
            for (Object o : attributelist) {
                Element attribute = (Element) o;
                attributemap.put(attribute.getName(),
                        attributes.getChildText(attribute.getName()));
            }
        }

        Element params = taskInfo.getChild("params");
        List paramElementsList = params.getChildren();
        for (Object o : paramElementsList) {
            Element paramElem = (Element) o;
            if ("formalInputParam".equals(paramElem.getName())) {
                paramsForTaskNCase.setFormalInputParam(paramElem.getText());
                continue;
            }
            YParameter param = new YParameter(null, paramElem.getName());
            YDecompositionParser.parseParameter(
                    paramElem,
                    param,
                    null,
                    false);
            if (param.isInput()) {
                paramsForTaskNCase.setInputParam(param);
            } else {
                paramsForTaskNCase.setOutputParam(param);
            }
            String paramOrdering = paramElem.getChildText("ordering");
            if (paramOrdering != null) {
                int order = Integer.parseInt(paramOrdering);
                param.setOrdering(order);
            }
        }

        TaskInformation taskInformation = new TaskInformation(
                paramsForTaskNCase,
                taskID,
                specificationID,
                taskName,
                taskDocumentation,
                decompositionID);

        taskInformation.setAttributes(attributemap);

        return taskInformation;
    }


    /**
     * Creates a list of SpecificationDatas from formatted XML.
     * These are brief meta data summary
     * information objects that describe a worklfow specification.
     * @param specificationSummaryListXML
     * @return  the list
     */
    public static List<SpecificationData> unmarshalSpecificationSummary(
            String specificationSummaryListXML) {
        List<SpecificationData> specSummaryList = new ArrayList<SpecificationData>();

        Element specElem = JDOMUtil.stringToElement(specificationSummaryListXML);
        List specSummaryElements = specElem.getChildren();
        for (Object o : specSummaryElements) {
            Element specElement = (Element) o;
            String specID = specElement.getChildText("id");
            String specURI = specElement.getChildText("uri");
            String specName = specElement.getChildText("name");
            String specDoco = specElement.getChildText("documentation");
            String specStatus = specElement.getChildText("status");
            String version = specElement.getChildText("version");
            String rootNetID = specElement.getChildText("rootNetID");
            String specVersion = specElement.getChildText("specversion");
            String dataGateway = specElement.getChildText("externalDataGateway");
            
            if (specURI != null && specStatus != null) {
                YSpecificationID ySpecID = new YSpecificationID(specID, specVersion, specURI);
                SpecificationData specData = new SpecificationData(ySpecID,
                            specName, specDoco, specStatus, version);
                
                specData.setRootNetID(rootNetID);
                specData.setExternalDataGateway(dataGateway);
                specSummaryList.add(specData);
                Element inputParams = specElement.getChild("params");
                if (inputParams != null) {
                    List paramElements = inputParams.getChildren();
                    for (Object p :paramElements) {
                        Element paramElem = (Element) p;
                        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
                        YDecompositionParser.parseParameter(
                                paramElem,
                                param,
                                null,
                                false);//todo check correctness
                        specData.addInputParam(param);
                    }
                }

                specData.setMetaTitle(specElement.getChildText("metaTitle"));
                Element authors = specElement.getChild("authors");
                if (authors != null) {
                    List authorlist = authors.getChildren();
                    for (Object e : authorlist) {
                        Element authorElem = (Element) e;
                        specData.setAuthors(authorElem.getText());
                    }
                }

            }
        }
        return specSummaryList;
    }


    public static WorkItemRecord unmarshalWorkItem(String workItemXML) {
        return unmarshalWorkItem(JDOMUtil.stringToElement(workItemXML));
    }


    public static WorkItemRecord unmarshalWorkItem(Element workItemElement) {
        if (workItemElement == null) return null;
        
        WorkItemRecord wir;
        String status = workItemElement.getChildText("status");
        String caseID = workItemElement.getChildText("caseid");
        String taskID = workItemElement.getChildText("taskid");
        String specURI = workItemElement.getChildText("specuri");
        String enablementTime = workItemElement.getChildText("enablementTime");
        if (caseID != null && taskID != null && specURI != null &&
            enablementTime != null && status != null) {

            wir = new WorkItemRecord(caseID, taskID, specURI, enablementTime, status);

            wir.setExtendedAttributes(unmarshalWorkItemAttributes(workItemElement));
            wir.setUniqueID(workItemElement.getChildText("uniqueid"));
            wir.setTaskName(workItemElement.getChildText("taskname"));
            wir.setDocumentation(workItemElement.getChildText("documentation"));
            wir.setAllowsDynamicCreation(workItemElement.getChildText(
                                                              "allowsdynamiccreation"));
            wir.setRequiresManualResourcing(workItemElement.getChildText(
                                                           "requiresmanualresourcing"));
            wir.setCodelet(workItemElement.getChildText("codelet"));
            wir.setDeferredChoiceGroupID(workItemElement.getChildText(
                                                              "deferredChoiceGroupid"));
            wir.setSpecVersion(workItemElement.getChildText("specversion"));
            wir.setFiringTime(workItemElement.getChildText("firingTime"));
            wir.setStartTime(workItemElement.getChildText("startTime"));
            wir.setCompletionTimeMs(workItemElement.getChildText("completionTime"));
            wir.setEnablementTimeMs(workItemElement.getChildText("enablementTimeMs"));
            wir.setFiringTimeMs(workItemElement.getChildText("firingTimeMs"));
            wir.setStartTimeMs(workItemElement.getChildText("startTimeMs"));
            wir.setCompletionTimeMs(workItemElement.getChildText("completionTimeMs"));
            wir.setTimerTrigger(workItemElement.getChildText("timertrigger"));
            wir.setTimerExpiry(workItemElement.getChildText("timerexpiry"));
            wir.setStartedBy(workItemElement.getChildText("startedBy"));
            wir.setTag(workItemElement.getChildText("tag"));
            wir.setCustomFormURL(workItemElement.getChildText("customform"));

            String specid = workItemElement.getChildText("specidentifier") ;
            if (specid != null) wir.setSpecIdentifier(specid);

            String resStatus = workItemElement.getChildText("resourceStatus");
            if (resStatus != null) wir.setResourceStatus(resStatus);
            
            Element data = workItemElement.getChild("data");
            if ((null != data) && (data.getContentSize() > 0))
                   wir.setDataList((Element) data.getContent(0));

            Element updateddata = workItemElement.getChild("updateddata");
            if ((null != updateddata) && (updateddata.getContentSize() > 0))
                   wir.setUpdatedData((Element) updateddata.getContent(0));

            Element logPredicate = workItemElement.getChild("logPredicate");
            if (logPredicate != null) {
                wir.setLogPredicateStarted(logPredicate.getChildText("start"));
                wir.setLogPredicateCompletion(logPredicate.getChildText("completion"));
            }
                        
            return wir;
        }
        throw new IllegalArgumentException("Input element could not be parsed.");
    }


    public static Hashtable<String, String> unmarshalWorkItemAttributes(Element item) {
        YAttributeMap result = new YAttributeMap();
        result.fromJDOM(item.getAttributes());
        return result ;
    }

    public static List<String> unmarshalCaseIDs(String casesAsXML) {
        List<String> cases = new ArrayList<String>();
        Element casesElem = JDOMUtil.stringToElement(casesAsXML);
        List caseList = casesElem.getChildren();
        for (Object o : caseList) {
            Element caseElem = (Element) o;
            String caseID = caseElem.getText();
            if (caseID != null) {
                cases.add(caseID);
            }
        }
        return cases;
    }


    public static String getMergedOutputData(Element inputData, Element outputData) {
        try {
            Element merged = (Element) inputData.clone();
            JDOMUtil.stripAttributes(merged);
            JDOMUtil.stripAttributes(outputData);

            List children = outputData.getChildren();

            //iterate through the output vars and add them to the merged doc.
            for (int i = children.size() - 1; i >= 0; i--) {
                Object o = children.get(i);
                if (o instanceof Element) {
                    Element child = (Element) o;
                    child.detach();

                    //the input data will be removed from the merged doc and
                    //the output data will be added.
                    merged.removeChild(child.getName());
                    merged.addContent(child);
                }
            }
            return JDOMUtil.elementToString(merged);
        }
        catch (Exception e) {
            return "";
        }
    }

    
    public static String filterDataAgainstOutputParams(String mergedOutputData,
                                                       List<YParameter> outputParams)
            throws JDOMException, IOException {

        //set up output document
        Element outputData = JDOMUtil.stringToElement(mergedOutputData);
        Element filteredData = new Element(outputData.getName());

        Collections.sort(outputParams);
        for (YParameter parameter : outputParams) {
            Element child = outputData.getChild(parameter.getPreferredName());
            if (null != child) {
                Element clone = (Element) child.clone();
                filteredData.addContent(clone);
            }
        }
        return JDOMUtil.elementToString(filteredData);
    }

}
