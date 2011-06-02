/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.yawlfoundation.yawl.util.JDOMUtil;
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


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_minInstances != null && _minInstances < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + " _minInstances < 1", YVerificationMessage.ERROR_STATUS));
        }
        if (_minInstances != null && _maxInstances != null &&
                _minInstances > _maxInstances) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._minInstances > _maxInstances", YVerificationMessage.ERROR_STATUS));
        }
        if (_maxInstances != null && _maxInstances < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._maxInstances < 1", YVerificationMessage.ERROR_STATUS));
        }
        if (_threshold != null && _threshold < 1) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._threshold < 1", YVerificationMessage.ERROR_STATUS));
        }
        if (!(_creationMode.equalsIgnoreCase("static") || _creationMode.equalsIgnoreCase("dynamic"))) {
            messages.add(new YVerificationMessage(_myTask, _myTask
                    + "._creationMode does not equal 'static' or 'dynamic'",
                    YVerificationMessage.ERROR_STATUS));
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
        StringBuilder xml = new StringBuilder();

        xml.append("<minimum>" + (_minInstances != null ? _minInstances.toString() : JDOMUtil.encodeEscapes(_minInstancesQuery)) + "</minimum>");
        xml.append("<maximum>" + (_maxInstances != null ? _maxInstances.toString() : JDOMUtil.encodeEscapes(_maxInstancesQuery)) + "</maximum>");
        xml.append("<threshold>" + (_threshold != null ? _threshold.toString() : JDOMUtil.encodeEscapes(_thresholdQuery)) + "</threshold>");
        xml.append("<creationMode code=\"" + _creationMode + "\"/>");
        xml.append("<miDataInput>");
        xml.append("<expression query=\"" + JDOMUtil.encodeEscapes(_myTask.getPreSplittingMIQuery()) + "\"/>");
        xml.append("<splittingExpression query=\"" + JDOMUtil.encodeEscapes(_inputSplittingQuery) + "\"/>");
        xml.append("<formalInputParam>" + _inputVarName + "</formalInputParam>");
        xml.append("</miDataInput>");
        if (_remoteOutputQuery != null) {
            xml.append("<miDataOutput>");
            xml.append("<formalOutputExpression query=\"" + JDOMUtil.encodeEscapes(_remoteOutputQuery) + "\"/>");
            xml.append("<outputJoiningExpression query=\"" + JDOMUtil.encodeEscapes(_outputProcessingQuery) + "\"/>");
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

