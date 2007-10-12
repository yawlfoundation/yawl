/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.YCaseData;
import au.edu.qut.yawl.engine.YPersistenceManager;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.util.YVerificationMessage;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 16:04:21
 * 
 */
public abstract class YDecomposition implements Cloneable, YVerifiable {
    protected String _id;
    protected YSpecification _specification;
    private String _name;
    private String _documentation;
    private Map _inputParameters;  //name --> parameter
    private Map _outputParameters; //name --> parameter
    private Set _outputExpressions;
    protected Document _data;
    private Hashtable _attribues;

    // if true, this decomposition requires resourcing decisions made at runtime
    protected boolean _manualInteraction ;

    /*
  INSERTED FOR PERSISTANCE
 */
    private YCaseData casedata = null;

    /**********************************
     Persistance MethodS
     */
    public void initializeDataStore(YPersistenceManager pmgr, YCaseData casedata) throws YPersistenceException {
        this.casedata = casedata;
        this.casedata.setData(new XMLOutputter().outputString(_data));

//todo AJH - External persistence !!!
        if (pmgr != null) {
            pmgr.storeObjectFromExternal(this.casedata);
        }
//        YPersistance.getInstance().storeData(this.casedata);
    }

    public void restoreData(YCaseData casedata) {

        this.casedata = casedata;
        _data = getNetDataDocument(casedata.getData());
    }

    public Document getNetDataDocument(String netData) {
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        try {
            document = builder.build(new StringReader(netData));
            return document;
        } catch (JDOMException je) {
            je.printStackTrace();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
        return document;
    }

    /************************************/

    public String getID() {
        return this._id;
    }


    public YDecomposition(String id, YSpecification specification) {
        this._id = id;
        _specification = specification;
        _inputParameters = new HashMap();  //name --> parameter
        _outputParameters = new HashMap(); //name --> parameter
        _outputExpressions = new HashSet();
        _data = new Document();
        _attribues = new Hashtable();

        _data.setRootElement(new Element(getRootDataElementName()));
    }

    public Hashtable getAttributes() {
        return this._attribues;
    }

    public void setAttributes(Hashtable attributes) {
        this._attribues = attributes;
    }

    public void setAttribute(String name, String value) {
        _attribues.put(name, value);
    }

    public String getAttribute(String name) {
        return _attribues.get(name).toString();
    }

    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }

    public String getDocumentation() {
        return _documentation;
    }

    public void setName(String name) {
        _name = name;
    }


    public String getName() {
        return _name;
    }


    public void setInputParam(YParameter parameter) {
        if (parameter.isInput()) {
            if (null != parameter.getName()) {
                _inputParameters.put(parameter.getName(), parameter);
            } else if (null != parameter.getElementName()) {
                _inputParameters.put(parameter.getElementName(), parameter);
            }
        } else {
            throw new RuntimeException("Can't set an output type param as an input param.");
        }
    }


    public Map getInputParameters() {
        return _inputParameters;
    }


    public Map getOutputParameters() {
        return _outputParameters;
    }

    // if set to true, any task that decomposes to this will need to have a resourcing
    // strategy specified
    public void setExternalInteraction(boolean interaction) {
        _manualInteraction = interaction ;
    }

    // when external interactions are specified as manual, tasks that decompose to
    // this will require resourcing decisions to be specified at design time
    public boolean requiresResourcingDecisions() { return _manualInteraction ; }


    /**
     * Adds an output parameter to this.
     * @param parameter the parameter to be added
     */
    public void setOutputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set input param as output param.");
        }
        if (null != parameter.getName()) {
            _outputParameters.put(parameter.getName(), parameter);
        } else if (null != parameter.getElementName()) {
            _outputParameters.put(parameter.getElementName(), parameter);
        }
    }


    /**
     * @param query
     */
    public void setOutputExpression(String query) {
        _outputExpressions.add(query);
    }


    /**
     * @return set of output queries
     */
    public Set getOutputQueries() {
        return _outputExpressions;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
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
        List parameters = new ArrayList(_inputParameters.values());
        Collections.sort(parameters);
        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            xml.append(parameter.toXML());
        }
        for (Iterator iter = _outputExpressions.iterator(); iter.hasNext();) {
            String expression = (String) iter.next();
            xml.append("<outputExpression query=\"").
                    append(YTask.marshal(expression)).
                    append("\"/>");
        }
        for (Iterator iterator = _outputParameters.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            xml.append(parameter.toXML());
        }

        return xml.toString();
    }


    public List verify() {
        List messages = new Vector();
        if (_id == null) {
            messages.add(new YVerificationMessage(this, this + " cannot have null id.", YVerificationMessage.ERROR_STATUS));
        }
        for (Iterator iterator = _inputParameters.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        for (Iterator iterator = _outputParameters.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        return messages;
    }


    public String toString() {
        String fullClassName = getClass().getName();
        String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        return shortClassName + ":" + getID();
    }


    public Object clone() throws CloneNotSupportedException {
        YDecomposition copy = (YDecomposition) super.clone();
        copy._inputParameters = new HashMap();
        Collection params = _inputParameters.values();
        for (Iterator iterator = params.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            YParameter copyParam = (YParameter) parameter.clone();
            copy.setInputParam(copyParam);
        }
        return copy;
    }


    /**
     * @return the document containing runtime variable values
     */
    public Document getInternalDataDocument() {
        return _data;
    }


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
        String rootDataElementName = _data.getRootElement().getName();
        Element rootElement = new Element(rootDataElementName);
        outputDoc.setRootElement(rootElement);
        //now prepare a list of output params to iterate over.
        Collection outputParams = getOutputParameters().values();
        List outputParamsList = new ArrayList(outputParams);
        Collections.sort(outputParamsList);

        for (Iterator iterator = outputParamsList.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            //System.out.println("parameter.toXML() = " + parameter.toXML());
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


    public void assignData(YPersistenceManager pmgr, Element variable) throws YPersistenceException {
        _data.getRootElement().removeChild(variable.getName());
        _data.getRootElement().addContent(variable);

        casedata.setData(new XMLOutputter().outputString(_data));

        //todo AJH External persistence !!!
//        YPersistance.getInstance().updateData(casedata);
        if (pmgr != null) {
            pmgr.updateObjectExternal(casedata);
        }
    }


    public void initialise(YPersistenceManager pmgr) throws YPersistenceException {
        Iterator iter = _inputParameters.values().iterator();
        while (iter.hasNext()) {
            YParameter inputParam = (YParameter) iter.next();
            Element initialValuedXMLDOM = null;
            if (inputParam.getInitialValue() != null) {
                String initialValue = inputParam.getInitialValue();
                initialValue =
                        "<" + inputParam.getName() + ">" +
                        initialValue +
                        "</" + inputParam.getName() + ">";
                try {
                    SAXBuilder builder = new org.jdom.input.SAXBuilder();
                    Document doc = builder.build(new StringReader(initialValue));
                    initialValuedXMLDOM = doc.detachRootElement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                initialValuedXMLDOM = new Element(inputParam.getName());
            }
            addData(pmgr, initialValuedXMLDOM);
        }
    }


    protected void addData(YPersistenceManager pmgr, Element element) throws YPersistenceException {
        _data.getRootElement().removeChild(element.getName());
        element = (Element) element.clone();
        _data.getRootElement().addContent(element);

        casedata.setData(new XMLOutputter().outputString(_data));

        //todo AJH External persistence
//        YPersistance.getInstance().updateData(casedata);
        if (pmgr != null) {
            pmgr.updateObjectExternal(casedata);
        }
    }


    public Set getInputParameterNames() {
        return _inputParameters.keySet();
    }


    /**
     * Returns a link to the containing specification.
     * @return the specification containing this decomposition.
     */
    public YSpecification getSpecification() {
        return _specification;
    }


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

    public Set getOutputParamNames() {
        return _outputParameters.keySet();
    }

    public String getRootDataElementName() {
        if (_specification.usesSimpleRootData()) {
            return "data";
        } else {
            return _id;
        }
    }
}
