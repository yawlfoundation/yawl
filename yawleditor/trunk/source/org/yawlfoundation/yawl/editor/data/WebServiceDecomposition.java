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

package org.yawlfoundation.yawl.editor.data;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;

public class WebServiceDecomposition extends Decomposition {

    public static final String DEFAULT_ENGINE_SERVICE_NAME = "Default Engine Worklist";

    private YAWLServiceReference _service;

    public WebServiceDecomposition() {
        super();
        setManualInteraction(true);
    }

    public WebServiceDecomposition(String yawlServiceID,
                                   String yawlServiceDescription) {
        _service = new YAWLServiceReference(yawlServiceID, null);
        setYawlServiceDescription(yawlServiceDescription);
        setManualInteraction(false);
    }

    public String getYawlServiceID() {
        return invokesWorklist() ? null : _service.getURI();
    }

    public void setYawlServiceID(String yawlServiceID) {
        _service.set_yawlServiceID(yawlServiceID);
    }

    public String getYawlServiceDescription() {
        return invokesWorklist() ? "" : _service.getDocumentation();
    }

    public void setYawlServiceDescription(String yawlServiceDescription) {
        _service.setDocumentation(yawlServiceDescription);
    }

    public boolean invokesWorklist() {
        return (_service == null);
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
