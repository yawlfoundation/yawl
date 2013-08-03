/*
 * Created on 14/11/2003
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

import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class YAWLTask extends YAWLVertex {

    private boolean _configurable;
    private boolean _hasBeenConfigureInitialised;
    private List<CPort> inputCPorts = new ArrayList<CPort>();
    private List<CPort> outputCPorts = new ArrayList<CPort>();
    private boolean _cancellationSetEnable = true;

    private CancellationSet _cancellationSet;

    private String _iconPath;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a task
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */
    public YAWLTask() {
        this(null, null);
        _configurable = false;
        _cancellationSetEnable = true;
    }

    /**
     * This constructor is to be invoked whenever we are creating a new task
     * from scratch. It also creates the correct ports needed for the vertex
     * as an intended side-effect.
     */
    public YAWLTask(Point2D startPoint) {
        this(startPoint, null);
    }

    public YAWLTask(Point2D startPoint, String iconPath) {
        super(startPoint);
        initialize(iconPath);
    }


    public void setTask(YTask task) { setYAWLElement(task); }

    public YTask getTask() { return (YTask) getYAWLElement(); }


    private void initialize(String iconPath) {
        setCancellationSet(new CancellationSet(this));
        setIconPath(iconPath);
    }

    public void setIconPath(String path) { _iconPath = path; }

    public String getIconPath() { return _iconPath; }


    public String getID() { return _yawlElement.getID(); }

    public void setID(String id) { _yawlElement.setID(id); }


    public String getName() { return _yawlElement.getName(); }

    public void setName(String name) { _yawlElement.setName(name); }


    public String getDocumentation() { return _yawlElement.getDocumentation(); }

    public void setDocumentation(String doco) { _yawlElement.setDocumentation(doco); }




    public int getSplitDecoratorPos() {
        if (getSplitDecorator() != null) {
            return getSplitDecorator().getCardinalPosition();
        }
        return getPositionOfOutgoingFlow();
    }

    public int getJoinDecoratorPos() {
        if (getJoinDecorator() != null) {
            return getJoinDecorator().getCardinalPosition();
        }
        return this.getPositionOfIncomingFlow();
    }

    public int hasJoinDecoratorAt() {
        return getJoinDecorator() != null ?
                getJoinDecorator().getCardinalPosition() : NOWHERE;
    }

    public int hasSplitDecoratorAt() {
        return getSplitDecorator() != null ?
                getSplitDecorator().getCardinalPosition() : NOWHERE;
    }

    public boolean hasSplitDecorator() {
        return getSplitDecorator() != null;
    }

    public boolean hasJoinDecorator() {
        return getJoinDecorator() != null;
    }

    public boolean hasDecoratorAtPosition(int position) {
        return decoratorTypeAtPosition(position) != Decorator.NO_TYPE;
    }

    public int decoratorTypeAtPosition(int position) {
        if (getJoinDecorator() != null &&
                getJoinDecorator().getCardinalPosition() == position) {
            return getJoinDecorator().getType();
        }
        if (getSplitDecorator() != null &&
                getSplitDecorator().getCardinalPosition() == position) {
            return getSplitDecorator().getType();
        }
        return Decorator.NO_TYPE;
    }

    public YAWLPort getDefaultSourcePort() {
        if (hasSplitDecorator()) {
            return getSplitDecorator().getDefaultPort();
        }
        return super.getDefaultSourcePort();
    }

    public YAWLPort getDefaultTargetPort() {
        if (hasJoinDecorator()) {
            return getJoinDecorator().getDefaultPort();
        }
        return super.getDefaultTargetPort();
    }

    public YAWLFlowRelation getOnlyOutgoingFlow() {
        if (getPositionOfOutgoingFlow() != NOWHERE) {
            return (YAWLFlowRelation)
                    (getPortAt(getPositionOfOutgoingFlow()).getEdges().toArray())[0];
        }
        return null;
    }

    public YAWLFlowRelation getOnlyIncomingFlow() {
        if (getPositionOfIncomingFlow() != NOWHERE) {

            return (YAWLFlowRelation)
                    (getPortAt(getPositionOfIncomingFlow()).getEdges().toArray())[0];
        }
        return null;
    }

    public JoinDecorator getJoinDecorator() {
        VertexContainer container = (VertexContainer) this.getParent();
        if (container == null) {
            return null;
        }
        return container.getJoinDecorator();
    }

    public SplitDecorator getSplitDecorator() {
        VertexContainer container = (VertexContainer) this.getParent();
        if (container == null) {
            return null;
        }
        return container.getSplitDecorator();
    }

    public Rectangle2D getBounds() {
        return GraphConstants.getBounds(getAttributes());
    }

    public Point2D getLocation() {
        return new Point2D.Double(getBounds().getX(), getBounds().getY());
    }

    public HashSet<YAWLFlowRelation> getIncomingFlows() {
        if (hasJoinDecorator()) {
            return getJoinDecorator().getFlows();
        }
        return super.getIncomingFlows();
    }


    public int getIncomingFlowCount() {
        return getJoinDecorator() != null ?
                getJoinDecorator().getFlowCount() : super.getIncomingFlows().size();
    }

    public HashSet<YAWLFlowRelation> getOutgoingFlows() {
        if (hasSplitDecorator()) {
            return getSplitDecorator().getFlows();
        }
        return super.getOutgoingFlows();
    }


    public int getOutgoingFlowCount() {
        return getSplitDecorator() != null ?
                getSplitDecorator().getFlowCount() : super.getOutgoingFlows().size();
    }

    public CancellationSet getCancellationSet() {
        return _cancellationSet;
    }

    public void setCancellationSet(CancellationSet set) {
        _cancellationSet = set;
    }

    public boolean hasCancellationSetMembers() {
        return getCancellationSet().hasMembers();
    }


    public void setDecomposition(YDecomposition decomposition) {
        ((YTask) _yawlElement).setDecompositionPrototype(decomposition);
    }

    public YDecomposition getDecomposition() {
        return ((YTask) _yawlElement).getDecompositionPrototype();
    }


    public void setCustomFormURL(String urlStr) throws MalformedURLException {
        setCustomFormURL(urlStr != null ? new URL(urlStr) : null);
    }

    public void setCustomFormURL(URL url) {
        ((YTask) _yawlElement).setCustomFormURI(url);
    }

    public URL getCustomFormURL() {
        return ((YTask) _yawlElement).getCustomFormURL();
    }

    public boolean hasBothDecorators() {
        return (hasJoinDecorator() && hasSplitDecorator());
    }

    public boolean hasTopLeftAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.TOP);
    }

    public boolean hasTopRightAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.TOP, Decorator.RIGHT);
    }

    public boolean hasBottomRightAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.RIGHT, Decorator.BOTTOM);
    }

    public boolean hasBottomLeftAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.BOTTOM);
    }

    public boolean hasVerticallyAlignedDecorators() {
        return hasDecoratorPairAt(Decorator.TOP, Decorator.BOTTOM);
    }

    public boolean hasHorizontallyAlignedDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.RIGHT);
    }

    private boolean hasDecoratorPairAt(int first, int second) {
        return hasDecoratorAtPosition(first) &&
               hasDecoratorAtPosition(second);
    }

    public boolean hasNoSelfReferencingFlows() {
        HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        if (! hasBothDecorators()) return true;

        flows.addAll(getSplitDecorator().getFlows());
        flows.addAll(getJoinDecorator().getFlows());
        for (YAWLFlowRelation flow : flows) {
            if (flow.connectsTaskToItself()) {
                return false;
            }
        }
        return true;
    }

    public void detachFlow(YAWLFlowRelation flow) {
        if (!(hasSplitDecorator() || hasJoinDecorator())) {
            super.detachFlow(flow);
        }
        if (hasSplitDecorator()) {
            getSplitDecorator().detachFlow(flow);
        }
        if (hasJoinDecorator()) {
            getJoinDecorator().detachFlow(flow);
        }
    }


    public Object clone() {
        YAWLTask clone = (YAWLTask) super.clone();

        CancellationSet clonedCancellationSet = (CancellationSet) getCancellationSet().clone();
        clone.setCancellationSet(clonedCancellationSet);
        clonedCancellationSet.setTriggeringTask(clone);

        clone.setTask(((YTask) _yawlElement));
        clone.setDecomposition(getDecomposition());
        clone.setCustomFormURL(getCustomFormURL());
        clone.setIconPath(_iconPath);
        return clone;
    }


    /**
     * Created by Jingxin Xu 13/01/2010
     */
    public List<CPort> getInputCPorts() {
        return inputCPorts;
    }

    /**
     * Created by Jingxin Xu 13/01/2010
     */
    public List<CPort> getOutputCPorts() {
        return outputCPorts;
    }

    public int getNextCPortID(int portType) {
        int id = -1;
        switch (portType) {
            case CPort.INPUTPORT:
                id = getMaxCPortID(inputCPorts) + 1;
                break;
            case CPort.OUTPUTPORT:
                id = getMaxCPortID(outputCPorts) + 1;
                break;
        }
        return id;
    }

    public int getMaxCPortID(List<CPort> ports) {
        int max = -1;
        for (CPort port : ports) {
            max = Math.max(max, port.getID());
        }
        return max;
    }

    /**
     * Created by Jingxin XU 13/01/2010
     * refactored MA 25/10/10
     * This method construct the configurable input ports
     */
    public void generateInputCPorts() {
        if (hasJoinDecorator() && (getJoinDecorator().getType() == Decorator.XOR_TYPE)) {
            int id = 0;
            for (YAWLFlowRelation flow : getIncomingFlows()) {
                if (!flow.isBroken()) {
                    CPort port = new CPort(this, CPort.INPUTPORT);
                    port.setID(id++);
                    port.getFlows().add(flow);
                    inputCPorts.add(port);
                }
            }
        } else if (getIncomingFlowCount() > 0) {
            CPort port = new CPort(this, CPort.INPUTPORT);
            port.setID(0);
            for (YAWLFlowRelation flow : getIncomingFlows()) {
                if (!flow.isBroken()) {
                    port.getFlows().add(flow);
                }
            }
            inputCPorts.add(port);
        }
    }

    /**
     * Created by Jingxin XU 13/01/2010
     * refactored MA 25/10/10
     * This method construct the configurable output ports
     */
    public void generateOutputCPorts() {
        if ((!hasSplitDecorator()) ||
                (getSplitDecorator().getType() == Decorator.AND_TYPE)) {

            CPort port = new CPort(this, CPort.OUTPUTPORT);
            port.setID(0);
            for (YAWLFlowRelation flow : getOutgoingFlows()) {
                if (!flow.isBroken()) {
                    port.getFlows().add(flow);
                }
            }
            outputCPorts.add(port);
        } else if (getSplitDecorator().getType() == Decorator.XOR_TYPE) {
            int id = 0;
            for (YAWLFlowRelation flow : getOutgoingFlows()) {
                if (!flow.isBroken()) {
                    CPort port = new CPort(this, CPort.OUTPUTPORT);
                    port.setID(id++);
                    port.getFlows().add(flow);
                    outputCPorts.add(port);
                }
            }
        } else if (getSplitDecorator().getType() == Decorator.OR_TYPE) {
            int id = 0;
            for (Set<YAWLFlowRelation> flowsPowerSet : getPowerSet(getOutgoingFlows())) {
                if ((flowsPowerSet != null) && (!flowsPowerSet.isEmpty())) {
                    CPort port = new CPort(this, CPort.OUTPUTPORT);
                    port.setID(id++);
                    for (YAWLFlowRelation flow : flowsPowerSet) {
                        if (!flow.isBroken()) {
                            port.getFlows().add(flow);
                        }
                    }
                    outputCPorts.add(port);
                }
            }
        }
    }


    /**
     * This is the get method of configurable;
     * Jingxin Xu
     */
    public boolean isConfigurable() {
        return _configurable;
    }

    /**
     * This is the set method of configurable;
     */
    public void setConfigurable(boolean newSetting) {
        if (_configurable != newSetting) {
            _configurable = newSetting;
            if (_configurable && (!_hasBeenConfigureInitialised)) {
                configureReset();
            }
        }
    }

    public void configureReset() {
        if (_configurable) {
            inputCPorts.clear();
            outputCPorts.clear();
            generateInputCPorts();
            generateOutputCPorts();
            if (this instanceof MultipleAtomicTask) {
                MultipleAtomicTask multipleTask = (MultipleAtomicTask) this;
                multipleTask.iniConfigure();
            } else if (this instanceof MultipleCompositeTask) {
                MultipleCompositeTask multipleTask = (MultipleCompositeTask) this;
                multipleTask.iniConfigure();
            }
            _hasBeenConfigureInitialised = true;
        }
    }


    public void removeInputPort(CPort port) {
        this.inputCPorts.remove(port);
    }

    public void removeOutputPort(CPort port) {
        this.outputCPorts.remove(port);
    }

    public void removeOutputPort(YAWLFlowRelation flow) {
        for (CPort port : outputCPorts) {
            if (port.getFlows().contains(flow)) {
                outputCPorts.remove(port);
                break;
            }
        }
    }

    public void removeInputPort(YAWLFlowRelation flow) {
        for (CPort port : inputCPorts) {
            if (port.getFlows().contains(flow)) {
                inputCPorts.remove(port);
                break;
            }
        }
    }

    public void addInputPort(CPort port) {
        this.inputCPorts.add(port);
    }

    public void addOutputPort(CPort port) {
        this.outputCPorts.add(port);
    }

    /**
     * After applying configuration, some ports have been removed. Correspondingly the
     * port Id should be reset.
     */
    public void resetCPortsID() {
        int i = 0;
        for (CPort port : this.inputCPorts) {
            port.setID(i);
            i++;
        }
        i = 0;
        for (CPort port : this.outputCPorts) {
            port.setID(i);
            i++;
        }
    }

    public boolean isCancellationSetEnable() {
        return _cancellationSetEnable;
    }

    public void setCancellationSetEnable(boolean cancellationSetEnable) {
        this._cancellationSetEnable = cancellationSetEnable;
    }

    public void setInputCPorts(List<CPort> inputCPorts) {
        this.inputCPorts = inputCPorts;
    }

    public void setOutputCPorts(List<CPort> outputCPorts) {
        this.outputCPorts = outputCPorts;
    }

    public boolean hasDefaultInputPorts() {
        boolean flag = false;
        for (CPort port : this.getInputCPorts()) {
            if (port.getDefaultValue() != null) {
                flag = true;
            }
        }
        return flag;
    }


    public boolean hasDefaultOutputPorts() {
        boolean flag = false;
        for (CPort port : this.getOutputCPorts()) {
            if (port.getDefaultValue() != null) {
                flag = true;
            }
        }
        return flag;
    }

    // this method accessed on 06/05/2010 from:
    // http://stackoverflow.com/questions/1670862/obtaining-powerset-of-a-set-in-java
    public static <T> Set<Set<T>> getPowerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : getPowerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }
}
