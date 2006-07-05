/*
 * Created on 20/09/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.swing.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class YawlServiceComboBox extends JComboBox {
  
  private HashMap services = null;
  
  public YawlServiceComboBox() {
    super();
    refresh();
  }
  
  public void refresh() {
    removeAllItems();
    addYawlServices();
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
//      System.out.println("Adding service \"" + serviceDescription + "\" to combobox.");
      addItem(serviceDescription);
    }
  }
  
  public String getSelectedItemID() {
    return (String) services.get(getSelectedItem());
  }
}