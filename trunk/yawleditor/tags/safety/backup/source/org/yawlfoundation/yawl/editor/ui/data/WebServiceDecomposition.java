/*
 * Created on 27/09/2004
 * YAWLEditor v1.01 
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
 */

package org.yawlfoundation.yawl.editor.ui.data;

import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

public class WebServiceDecomposition extends Decomposition {

    public static final String DEFAULT_WORKLIST_LABEL = "Default Engine Worklist";
    public static final String ENGINE_WORKLIST_NAME = "DefaultWorklist";

    // set if the uri in a loading decomposition matches no known YAWL service
    private String _unresolvedURI;


    public WebServiceDecomposition() {
        this("__temp__");
    }

    public WebServiceDecomposition(String id) {
        super(id);
        setManualInteraction(true);
        setLabel(id);
    }

    public void setService(YAWLServiceReference service) {
        ((YAWLServiceGateway) _decomposition).setYawlService(service);
        _unresolvedURI = null;                           // service is now resolved
    }

    public YAWLServiceReference getService() {
        return ((YAWLServiceGateway) _decomposition).getYawlService();
    }

    public String getServiceURI() {
        if (_unresolvedURI != null) return _unresolvedURI;
        return invokesWorklist() ? null : getService().getURI();
    }

    public String getServiceDescription() {
        return invokesWorklist() ? DEFAULT_WORKLIST_LABEL : getService().getDocumentation();
    }

    public String getUnresolvedURI() { return _unresolvedURI; }

    public void setUnresolvedURI(String uri) { _unresolvedURI = uri; }


    public boolean invokesWorklist() {
        return getService() == null || (getService().getServiceName() != null &&
                getService().getServiceName().equals(ENGINE_WORKLIST_NAME));
    }

    public void setManualInteraction(boolean isManual) {
        _decomposition.setExternalInteraction(isManual);
    }

    public boolean isManualInteraction() {
        return _decomposition.requiresResourcingDecisions();
    }

    public void setCodelet(String codelet) {
        _decomposition.setCodelet(codelet);
    }

    public String getCodelet() {
        return _decomposition.getCodelet();
    }

}
