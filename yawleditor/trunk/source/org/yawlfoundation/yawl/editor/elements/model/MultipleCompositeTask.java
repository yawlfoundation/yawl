/*
 * Created on 23/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.elements.model;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;

import java.awt.geom.Point2D;

public class MultipleCompositeTask extends YAWLTask
        implements YAWLMultipleInstanceTask, YAWLCompositeTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private MultipleInstanceTaskConfigSet configureSet;

    private long _minimumInstances;
    private long _maximumInstances;
    private long _continuationThreshold;
    private int _instanceCreationType;
    private DataVariable _multipleInstanceVariable;
    private String _splitterQuery;
    private String _aggregateQuery;
    private DataVariable _resultNetVariable;


    /**
     * This constructor is ONLY to be invoked when we are reconstructing an
     * multiple composite task from saved state. Ports will not be created with this
     * constructor, as they are already part of the JGraph state-space.
     */

    public MultipleCompositeTask() {
        this(null);
    }

    /**
     * This constructor is to be invoked whenever we are creating a new
     * multiple composite task from scratch. It also creates the correct ports
     * needed for the task as an intended side-effect.
     */

    public MultipleCompositeTask(Point2D startPoint) {
        super(startPoint);
        initialise();
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
        if (getDecomposition() != null) {
            return getDecomposition().getLabel();
        }
        return "";
    }

    public void setDecomposition(Decomposition decomposition) {
        if (getDecomposition() == null ||
                !getDecomposition().equals(decomposition)) {
            super.setDecomposition(decomposition);
            resetParameterLists();
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


    public DataVariable getMultipleInstanceVariable() {
        return _multipleInstanceVariable;
    }

    public void setMultipleInstanceVariable(DataVariable variable) {

        if (! ((_multipleInstanceVariable == null) ||
               _multipleInstanceVariable.equals(variable))) {

            // destroy now defunct accessor query for multiple instance variable */
            getParameterLists().getInputParameters().remove(_multipleInstanceVariable);
        }

        _multipleInstanceVariable = variable;
    }


    public String getAccessorQuery() {
        return getParameterLists().getInputParameters().getQueryFor(
                getMultipleInstanceVariable()
        );
    }

    public void setAccessorQuery(String query) {
        if (getMultipleInstanceVariable() != null) {
            getParameterLists().getInputParameters().setQueryFor(
                    getMultipleInstanceVariable(), query
            );
        }
    }


    public String getSplitterQuery() {
        return _splitterQuery;
    }

    public void setSplitterQuery(String query) {
        _splitterQuery = query;
    }


    public String getInstanceQuery() {
        return getParameterLists().getOutputParameters().getQueryFor(
                getResultNetVariable()
        );
    }

    public void setInstanceQuery(String query) {
        if (getResultNetVariable() != null) {
            getParameterLists().getOutputParameters().setQueryFor(
                    getResultNetVariable(), query
            );
        }
    }


    public String getAggregateQuery() {
        return _aggregateQuery;
    }

    public void setAggregateQuery(String query) {
        _aggregateQuery = query;
    }


    public DataVariable getResultNetVariable() {
        return _resultNetVariable;
    }

    public void setResultNetVariable(DataVariable variable) {
        if (! ((_resultNetVariable == null) ||
               _resultNetVariable.equals(variable))) {

            // destroy now defunct instance query for result net variable */
            getParameterLists().getOutputParameters().remove(_resultNetVariable);
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
