/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.unmarshal.YDecompositionParser;


/**
 * 
 * @author Lachlan Aldred
 * Date: 16/02/2004
 * Time: 18:41:17
 * 
 */
public class Marshaller {
	private static Logger logger = Logger.getLogger(Marshaller.class);
    // private static SAXBuilder builder = new SAXBuilder();

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
            
            Element attributes = taskInfo.getChild("attributes");
            if (attributes!=null) {
                List attributelist = attributes.getChildren();
                for (int i = 0; i < attributelist.size(); i++) {
                    Element attribute = (Element) attributelist.get(i);
                    attributemap.put(attribute.getName(),attributes.getChildText(attribute.getName()));
                    logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    logger.debug(attribute.getName() + " " + attributes.getChildText(attribute.getName()));
                    logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
//                String paramOrdering = paramElem.getChildText("ordering");
//                int order = Integer.parseInt(paramOrdering);
//                param.setOrdering(order);
            }
        } catch (JDOMException e) {
        	logger.debug("JDOM Error unmarshalling task information!", e);
        } catch (IOException e) {
            logger.debug("IO Error unmarshalling task information!", e);
        }
        // The following code throws a runtime exception because the code in the try block
        // only declares that it will throw JDOMException and IOException, which are already
        // handled (ie: the only exceptions this catch block will catch are already subclasses
        // of RuntimeException)
        catch( Throwable t ) {
            logger.error("Error unmarshalling task information!", t);
            throw new RuntimeException( t );
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
     * information objects that describe a workflow specification.
     * @param specificationSummaryListXML
     * @return  the list
     */
    public static List<SpecificationData> unmarshalSpecificationSummary(String specificationSummaryListXML) {
        List<SpecificationData> specSummaryList =
                new ArrayList<SpecificationData>();
        Document doc = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(new StringReader(specificationSummaryListXML));
            List specSummaryElements = doc.getRootElement().getChildren();
            for (int i = 0; i < specSummaryElements.size(); i++) {
                SpecificationData specData;
                Element specElement = (Element) specSummaryElements.get(i);
                String specID = specElement.getChildText("id");
                String specName = specElement.getChildText("name");
                String specDodo = specElement.getChildText("documentation");
                String specStatus = specElement.getChildText("status");
                String version = specElement.getChildText("version");
                String rootNetID = specElement.getChildText("rootNetID");
                boolean archived = Boolean.parseBoolean(specElement.getChildText("archived"));
                String loadversion = specElement.getChildText("loadversion");

                if (specID != null && specStatus != null) {
                    specData = new SpecificationData(
                            specID,
                            specName,
                            specDodo,
                            specStatus,
                            version);
                    specData.setArchived(archived);
                    specData.setLoadversion(loadversion);
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
            logger.debug("JDOM Error unmarshalling specification summary!", e);
        } catch (IOException e) {
            logger.debug("IO Error unmarshalling specification summary!", e);
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
            logger.error("JDOM Error unmarshalling specification summary!", e);
        } catch (IOException e) {
            logger.error("IO Error unmarshalling specification summary!", e);
        }
        return workItem;
    }


    public static WorkItemRecord unmarshalWorkItem(Element workItemElement) {
        String status = workItemElement.getChildText("status");
        String caseID = workItemElement.getChildText("caseID");
        String taskID = workItemElement.getChildText("taskID");
        String specID = workItemElement.getChildText("specID");
        String uniqueID = workItemElement.getChildText("uniqueID");
        String enablementTime = workItemElement.getChildText("enablementTime");
        if (caseID != null && taskID != null && specID != null &&
                enablementTime != null && status != null) {
            WorkItemRecord workItem = new WorkItemRecord(
                    caseID, taskID, specID,
                    enablementTime, status);
            String firingTime = workItemElement.getChildText("firingTime");
            workItem.setFiringTime(firingTime);
            String startTime = workItemElement.getChildText("startTime");
            workItem.setStartTime(startTime);
            String user = workItemElement.getChildText("assignedTo");
            workItem.setAssignedTo(user);
            Element data = workItemElement.getChild("data");
            workItem.setUniqueID(uniqueID);
            if (null != data) {
                workItem.setDataList((Element) data.getContent(0));
            }
            return workItem;
        }
        try {
        	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            logger.debug("Error unmarshalling work item! WorkItem XML:\n" + outputter.outputString(workItemElement));
        }
        catch(Throwable t) {
        	logger.debug("Error printing out XML for error message", t);
        }
        throw new IllegalArgumentException("Input element could not be parsed.");
    }


    public static List unmarshalCaseIDs(String casesAsXML) {
        List cases = new ArrayList();
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
            logger.debug("JDOM Error unmarshalling case IDs!", e);
        } catch (IOException e) {
            logger.debug("IO Error unmarshalling case IDs!", e);
        }
        return cases;
    }


    public static String getMergedOutputData(Element inputData, Element outputData) {
        Document mergedDoc = null;
        //Document outputDataDoc = null;
        boolean exception = false;
        try {
            mergedDoc = new Document();//
            mergedDoc.setRootElement((Element) inputData.clone());

            // create a new list containing the contents of the old list, because
            // the call to Content.detach() in the loop will modify the original list
            // and will cause elements to be skipped if you use the original list
            List children = new LinkedList( outputData.getContent() );

            //iterate through the output vars and add them to the merged doc.
            for (int i = 0; i < children.size(); i++) {
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
            logger.debug("Caught exception merging output data", e);
            exception = true;
        }
        if (exception || mergedDoc == null) {
            return "";
        }
        String result = new XMLOutputter().outputString(mergedDoc.getRootElement()).trim();
        logger.debug(result);
        return result;
    }

    /**
     * 
     * Filter output data so that only the elements which are actually
     * in the outputParams list are returned
     * 
     * @param mergedOutputData
     * @param outputParams
     * @return XML String representation of output data
     * @throws JDOMException
     * @throws IOException
     */
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