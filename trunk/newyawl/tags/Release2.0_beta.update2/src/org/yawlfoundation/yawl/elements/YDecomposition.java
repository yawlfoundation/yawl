/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.elements;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YCaseData;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.StringReader;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 16:04:21
 *
 * Refactored for 2.0 by Michael Adams 13/05/2008
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
    private YCaseData _casedata = null;
    private Hashtable<String, String> _attributes;

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
        _attributes = new Hashtable<String, String>();

        _data.setRootElement(new Element(getRootDataElementName()));
    }


    /*****************************************************************************/

    // PERSISTENCE METHODS //

    public void initializeDataStore(YPersistenceManager pmgr, YCaseData casedata)
                                                        throws YPersistenceException {
        _casedata = casedata;
        _casedata.setData(JDOMUtil.documentToString(_data));

        if (pmgr != null) pmgr.storeObjectFromExternal(_casedata);
    }

    
    public void restoreData(YCaseData casedata) {
        _casedata = casedata;
        _data = getNetDataDocument(casedata.getData());
    }

    
    public Document getNetDataDocument(String netData) {
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        try {
            document = builder.build(new StringReader(netData));
        } catch (JDOMException je) {
            je.printStackTrace();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        return document;
    }

    /******************************************************************************/

    // GETTERS & SETTERS //

    public String getID() { return _id; }


    public Hashtable getAttributes() { return _attributes; }

    public void setAttributes(Hashtable<String, String> attributes) {
        _attributes = attributes;
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


    public void setInputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            if (null != parameter.getName()) {
                _inputParameters.put(parameter.getName(), parameter);
            } else if (null != parameter.getElementName()) {
                _inputParameters.put(parameter.getElementName(), parameter);
            }
        } else {
            throw new RuntimeException("Can't set an output param as an input param.");
        }
    }


    /**
     * Adds an output parameter to this.
     * @param parameter the parameter to be added
     */
    public void setOutputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set an input param as an output param.");
        }
        if (parameter.isEnablement())
            setEnablementParameter(parameter);
        else {
            if (null != parameter.getName()) {
                _outputParameters.put(parameter.getName(), parameter);
            } else if (null != parameter.getElementName()) {
                _outputParameters.put(parameter.getElementName(), parameter);
            }
        }    
    }


    public void setEnablementParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set an input param as an enablement param.");
        }
        if (null != parameter.getName()) {
            _enablementParameters.put(parameter.getName(), parameter);
        } else if (null != parameter.getElementName()) {
            _enablementParameters.put(parameter.getElementName(), parameter);
        }
    }


    public void setOutputExpression(String query) { _outputExpressions.add(query); }

    public Set getOutputQueries() { return _outputExpressions; }


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

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        //just do the decomposition facts (not the surrounding element) - to keep life simple
        if (_name != null) {
            xml.append("<name>").
                    append(_name).
                    append("</name>");
        }
        if (_documentation != null) {
            xml.append("<documentation>").
                    append(_documentation).
                    append("</documentation>");
        }

        xml.append(paramMapToXML(_inputParameters));

        for (Iterator iter = _outputExpressions.iterator(); iter.hasNext();) {
            String expression = (String) iter.next();
            xml.append("<outputExpression query=\"").
                    append(YTask.marshal(expression)).
                    append("\"/>");
        }

        xml.append(paramMapToXML(_outputParameters));
        xml.append(paramMapToXML(_enablementParameters));

        return xml.toString();
    }


    private String paramMapToXML(Map<String, YParameter> paramMap) {
        StringBuilder result = new StringBuilder() ;
        List parameters = new ArrayList<YParameter>(paramMap.values());
        Collections.sort(parameters);
        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            result.append(parameter.toXML());
        }
        return result.toString() ;
    }


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_id == null)
            messages.add(new YVerificationMessage(this, this + " cannot have null id.",
                             YVerificationMessage.ERROR_STATUS));

        for (YParameter param : _inputParameters.values())
            messages.addAll(param.verify());

        for (YParameter param : _outputParameters.values())
            messages.addAll(param.verify());

        return messages;
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
            copy.setInputParameter(copyParam);
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
        outputDoc.setRootElement(new Element(_data.getRootElement().getName()));

        //now prepare a list of output params to iterate over.
        List<YParameter> outputParamsList = new ArrayList<YParameter>(
                                                        getOutputParameters().values());
        Collections.sort(outputParamsList);

        for (Iterator iterator = outputParamsList.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            String varElementName =
                    parameter.getName() != null ?
                    parameter.getName() : parameter.getElementName();
            Element root = _data.getRootElement();
            Element child = root.getChild(varElementName);
            Element clone = (Element) child.clone();
            outputDoc.getRootElement().addContent(clone);
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
        assignData(pmgr, (Element) element.clone());
    }


    public void initialise(YPersistenceManager pmgr) throws YPersistenceException {
        for (YParameter inputParam : _inputParameters.values()) {
            Element initialValueXML = null;
            String initialValue = inputParam.getInitialValue();
            if (initialValue != null) {
                initialValue = StringUtil.wrap(initialValue, inputParam.getName()) ;
                initialValueXML = JDOMUtil.stringToElement(initialValue);
            }
            else initialValueXML = new Element(inputParam.getName());

            addData(pmgr, initialValueXML);
        }
    }


    public Set getInputParameterNames() { return _inputParameters.keySet(); }


    /**
     * Returns a link to the containing specification.
     * @return the specification containing this decomposition.
     */
    public YSpecification getSpecification() { return _specification; }


    /**
     * Gets those params that bypass the decomposition state space.
     * @return a map of them.
     */
    public Map getStateSpaceBypassParams() {
        Map result = new HashMap();
        Collection ps = _outputParameters.values();
        for (Iterator iterator = ps.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            if (parameter.bypassesDecompositionStateSpace()) {
                result.put(
                        parameter.getName() != null ?
                        parameter.getName() : parameter.getElementName(),
                        parameter);
            }
        }
        return result;
    }

    
    public Set getOutputParamNames() { return _outputParameters.keySet(); }


    public String getRootDataElementName() {
        return _specification.usesSimpleRootData() ? "data" : _id;
    }
}
