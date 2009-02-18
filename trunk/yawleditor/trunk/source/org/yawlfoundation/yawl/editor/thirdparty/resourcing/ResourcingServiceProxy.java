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

import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.editor.thirdparty.engine.ServerLookup;

import java.util.List;
import java.util.Map;

public class ResourcingServiceProxy implements ResourcingServiceProxyInterface {
  
  private transient static final ResourcingServiceProxy INSTANCE 
    = new ResourcingServiceProxy();

//  private ResourcingServiceProxyInterface availableImplementation;
//  private ResourcingServiceProxyInterface unavailableImplementation;
  private ResourcingServiceProxyInterface implementation;

  public static ResourcingServiceProxy getInstance() {
    return INSTANCE; 
  }
    
  private ResourcingServiceProxy() {
      setImplementation(prefs.get("resourcingServiceURI", DEFAULT_RESOURCING_SERVICE_URI));
  }
  
  public ResourcingServiceProxyInterface getImplementation() {
    return implementation;
  }

    public void setImplementation(ResourcingServiceProxyInterface impl) {
        implementation = impl;
    }


    public void setImplementation(String serviceURI) {
        try {
            if (serviceLibrariesAvailable() && ServerLookup.isReachable(serviceURI)) {
                implementation = new AvailableResourcingServiceProxyImplementation();
            }
            else {
                implementation = new UnavailableResourcingServiceProxyImplementation();
            }
        }
        catch (Exception e) {
            implementation = new UnavailableResourcingServiceProxyImplementation();
        }
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
  
//  private ResourcingServiceProxyInterface getAvailableImplementation() {
//    if (availableImplementation == null) {
//      availableImplementation = new AvailableResourcingServiceProxyImplementation();
//    }
//    return availableImplementation;
//  }
//
//  private ResourcingServiceProxyInterface getUnavailableImplementation() {
//    if (unavailableImplementation == null) {
//      unavailableImplementation = new UnavailableResourcingServiceProxyImplementation();
//    }
//    return unavailableImplementation;
//  }

  public boolean connect() {
    return implementation.connect();
  }

  public void disconnect() {
    implementation.disconnect();
  }

  public boolean testConnection() {
    return implementation.testConnection();
  }

  public boolean isLiveService() {
    return (implementation instanceof AvailableResourcingServiceProxyImplementation);
  }
  
  public boolean testConnection(String serviceURI, String userID, String password) {
    return implementation.testConnection(serviceURI, userID, password);
  }
  
  public List<ResourcingParticipant> getAllParticipants() {
    return implementation.getAllParticipants();
  }
  
  public List<ResourcingRole> getAllRoles() {
    return implementation.getAllRoles();
  }
  
  public List<ResourcingFilter> getRegisteredResourcingFilters() {
    return implementation.getRegisteredResourcingFilters();
  }

  public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
    return implementation.getRegisteredAllocationMechanisms();
  }

  public List getCapabilities() {
    return implementation.getCapabilities();
  }

  public List getPositions() {
    return implementation.getPositions();
  }

  public List getOrgGroups() {
    return implementation.getOrgGroups();
  }


  public Map<String, String> getRegisteredCodelets() {
    return implementation.getRegisteredCodelets();
  }

  public List<String> getAllParticipantIDs() {
    return implementation.getAllParticipantIDs();
  }

  public List<String> getAllRoleIDs() {
    return implementation.getAllRoleIDs();
  }


}
