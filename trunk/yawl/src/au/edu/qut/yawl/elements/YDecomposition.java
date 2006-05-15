/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.CollectionOfElements;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.data.YInputParameter;
import au.edu.qut.yawl.elements.data.YOutputParameter;
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
@XmlAccessorType(XmlAccessType.PROPERTY )
@XmlType(name = "DecompositionFactsType", propOrder = {
	"id",
    "name",
    "documentation",
    "inputParameters",
    "outputParameters"
})
public class YDecomposition implements Cloneable, YVerifiable, PolymorphicPersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    protected String _id;
    private Long _dbSpecificationID;
    protected YSpecification _specification;
    private String _name;
    private String _documentation;
    private List<YInputParameter> _inputParameters = new ArrayList<YInputParameter>();
    private List<YOutputParameter> _outputParameters = new ArrayList<YOutputParameter>();
    private Set<String> _outputExpressions;
    protected Document _data;
    private Map<String, String> _attribues = new HashMap<String, String>();
    /*
  INSERTED FOR PERSISTANCE
 */
	
	
    private YCaseData casedata = null;

    /**
     * Method for hibernate only
     * @return
     */
    @OneToOne
    @PrimaryKeyJoinColumn
    @XmlTransient
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

    /************************************/

    @Id
    @Column(name="decomp_id")
    @XmlAttribute(name="id",required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
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
        _outputExpressions = new HashSet();
        _data = new Document();
        _attribues = new Hashtable();
	}

    public YDecomposition(String id, YSpecification specification) {
        this._id = id;
        _specification = specification;
        _inputParameters = new ArrayList<YInputParameter>();  //name --> parameter
        _outputParameters = new ArrayList<YOutputParameter>(); //name --> parameter
        _outputExpressions = new HashSet();
        _data = new Document();
        _attribues = new Hashtable();

        _data.setRootElement(new Element(getRootDataElementName()));
    }

    
    @Transient
    @XmlTransient
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

    /**
     * 
     * @return
     */
    @Column(name="documentation")
    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }

    public String getDocumentation() {
        return _documentation;
    }


    /**
     * 
     * @return
     */
    @Column(name="name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }


    public void setInputParam(YInputParameter parameter) {
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
    public void setOutputParameter(YOutputParameter parameter) {
        if (parameter.isInput()) {
            throw new RuntimeException("Can't set input param as output param.");
        }
        if (null != parameter.getName()) {
            _outputParameters.add(parameter);
        } else if (null != parameter.getElementName()) {
            _outputParameters.add(parameter);
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
     * 
     */
	@CollectionOfElements
	@XmlTransient
    public Set<String> getOutputQueries() {
        return _outputExpressions;
    }

    /**
     * Inserted for hibernate
     * @param set
     */
    protected void setOutputQueries(Set<String> outputQueries) {
    	_outputExpressions = outputQueries;
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

        List parameters = new ArrayList(_inputParameters);
        Collections.sort(parameters);
        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            xml.append(parameter.toXML());
        }
        for (Iterator<String> iter = _outputExpressions.iterator(); iter.hasNext();) {
            String expression = iter.next();
            xml.append("<outputExpression query=\"").
                    append(YTask.marshal(expression)).
                    append("\"/>");
        }
        for (Iterator iterator = _outputParameters.iterator(); iterator.hasNext();) {
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
        for (Iterator iterator = _inputParameters.iterator(); iterator.hasNext();) {
            YInputParameter parameter = (YInputParameter) iterator.next();
            messages.addAll(parameter.verify());
        }
        for (Iterator iterator = _outputParameters.iterator(); iterator.hasNext();) {
            YOutputParameter parameter = (YOutputParameter) iterator.next();
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
        copy._inputParameters = new ArrayList<YInputParameter>();
        Collection params = _inputParameters;
        for (Iterator iterator = params.iterator(); iterator.hasNext();) {
        	YInputParameter parameter = (YInputParameter) iterator.next();
        	YInputParameter copyParam = (YInputParameter) parameter.clone();
            copy.setInputParam(copyParam);
        }
        return copy;
    }


    /**
     * @return the document containing runtime variable values
     */
    @Transient
    @XmlTransient
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
        List outputParamsList = new ArrayList<YVariable>(getOutputParameters());
        Collections.sort(outputParamsList);

        for (Iterator iterator = outputParamsList.iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
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
        Iterator iter = _inputParameters.iterator();
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

    	for(YVariable entry:_inputParameters) {
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
     * @return
     */
    @ManyToOne(cascade = {CascadeType.ALL})
    @XmlTransient
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
    public Map getStateSpaceBypassParams() {
        Map result = new HashMap();
        Collection ps = _outputParameters;
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

    @Transient
    public Set getOutputParamNames() {
    	Set<String> names = new HashSet<String>();

    	for(YVariable entry:_outputParameters) {
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

    @OneToMany(mappedBy="parentInputParameters", cascade = {CascadeType.ALL})
    public List<YInputParameter> getInputParameters() {
    	List<YInputParameter> retval = new ArrayList<YInputParameter>();
    	for(YInputParameter entry:_inputParameters) {
    		retval.add(entry);
    	}
    	return retval;
	}
    @XmlElement(name="inputParam", namespace="http://www.citi.qut.edu.au/yawl")
	protected void setInputParameters(List<YInputParameter> inputParam) {
		for (YInputParameter parm: inputParam) {
			parm.setParentInputParameters(this);
			this._inputParameters.add(parm);
		}
	}
    
    @Transient
    public List<YOutputParameter> getOutputParameters() {
    	List<YOutputParameter> retval = new ArrayList<YOutputParameter>();
    	for(YOutputParameter entry:_outputParameters) {
    		retval.add(entry);
    	}
    	return retval;
	}
    @OneToMany(mappedBy="parentOutputParameters", cascade = {CascadeType.ALL})
    @XmlElement(name="outputParam", namespace="http://www.citi.qut.edu.au/yawl")
	protected void setOutputParameters(List<YOutputParameter> outputParam) {
		for (YOutputParameter parm: outputParam) {
			parm.setParentOutputParameters(this);
			this._outputParameters.add(parm);
		}
	}

}
