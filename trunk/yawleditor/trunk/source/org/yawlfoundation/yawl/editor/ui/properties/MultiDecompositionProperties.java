/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.CodeletData;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ServicesPropertyEditor;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.logging.YLogPredicate;

import java.io.IOException;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/07/12
 */
public class MultiDecompositionProperties extends MultiCellProperties {

    private YDecomposition _decomposition;

    public MultiDecompositionProperties() {
        super();
    }

    protected void setCells(Object[] cells) {
        super.setCells(cells);
        _decomposition = getCommonDecomposition();
    }


    public String getStartLogPredicate() {
        if (_decomposition != null) {
            YLogPredicate predicate = _decomposition.getLogPredicate();
            return predicate != null ? predicate.getStartPredicate() : "";
        }
        return "";
    }

    public void setStartLogPredicate(String predicate) {
        if (_decomposition != null) {
            YLogPredicate logPredicate = _decomposition.getLogPredicate();
            if (logPredicate == null) {
                logPredicate = new YLogPredicate();
                _decomposition.setLogPredicate(logPredicate);
            }
            logPredicate.setStartPredicate(predicate);
            setDirty();
        }
    }


    public String getCompletionLogPredicate() {
        if (_decomposition != null) {
            YLogPredicate predicate = _decomposition.getLogPredicate();
            return predicate != null ? predicate.getCompletionPredicate() : "";
        }
        return "";
    }

    public void setCompletionLogPredicate(String predicate) {
        if (_decomposition != null) {
            YLogPredicate logPredicate = _decomposition.getLogPredicate();
            if (logPredicate == null) {
                logPredicate = new YLogPredicate();
                _decomposition.setLogPredicate(logPredicate);
            }
            logPredicate.setCompletionPredicate(predicate);
            setDirty();
        }
    }


    public String getCustomService() {
        String serviceName = null;
        if (_decomposition != null) {
            if (_decomposition instanceof YNet) return "";

            YAWLServiceReference service =
                    ((YAWLServiceGateway) _decomposition).getYawlService();
            if (service != null) {
                serviceName = service.getServiceName();
            }
        }

        // update for old specs
        if (serviceName == null || serviceName.equals("Default Engine Worklist")) {
            serviceName = ServicesPropertyEditor.DEFAULT_WORKLIST;
        }
        enableServiceProperties(serviceName);
        return serviceName;
    }

    public void setCustomService(String serviceName) {
        if (_decomposition != null) {
            YAWLServiceReference service = ServicesPropertyEditor.getService(serviceName);
            if (service != null) {
                ((YAWLServiceGateway) _decomposition).setYawlService(service);
                try {
                    addParameters(YConnector.getServiceParameters(service.getURI()));
                }
                catch (IOException ioe) {
                    showWarning("Service Parameter Error",
                            "Failed to load required parameters from service");
                }
            }
            setDirty();
            enableServiceProperties(serviceName);
        }
    }


    public boolean isAutomated() {
        boolean auto = _decomposition != null &&
                ! _decomposition.requiresResourcingDecisions();
        setReadOnly("Codelet", ! auto);
        setReadOnly("CustomForm", auto);
        return auto;
    }

    public void setAutomated(boolean auto) {
        if (_decomposition != null) {
            _decomposition.setExternalInteraction(!auto);
            setReadOnly("Codelet", ! auto);
            setReadOnly("CustomForm", auto);
            refreshCellViews(vertexSet);
            setDirty();
            if (! auto) firePropertyChange("Codelet", null);
        }
    }


    public CodeletData getCodelet() {
        if (_decomposition != null) {
            String codeletName = _decomposition.getCodelet();
            if (codeletName != null) {
                return new CodeletData(codeletName, null);
            }
        }
        return null;
    }

    public void setCodelet(CodeletData codelet) {
        if (_decomposition != null) {
            if (codelet != null) {
                if (codelet.getName().equals("None")) {
                    codelet.setName(null);
                }
                _decomposition.setCodelet(codelet.getName());
                try {
                    addParameters(YConnector.getCodeletParameters(codelet.getName()));
                }
                catch (IOException ioe) {
                    showWarning("Codelet Parameter Error",
                            "Failed to load required parameters from codelet");
                }
            }
            refreshCellViews(vertexSet);
            setDirty();
        }
    }



    public NetTaskPair getExtAttributes() {
        NetTaskPair pair = new NetTaskPair(null, _decomposition, null);
        String text = _decomposition.getAttributes().isEmpty() ? "None" :
                _decomposition.getAttributes().size() + " active";
        pair.setSimpleText(text);
        return pair;
    }

    public void setExtAttributes(NetTaskPair pair) {
        // all handled by dialog
    }


    /****************************************************************************/

    private void enableServiceProperties(String service) {
        boolean isDefaultWorklist = service.equals(ServicesPropertyEditor.DEFAULT_WORKLIST);
        setReadOnly("Automated", ! isDefaultWorklist);
        setReadOnly("Timer", ! isDefaultWorklist);
        if (! isDefaultWorklist) {
            firePropertyChange("Automated", false);
        }
    }


    private void addParameters(List<YParameter> parameters) {
        if (! (parameters == null || parameters.isEmpty())) {
            for (YParameter parameter : parameters) {
                parameter.setParentDecomposition(_decomposition);
                if (parameter.isInput()) {
                    parameter.setOrdering(_decomposition.getInputParameters().size());
                    _decomposition.addInputParameter(parameter);
                }
                else if (parameter.isOutput()) {
                    parameter.setOrdering(_decomposition.getOutputParameters().size());
                    _decomposition.addOutputParameter(parameter);
                }
            }
        }
    }
}
