/*
 * Created on 03/06/2005
 * YAWLEditor v1.3
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

package org.yawlfoundation.yawl.editor.thirdparty.resourcing;

import java.util.List;

import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class ResourcingServiceProxy implements ResourcingServiceProxyInterface {
  
  private transient static final ResourcingServiceProxy INSTANCE 
    = new ResourcingServiceProxy();

  private ResourcingServiceProxyInterface availableImplementation;
  private ResourcingServiceProxyInterface unavailableImplementation;

  public static ResourcingServiceProxy getInstance() {
    return INSTANCE; 
  }
    
  private ResourcingServiceProxy() {}
  
  private ResourcingServiceProxyInterface getImplementation() {
    if (serviceLibrariesAvailable() && getAvailableImplementation().testConnection() ) {
      return getAvailableImplementation();
    } 
    return getUnavailableImplementation();
  }
  
  public static boolean serviceLibrariesAvailable() {
    // assumption: If we can find ResourceGatewayClientAdapter, we can find everything we
    //             need from the resource service libraries.
    try {
      Class.forName("org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter");
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  private ResourcingServiceProxyInterface getAvailableImplementation() {
    if (availableImplementation == null) {
      availableImplementation = new AvailableResourcingServiceProxyImplementation();
    }
    return availableImplementation;
  }
  
  private ResourcingServiceProxyInterface getUnavailableImplementation() {
    if (unavailableImplementation == null) {
      unavailableImplementation = new UnavailableResourcingServiceProxyImplementation();
    }
    return unavailableImplementation;
  }

  public void connect() {
    getImplementation().connect();
  }

  public void disconnect() {
    getImplementation().disconnect();
  }

  public boolean testConnection() {
    return getImplementation().testConnection();
  }
  
  public boolean testConnection(String serviceURI, String userID, String password) {
    return getImplementation().testConnection(serviceURI, userID, password);
  }
  
  public List<ResourcingParticipant> getAllParticipants() {
    return getImplementation().getAllParticipants();
  }
  
  public List<ResourcingRole> getAllRoles() {
    return getImplementation().getAllRoles();
  }
  
  public List<ResourcingFilter> getRegisteredResourcingFilters() {
    return getImplementation().getRegisteredResourcingFilters();
  }

  public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
    return getImplementation().getRegisteredAllocationMechanisms();
  }
}
