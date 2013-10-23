/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.editor.ui.configuration.MultipleInstanceTaskConfigSet;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YVariable;

import java.awt.geom.Point2D;

public class MultipleCompositeTask extends YAWLTask
        implements YAWLMultipleInstanceTask, YAWLCompositeTask {

    private MultipleInstanceTaskConfigSet configureSet;

    private long _minimumInstances;
    private long _maximumInstances;
    private long _continuationThreshold;
    private int _instanceCreationType;
    private YVariable _multipleInstanceVariable;
    private String _splitterQuery;
    private String _aggregateQuery;
    private YVariable _resultNetVariable;



    /**
     * This constructor is to be invoked whenever we are creating a new
     * multiple composite task from scratch. It also creates the correct ports
     * needed for the task as an intended side-effect.
     */

    public MultipleCompositeTask(Point2D startPoint) {
        super(startPoint);
        initialise();
    }

    public MultipleCompositeTask(Point2D startPoint, YTask yTask) {
        super(startPoint);
        setTask(yTask);
    }


    public void iniConfigure() {
        configureSet = new MultipleInstanceTaskConfigSet(this);
    }

    private void initialise() {
        setMinimumInstances(1);
        setMaximumInstances(1);
        setContinuationThreshold(1);
        setInstanceCreationType(STATIC_INSTANCE_CREATION);
        setMultipleInstanceVariable(null);
        setResultNetVariable(null);
        setSplitterQuery("true()");
        setAggregateQuery("true()");
    }

    public String getUnfoldingNetName() {
        return getDecomposition() != null ? getDecomposition().getID() : "";
    }

    public void setDecomposition(YDecomposition decomposition) {
        if (getDecomposition() == null ||
                !getDecomposition().equals(decomposition)) {
            super.setDecomposition(decomposition);
        }
    }

    public long getMinimumInstances() { return _minimumInstances; }

    public void setMinimumInstances(long instanceBound) {
        _minimumInstances = instanceBound;
    }


    public long getMaximumInstances() {
        return _maximumInstances;
    }

    public void setMaximumInstances(long instanceBound) {
        _maximumInstances = instanceBound;
    }


    public long getContinuationThreshold() {
        return _continuationThreshold;
    }

    public void setContinuationThreshold(long threshold) {
        _continuationThreshold = threshold;
    }


    public int getInstanceCreationType() {
        return _instanceCreationType;
    }

    public void setInstanceCreationType(int creationType) {
        _instanceCreationType = creationType;
    }


    public YVariable getMultipleInstanceVariable() {
        return _multipleInstanceVariable;
    }

    public void setMultipleInstanceVariable(YVariable variable) {

        if (! ((_multipleInstanceVariable == null) ||
               _multipleInstanceVariable.equals(variable))) {

        }

        _multipleInstanceVariable = variable;
    }


    public String getAccessorQuery() {
//        return getParameterLists().getInputParameters().getQueryFor(
//                getMultipleInstanceVariable()
//        );
        return null;
    }

    public void setAccessorQuery(String query) {
        if (getMultipleInstanceVariable() != null) {
//            getParameterLists().getInputParameters().setQueryFor(
//                    getMultipleInstanceVariable(), query
//            );
        }
    }


    public String getSplitterQuery() {
        return _splitterQuery;
    }

    public void setSplitterQuery(String query) {
        _splitterQuery = query;
    }


    public String getInstanceQuery() {
//        return getParameterLists().getOutputParameters().getQueryFor(
//                getResultNetVariable()
//        );
        return null;
    }

    public void setInstanceQuery(String query) {
        if (getResultNetVariable() != null) {
//            getParameterLists().getOutputParameters().setQueryFor(
//                    getResultNetVariable(), query
//            );
        }
    }


    public String getAggregateQuery() {
        return _aggregateQuery;
    }

    public void setAggregateQuery(String query) {
        _aggregateQuery = query;
    }


    public YVariable getResultNetVariable() {
        return _resultNetVariable;
    }

    public void setResultNetVariable(YVariable variable) {
        if (! ((_resultNetVariable == null) ||
               _resultNetVariable.equals(variable))) {

            // destroy now defunct instance query for result net variable */
//            getParameterLists().getOutputParameters().remove(_resultNetVariable);
        }

        _resultNetVariable = variable;
    }

    public String getType() {
        return "Multiple Composite Task";
    }


    public MultipleInstanceTaskConfigSet getConfigurationInfor() {

        return this.configureSet;
    }
}
