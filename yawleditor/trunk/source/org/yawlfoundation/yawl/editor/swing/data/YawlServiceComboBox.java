/*
 * Created on 20/09/2004
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

package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.client.YConnector;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class YawlServiceComboBox extends JComboBox {

    private static final long serialVersionUID = 1L;
    private Map<String, String> services = new HashMap<String, String>();

    public YawlServiceComboBox() {
        super();
    }

    public void refresh() {
        removeAllItems();
        setEnabled(false);
        addYawlServicesFromEngine();
        setEnabled(getItemCount() > 1);
    }


    private void addYawlServicesFromEngine() {
        services.clear();
        try {
            for (YAWLServiceReference service : YConnector.getServices()) {
                if (!service.canBeAssignedToTask()) {
                    continue;  // ignore services that are not for tasks.
                }

                // Short and sweet description for the editor.
                String doco = service.getDocumentation();
                if (doco == null) doco = service.get_serviceName();
                services.put(doco, service.getURI());
            }
        }
        catch (IOException ioe) {

            // do nothing - services remains an empty list
            return;
        }

        List<String> sortedServiceLabels = new ArrayList<String>(services.keySet());
        Collections.sort(sortedServiceLabels);
        for (String label : sortedServiceLabels) {
            addItem(label);                           // add service to combobox
        }
    }


    public String getSelectedItemID() {
        return services.get(getSelectedItem());
    }


    public String getDescriptionFromID(String id) {
        for (String description : services.keySet()) {
            String serviceID = services.get(description);
            if ((serviceID != null) && serviceID.equals(id)) {
                return description;
            }
        }
        return null;
    }

    /**
     * Returns whether the selected item will need further web service detail
     * to be specified for the selected item to be meaningful.
     */
    public boolean needsWebServiceDetail() {
        if (getSelectedItem() == null) {
            return false;
        }

        String serviceURI = services.get(getSelectedItem());
        return (serviceURI != null) && serviceURI.matches(".*/yawlWSInvoker/$");
    }


    /**
     * Returns whether the selected item represents a service
     * that specifies its own interface in terms of variables.
     */

    public boolean definesOwnVariableInterface() {
        return (getSelectedItem() != null) && (services.get(getSelectedItem()) != null);
    }
}