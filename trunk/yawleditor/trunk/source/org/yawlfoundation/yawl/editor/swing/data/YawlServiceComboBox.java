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

import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import javax.swing.*;
import java.util.*;

public class YawlServiceComboBox extends JComboBox {
  
  private static final long serialVersionUID = 1L;
  private HashMap services = new HashMap();
  
  public YawlServiceComboBox() {
    super();
  }
  
  public void refresh() {
    removeAllItems();
    setEnabled(false);
    
    addYawlServices();
    if (getItemCount() > 1) {
      setEnabled(true);
    }
  }

  private void addYawlServices() {
    addYawlServicesFromEngine();    
  }
  
  private void addYawlServicesFromEngine() {
    services = YAWLEngineProxy.getInstance().getRegisteredYAWLServices();
    if (services == null) {
      return;
    }

    LinkedList sortedServices = new LinkedList();
    sortedServices.addAll(services.keySet());
    Collections.sort(sortedServices);
    
    Iterator serviceIterator = sortedServices.iterator();
    while(serviceIterator.hasNext()) {
      String serviceDescription = (String) serviceIterator.next();
      addItem(serviceDescription);                            // add service to combobox
    }
  }
  
  public String getSelectedItemID() {
    return (String) services.get(getSelectedItem());
  }

  public String getDescriptionFromID(String id) {
      for (Object description : services.keySet()) {
          String serviceID = (String) services.get(description);
          if ((serviceID != null) && serviceID.equals(id)) {
              return (String) description;
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
    
    String serviceURI = (String) services.get(getSelectedItem());
    if (serviceURI != null && serviceURI.matches(".*/yawlWSInvoker/$")) {
      return true;
    }
    return false;
  }

  /**
   * Returns whether the selected item represents a service 
   * that specifies its own interface in terms of variables. 
   */
  
  public boolean definesOwnVariableInterface() {
    if (getSelectedItem() == null) {
      return false;
    }
    String serviceURI = (String) services.get(getSelectedItem());
    if (serviceURI == null) {
      return false;
    }
    return true;
  }
}