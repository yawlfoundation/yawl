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

package org.yawlfoundation.yawl.configuration.element;

import org.yawlfoundation.yawl.configuration.CPort;
import org.yawlfoundation.yawl.configuration.MultipleInstanceTaskConfigSet;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class TaskConfiguration {

    private YAWLTask _task;
    private NetGraphModel _model;
    private boolean _configurable;
    private boolean _hasBeenConfigureInitialised;
    private List<CPort> inputCPorts = new ArrayList<CPort>();
    private List<CPort> outputCPorts = new ArrayList<CPort>();
    private boolean _cancellationSetEnable = true;

    private MultipleInstanceTaskConfigSet configureSet;   // mi tasks only



    public TaskConfiguration(YAWLTask task, NetGraphModel model) {
        _task = task;
        _model = model;
        if (task instanceof YAWLMultipleInstanceTask) {
            initMITask();
        }
    }


    public YAWLTask getTask() { return _task; }

    public NetGraphModel getGraphModel() { return _model; }

    /**
     * Created by Jingxin Xu 13/01/2010
     */

    public void initMITask() {
        configureSet = new MultipleInstanceTaskConfigSet((YAWLMultipleInstanceTask) _task);
    }

    public MultipleInstanceTaskConfigSet getConfigurationInfor() {
        return configureSet;
    }


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
        if (_task.hasJoinDecorator() && (_task.getJoinDecorator().getType() == Decorator.XOR_TYPE)) {
            int id = 0;
            for (YAWLFlowRelation flow : _task.getIncomingFlows()) {
                if (!flow.isBroken()) {
                    CPort port = new CPort(this, CPort.INPUTPORT);
                    port.setID(id++);
                    port.getFlows().add(flow);
                    inputCPorts.add(port);
                }
            }
        } else if (_task.getIncomingFlowCount() > 0) {
            CPort port = new CPort(this, CPort.INPUTPORT);
            port.setID(0);
            for (YAWLFlowRelation flow : _task.getIncomingFlows()) {
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
        if ((!_task.hasSplitDecorator()) ||
                (_task.getSplitDecorator().getType() == Decorator.AND_TYPE)) {

            CPort port = new CPort(this, CPort.OUTPUTPORT);
            port.setID(0);
            for (YAWLFlowRelation flow : _task.getOutgoingFlows()) {
                if (!flow.isBroken()) {
                    port.getFlows().add(flow);
                }
            }
            outputCPorts.add(port);
        } else if (_task.getSplitDecorator().getType() == Decorator.XOR_TYPE) {
            int id = 0;
            for (YAWLFlowRelation flow : _task.getOutgoingFlows()) {
                if (!flow.isBroken()) {
                    CPort port = new CPort(this, CPort.OUTPUTPORT);
                    port.setID(id++);
                    port.getFlows().add(flow);
                    outputCPorts.add(port);
                }
            }
        } else if (_task.getSplitDecorator().getType() == Decorator.OR_TYPE) {
            int id = 0;
            for (Set<YAWLFlowRelation> flowsPowerSet : getPowerSet(_task.getOutgoingFlows())) {
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
            if (_task instanceof YAWLMultipleInstanceTask) {
                initMITask();
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
    private static <T> Set<Set<T>> getPowerSet(Set<T> originalSet) {
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
