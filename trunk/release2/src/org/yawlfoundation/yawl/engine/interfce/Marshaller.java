/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;


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
        StringBuffer result = new StringBuffer();

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

        boolean typedParam = param.getDataTypeName() != null;
        StringBuffer result = new StringBuffer();
        result.append("\n  <!--");
        if (typedParam) {
            result.append("Data Type:     " + param.getDataTypeName());
        }
        result.append("\n      Is Mandatory:  " + param.isMandatory());
        if (param.getDocumentation() != null) {
            result.append("\n      Documentation: " + param.getDocumentation());
        }
        result.append("-->\n  ");
        if (typedParam || param.isUntyped()) {
            result.append("<" + param.getName() + "></" + param.getName() + ">");
        } else {
            result.append("<" + param.getElementName() + "></" + param.getElementName() + ">");
        }
        return result.toString();
    }


    public static TaskInformation unmarshalTaskInformation(String taskInfoAsXML) {
        YParametersSchema paramsForTaskNCase = new YParametersSchema();
        HashMap attributemap = new HashMap();

        Element taskInfo = JDOMUtil.stringToElement(taskInfoAsXML);

        // if the string was encased in response tags, go down one level
        if (taskInfoAsXML.startsWith("<response>")) {
            taskInfo = taskInfo.getChild("taskInfo");
        }
        String taskID = taskInfo.getChildText("taskID");
        String specificationID = taskInfo.getChildText("specificationID");
        String taskName = taskInfo.getChildText("taskName");
        String taskDocumentation = taskInfo.getChildText("taskDocumentation");
        String decompositionID = taskInfo.getChildText("decompositionID");
        Element yawlService = taskInfo.getChild("yawlService");

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
            String specName = specElement.getChildText("name");
            String specDoco = specElement.getChildText("documentation");
            String specStatus = specElement.getChildText("status");
            String version = specElement.getChildText("version");
            String rootNetID = specElement.getChildText("rootNetID");

            if (specID != null && specStatus != null) {
                SpecificationData specData = new SpecificationData(
                        specID,
                        specName,
                        specDoco,
                        specStatus,
                        version);
                specData.setRootNetID(rootNetID);
                specData.setSpecVersion(specElement.getChildText("specversion"));
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
        String specID = workItemElement.getChildText("specid");
        String enablementTime = workItemElement.getChildText("enablementTime");
        if (caseID != null && taskID != null && specID != null &&
            enablementTime != null && status != null) {

            wir = new WorkItemRecord(caseID, taskID, specID, enablementTime, status);

            wir.setExtendedAttributes(unmarshalWorkItemAttributes(workItemElement));
            wir.setUniqueID(workItemElement.getChildText("uniqueid"));
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

            String resStatus = workItemElement.getChildText("resourceStatus");
            if (resStatus != null) wir.setResourceStatus(resStatus);
            
            String edited = workItemElement.getChildText("edited") ;
            if (edited != null)
                wir.setEdited(edited.equalsIgnoreCase("true"));

            Element data = workItemElement.getChild("data");
            if ((null != data) && (data.getContentSize() > 0))
                   wir.setDataList((Element) data.getContent(0));
            
            return wir;
        }
        throw new IllegalArgumentException("Input element could not be parsed.");
    }


    public static Hashtable<String, String> unmarshalWorkItemAttributes(Element item) {
        Hashtable<String, String> result = null ;
        List attributes = item.getAttributes();
        if (attributes != null) {
            result = new Hashtable<String, String>();
            Iterator itr = attributes.iterator();
            while (itr.hasNext()) {
                Attribute attribute = (Attribute) itr.next();
                result.put(attribute.getName(), attribute.getValue()) ;
            }
        }
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
        Document mergedDoc;
        try {
            mergedDoc = new Document();
            mergedDoc.setRootElement((Element) inputData.clone());

            List children = outputData.getContent();

            //iterate through the output vars and add them to the merged doc.
            for (int i = children.size() - 1; i >= 0; i--) {
                Object o = children.get(i);
                if (o instanceof Element) {
                    Element child = (Element) o;
                    child.detach();
                    child.setAttributes(null);
                    //the input data will be removed from the merged doc and
                    //the output data will be added.
                    mergedDoc.getRootElement().removeChild(child.getName());
                    mergedDoc.getRootElement().addContent(child);
                }
            }
        } catch (Exception e) {
            return "";
        }
        return new XMLOutputter().outputString(mergedDoc.getRootElement()).trim();
    }

    
    public static String filterDataAgainstOutputParams(String mergedOutputData,
                                                       List outputParams) throws JDOMException, IOException {
        //build the merged output data document
        SAXBuilder builder = new SAXBuilder();
        Document internalDecompDoc = builder.build(new StringReader(mergedOutputData));

        //set up output document
        String rootElemntName = internalDecompDoc.getRootElement().getName();
        Document outputDoc = new Document();
        Element rootElement = new Element(rootElemntName);
        outputDoc.setRootElement(rootElement);

        Collections.sort(outputParams); //make sure its sorted
        for (Iterator iterator = outputParams.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            String varElementName =
                    parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();

            Element child = internalDecompDoc.getRootElement().getChild(varElementName);
            if (null != child) {
                Element clone = (Element) child.clone();
                outputDoc.getRootElement().addContent(clone);
            }
        }
        return new XMLOutputter().outputString(outputDoc);
    }


}
