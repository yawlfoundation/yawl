/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import au.edu.qut.yawl.util.YVerificationMessage;
import au.edu.qut.yawl.exceptions.YQueryException;

/**
 * 
 * A collection of attributes that apply to multiple instance tasks.  Used as a property
 * of YTask.
 * Date: 15/04/2003
 * Time: 12:06:19
 * 
 * 
 * Set class to non-final for purposes of hibernate
 * 
 * @hibernate.class table="MULTI_INSTANCE_ATTRIBUTES"
 */
@Entity
public class YMultiInstanceAttributes implements Cloneable, YVerifiable, Serializable  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private static Logger logger = Logger.getLogger(YMultiInstanceAttributes.class);
	
    public final static String _creationModeDynamic = "dynamic";
    public final static String _creationModeStatic = "static";

    private Integer _minInstances;
    private Integer _maxInstances;
    private Integer _threshold;
    private String _minInstancesQuery;
    private String _maxInstancesQuery;
    private String _thresholdQuery;
    private String _creationMode;
    YExternalNetElement _myTask;
    private String _inputVarName;
    private String _inputSplittingQuery;
    private String _remoteOutputQuery;
    private String _outputProcessingQuery;

    /**
     * Null constructor inserted for hibernate
     */
    protected YMultiInstanceAttributes() {
    	_minInstances = Integer.valueOf(1);
    	_maxInstances = Integer.valueOf(1);
    	_creationMode = _creationModeStatic;
    }


    public YMultiInstanceAttributes(YTask container, String minInstancesQuery,
                                       String maxInstancesQuery, String thresholdQuery, String creationMode) {
        _myTask = container;
        try {
            _minInstances = new Integer(minInstancesQuery);
        } catch (NumberFormatException e) {
            _minInstancesQuery = minInstancesQuery;
        }
        try {
            _maxInstances = new Integer(maxInstancesQuery);
        } catch (NumberFormatException e) {
            _maxInstancesQuery = maxInstancesQuery;
        }
        try {
            _threshold = new Integer(thresholdQuery);
        } catch (NumberFormatException e) {
            _thresholdQuery = thresholdQuery;
        }
        _creationMode = creationMode;
    }

    @Transient
    public int getMinInstances() throws YQueryException {
        if (_minInstances != null) {
            return _minInstances.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_minInstancesQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
        	logger.debug(e.getMessage());
        	throw new YQueryException("MI query for min instances failed (query:"
        			+ _minInstancesQuery + ")", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The minInstances query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }
    
    /**
     * Inserted for hibernate TODO Set to protected later
     * @return
     * @hibernate.property column="MIN_INSTANCES"
     */
    @Column(name="min_instances", nullable=true)
    protected Integer getMinInstancesHibernate() {
    	return _minInstances;
    }
    /**
     * Inserted for hibernate TODO Set to protected later
     */
    protected void setMinInstancesHibernate(Integer i) {
    	_minInstances = i == 0 ? null : i;
    }

    @Transient
    public int getMaxInstances() throws YQueryException {
        if (_maxInstances != null) {
            return _maxInstances.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_maxInstancesQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
        	logger.debug(e.getMessage());
        	throw new YQueryException("MI query for max instances failed (query:"
        			+ _maxInstancesQuery + ")", e);
        } catch (ClassCastException e) {
            throw new RuntimeException("The maxInstances query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }

    /**
     * Inserted for hibernate
     * @return
     * @hibernate.property column="MAX_INSTANCES"
     */
    @Column(name="max_instances", nullable=true)
    protected Integer getMaxInstancesHibernate() {
    	return _maxInstances;
    }
    /**
     * Inserted for hibernate
     * @param i
     */
    protected void setMaxInstancesHibernate(Integer i) {
    	_maxInstances = i == 0 ? null : i;
    }

    @Transient
    public int getThreshold() {
        if (_threshold != null) {
            return _threshold.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_thresholdQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
            //e.printStackTrace();
            /*
             * TODO Tore: Syntax error messages (JDOMExceptions) should be thrown???
             * Right now assuming that when an error occurs, we default to one
             * */
        	result = 1;
        	logger.debug(e.getMessage());
        } catch (ClassCastException e) {
            throw new RuntimeException("The threshold query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }
    
    /**
     * Inserted for hibernate
     * @return
     * @hibernate.property column="THRESHOLD"
     */
    @Column(name="threshold", nullable=true)
    protected Integer getThresholdHibernate() {
    	return _threshold;
    }
    /**
     * Inserted for hibernate
     * @param i
     */
    protected void setThresholdHibernate(Integer i) {
    	_threshold = i == 0 ? null : i;
    }

    /**
     * 
     * @return
     * @hibernate.property column="CREATION_MODE"
     */
    @Basic
    public String getCreationMode() {
        return this._creationMode;
    }
    /**
     * Inserted for hibernate
     * @param s
     */
    public void setCreationMode(String s) {
    	_creationMode = s;
    }


    public List verify() {
        List messages = new Vector();
        if (_minInstances != null && _minInstances.intValue() < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + " _minInstances < 1", YVerificationMessage.ERROR_STATUS));
        }
        if (_minInstances != null && _maxInstances != null &&
                _minInstances.intValue() > _maxInstances.intValue()) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._minInstances > _maxInstances", YVerificationMessage.ERROR_STATUS));
        }
        if (_maxInstances != null && _maxInstances.intValue() < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._maxInstances < 1", YVerificationMessage.ERROR_STATUS));
        }
/*        if(_threshold != null && _minInstances != null && _threshold.intValue() < _minInstances.intValue()){
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._threshold < _minInstances"));
        }*/
        if (_threshold != null && _threshold.intValue() < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._threshold < 1", YVerificationMessage.ERROR_STATUS));
        }
        if (!(_creationMode.equalsIgnoreCase("static") || _creationMode.equalsIgnoreCase("dynamic"))) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._creationMode does not equal 'static' or 'dynamic'", YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }

    public void setUniqueInputMISplittingQuery(String inputQuery) {
        _inputSplittingQuery = inputQuery;
    }

    /**
     * 
     * @return
     * @hibernate.property column="MI_SPLITTING_QUERY"
     */
    @Lob
    @Column(name="splitting_query")
    public String getMISplittingQuery() {
        return _inputSplittingQuery;
    }
    /**
     * Inserted for hibernate
     * 
     * @param s
     */
    public void setMISplittingQuery(String s) {
    	setUniqueInputMISplittingQuery(s);
    }

    /**
     * 
     * @return
     * @hibernate.property column="MI_FORMAL_INPUT_PARAM"
     */    
    @Column(name="formal_input_param")
    public String getMIFormalInputParam() {
        return _inputVarName;
    }

    public void setMIFormalInputParam(String variableName) {
        _inputVarName = variableName;
    }

    /**
     * 
     * @return
     * @hibernate.property column="MI_FORMAL_OUTPUT_QUERY"
     */
    @Lob
    @Column(name="formal_output_query")
    public String getMIFormalOutputQuery() {
        return _remoteOutputQuery;
    }

    public void setMIFormalOutputQuery(String remoteOutputQuery) {
        _remoteOutputQuery = remoteOutputQuery;
    }

    /**
     * 
     * @return
     * @hibernate.property column="MI_JOINING_QUERY"
     */
    @Lob
    @Column(name="joining_query")
    public String getMIJoiningQuery() {
        return _outputProcessingQuery;
    }
    /**
     * Inserted for hibernate TODO Set to protected later
     * @param s
     */
    public void setMIJoiningQuery(String s) {
    	setUniqueOutputMIJoiningQuery(s);
    }

    public void setUniqueOutputMIJoiningQuery(String outputProcessingQuery) {
        _outputProcessingQuery = outputProcessingQuery;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();

        xml.append("<minimum>" + (_minInstances != null ? _minInstances.toString() : YTask.marshal(_minInstancesQuery)) + "</minimum>");
        xml.append("<maximum>" + (_maxInstances != null ? _maxInstances.toString() : YTask.marshal(_maxInstancesQuery)) + "</maximum>");
        xml.append("<threshold>" + (_threshold != null ? _threshold.toString() : YTask.marshal(_thresholdQuery)) + "</threshold>");
        xml.append("<creationMode code=\"" + _creationMode + "\"/>");
        xml.append("<miDataInput>");
        xml.append("<expression query=\"" + YTask.marshal(((YTask)_myTask).getPreSplittingMIQuery()) + "\"/>");
        xml.append("<splittingExpression query=\"" + YTask.marshal(_inputSplittingQuery) + "\"/>");
        xml.append("<formalInputParam>" + _inputVarName + "</formalInputParam>");
        xml.append("</miDataInput>");
        if (_remoteOutputQuery != null) {
            xml.append("<miDataOutput>");
            xml.append("<formalOutputExpression query=\"" + YTask.marshal(_remoteOutputQuery) + "\"/>");
            xml.append("<outputJoiningExpression query=\"" + YTask.marshal(_outputProcessingQuery) + "\"/>");
            xml.append("<resultAppliedToLocalVariable>" +
            		((YTask)_myTask).getMIOutputAssignmentVar(_remoteOutputQuery) +
                    "</resultAppliedToLocalVariable>"
            );
            xml.append("</miDataOutput>");
        }
        return xml.toString();
    }

    @Transient
    public boolean isMultiInstance() {
        if (_maxInstances != null) {
            return _maxInstances.intValue() > 1;
        } else {
            return _maxInstancesQuery != null;
        }
    }


    public Object clone() {
        YMultiInstanceAttributes copy = null;
        try {
            copy = (YMultiInstanceAttributes) super.clone();
            copy._id = null;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copy;
    }

    private Long _id;
    /**
     * Method inserted for hibernate
     * @hibernate.id column="MULTI_INSTANCE_ID"
     */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	protected Long getID() {
		return _id;
	}

	/**
	 * Method inserted for hibernate
	 */
    protected void setID( Long id ) {
		_id = id;
	}
    

    @OneToOne(mappedBy = "multiInstanceAttributes")
    public YExternalNetElement getContainerTask() {
    	return _myTask;
    }
    
    public void setContainerTask(YExternalNetElement task) {
    	_myTask = task;
    }


	@Lob
	public void setMaxInstancesQuery(String instancesQuery) {
		_maxInstancesQuery = instancesQuery;
	}


	public String getMaxInstancesQuery() {
		return _maxInstancesQuery;
	}


	@Lob
	public void setMinInstancesQuery(String instancesQuery) {
		_minInstancesQuery = instancesQuery;
	}


	public String getMinInstancesQuery() {
		return _minInstancesQuery;
	}


	@Lob
	public void setThresholdQuery(String query) {
		_thresholdQuery = query;
	}

	
	public String getThresholdQuery() {
		return _thresholdQuery;
	}
	
}//end class

