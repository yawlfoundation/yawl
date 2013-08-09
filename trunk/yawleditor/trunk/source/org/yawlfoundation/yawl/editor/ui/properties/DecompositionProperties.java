package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ServicesPropertyEditor;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.logging.YLogPredicate;

/**
 * @author Michael Adams
 * @date 16/07/12
 */
public class DecompositionProperties extends CellProperties {

    private YDecomposition _decomposition;
    private UserDefinedAttributes _udAttributes;

    public DecompositionProperties() {
        super();
        _udAttributes = UserDefinedAttributes.getInstance();
    }

    protected void setVertex(YAWLVertex vertex) {
        super.setVertex(vertex);
        _decomposition = ((YAWLTask) vertex).getDecomposition();
        _udAttributes.setDecomposition(_decomposition);
        setReadOnly("CustomService", _decomposition instanceof YNet);
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
            serviceName = service != null ? service.getServiceName() :
                    ServicesPropertyEditor.DEFAULT_WORKLIST;
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
            ((YAWLServiceGateway) _decomposition).setYawlService(
                    ServicesPropertyEditor.getService(serviceName));
            setDirty();
            enableServiceProperties(serviceName);
        }
    }


    public boolean isAutomated() {
        boolean auto = _decomposition != null &&
                ! _decomposition.requiresResourcingDecisions();
        setReadOnly("Codelet", ! auto);
        setReadOnly("Resourcing", auto);
        setReadOnly("CustomForm", auto);
        return auto;
    }

    public void setAutomated(boolean auto) {
        if (_decomposition != null) {
            _decomposition.setExternalInteraction(!auto);
            setReadOnly("Codelet", ! auto);
            setReadOnly("Resourcing", auto);
            setReadOnly("CustomForm", auto);
            setDirty();
            if (! auto) firePropertyChange("Codelet", null);
        }
    }


    public String getCodelet() {
        if (_decomposition != null) {
            String codelet = _decomposition.getCodelet();
            if (codelet != null) return codelet;
        }
        return null;
    }

    public void setCodelet(String codelet) {
        if (_decomposition != null) {
            if (codelet != null && codelet.equals("None")) codelet = null;
            _decomposition.setCodelet(codelet);
            setDirty();
        }
    }


    public NetTaskPair getTaskDataVariables() {
        return new NetTaskPair(flowHandler.getRootNet(), _decomposition, (YAWLTask) vertex);
    }

    public void setTaskDataVariables(NetTaskPair value) {

        // need to update net-level data property changed via the task data dialog
        NetTaskPair netPair = new NetTaskPair(flowHandler.getNet(getName()), null, null);
        firePropertyChange("DataVariables", netPair);
    }


    public YDecomposition getExtAttributes() {
        return _decomposition;
    }

    public void setExtAttributes(YDecomposition value) { }


    /****************************************************************************/

    private void enableServiceProperties(String service) {
        boolean isDefaultWorklist = service.equals(ServicesPropertyEditor.DEFAULT_WORKLIST);
        setReadOnly("Automated", ! isDefaultWorklist);
        if (! isDefaultWorklist) {
            firePropertyChange("Automated", false);
        }
    }

}
