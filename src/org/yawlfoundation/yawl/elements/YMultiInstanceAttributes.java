/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;

/**
 * 
 * A collection of attributes that apply to multiple instance tasks.  Used as a property
 * of YTask.
 * Date: 15/04/2003
 * Time: 12:06:19
 * 
 */
public final class YMultiInstanceAttributes implements Cloneable, YVerifiable {

    public final static String CREATION_MODE_DYNAMIC = "dynamic";
    public final static String CREATION_MODE_STATIC = "static";

    private final Logger _log = LogManager.getLogger(this.getClass());

    private YTask _task;
    private Integer _minInstances;
    private Integer _maxInstances;
    private Integer _threshold;
    private String _minInstancesQuery;
    private String _maxInstancesQuery;
    private String _thresholdQuery;
    private String _creationMode;
    private String _inputVarName;
    private String _inputSplittingQuery;
    private String _remoteOutputQuery;
    private String _outputProcessingQuery;


    public YMultiInstanceAttributes(YTask container) {
        // max and threshold initially set to 'infinity' (internally Integer.MAX_VALUE)
        this(container, "1", "2147483647",
                "2147483647", CREATION_MODE_STATIC);
    }


    protected YMultiInstanceAttributes(YTask container, String minInstancesQuery,
                                       String maxInstancesQuery, String thresholdQuery,
                                       String creationMode) {
        _task = container;
        setProperties(minInstancesQuery, maxInstancesQuery, thresholdQuery, creationMode);
    }


    protected YTask getTask() { return _task; }

    protected void setTask(YTask task) { _task = task; }


    public int getMinInstances() {
        if (_minInstances != null) return _minInstances;

        try {
            return getQueryValue(_minInstancesQuery);
        }
        catch (Exception e) {
            _log.warn("The minInstances query at {} didn't produce numerical output" +
                    " as expected. Returning default 1.", _task);
            return 1;
        }
    }


    public String getMinInstancesQuery() {
        return _minInstances != null ? _minInstances.toString() : _minInstancesQuery;
    }


    public int getMaxInstances() {
        if (_maxInstances != null) return _maxInstances;

        try {
            return getQueryValue(_maxInstancesQuery);
        }
        catch (Exception e) {
            _log.warn("The maxInstances query at {} didn't produce numerical output" +
                    " as expected. Returning default 2.",  _task);
            return 2;
        }
    }


    public String getMaxInstancesQuery() {
        return _maxInstances != null ? _maxInstances.toString() : _maxInstancesQuery;
    }


    public int getThreshold() {
        if (_threshold != null) return _threshold;

        try {
            return getQueryValue(_thresholdQuery);
        }
        catch (Exception e) {
            _log.warn("The threshold query at {} didn't produce numerical output" +
                    " as expected. Returning default 1.", _task);
            return 1;
        }
    }


    public String getThresholdQuery() {
        return _threshold != null ? _threshold.toString() : _thresholdQuery;
    }


    public String getCreationMode() {
        return this._creationMode;
    }


    public boolean isDynamicCreationMode() {
        return _creationMode.equalsIgnoreCase(CREATION_MODE_DYNAMIC);
    }


    public void setProperties(String minInstancesQuery, String maxInstancesQuery,
                              String thresholdQuery, String creationMode) {
        try {
            _minInstances = new Integer(minInstancesQuery);
        } catch (NumberFormatException e) {
            _minInstancesQuery = minInstancesQuery;
            _minInstances = null;
        }
        try {
            _maxInstances = new Integer(maxInstancesQuery);
        } catch (NumberFormatException e) {
            _maxInstancesQuery = maxInstancesQuery;
            _maxInstances = null;
        }
        try {
            _threshold = new Integer(thresholdQuery);
        } catch (NumberFormatException e) {
            _thresholdQuery = thresholdQuery;
            _threshold = null;
        }
        _creationMode = creationMode;
    }


    public void verify(YVerificationHandler handler) {
        if (_minInstances != null && _minInstances < 1) {
            handler.error(_task, _task + " minInstances < 1");
        }
        if (_minInstances != null && _maxInstances != null && _minInstances > _maxInstances) {
            handler.error(_task, _task + ".minInstances > _maxInstances");
        }
        if (_maxInstances != null && _maxInstances < 1) {
            handler.error(_task, _task + ".maxInstances < 1");
        }
        if (_threshold != null && _threshold < 1) {
            handler.error(_task, _task + ".threshold < 1");
        }
        if (!(_creationMode.equalsIgnoreCase("static") || _creationMode.equalsIgnoreCase("dynamic"))) {
            handler.error(_task, _task
                    + ".creationMode does not equal 'static' or 'dynamic'");
        }
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

    public String getMIOutputAssignmentVar() {
        return _remoteOutputQuery != null ?
                _task.getMIOutputAssignmentVar(_remoteOutputQuery) : null;
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
        xml.append(StringUtil.wrap(JDOMUtil.encodeEscapes(getMinInstancesQuery()), "minimum"));
        xml.append(StringUtil.wrap(JDOMUtil.encodeEscapes(getMaxInstancesQuery()), "maximum"));
        xml.append(StringUtil.wrap(JDOMUtil.encodeEscapes(getThresholdQuery()), "threshold"));
        xml.append("<creationMode code=\"" + _creationMode + "\"/>");
        xml.append("<miDataInput>");
        xml.append("<expression query=\"" + JDOMUtil.encodeEscapes(_task.getPreSplittingMIQuery()) + "\"/>");
        xml.append("<splittingExpression query=\"" + JDOMUtil.encodeEscapes(_inputSplittingQuery) + "\"/>");
        xml.append(StringUtil.wrap(_inputVarName, "formalInputParam"));
        xml.append("</miDataInput>");
        if (_remoteOutputQuery != null) {
            xml.append("<miDataOutput>");
            xml.append("<formalOutputExpression query=\"" + JDOMUtil.encodeEscapes(_remoteOutputQuery) + "\"/>");
            xml.append("<outputJoiningExpression query=\"" + JDOMUtil.encodeEscapes(_outputProcessingQuery) + "\"/>");
            xml.append("<resultAppliedToLocalVariable>" +
                    getMIOutputAssignmentVar() +
                    "</resultAppliedToLocalVariable>"
            );
            xml.append("</miDataOutput>");
        }
        return xml.toString();
    }

    public boolean isMultiInstance() {
        return _maxInstances != null ? _maxInstances > 1 : _maxInstancesQuery != null;
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

    private int getQueryValue(String query) throws IllegalArgumentException {
        Element element = JDOMUtil.selectElement(_task._net.getInternalDataDocument(), query);
        if (element == null) throw new IllegalArgumentException();
        return new Integer(element.getText());
    }

}//end class

