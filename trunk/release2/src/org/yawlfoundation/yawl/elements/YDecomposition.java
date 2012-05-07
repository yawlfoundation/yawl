/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements;

import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 16:04:21
 *
 * Refactored for 2.0 by Michael Adams 
 * 
 */

public abstract class YDecomposition implements Cloneable, YVerifiable {
    protected String _id;
    protected YSpecification _specification;
    private String _name;
    private String _documentation;
    private Map<String, YParameter> _inputParameters;
    private Map<String, YParameter> _outputParameters;
    private Map<String, YParameter> _enablementParameters;  // only used to generate editor xml
    private Set<String> _outputExpressions;
    protected Document _data;
    private YNetData _casedata = null;
    private YAttributeMap _attributes;
    private YLogPredicate _logPredicate;

    // if true, this decomposition requires resourcing decisions made at runtime
    protected boolean _manualInteraction = true;

    protected String _codelet;       // specified codelet to execute for automated tasks


    // CONSTRUCTOR //

    public YDecomposition(String id, YSpecification specification) {
        _id = id;
        _specification = specification;
        _inputParameters = new HashMap<String, YParameter>();
        _outputParameters = new HashMap<String, YParameter>();
        _enablementParameters = new HashMap<String, YParameter>();
        _outputExpressions = new HashSet<String>();
        _data = new Document();
        _attributes = new YAttributeMap();

        _data.setRootElement(new Element(getRootDataElementName()));
    }


    /*****************************************************************************/

    // PERSISTENCE METHODS //

    public void initializeDataStore(YPersistenceManager pmgr, YNetData casedata)
                                                        throws YPersistenceException {
        _casedata = casedata;
        _casedata.setData(JDOMUtil.documentToString(_data));

        if (pmgr != null) pmgr.storeObjectFromExternal(_casedata);
    }

    
    public void restoreData(YNetData casedata) {
        _casedata = casedata;
        _data = getNetDataDocument(casedata.getData());
    }

    
    public Document getNetDataDocument(String netData) {
        return JDOMUtil.stringToDocument(netData);
    }

    /******************************************************************************/

    // GETTERS & SETTERS //

    public String getID() { return _id; }


    public YAttributeMap getAttributes() { return _attributes; }

    public void setAttributes(Map<String, String> attributes) {
        _attributes.set(attributes);
    }

    public String getAttribute(String name) { return _attributes.get(name); }

    public void setAttribute(String name, String value) {
        _attributes.put(name, value);
    }


    public String getDocumentation() { return _documentation; }

    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }


    public String getName() { return _name; }

    public void setName(String name) { _name = name; }


    public Map<String, YParameter> getInputParameters() { return _inputParameters; }


    public Map<String, YParameter> getOutputParameters() { return _outputParameters; }


    public void addInputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            String paramName = parameter.getPreferredName();
            if (paramName != null) {
                _inputParameters.put(paramName, parameter);
            }
        }
        else throw new RuntimeException("Can't set an output param as an input param.");
    }


    public void addOutputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set an input param as an output param.");
        }
        if (parameter.isEnablement())
            setEnablementParameter(parameter);
        else {
            String paramName = parameter.getPreferredName();
            if (paramName != null) {
                _outputParameters.put(paramName, parameter);
            }
        }
    }


    public void setEnablementParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set an input param as an enablement param.");
        }
        String paramName = parameter.getPreferredName();
        if (paramName != null) {
            _enablementParameters.put(paramName, parameter);
        }
    }


    public void setOutputExpression(String query) { _outputExpressions.add(query); }

    public Set<String> getOutputQueries() { return _outputExpressions; }


    // if set to true, any task that decomposes to this will need to have a resourcing
    // strategy specified
    public void setExternalInteraction(boolean interaction) {
        _manualInteraction = interaction ;
    }

    // when external interactions are specified as manual, tasks that decompose to
    // this will require resourcing decisions to be specified at design time
    public boolean requiresResourcingDecisions() { return _manualInteraction ; }


    public String getCodelet() { return _codelet; }

    public void setCodelet(String codelet) { _codelet = codelet ; }

    public YLogPredicate getLogPredicate() { return _logPredicate; }

    public void setLogPredicate(YLogPredicate predicate) { _logPredicate = predicate; }

    public String toXML() {

        // just do the decomposition facts (not the surrounding element) - to keep life simple
        StringBuilder xml = new StringBuilder();
        if (_name != null)
            xml.append(StringUtil.wrap(_name, "name"));
        if (_documentation != null)
            xml.append(StringUtil.wrap(_documentation, "documentation"));

        xml.append(paramMapToXML(_inputParameters));

        for (String expression : _outputExpressions) {
            xml.append("<outputExpression query=\"")
               .append(JDOMUtil.encodeEscapes(expression))
               .append("\"/>");
        }

        xml.append(paramMapToXML(_outputParameters));
        xml.append(paramMapToXML(_enablementParameters));

        if (_logPredicate != null) xml.append(_logPredicate.toXML());

        return xml.toString();
    }


    private String paramMapToXML(Map<String, YParameter> paramMap) {
        StringBuilder result = new StringBuilder() ;
        List<YParameter> parameters = new ArrayList<YParameter>(paramMap.values());
        Collections.sort(parameters);
        for (YParameter parameter : parameters) {
            result.append(parameter.toXML());
        }
        return result.toString() ;
    }


    public void verify(YVerificationHandler handler) {
        if (_id == null)
            handler.error(this, this + " cannot have null id.");

        for (YParameter param : _inputParameters.values())
            param.verify(handler);

        for (YParameter param : _outputParameters.values())
            param.verify(handler);
    }


    public Set<String> getInputParameterNames() { return _inputParameters.keySet(); }

    public Set<String> getOutputParameterNames() { return _outputParameters.keySet(); }


    /**
     * Returns a link to the containing specification.
     * @return the specification containing this decomposition.
     */
    public YSpecification getSpecification() { return _specification; }


    /**
     * Gets those params that bypass the decomposition state space.
     * @return a map of them.
     */
    public Map<String, YParameter> getStateSpaceBypassParams() {
        Map<String, YParameter> result = new HashMap<String, YParameter>();
        for (YParameter parameter : _outputParameters.values()) {
            if (parameter.bypassesDecompositionStateSpace()) {
                result.put(parameter.getPreferredName(), parameter);
            }
        }
        return result;
    }


    public String toString() {
        String fullClassName = getClass().getName();
        String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        return shortClassName + ":" + getID();
    }


    public Object clone() throws CloneNotSupportedException {
        YDecomposition copy = (YDecomposition) super.clone();
        copy._inputParameters = new HashMap<String, YParameter>();
        for (YParameter parameter : _inputParameters.values()) {
            YParameter copyParam = (YParameter) parameter.clone();
            copy.addInputParameter(copyParam);
        }
        return copy;
    }


    /**
     * @return the document containing runtime variable values
     */
    public Document getInternalDataDocument() { return _data; }


    /**
     * This method returns the list of data from a decomposition. According to
     * its declared output parameters.  Only useful for Beta 4 and above.
     * The data inside this decomposition is groomed so to speak so that the
     * output data is returned in sequence.  Furthermore no internal variables, \
     * or input only parameters are returned.
     * @return a JDom Document of the output data.
     */
    public Document getOutputData() {

        //create a new output document to return
        Document outputDoc = new Document();
        Element root = _data.getRootElement();
        outputDoc.setRootElement(new Element(root.getName()));

        //now prepare a list of output params to iterate over.
        List<YParameter> outputParamsList = new ArrayList<YParameter>(
                                                        getOutputParameters().values());
        Collections.sort(outputParamsList);

        for (YParameter parameter : outputParamsList) {
            Element child = root.getChild(parameter.getPreferredName());
            outputDoc.getRootElement().addContent(child.clone());
        }
        return outputDoc;
    }


    public Element getVariableDataByName(String name) {
        return _data.getRootElement().getChild(name);
    }


    public void assignData(YPersistenceManager pmgr, Element variable)
                                                     throws YPersistenceException {

        _data.getRootElement().removeChild(variable.getName());
        _data.getRootElement().addContent(variable);
        _casedata.setData(JDOMUtil.documentToString(_data));

        if (pmgr != null)  pmgr.updateObjectExternal(_casedata);
    }


    protected void addData(YPersistenceManager pmgr, Element element)
                                                     throws YPersistenceException {
        assignData(pmgr, element.clone());
    }


    public void initialise(YPersistenceManager pmgr) throws YPersistenceException {
        for (YParameter inputParam : _inputParameters.values()) {
            Element initialValueXML;
            String initialValue = inputParam.getInitialValue();
            if (initialValue != null) {
                initialValue = StringUtil.wrap(initialValue, inputParam.getName()) ;
                initialValueXML = JDOMUtil.stringToElement(initialValue);
            }
            else initialValueXML = new Element(inputParam.getName());

            addData(pmgr, initialValueXML);
        }
    }


    public String getRootDataElementName() {
        return _specification.getSchemaVersion().usesSimpleRootData() ? "data" : _id;
    }

}
