/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.unmarshal;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Builds decomposition objects from XML doclets.
 *
 * @author Lachlan Aldred
 * @author Michael Adams refactored for version 2
 */
public class YDecompositionParser {
    private Element _decompElem;
    private Postset _postsetIDs;
    private Map<YTask, List<String>> _removeSetIDs;
    private Namespace _yawlNS;
    private YDecomposition _decomposition;
    private Map<YTask, String> _decomposesToIDs;
    private YSpecificationParser _specificationParser;
    Map<YTask, List<Element>> _removeSetForFlows = new HashMap<YTask, List<Element>>();
    private String _version;



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
     * @param specificationParser a reference to parent spec parser.
     * @param version the version of XML structure
     */
    YDecompositionParser(Element decompElem, YSpecificationParser specificationParser, String version) {
        _decompElem = decompElem;
        _yawlNS = decompElem.getNamespace();
        _specificationParser = specificationParser;
        _version = version;
        _postsetIDs = new Postset();
        _removeSetIDs = new HashMap<YTask, List<String>>();
        _decomposesToIDs = new HashMap<YTask, String>();
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
        }
        else if ("WebServiceGatewayFactsType".equals(xsiType)) {
            _decomposition = new YAWLServiceGateway(id, _specificationParser.getSpecification());
            parseWebServiceGateway((YAWLServiceGateway) _decomposition, decompElem);
        }
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
            if(!("id".equals(attname) || ("type".equals(attname) && isXsiNS))) {
                _decomposition.setAttribute(attr.getName(), attr.getValue());
            }
        }
        
        parseDecompositionRoles(_decomposition, decompElem);
        _decomposition.setLogPredicate(parseLogPredicate(decompElem, _yawlNS));

        // added for resourcing
        parseExternalInteraction(_decomposition, decompElem);
        parseCodelet(_decomposition, decompElem);

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
            yparameter.setOrdering(i);
            parseParameter(inputParamElem, yparameter, _yawlNS, isBeta2Version());
            decomposition.addInputParameter(yparameter);
        }
        List outputParamElems = decompElem.getChildren("outputParam", _yawlNS);
        for (int i = 0; i < outputParamElems.size(); i++) {
            Element outputParamElem = (Element) outputParamElems.get(i);
            YParameter yparameter = new YParameter(_decomposition, YParameter._OUTPUT_PARAM_TYPE);
            yparameter.setOrdering(i);
            parseParameter(outputParamElem, yparameter, _yawlNS, isBeta2Version());
            decomposition.addOutputParameter(yparameter);
        }
        List outputExpressions = decompElem.getChildren("outputExpression", _yawlNS);
        for (int i = 0; i < outputExpressions.size(); i++) {
            Element outputExpressionElem = (Element) outputExpressions.get(i);
            String outputExpressionText = outputExpressionElem.getAttributeValue("query");
            decomposition.setOutputExpression(outputExpressionText);
        }
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
            parameter.setOrdering(i);
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

        if ((task != null) && (! _version.startsWith("Beta"))) {
            task.setResourcingSpecs(taskElem.getChild("resourcing", _yawlNS));
            parseTimerParameters(task, taskElem) ;
            parseCustomFormURL(task, taskElem) ;
            parseConfiguration(task, taskElem);
        }
        
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
        List<String> removeSet = parseRemoveSet(externalTaskElem);
        _removeSetIDs.put(externalTask, removeSet);
        List<Element> removeSetForFlows = parseRemoveSetFromFlow(externalTaskElem);
        _removeSetForFlows.put(externalTask, removeSetForFlows);
        List postsetElements = parsePostset(externalTaskElem);
        this._postsetIDs.add(externalTask.getID(), postsetElements);
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


    private List<Element> parseRemoveSetFromFlow(Element externalTaskElem) {
        List removeSetForFlows = externalTaskElem.getChildren("removesTokensFromFlow", _yawlNS);
        List<Element> removeIDs = new Vector<Element>();
        if (removeSetForFlows != null) {
            for (Object o : removeSetForFlows) {
                removeIDs.add((Element) o);
            }
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
        List<String> removeIDs = new Vector<String>();
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
        if ("condition".equals(conditionType)) {
            String label = conditionElem.getChildText("label");
            if (label != null) {
                condition = new YCondition(id, label, aNet);
            } else {
                condition = new YCondition(id, aNet);
            }
        }
        else if ("inputCondition".equals(conditionType)) {
            String name = conditionElem.getChildText("name");
            if (name != null) {
                condition = new YInputCondition(id, name, aNet);
            } else {
                condition = new YInputCondition(id, aNet);
            }
        }
        else if ("outputCondition".equals(conditionType)) {
            String name = conditionElem.getChildText("name");
            if (name != null) {
                condition = new YOutputCondition(id, name, aNet);
            } else {
                condition = new YOutputCondition(id, aNet);
            }
        }
        List postsetElements = parsePostset(conditionElem);
        parseNameAndDocumentation(condition, conditionElem);
        this._postsetIDs.add(condition.getID(), postsetElements);
        return condition;
    }


    private void parseNameAndDocumentation(YExternalNetElement element, Element netElementElem) {
        String name = netElementElem.getChildText("name", _yawlNS);
        if (name == null && (element instanceof YTask)) {
            String id = netElementElem.getAttributeValue("id");
            name = stripEngineID(id);
        }
        element.setName(name);
        String documentation = netElementElem.getChildText("documentation", _yawlNS);
        element.setDocumentation(documentation);
    }


    private void parseCustomFormURL(YTask task, Element taskElem) {
        String formStr = taskElem.getChildText("customForm", _yawlNS);
        if (formStr != null) {
            try {
                URL formURI = new URL(formStr) ;
                task.setCustomFormURI(formURI);
            }
            catch (MalformedURLException use) {
                Logger.getLogger(this.getClass()).error(
                    "Invalid custom form URL in specification for task " + task.getID() +
                    ": " + formStr + ". Custom form URL will be ignored.");
            }
        }
    }

    private void parseConfiguration(YTask task, Element taskElem) {
         Element configure =  taskElem.getChild("configuration", _yawlNS);
        if (configure != null) {
           task.setConfigurationElement(taskElem);
        }
    }


    private void parseTimerParameters(YTask task, Element taskElem) {
        Element timerElem = taskElem.getChild("timer", _yawlNS);
        if (timerElem != null) {
            String netParam = timerElem.getChildText("netparam", _yawlNS) ;

            // net-level param holds values at runtime
            if (netParam != null)
                task.setTimerParameters(netParam);
            else {
                // get the triggering event
                String triggerStr = timerElem.getChildText("trigger", _yawlNS) ;
                YWorkItemTimer.Trigger trigger = YWorkItemTimer.Trigger.valueOf(triggerStr) ;

                // expiry is a stringified long value representing a specific datetime
                String expiry = timerElem.getChildText("expiry", _yawlNS) ;
                if (expiry != null)
                    task.setTimerParameters(trigger, new Date(new Long(expiry)));
                else {
                    // duration type - specified as a Duration?
                    String durationStr = timerElem.getChildText("duration", _yawlNS);
                    if (durationStr != null) {
                        try {
                            Duration duration = DatatypeFactory.newInstance()
                                                               .newDuration(durationStr) ;
                            task.setTimerParameters(trigger, duration);
                        }
                        catch (DatatypeConfigurationException dce) {
                            // nothing to do - timer won't be set
                        }
                    }
                    else {
                        // ticks / interval durationparams type
                        Element durationElem = timerElem.getChild("durationparams", _yawlNS);
                        String tickStr = durationElem.getChildText("ticks", _yawlNS);
                        String intervalStr = durationElem.getChildText("interval", _yawlNS);
                        YTimer.TimeUnit interval = YTimer.TimeUnit.valueOf(intervalStr);
                        task.setTimerParameters(trigger, new Long(tickStr), interval);
                    }
                }
            }
        }
    }


    private List parsePostset(Element netElementElem) {
        List postsetFlowStructs = new Vector();
        List flowsIntoElems = netElementElem.getChildren("flowsInto", _yawlNS);
        for (int i = 0; i < flowsIntoElems.size(); i++) {
            Element flowsIntoElem = (Element) flowsIntoElems.get(i);
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

                // set external data gateway (since 2.1)
                String gateway = netElem.getChildText("externalDataGateway", _yawlNS);
                if (gateway != null) net.setExternalDataGateway(gateway);
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

            // set ordering (if schema 2.1 or later)
            String orderingStr = localVariableElem.getChildText("index", ns);
            if (StringUtil.isIntegerString(orderingStr)) {
                variable.setOrdering(new Integer(orderingStr));
            }


        }
        //the variable either is data typed xor linked to an element declaration
        variable.setDataTypeAndName(dataType, name, namespace);
    }


    /**
     * @return whether this is a a beta 2 specification version or not.
     */
    private boolean isBeta2Version() {
        return YSpecification.Beta2.equals(_version);
    }


    /**
     * Adds parameter specific information to the YParameter argument (in &amp; out var).
     * @param paramElem the XML data
     * @param parameter the in &amp; out var.
     */
    public static void parseParameter(Element paramElem, YParameter parameter, Namespace ns, boolean version2) {
        parseLocalVariable(paramElem, parameter, ns, version2);

        String orderStr = paramElem.getChildText("ordering");
        if ((orderStr != null) && (StringUtil.isIntegerString(orderStr))) 
            parameter.setOrdering(Integer.parseInt(orderStr));
        
        boolean isCutThroughParam = paramElem.getChild("isCutThroughParam", ns) != null;
        if (isCutThroughParam) {
            parameter.setIsCutThroughParam(isCutThroughParam);
        }

        parameter.setMandatory(paramElem.getChild("mandatory", ns) != null);

        String defaultValue = paramElem.getChildText("defaultValue", ns);
        if (defaultValue != null) parameter.setDefaultValue(defaultValue);

        parameter.setLogPredicate(parseLogPredicate(paramElem, ns));
        parameter.getAttributes().fromJDOM(paramElem.getAttributes());
    }


    private void parseExternalInteraction(YDecomposition decomposition,
                                           Element decompElem) {
         String interactionStr = decompElem.getChildText("externalInteraction", _yawlNS);
         if (interactionStr != null)
             decomposition.setExternalInteraction(
                           interactionStr.equalsIgnoreCase("manual"));
    }


    private void parseCodelet(YDecomposition decomposition, Element decompElem){
        String codelet = decompElem.getChildText("codelet", _yawlNS);
        if (codelet != null) decomposition.setCodelet(codelet);
    }

    
    private static YLogPredicate parseLogPredicate(Element decompElem, Namespace ns){
        Element elemLogPredicate = decompElem.getChild("logPredicate", ns);
        return elemLogPredicate != null ? new YLogPredicate(elemLogPredicate, ns) : null;
    }


    protected Map<YTask, String> getDecomposesToIDs() {
        return _decomposesToIDs;
    }

    private String stripEngineID(String id) {
        if (id != null) {
            int pos = id.indexOf('_');
            if (pos > -1) {
                String lhs = id.substring(0, pos);
                id = StringUtil.isIntegerString(lhs) ? id.substring(pos + 1) : lhs;
            }
        }
        return id;
    }


    private void linkElements() {
        if (_decomposition instanceof YNet) {
            YNet decomposition = (YNet) _decomposition;
            for (YExternalNetElement currentNetElement : decomposition.getNetElements().values()) {
                List<FlowStruct> postsetIDs = _postsetIDs.getQPostset(currentNetElement.getID());
                if (postsetIDs != null) {
                    for (FlowStruct flowStruct : _postsetIDs.getQPostset(currentNetElement.getID())) {
                        YExternalNetElement nextElement = decomposition.getNetElement(flowStruct._flowInto);
                        YFlow flow = new YFlow(currentNetElement, nextElement);
                        flow.setEvalOrdering(flowStruct._predicateOrdering);
                        flow.setIsDefaultFlow(flowStruct._isDefaultFlow);
                        flow.setXpathPredicate(flowStruct._flowPredicate);
                        flow.setDocumentation(flowStruct._label);
                        currentNetElement.addPostset(flow);
                    }
                }
            }

            for (YTask externalTask : _removeSetIDs.keySet()) {
                List<YExternalNetElement> removeSetObjects = new Vector<YExternalNetElement>();
                for (String id : _removeSetIDs.get(externalTask)) {
                    removeSetObjects.add(decomposition.getNetElement(id));
                }
                if (removeSetObjects.size() > 0) {
                    externalTask.addRemovesTokensFrom(removeSetObjects);
                }
            }

            for (YTask taskWithRemoveSet : _removeSetForFlows.keySet()) {
                for (Element removesTokensFromFlowElem : _removeSetForFlows.get(taskWithRemoveSet)) {
                    Element flowSourceElem = removesTokensFromFlowElem.getChild("flowSource", _yawlNS);
                    Element flowDestElem = removesTokensFromFlowElem.getChild("flowDestination", _yawlNS);
                    String flowSourceID = flowSourceElem.getAttributeValue("id");
                    String flowDestID = flowDestElem.getAttributeValue("id");
                    YExternalNetElement flowSource = decomposition.getNetElement(flowSourceID);
                    YExternalNetElement flowDest = decomposition.getNetElement(flowDestID);
                    for (YExternalNetElement maybeImplicit : flowSource.getPostsetElements()) {
                        if (((YCondition) maybeImplicit).isImplicit()) {
                            Set<YExternalNetElement> oneTaskInThisList = maybeImplicit.getPostsetElements();
                            YTask taskAfterImplicit = (YTask) oneTaskInThisList.iterator().next();
                            if (taskAfterImplicit.equals(flowDest)) {
                                List<YExternalNetElement> additionalRemoves =
                                        new ArrayList<YExternalNetElement>();
                                additionalRemoves.add(maybeImplicit);
                                taskWithRemoveSet.addRemovesTokensFrom(additionalRemoves);
                            }
                        }
                    }
                }
            }
        }
    }


    private class Postset {

        // maps a String ID to a List of FlowStruct objects
        Map<String, List<FlowStruct>> _postsetMap = new HashMap<String, List<FlowStruct>>();

        public void add(String id, List<FlowStruct> postsetStruct) {
            List<FlowStruct> oldRefIDs = _postsetMap.get(id);
            if (oldRefIDs == null) {
                oldRefIDs = new Vector<FlowStruct>();
            }
            oldRefIDs.addAll(postsetStruct);
            _postsetMap.put(id, oldRefIDs);
        }


        public List<FlowStruct> getQPostset(String id) {
            return _postsetMap.get(id);
        }


        public List getPostset(List<String> ids) {
            List<FlowStruct> postset = new Vector<FlowStruct>();
            for (String id : ids) {
                postset.addAll(_postsetMap.get(id));
            }
            return postset;
        }


        public void addImplicitConditions() {
            List<String> elementIDs = new Vector<String>(_postsetMap.keySet());
            for (String currentElementID : elementIDs) {
                YExternalNetElement currentNetElement = getNetElement(currentElementID);
                if (currentNetElement instanceof YTask) {
                    for (FlowStruct flowStruct : _postsetMap.get(currentElementID)) {
                        YExternalNetElement netElement = getNetElement(flowStruct._flowInto);

                        // if one task flows directly to another task then we create an
                        // anonymous condition and insert it between the two tasks.
                        if (netElement instanceof YTask) {
                            String anonID = makeAnonID(currentNetElement.getID(),
                                                       netElement.getID());
                            YCondition condition =
                                    new YCondition(anonID, (YNet) _decomposition);
                            condition.setImplicit(true);
                            ((YNet) _decomposition).addNetElement(condition);
                            String tempElemID = flowStruct._flowInto;
                            flowStruct._flowInto = condition.getID();
                            List<FlowStruct> dom = new Vector<FlowStruct>();
                            dom.add(new FlowStruct(tempElemID, null, null));
                            _postsetMap.put(condition.getID(), dom);
                        }
                    }
                }
            }
        }

        private YExternalNetElement getNetElement(String id) {
            return ((YNet) _decomposition).getNetElement(id);
        }

        private String makeAnonID(String current, String next) {
            StringBuilder anonID = new StringBuilder(30);
            anonID.append("c{").append(current).append("_").append(next).append("}");
            return anonID.toString();
        }
    }


    private static class FlowStruct {
        String _flowInto;
        String _flowPredicate;
        Integer _predicateOrdering;
        boolean _isDefaultFlow;
        String _label;

        public FlowStruct(String flowInto, String flowPredicate, String label) {
            _flowInto = flowInto;
            _flowPredicate = flowPredicate;
            _label = label;
        }
    }
}
