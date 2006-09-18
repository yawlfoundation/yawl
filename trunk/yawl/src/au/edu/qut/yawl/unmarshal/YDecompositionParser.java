/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.unmarshal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import au.edu.qut.yawl.elements.ExtensionListContainer;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

/**
 * Builds decomposition objects from XML doclets.
 *
 *
 * 
 * @author Lachlan Aldred
 * 
 */
public class YDecompositionParser {
    private Element _decompElem;
    private Postset _postsetIDs;
    private Map _removeSetIDs;
    private Namespace _yawlNS;
    private YDecomposition _decomposition;
    private Map _decomposesToIDs;
    private YSpecificationParser _specificationParser;
    Map _removeSetForFlows = new HashMap();
    private String _version;

    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String SKIP_OUTBOUND_VALIDATION = "skipOutboundSchemaValidation";


    /**
     * gets the built decomposition.
     * @return the decomposition
     */
    YDecomposition getDecomposition() {
        return _decomposition;
    }


    /**
     * Parses the decomposition from the appropriate XML doclet.
     * @param decompElem the XML doclet containing the decomp configuration.
     * @param specifcationParser a reference to parent spec parser.
     * @param version the version of XML structure
     */
    YDecompositionParser(Element decompElem, YSpecificationParser specifcationParser, String version) {
        _decompElem = decompElem;
        _yawlNS = decompElem.getNamespace();
        _specificationParser = specifcationParser;
        _version = version;
        _postsetIDs = new Postset();
        _removeSetIDs = new HashMap();
        _decomposesToIDs = new HashMap();
        _decomposition = createDecomposition(decompElem);
        _postsetIDs.addImplicitConditions();
        linkElements();
    }


    //done
    private YDecomposition createDecomposition(Element decompElem) {
        Namespace schemaInstanceNS = decompElem.getNamespace("xsi");
        String xsiType = _decompElem.getAttributeValue("type", schemaInstanceNS);
        String id = _decompElem.getAttributeValue("id");

        String elementName = _decompElem.getName();
        if ("NetFactsType".equals(xsiType) || "rootNet".equals(elementName)) {
            _decomposition = new YNet(id, _specificationParser.getSpecification());
            parseNet((YNet) _decomposition, decompElem);
        } else if ("WebServiceGatewayFactsType".equals(xsiType)) {
            _decomposition = new YAWLServiceGateway(id, _specificationParser.getSpecification());
            parseWebServiceGateway(
                    (YAWLServiceGateway) _decomposition,
                    decompElem);
        }
        List<Element> extensions = _decompElem.getChildren(ExtensionListContainer.ELEMENT_NAME, _yawlNS);
        _decomposition.setInternalExtensions(extensions);
        /**
         * AJH: Added to support XML attribute pass-thru from specification into task output data doclet.
         * Load element attributes
         */
        Iterator iter = decompElem.getAttributes().iterator();
        while(iter.hasNext())
        {
            Attribute attr = (Attribute)iter.next();
            String attname = attr.getName();
            boolean isXsiNS = attr.getNamespace() == schemaInstanceNS;
            //don't add the standard YAWL schema attributes to the pass through list.
            if(!(ID.equals(attname) || (TYPE.equals(attname) && isXsiNS))) {
                _decomposition.setAttribute(attname, attr.getValue());
            }
//            if(SKIP_OUTBOUND_VALIDATION.equals(attname)) {
//                _decomposition.setSkipOutboundSchemaChecks(
//                        Boolean.getBoolean(attr.getValue()));
//            }
        }
        
        parseDecompositionRoles(_decomposition, decompElem);
        return _decomposition;
    }

    //done
    private void parseDecompositionRoles(YDecomposition decomposition, Element decompElem) {
    	String name = decompElem.getChildText("name", _yawlNS);
        if (name != null) {
            decomposition.setName(name);
        }
        String documentation = decompElem.getChildText("documentation", _yawlNS);
        if (documentation != null) {
            decomposition.setDocumentation(documentation);
        }
        List inputParamElems = decompElem.getChildren("inputParam", _yawlNS);
        for (int i = 0; i < inputParamElems.size(); i++) {
            Element inputParamElem = (Element) inputParamElems.get(i);
            YParameter yparameter = new YParameter(_decomposition, YParameter._INPUT_PARAM_TYPE);
//            yparameter.setOrdering(i);
            parseParameter(inputParamElem, yparameter, _yawlNS, isBeta2Version());
            decomposition.setInputParam(yparameter);
        }
        List outputParamElems = decompElem.getChildren("outputParam", _yawlNS);
        for (int i = 0; i < outputParamElems.size(); i++) {
            Element outputParamElem = (Element) outputParamElems.get(i);
            YParameter yparameter = new YParameter(_decomposition, YParameter._OUTPUT_PARAM_TYPE);
//            yparameter.setOrdering(i);
            parseParameter(outputParamElem, yparameter, _yawlNS, isBeta2Version());
            decomposition.setOutputParameter(yparameter);
        }
//        List outputExpressions = decompElem.getChildren("outputExpression", _yawlNS);
//        for (int i = 0; i < outputExpressions.size(); i++) {
//            Element outputExpressionElem = (Element) outputExpressions.get(i);
//            String outputExpressionText = outputExpressionElem.getAttributeValue("query");
//            decomposition.setOutputExpression(outputExpressionText);
//        }
    }

    //done
    private void parseWebServiceGateway(YAWLServiceGateway webServiceGateway, Element decompositionElem) {

        List yawlServiceElems = decompositionElem.getChildren("yawlService", _yawlNS);
        for (int i = 0; i < yawlServiceElems.size(); i++) {

            Element yawlServiceElem = (Element) yawlServiceElems.get(i);
            String yawlServiceID = yawlServiceElem.getAttributeValue("id");
            YAWLServiceReference yawlService = new YAWLServiceReference(yawlServiceID, webServiceGateway);
            webServiceGateway.setYawlService(yawlService);
        }
        List enablementParamElems = decompositionElem.getChildren("enablementParam", _yawlNS);
        for (int i = 0; i < enablementParamElems.size(); i++) {
            Element enablementParamElem = (Element) enablementParamElems.get(i);
            YParameter parameter = new YParameter(_decomposition, YParameter._ENABLEMENT_PARAM_TYPE);
//            parameter.setOrdering(i);
            parseParameter(enablementParamElem, parameter, _yawlNS, isBeta2Version());
            webServiceGateway.setEnablementParameter(parameter);
        }
    }


    private void parseProcessControlElements(YNet aNet, Element processControlElementsElem) {
        Element inputConditionElem = processControlElementsElem.getChild("inputCondition", _yawlNS);
        aNet.setInputCondition((YInputCondition) parseCondition(aNet, inputConditionElem));
        Element outputConditionElem = processControlElementsElem.getChild("outputCondition", _yawlNS);
        aNet.setOutputCondition((YOutputCondition) parseCondition(aNet, outputConditionElem));
        List taskElems = processControlElementsElem.getChildren("task", _yawlNS);

        for (int i = 0; i < taskElems.size(); i++) {
            Element taskElem = (Element) taskElems.get(i);
            aNet.addNetElement(parseTask(taskElem));
        }
        List conditionElems = processControlElementsElem.getChildren("condition", _yawlNS);
        for (int i = 0; i < conditionElems.size(); i++) {
            Element element = (Element) conditionElems.get(i);
            aNet.addNetElement(parseCondition(aNet, element));
        }
    }


    private YTask parseTask(Element taskElem) {
        short[] splitJoinTypes = parseSplitJoinTypes(taskElem);

        List<Element> extensions = taskElem.getChildren(ExtensionListContainer.ELEMENT_NAME, _yawlNS);
        Element decomposesToRefElem = taskElem.getChild("decomposesTo", _yawlNS);
        String decomposesToID = null;
        if (decomposesToRefElem != null) {
            decomposesToID = decomposesToRefElem.getAttributeValue("id");
        }
        String taskID = taskElem.getAttributeValue("id");
        String decompositionType = _specificationParser.getDecompositionType(decomposesToID);
        YTask task = null;
        if ("NetFactsType".equals(decompositionType)) {
            task = new YCompositeTask(
                    taskID,
                    splitJoinTypes[1], splitJoinTypes[0],
                    (YNet) _decomposition);
        } else if ("WebServiceGatewayFactsType".equals(decompositionType) || decomposesToID == null) {
            task = new YAtomicTask(
                    taskID,
                    splitJoinTypes[1], splitJoinTypes[0],
                    (YNet) _decomposition);
        }
        if (decomposesToID != null) {
            _decomposesToIDs.put(task, decomposesToID);
        }
        parseExternalTaskRoles(taskElem, task);
        parseNameAndDocumentation(task, taskElem);
        task.setInternalExtensions(extensions);
        return task;
    }


    private short[] parseSplitJoinTypes(Element externalTaskElem) {
        String[] xmlSplitJoinCodes = new String[2];
        xmlSplitJoinCodes[0] = externalTaskElem.getChild("split", _yawlNS).getAttributeValue("code");
        xmlSplitJoinCodes[1] = externalTaskElem.getChild("join", _yawlNS).getAttributeValue("code");
        short[] splitJoinTypes = new short[2];
        for (int i = 0; i < splitJoinTypes.length; i++) {
            if (xmlSplitJoinCodes[i].equalsIgnoreCase("and")) {
                splitJoinTypes[i] = YTask._AND;
            } else if (xmlSplitJoinCodes[i].equalsIgnoreCase("or")) {
                splitJoinTypes[i] = YTask._OR;
            } else if (xmlSplitJoinCodes[i].equalsIgnoreCase("xor")) {
                splitJoinTypes[i] = YTask._XOR;
            }
        }
        return splitJoinTypes;
    }


    private void parseExternalTaskRoles(Element externalTaskElem, YTask externalTask) {
        List removeSet = parseRemoveSet(externalTaskElem);
        _removeSetIDs.put(externalTask, removeSet);
        List removeSetForFlows = parseRemoveSetFromFlow(externalTaskElem);
        _removeSetForFlows.put(externalTask, removeSetForFlows);
        List postsetElements = parsePostset(externalTaskElem);
        _postsetIDs.add(externalTask.getID(), postsetElements);
        Element startingMappings = externalTaskElem.getChild("startingMappings", _yawlNS);
        if (startingMappings != null) {
            externalTask.setDataMappingsForTaskStarting(parseMappings(startingMappings, true));
        }
        Element completedMappings = externalTaskElem.getChild("completedMappings", _yawlNS);
        if (completedMappings != null) {
            externalTask.setDataMappingsForTaskCompletion(parseMappings(completedMappings, false));
        }
        Element enablementMappings = externalTaskElem.getChild("enablementMappings", _yawlNS);
        if (enablementMappings != null && externalTask instanceof YAtomicTask) {
            ((YAtomicTask) externalTask).setDataMappingsForEnablement(parseMappings(enablementMappings, true));
        }
        String taskType = externalTaskElem.getAttributeValue("type",
                externalTaskElem.getNamespace("xsi"));
        if ("MultipleInstanceExternalTaskFactsType".equals(taskType)) {
            externalTask.setUpMultipleInstanceAttributes(
                    externalTaskElem.getChildText("minimum", _yawlNS),
                    externalTaskElem.getChildText("maximum", _yawlNS),
                    externalTaskElem.getChildText("threshold", _yawlNS),
                    externalTaskElem.getChild("creationMode", _yawlNS).getAttributeValue("code")
            );
            String expression = externalTaskElem.getChild("miDataInput", _yawlNS)
                    .getChild("expression", _yawlNS).getAttributeValue("query");
            String processingExpressionForInput = externalTaskElem.getChild("miDataInput", _yawlNS)
                    .getChild("splittingExpression", _yawlNS).getAttributeValue("query");
            String remoteVarName = externalTaskElem.getChild("miDataInput", _yawlNS)
                    .getChildText("formalInputParam", _yawlNS);
            externalTask.setDataBindingForInputParam(expression, remoteVarName);
            externalTask.setMultiInstanceInputDataMappings(remoteVarName, processingExpressionForInput);
            Element miDataOutputElem = externalTaskElem.getChild("miDataOutput", _yawlNS);
            if (miDataOutputElem != null) {
                String remoteExpressionName = miDataOutputElem
                        .getChild("formalOutputExpression", _yawlNS).getAttributeValue("query");
                String aggregationQuery = miDataOutputElem
                        .getChild("outputJoiningExpression", _yawlNS).getAttributeValue("query");
                String localVarName = miDataOutputElem
                        .getChildText("resultAppliedToLocalVariable", _yawlNS);
                externalTask.setDataBindingForOutputExpression(remoteExpressionName, localVarName);
                externalTask.setMultiInstanceOutputDataMappings(remoteExpressionName, aggregationQuery);
            }
        }
    }


    private List parseRemoveSetFromFlow(Element externalTaskElem) {
        List removeSetForFlows = externalTaskElem.getChildren("removesTokensFromFlow", _yawlNS);
        List removeIDs = new Vector();
        for (int i = 0; i < removeSetForFlows.size(); i++) {
            Element removeSetForFlowsElem = (Element) removeSetForFlows.get(i);
            removeIDs.add(removeSetForFlowsElem);
        }
        return removeIDs;
    }


    private Map parseMappings(Element mappingsElem, boolean isStartMappings) {
        Map map = new TreeMap();
        List mappings = mappingsElem.getChildren();
        for (int i = 0; i < mappings.size(); i++) {
            Element mapping = (Element) mappings.get(i);
            String expression = mapping.getChild("expression", _yawlNS).getAttributeValue("query");
            String varOrParam = mapping.getChildText("mapsTo", _yawlNS);
            if (isStartMappings) {
                map.put(varOrParam, expression);
            } else {
                map.put(expression, varOrParam);
            }
        }
        return map;
    }


    private List parseRemoveSet(Element externalTaskElem) {
        List removeSetElems = externalTaskElem.getChildren("removesTokens", _yawlNS);
        List removeIDs = new Vector();
        for (int i = 0; i < removeSetElems.size(); i++) {
            removeIDs.add(((Element) removeSetElems.get(i)).getAttributeValue("id"));
        }
        return removeIDs;
    }


    //done
    private YCondition parseCondition(YNet aNet, Element conditionElem) {
        YCondition condition = null;
        String conditionType = conditionElem.getName();
        String id = conditionElem.getAttributeValue("id");
        String label = conditionElem.getChildText("label");
        

        if ("inputCondition".equals(conditionType)) {
            if (label != null) {
                condition = new YInputCondition(id, label, aNet);
            } else {
                condition = new YInputCondition(id, aNet);
            }
        } else if ("outputCondition".equals(conditionType)) {
            if (label != null) {
                condition = new YOutputCondition(id, label, aNet);
            } else {
                condition = new YOutputCondition(id, aNet);
            }
        } else if ("condition".equals(conditionType)) {
            if (label != null) {
                condition = new YCondition(id, label, aNet);
            } else {
                condition = new YCondition(id, aNet);
            }
        }
        List<Element> extensions = conditionElem.getChildren(ExtensionListContainer.ELEMENT_NAME, _yawlNS);
        condition.setInternalExtensions(extensions);
        List postsetElements = parsePostset(conditionElem);
        parseNameAndDocumentation(condition, conditionElem);
        this._postsetIDs.add(condition.getID(), postsetElements);
        return condition;
    }


    private void parseNameAndDocumentation(YExternalNetElement element, Element netElementElem) {
        String name = netElementElem.getChildText("name", _yawlNS);
        element.setName(name);
        String documentation = netElementElem.getChildText("documentation", _yawlNS);
        element.setDocumentation(documentation);
    }


    private List parsePostset(Element netElementElem) {
        List postsetFlowStructs = new Vector();
        List flowsIntoElems = netElementElem.getChildren("flowsInto", _yawlNS);
        for (int i = 0; i < flowsIntoElems.size(); i++) {
            Element flowsIntoElem = (Element) flowsIntoElems.get(i);
            List<Element> extensions = flowsIntoElem.getChildren(ExtensionListContainer.ELEMENT_NAME, _yawlNS);
            String nextElementRef = flowsIntoElem.getChild("nextElementRef", _yawlNS).getAttributeValue("id");

            /**
             * AJH: Store label
             */
            String documentation = null;
            Element documentationEl = flowsIntoElem.getChild("documentation", _yawlNS);
            if (documentationEl != null)
            {
                documentation = documentationEl.getText();
            }

            FlowStruct flowStruct = new FlowStruct(nextElementRef, flowsIntoElem.getChildText("predicate"), documentation);
            String predicateString = flowsIntoElem.getChildText("predicate", _yawlNS);
            if (predicateString != null) {
                flowStruct._flowPredicate = predicateString;
                String predicateOrderingStr = flowsIntoElem.getChild("predicate", _yawlNS).
                        getAttributeValue("ordering");
                if (predicateOrderingStr != null) {
                    flowStruct._predicateOrdering = new Integer(predicateOrderingStr);
                }
            }
            Element isDefaultFlow = flowsIntoElem.getChild("isDefaultFlow", _yawlNS);
            if (isDefaultFlow != null) {
                flowStruct._isDefaultFlow = true;
            }
            postsetFlowStructs.add(flowStruct);
        }
        return postsetFlowStructs;
    }


    private YNet parseNet(YNet net, Element netElem) {
        if (!isBeta2Version()) {
            String isRootNet = netElem.getAttributeValue("isRootNet");
            if ("true".equals(isRootNet)) {
                _specificationParser.getSpecification().setRootNet(net);
            }
        }
        List localVariables = netElem.getChildren("localVariable", _yawlNS);
        for (int i = 0; i < localVariables.size(); i++) {
            Element localVariableElem = (Element) localVariables.get(i);
            YVariable localVar = new YVariable(_decomposition);
            parseLocalVariable(localVariableElem, localVar, _yawlNS, isBeta2Version());
            net.setLocalVariable(localVar);
        }
        parseProcessControlElements(net, netElem.getChild("processControlElements", _yawlNS));
        return net;
    }


    /**
     * Adds variable specific information to the YParameter argument (in &amp; out var).
     * @param localVariableElem
     * @param variable
     */
    public static void parseLocalVariable(Element localVariableElem, YVariable variable, Namespace ns, boolean version2) {
        //parse & set the initial value
        String initialValue = localVariableElem.getChildText("initialValue", ns);
        variable.setInitialValue(initialValue);

        String name;
        String dataType;
        String namespace = null;
        //if version is beta 2 vars are stored in different format
        if (version2) {
            name = localVariableElem.getAttributeValue("name");
            dataType = localVariableElem.getChildText("type", ns);
            if (null != dataType) {
                namespace = dataType.startsWith("xs")
                        ? "http://www.w3.org/2001/XMLSchema" :
                        null;
                //if data type is of QName form eliminate the first bit as it is useless for
                //version 2 anyway
                if (dataType.indexOf(':') != -1) {
                    dataType = dataType.substring(dataType.indexOf(':') + 1);
                }
            }
            if (null == namespace) {
                variable.setUntyped(true);
            }
        } else {//must be version 3 or greater
            name = localVariableElem.getChildText("name", ns);
            dataType = localVariableElem.getChildText("type", ns);
            namespace = localVariableElem.getChildText("namespace", ns);

            //check to see if the variable is untyped
            boolean isUntyped = localVariableElem.getChild("isUntyped", ns) != null;
            variable.setUntyped(isUntyped);

            //set the element name if it uses one
            String elementName;
            elementName = localVariableElem.getChildText("element", ns);
            variable.setElementName(elementName);
        }
        //the variable either is data typed xor linked to an element declaration
        variable.setDataTypeAndName(dataType, name, namespace);

    }


    /**
     * @return whether this is a a beta 2 specification version or not.
     */
    private boolean isBeta2Version() {
        return YSpecification._Beta2.equals(_version);
    }


    /**
     * Adds parameter specific information to the YParameter argument (in &amp; out var).
     * @param paramElem the XML data
     * @param parameter the in &amp; out var.
     */
    public static void parseParameter(Element paramElem, YParameter parameter, Namespace ns, boolean version2) {
        parseLocalVariable(paramElem, parameter, ns, version2);

        boolean isCutThroughParam = paramElem.getChild("isCutThroughParam", ns) != null;
        if (isCutThroughParam) {
            parameter.setIsCutThroughParam(isCutThroughParam);
        }

        boolean mandatory = paramElem.getChild("mandatory", ns) != null;
        parameter.setManadatory(mandatory);


        /**
         * Store any attributes defined against the parameter
         */
        List attrs = paramElem.getAttributes();
        for(int i=0; i< attrs.size(); i++)
        {
            Attribute attr = (Attribute)attrs.get(i);
            parameter.addAttribute(attr.getName(), attr.getValue());
        }        
    }


    //#################################################################################################
    //                                         ACCESSOR
    //#################################################################################################




    //#################################################################################################
    //                                        Things I NEED TO REUSE
    //#################################################################################################
    protected Map getDecomposesToIDs() {
        return _decomposesToIDs;
    }


    private void linkElements() {
        if (_decomposition instanceof YNet) {
            YNet decomposition = (YNet) _decomposition;
            Iterator netElementIterator = decomposition.getNetElements().iterator();
            while (netElementIterator.hasNext()) {
                YExternalNetElement currentNetElement =
                        (YExternalNetElement) netElementIterator.next();
                List postsetIDs = _postsetIDs.getQPostset(currentNetElement.getID());
                if (postsetIDs != null) {
                    Iterator postsetIDsIter = postsetIDs.iterator();
                    while (postsetIDsIter.hasNext()) {
                        FlowStruct flowStruct = (FlowStruct) postsetIDsIter.next();
                        YExternalNetElement nextElement = decomposition.getNetElement(flowStruct._flowInto);
                        YFlow flow = new YFlow(currentNetElement, nextElement);
                        flow.setEvalOrdering(flowStruct._predicateOrdering);
                        flow.setDefaultFlow(flowStruct._isDefaultFlow);
                        flow.setXpathPredicate(flowStruct._flowPredicate);
                        flow.setInternalExtensions(flowStruct.extensions);
                        /**
                         * AJH: Added to support flow/link labels
                         */
                        flow.setDocumentation(flowStruct._label);
                        currentNetElement.setPostset(flow);
                    }
                }
            }
            Iterator removeSetIter = _removeSetIDs.keySet().iterator();
            while (removeSetIter.hasNext()) {
                YTask externalTask = (YTask) removeSetIter.next();
                Iterator removeSetForTaskIter = ((List) _removeSetIDs.get(externalTask)).iterator();
                List removeSetObjects = new Vector();
                while (removeSetForTaskIter.hasNext()) {
                    removeSetObjects.add(decomposition.getNetElement((String) removeSetForTaskIter.next()));
                }
                if (removeSetObjects.size() > 0) {
                    externalTask.setRemovesTokensFrom(removeSetObjects);
                }
            }
            Iterator removeSetFromFlowsIter = _removeSetForFlows.keySet().iterator();
            while (removeSetFromFlowsIter.hasNext()) {
                YTask taskWithRemoveSet = (YTask) removeSetFromFlowsIter.next();
                List removesTokensFromFlow = (List) _removeSetForFlows.get(taskWithRemoveSet);
                for (int i = 0; i < removesTokensFromFlow.size(); i++) {
                    Element removesTokensFromFlowElem = (Element) removesTokensFromFlow.get(i);
                    Element flowSourceElem = removesTokensFromFlowElem.getChild("flowSource", _yawlNS);
                    Element flowDestElem = removesTokensFromFlowElem.getChild("flowDestination", _yawlNS);
                    String flowSourceID = flowSourceElem.getAttributeValue("id");
                    String flowDestID = flowDestElem.getAttributeValue("id");
                    YExternalNetElement flowSource = decomposition.getNetElement(flowSourceID);
                    YExternalNetElement flowDest = decomposition.getNetElement(flowDestID);
                    for (Iterator iterator = flowSource.getPostsetElements().iterator(); iterator.hasNext();) {
                        YCondition maybeImplicit = (YCondition) iterator.next();
                        if (maybeImplicit.isImplicit()) {
                            List<YExternalNetElement> oneTaskInThisList = maybeImplicit.getPostsetElements();
                            YTask taskAfterImplicit = (YTask) oneTaskInThisList.iterator().next();
                            if (taskAfterImplicit.equals(flowDest)) {
                                List additionalRemoves = new ArrayList();
                                additionalRemoves.add(maybeImplicit);
                                taskWithRemoveSet.setRemovesTokensFrom(additionalRemoves);
                            }
                        }
                    }
                }
            }
        }
    }


    private class Postset {
        //YNet _parentDecomposition;
        Map _postsetMap = new HashMap(); // maps a String ID to a List of FlowStruct objects.


        public void add(String id, List postsetStruct) {
            List oldRefIDs = (List) _postsetMap.get(id);
            if (oldRefIDs == null) {
                oldRefIDs = new Vector();
            }
            oldRefIDs.addAll(postsetStruct);
            _postsetMap.put(id, oldRefIDs);
        }


        public List getQPostset(String id) {
            return (List) _postsetMap.get(id);
        }


        public List getPostset(List ids) {
            List postset = new Vector();
            Iterator idIter = ids.iterator();
            while (idIter.hasNext()) {
                postset.addAll((List) _postsetMap.get(idIter.next()));
            }
            return postset;
        }


        public void addImplicitConditions() {
            List elementIDs = new Vector(_postsetMap.keySet());
            for (int i = 0; i < elementIDs.size(); i++) {
                String currentElementID = (String) elementIDs.get(i);
                YExternalNetElement currentNetElement = ((YNet) _decomposition).getNetElement(currentElementID);
                if (currentNetElement instanceof YTask) {
                    List nextElementFlowStructs =
                            ((List) this._postsetMap.get(currentElementID));
                    for (int j = 0; j < nextElementFlowStructs.size(); j++) {
                        FlowStruct flowStruct = (FlowStruct) nextElementFlowStructs.get(j);
                        YExternalNetElement netElement = ((YNet) _decomposition).getNetElement(flowStruct._flowInto);

                        if (netElement instanceof YTask)
                        /*in other words if one task flows to another task then we create an
                        anonymous condition and insert it between the two tasks. */ {
                            String anonID = "c{" + currentNetElement.getID() + "_" + netElement.getID() + "}";
                            YCondition condition =
                                    new YCondition(anonID, (YNet) _decomposition);
                            condition.setImplicit(true);
                            ((YNet) _decomposition).addNetElement(condition);
                            String tempElemID = flowStruct._flowInto;
                            flowStruct._flowInto = condition.getID();
                            List dom = new Vector();
                            dom.add(new FlowStruct(tempElemID, null, null));
                            _postsetMap.put(condition.getID(), dom);
                        }
                    }
                }
            }
        }
    }


    private static class FlowStruct {
        String _flowInto;
        String _flowPredicate;
        Integer _predicateOrdering;
        List<Element> extensions;
        boolean _isDefaultFlow;

        /**
         * AJH: Label added
         */
        String _label;

        /**
         * AJH: LAbel added
         */
        public FlowStruct(String flowInto, String flowPredicate, String label) {
            this._flowInto = flowInto;
            this._flowPredicate = flowPredicate;
            this._label = label;
        }
    }
}
