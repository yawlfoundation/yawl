/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.util.List;
import java.util.Vector;

/**
 * 
 * A collection of attributes that apply to multiple instance tasks.  Used as a property
 * of YTask.
 * Date: 15/04/2003
 * Time: 12:06:19
 * 
 */
public final class YMultiInstanceAttributes implements Cloneable, YVerifiable {
    public final static String _creationModeDynamic = "dynamic";
    public final static String _creationModeStatic = "static";

    private Integer _minInstances;
    private Integer _maxInstances;
    private Integer _threshold;
    private String _minInstancesQuery;
    private String _maxInstancesQuery;
    private String _thresholdQuery;
    private String _creationMode;
    YTask _myTask;
    private String _inputVarName;
    private String _inputSplittingQuery;
    private String _remoteOutputQuery;
    private String _outputProcessingQuery;


    protected YMultiInstanceAttributes(YTask container, String minInstancesQuery,
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


    public int getMinInstances() {
        if (_minInstances != null) {
            return _minInstances.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_minInstancesQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            throw new RuntimeException("The minInstances query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }


    public int getMaxInstances() {
        if (_maxInstances != null) {
            return _maxInstances.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_maxInstancesQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            throw new RuntimeException("The maxInstances query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }


    public int getThreshold() {
        if (_threshold != null) {
            return _threshold.intValue();
        }
        Number result = null;
        try {
            XPath xpath = XPath.newInstance(_thresholdQuery);
            result = (Number) xpath.selectSingleNode(_myTask._net.getInternalDataDocument());
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            throw new RuntimeException("The threshold query at " + _myTask
                    + " didn't produce numerical output as excepted.");
        }
        return result.intValue();
    }


    public String getCreationMode() {
        return this._creationMode;
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

    public String getMISplittingQuery() {
        return _inputSplittingQuery;
    }

    public String getMIFormalInputParam() {
        return _inputVarName;
    }

    public void setMIFormalInputParam(String variableName) {
        _inputVarName = variableName;
    }

    public String getMIFormalOutputQuery() {
        return _remoteOutputQuery;
    }

    public void setMIFormalOutputQuery(String remoteOutputQuery) {
        _remoteOutputQuery = remoteOutputQuery;
    }

    public String getMIJoiningQuery() {
        return _outputProcessingQuery;
    }

    public void setUniqueOutputMIJoiningQuery(String outputProcessingQuery) {
        _outputProcessingQuery = outputProcessingQuery;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();

        xml.append("<minimum>" + (_minInstances != null ? _minInstances.toString() : _myTask.marshal(_minInstancesQuery)) + "</minimum>");
        xml.append("<maximum>" + (_maxInstances != null ? _maxInstances.toString() : _myTask.marshal(_maxInstancesQuery)) + "</maximum>");
        xml.append("<threshold>" + (_threshold != null ? _threshold.toString() : _myTask.marshal(_thresholdQuery)) + "</threshold>");
        xml.append("<creationMode code=\"" + _creationMode + "\"/>");
        xml.append("<miDataInput>");
        xml.append("<expression query=\"" + _myTask.marshal(_myTask.getPreSplittingMIQuery()) + "\"/>");
        xml.append("<splittingExpression query=\"" + _myTask.marshal(_inputSplittingQuery) + "\"/>");
        xml.append("<formalInputParam>" + _inputVarName + "</formalInputParam>");
        xml.append("</miDataInput>");
        if (_remoteOutputQuery != null) {
            xml.append("<miDataOutput>");
            xml.append("<formalOutputExpression query=\"" + _myTask.marshal(_remoteOutputQuery) + "\"/>");
            xml.append("<outputJoiningExpression query=\"" + _myTask.marshal(_outputProcessingQuery) + "\"/>");
            xml.append("<resultAppliedToLocalVariable>" +
                    _myTask.getMIOutputAssignmentVar(_remoteOutputQuery) +
                    "</resultAppliedToLocalVariable>"
            );
            xml.append("</miDataOutput>");
        }
        return xml.toString();
    }

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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return copy;
    }
}//end class

