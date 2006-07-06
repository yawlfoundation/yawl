/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 *
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 16:04:21
 *
 *
 * ***************************************************************************************
 *
 * an abstract class sub-classed by YNet and YAWLServiceGateway.  This class is where input
 * and output parameters are defined.
 *
 * @hibernate.class table="DECOMPOSITION" discriminator-value="0"
 * @hibernate.discriminator column="DECOMPOSITION_TYPE_ID" type="integer"
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="decomposition_type",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("decomposition")
public class YDecomposition implements Parented, Cloneable, YVerifiable, PolymorphicPersistableObject, ExtensionListContainer {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed.
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

    protected String _id;
    protected YSpecification _specification;
    private String _name;
    private String _documentation;
    /**
     * All accesses to this collection should be done through the getter, {@link #getInputParameters()}.
     * Adding to/removing from the collection should be done directly.
     */
    private List<YParameter> _inputParameters = new ArrayList<YParameter>();
    /**
     * All accesses to this collection should be done through the getter, {@link #getOutputParameters()}.
     * Adding to/removing from the collection should be done directly.
     */
    private List<YParameter> _outputParameters = new ArrayList<YParameter>();
    protected Document _data = new Document();
    private Map<String, String> _attribues = new HashMap<String, String>();
    private List<Element> _internalExtensions;

    private boolean _outBoundSchemaChecking;

    /*
  INSERTED FOR PERSISTANCE
 */
    @Transient
    public Object getParent() {return _specification;}


    private YCaseData casedata;

    /**
     * Method for hibernate only
     */
    @OneToOne
    @PrimaryKeyJoinColumn
	protected YCaseData getCasedata() {
		return casedata;
	}

	/**
	 * Method for hibernate only
	 * @param casedata
	 */
    protected void setCasedata( YCaseData casedata ) {
		this.casedata = casedata;
	}

    /**********************************
     Persistance MethodS
     */
    public void initializeDataStore(YCaseData casedata) throws YPersistenceException {
        this.casedata = casedata;
        this.casedata.setData(new XMLOutputter().outputString(_data));

//todo AJH - External persistence !!!
//        if (pmgr != null) {
//            pmgr.storeObjectFromExternal(this.casedata);
//        }
//        DaoFactory.createYDao().create(this.casedata);
//        YPersistance.getInstance().storeData(this.casedata);
    }

    public void restoreData(YCaseData casedata) {

        this.casedata = casedata;
        _data = getNetDataDocument(casedata.getData());
    }

    @Transient
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

    Long _dbid;

    @Id
    @Column(name="decomp_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getDbID() {
    	return _dbid;
    }

    private void setDbID(Long dbid) {
    	_dbid = dbid;
    }

    public String getId() {
        return _id;
    }

    /**
     * Method inserted for hibernate use only
     * @param id
     */
    protected void setId(String id) {
    	_id = id;
    }

	/**
	 * Null constructor for hibernate
	 */
	public YDecomposition() {
	}

    public YDecomposition(String id, YSpecification specification) {
        this._id = id.replace(" ", "_");
        _specification = specification;
        _data.setRootElement(new Element(getRootDataElementName()));
    }


    @Transient
    public Map<String, String> getAttributes() {
        return this._attribues;
    }

    public void setAttributes(Map<String, String> attributes) {
        this._attribues = attributes;
    }

    public void setAttribute(String name, String value) {
        _attribues.put(name, value);
    }

    public String getAttribute(String name) {
        return _attribues.get(name);
    }


    @Column(name="documentation")
    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }

    public String getDocumentation() {
        return _documentation;
    }


    @Column(name="name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }


    public void setInputParam(YParameter parameter) {
        if (parameter.isInput()) {
            if (null != parameter.getName()) {
                _inputParameters.add(parameter);
            } else if (null != parameter.getElementName()) {
                _inputParameters.add(parameter);
            }
        } else {
            throw new RuntimeException("Can't set an output type param as an input param.");
        }
    }


    /**
     * Adds an output parameter to this.
     * @param parameter the parameter to be added
     */
    public void setOutputParameter(YParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set input param as output param.");
        }
        if (null != parameter.getName()) {
            _outputParameters.add(parameter);
        } else if (null != parameter.getElementName()) {
            _outputParameters.add(parameter);
        }
    }


 

    public String toXML() {
        StringBuffer xml = new StringBuffer();
        //just do the decomposition facts (not the surrounding element) - to keep life simple
        if (_internalExtensions != null && !(_internalExtensions.size() == 0)) {
        	xml.append(getInternalExtensionsAsString());
        }
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

        List<YParameter> parameters =
                new ArrayList<YParameter>(_inputParameters);
        for (YParameter parameter : parameters) {
            xml.append(parameter.toXML());
        }
        List<YParameter> outParameters = getOutputParameters();
        Collections.sort(outParameters);
        for (YParameter parameter : outParameters) {
            xml.append(parameter.toXML());
        }
        return xml.toString();
    }


    public List <YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_id == null) {
            messages.add(new YVerificationMessage(this, this + " cannot have null id.", YVerificationMessage.ERROR_STATUS));
        }
        for (YParameter inputParameter : getInputParameters()) {
            messages.addAll(inputParameter.verify());
        }
        for (YParameter parameter : getOutputParameters()) {
            messages.addAll(parameter.verify());
        }
        return messages;
    }


    public String toString() {
        String fullClassName = getClass().getName();
        String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        return shortClassName + ":" + getId();
    }


    public Object clone() throws CloneNotSupportedException {
        YDecomposition copy = (YDecomposition) super.clone();
        copy._inputParameters = new ArrayList<YParameter>();
        Collection<YParameter> params = getInputParameters();
        for (YParameter parameter : params) {
            YParameter copyParam = (YParameter) parameter.clone();
            copy.setInputParam(copyParam);
        }
        return copy;
    }


    /**
     * @return the document containing runtime variable values
     */
    @Transient
    public Document getInternalDataDocument() {
        return _data;
    }

    public void setInternalDataDocument(Document document) {
        _data = document;
    }


    /**
     * This method returns the list of data from a decomposition. According to
     * its declared output parameters.  Only useful for Beta 4 and above.
     * The data inside this decomposition is groomed so to speak so that the
     * output data is returned in sequence.  Furthermore no internal variables, \
     * or input only parameters are returned.
     * @return a JDom Document of the output data.
     */
    @Transient
    public Document getOutputData() {
        //create a new output document to return
        Document outputDoc = new Document();
        String rootDataElementName = _data.getRootElement().getName();
        Element rootElement = new Element(rootDataElementName);
        outputDoc.setRootElement(rootElement);
        //now prepare a list of output params to iterate over.
        List <YParameter> outputParamsList = new ArrayList<YParameter>(getOutputParameters());
        Collections.sort(outputParamsList);

        for (YParameter parameter : outputParamsList) {
            System.out.println("parameter.toXML() = " + parameter.toXML());
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

    @Transient
    public Element getVariableDataByName(String name) {
        return _data.getRootElement().getChild(name);
    }

    public void assignData(Element variable) throws YPersistenceException {
        _data.getRootElement().removeChild(variable.getName());
        _data.getRootElement().addContent(variable);

        casedata.setData(new XMLOutputter().outputString(_data));

        //todo AJH External persistence !!!
//        YPersistance.getInstance().updateData(casedata);
//        if (pmgr != null) {
//            pmgr.updateObjectExternal(casedata);
//        }
//        DaoFactory.createYDao().update(casedata);
    }


    public void initialise() throws YPersistenceException {
        for (YParameter inputParam : getInputParameters()) {
            Element initialValuedXMLDOM = null;
            if (inputParam.getInitialValue() != null) {
                String initialValue = inputParam.getInitialValue();
                initialValue =
                        "<" + inputParam.getName() + ">" +
                                initialValue +
                                "</" + inputParam.getName() + ">";
                try {
                    SAXBuilder builder = new SAXBuilder();
                    Document doc = builder.build(new StringReader(initialValue));
                    initialValuedXMLDOM = doc.detachRootElement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                initialValuedXMLDOM = new Element(inputParam.getName());
            }
            addData(initialValuedXMLDOM);
        }
    }


    protected void addData(Element element) throws YPersistenceException {
        _data.getRootElement().removeChild(element.getName());
        element = (Element) element.clone();
        _data.getRootElement().addContent(element);

        casedata.setData(new XMLOutputter().outputString(_data));

        //todo AJH External persistence
//        YPersistance.getInstance().updateData(casedata);
//        if (pmgr != null) {
//            pmgr.updateObjectExternal(casedata);
//        }
//        DaoFactory.createYDao().update(casedata);
    }

    @Transient
    public Set<String> getInputParameterNames() {
    	Set<String> names = new HashSet<String>();

    	for(YVariable entry: getInputParameters()) {
    		if (null != entry.getName()) {
    			names.add(entry.getName());
    		} else if (null != entry.getElementName()) {
    			names.add(entry.getElementName());
    		}
    	}
    	return names;
    }

    /**
     * Returns a link to the containing specification.
     */
    @ManyToOne(cascade = {CascadeType.ALL})
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YSpecification getSpecification() {
        return _specification;
    }
    /**
     * Inserted for hibernate
     * @param spec
     */
    public void setSpecification(YSpecification spec) {
    	_specification = spec;
    }


    /**
     * Gets those params that bypass the decomposition state space.
     * @return a map of them.
     */
    @Transient
    public Map<String,YParameter> getStateSpaceBypassParams() {
        Map<String,YParameter> result = new HashMap<String, YParameter>();
        Collection<YParameter> ps = getOutputParameters();
        for (YParameter parameter : ps) {
            if (parameter.bypassesDecompositionStateSpace()) {
                result.put(
                        parameter.getName() != null ?
                                parameter.getName() : parameter.getElementName(),
                        parameter);
            }
        }
        return result;
    }

    @Transient
    public Set<? extends String> getOutputParamNames() {
    	Set<String> names = new HashSet<String>();

    	for(YVariable entry:getOutputParameters()) {
    		if (null != entry.getName()) {
    			names.add(entry.getName());
    		} else if (null != entry.getElementName()) {
    			names.add(entry.getElementName());
    		}
    	}
    	return names;
    }

    @Transient
    public String getRootDataElementName() {
        if (_specification.usesSimpleRootData()) {
            return "data";
        } else {
            return _id;
        }
    }

    @OneToMany(mappedBy="decomposition", cascade={CascadeType.ALL})
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="DataTypeName='inputParam'")
    public List<YParameter> getInputParameters() {
    	List<YParameter> retval = new ArrayList<YParameter>();
    	for(YParameter entry:_inputParameters) {
    		retval.add(entry);
    	}
    	Collections.sort(retval);
    	return retval;
	}
    @OneToMany(mappedBy="decomposition", cascade={CascadeType.ALL})
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="DataTypeName='inputParam'")
	protected void setInputParameters(List<YParameter> inputParam) {
		for (YParameter parm: inputParam) {
			parm.setParent(this);
			this._inputParameters.add(parm);
		}
	}

    @OneToMany(mappedBy="decomposition", cascade={CascadeType.ALL})
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="DataTypeName='outputParam'")
    public List<YParameter> getOutputParameters() {
    	List<YParameter> retval = new ArrayList<YParameter>();
    	for(YParameter entry:_outputParameters) {
    		retval.add(entry);
    	}
    	return retval;
	}

	protected void setOutputParameters(List<YParameter> outputParam) {
		for (YParameter parm: outputParam) {
			parm.setParent(this);
			this._outputParameters.add(parm);
		}
	}

    @Column(name="extensions", length=32768)
	public String getInternalExtensionsAsString() {
		if (_internalExtensions == null) return "";
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		StringBuffer buffer = new StringBuffer();
		for (Element e: _internalExtensions) {
			String representation = outputter.outputString(e);
			buffer.append(representation);
		}
    	return buffer.toString();
	}

    @Column(name="extensions", length=32768)
	public void setInternalExtensionsAsString(String extensions) {
		_internalExtensions = new ArrayList<Element>();
		if (extensions == null || extensions.length() == 0) {
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        SAXBuilder sb = new SAXBuilder();
        try {
            Document d = sb.build(new InputSource(new StringReader("<fragment>" + extensions + "</fragment>")));
            Iterator i = d.getDescendants();
            while(i.hasNext()) {
                Element element = (Element) i.next();
                if (element.getAttributeValue(ExtensionListContainer.IDENTIFIER_ATTRIBUTE) != null) {
                    _internalExtensions.add(element);
                }
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}

	@Transient
	public List getInternalExtensions() {
		return _internalExtensions;
	}

	@Transient
	public void setInternalExtensions(List<Element> extensions) {
		_internalExtensions = extensions;
	}

    public boolean skipOutboundSchemaChecks() {
        return _outBoundSchemaChecking;
    }

    public void setSkipOutboundSchemaChecks(boolean skipSchemaCheck) {
        _outBoundSchemaChecking = skipSchemaCheck;
    }

/*we shouldnt need these if the where clause works.    
    @OneToMany(mappedBy="parent", cascade = {CascadeType.ALL})
    private List<YParameter> getParameters() {
    	List<YParameter> retval = new ArrayList<YParameter>();
    	retval.addAll(_inputParameters);
    	retval.addAll(_outputParameters);
    	return retval;
    }
    private void setParameters(List<YParameter> params) {
    	for (YParameter parm: params) {
    		if (parm.getDataTypeName().equals(YParameter.getTypeForInput())) {
    			this.setInputParam(parm);
    		}
    		if (parm.getDataTypeName().equals(YParameter.getTypeForOutput())) {
    			this.setOutputParameter(parm);
    		}
    	}
    }
    */
}
