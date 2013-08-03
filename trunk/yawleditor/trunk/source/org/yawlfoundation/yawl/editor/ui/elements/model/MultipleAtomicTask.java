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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YVariable;

import java.awt.geom.Point2D;

public class MultipleAtomicTask extends YAWLTask
        implements YAWLMultipleInstanceTask, YAWLAtomicTask {

    private MultipleInstanceTaskConfigSet configureSet;
    private YMultiInstanceAttributes _miAttributes;

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
     * multiple atomic task from scratch. It also creates the correct ports needed for
     * the task  as an intended side-effect.
     */

    public MultipleAtomicTask(Point2D startPoint) {
        this(startPoint, (String) null);
    }

    public MultipleAtomicTask(Point2D startPoint, YTask yTask) {
        this(startPoint, (String) null);
        setYAWLElement(yTask);
    }


    /**
     * This constructor is to be invoked whenever we are creating a new
     * atomic task from scratch with an icon. It also creates the correct
     * ports needed for the task as an intended side-effect.
     */

    public MultipleAtomicTask(Point2D startPoint, String iconPath) {
        super(startPoint, iconPath);
        initialise();
    }

    public void setYAWLElement(YTask shadow) {
        super.setTask(shadow);
        _miAttributes = shadow.getMultiInstanceAttributes();
    }


    public void iniConfigure() {
        configureSet = new MultipleInstanceTaskConfigSet(this);
    }

    private void initialise() {
        setMinimumInstances(1);
        setMaximumInstances(2);
        setContinuationThreshold(1);
        setInstanceCreationType(STATIC_INSTANCE_CREATION);
    }


    public long getMinimumInstances() { return _miAttributes.getMinInstances(); }

    public void setMinimumInstances(long instanceBound) {
//        _minimumInstances = instanceBound;
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

            // destroy now defunct accessor query for multiple instance variable */
//            getParameterLists().getInputParameters().remove(_multipleInstanceVariable);
        }

        _multipleInstanceVariable = variable;
    }


    public String getAccessorQuery() {
//        return getParameterLists().getInputParameters().getQueryFor(
//                getMultipleInstanceVariable()
//        );
        return "";
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
   //         getParameterLists().getOutputParameters().remove(_resultNetVariable);
        }

        _resultNetVariable = variable;
    }


    public void setDecomposition(YDecomposition decomposition) {
        super.setDecomposition(decomposition);
    }

    public YDecomposition getDecomposition() {
        return super.getDecomposition();
    }

    public String getType() {
        return "Multiple Atomic Task";
    }


    public MultipleInstanceTaskConfigSet getConfigurationInfor() {
        return this.configureSet;
    }
}
