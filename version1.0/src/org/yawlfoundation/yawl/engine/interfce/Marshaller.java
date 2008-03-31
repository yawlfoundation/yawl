/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;

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
    //private static SAXBuilder builder = new SAXBuilder();

    public static String getOutputParamsInXML(YParametersSchema params, String dataSpaceRootElementNm) {
        StringBuffer result = new StringBuffer();

        result.append("<" + dataSpaceRootElementNm + ">");
        if (params != null) {
            for (Iterator iter = params.getOutputParams().iterator();
                 iter.hasNext();) {
                YParameter param = (YParameter) iter.next();
                result.append(presentParam(param));
            }
        }
        result.append("\n</" + dataSpaceRootElementNm + ">");
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
        String taskID = null;
        String specificationID = null;
        String taskName = null;
        String taskDocumentation = null;
	    HashMap attributemap = new HashMap();
	

        String decompositionID = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doct = builder.build(new StringReader(taskInfoAsXML));
            Element taskInfo = doct.getRootElement();
            taskID = taskInfo.getChildText("taskID");
            specificationID = taskInfo.getChildText("specificationID");
            taskName = taskInfo.getChildText("taskName");
            taskDocumentation = taskInfo.getChildText("taskDocumentation");
            decompositionID = taskInfo.getChildText("decompositionID");
            Element yawlService = taskInfo.getChild("yawlService");

	        Element attributes = taskInfo.getChild("attributes");
    	    if (attributes!=null) {
    		List attributelist = attributes.getChildren();
	    	for (int i = 0; i < attributelist.size(); i++) {
		        Element attribute = (Element) attributelist.get(i);
		        attributemap.put(attribute.getName(),attributes.getChildText(attribute.getName()));
		    }
	    }

            Element params = taskInfo.getChild("params");
            List paramElementsList = params.getChildren();
            for (int i = 0; i < paramElementsList.size(); i++) {
                Element paramElem = (Element) paramElementsList.get(i);
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
                int order = Integer.parseInt(paramOrdering);
                param.setOrdering(order);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TaskInformation taskInfo = new TaskInformation(
                paramsForTaskNCase,
                taskID,
                specificationID,
                taskName,
                taskDocumentation,
                decompositionID);

	taskInfo.setAttributes(attributemap);

        return taskInfo;
    }


    /**
     * Creates a list of SpecificationDatas from formatted XML.
     * These are brief meta data summary
     * information objects that describe a worklfow specification.
     * @param specificationSummaryListXML
     * @return  the list
     */
    public static List unmarshalSpecificationSummary(String specificationSummaryListXML) {
        List specSummaryList = new ArrayList();
        Document doc = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(new StringReader(specificationSummaryListXML));
            List specSummaryElements = doc.getRootElement().getChildren();
            for (int i = 0; i < specSummaryElements.size(); i++) {
                SpecificationData specData = null;
                Element specElement = (Element) specSummaryElements.get(i);
                String specID = specElement.getChildText("id");
                String specName = specElement.getChildText("name");
                String specDoco = specElement.getChildText("documentation");
                String specStatus = specElement.getChildText("status");
                String version = specElement.getChildText("version");
                String rootNetID = specElement.getChildText("rootNetID");

                if (specID != null && specStatus != null) {
                    specData = new SpecificationData(
                            specID,
                            specName,
                            specDoco,
                            specStatus,
                            version);
                    specData.setRootNetID(rootNetID);
                    specSummaryList.add(specData);
                    Element inputParams = specElement.getChild("params");
                    if (inputParams != null) {
                        List paramElements = inputParams.getChildren();
                        for (int j = 0; j < paramElements.size(); j++) {
                            Element paramElem = (Element) paramElements.get(j);
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
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return specSummaryList;
    }


    public static WorkItemRecord unmarshalWorkItem(String workItemXML) {
        WorkItemRecord workItem = null;
        Document doc = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(new StringReader(workItemXML));
            workItem = unmarshalWorkItem(doc.getRootElement());
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workItem;
    }


    public static WorkItemRecord unmarshalWorkItem(Element workItemElement) {

        WorkItemRecord wir = null;
        String status = workItemElement.getChildText("status");
        String caseID = workItemElement.getChildText("caseid");
        String taskID = workItemElement.getChildText("taskid");
        String specID = workItemElement.getChildText("specid");
        String enablementTime = workItemElement.getChildText("enablementTime");
        if (caseID != null && taskID != null && specID != null &&
            enablementTime != null && status != null) {

            wir = new WorkItemRecord(caseID, taskID, specID, enablementTime, status);

            wir.setUniqueID(workItemElement.getChildText("uniqueid"));
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
            wir.setCompletionTimeMs(workItemElement.getChildText("resourceStatus"));
            wir.setStartedBy(workItemElement.getChildText("startedBy"));
            wir.setTag(workItemElement.getChildText("tag"));

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


    public static List<String> unmarshalCaseIDs(String casesAsXML) {
        List<String> cases = new ArrayList<String>();
        try {
            SAXBuilder builder = new SAXBuilder();
            StringReader reader = new StringReader(casesAsXML);
            Element casesElem = builder.build(reader).getRootElement();
            for (Iterator iterator = casesElem.getChildren().iterator(); iterator.hasNext();) {
                Element caseElem = (Element) iterator.next();
                String caseID = caseElem.getText();
                if (caseID != null) {
                    cases.add(caseID);
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cases;
    }


    public static String getMergedOutputData(Element inputData, Element outputData) {
        Document mergedDoc;
        try {
            mergedDoc = new Document();//
            mergedDoc.setRootElement((Element) inputData.clone());

            List children = outputData.getContent();

            //iterate through the output vars and add them to the merged doc.
            for (int i = children.size() - 1; i >= 0; i--) {
                Object o = children.get(i);
                if (o instanceof Element) {
                    Element child = (Element) o;
                    child.detach();
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
