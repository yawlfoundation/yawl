/*
 * Created on 03/06/2005
 * YAWLEditor v1.3
 *
 * @author Lindsay Bradford
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

import java.util.prefs.Preferences;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;

import java.util.List;
import java.util.LinkedList;

import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

public class AvailableResourcingServiceProxyImplementation implements ResourcingServiceProxyInterface {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static String sessionHandle = null;

  private ResourceGatewayClientAdapter gateway;
  
  public void connect() {
    connect(
        prefs.get(  
            "resourcingServiceURI", 
            DEFAULT_RESOURCING_SERVICE_URI
        ),
        prefs.get(
            "resourcingServiceUserID", 
            DEFAULT_RESOURCING_SERVICE_USERID
        ),    
        prefs.get(
            "resourcingServiceUserPassword", 
            DEFAULT_RESOURCING_SERVICE_USER_PASSWORD
        )
    );
  }
  
  public boolean connected() {
    return (sessionHandle != null);
  }
  
  public void connect(String serviceURI, String userID, String password) {
    try {
      if (!connected()) {
        sessionHandle = tryConnect(serviceURI, userID, password);
      } 
    } catch (Exception e) {
      //e.printStackTrace();
      sessionHandle = null;
    }
  }
  
  private String tryConnect(String serviceURI, String userID, String password) {
    if (gateway == null) {
      gateway = new ResourceGatewayClientAdapter(serviceURI);
    }
    String thisSessionHandle = gateway.connect(userID, password);
    //System.out.println("Session Handle on connect attempt = " + thisSessionHandle);
    return thisSessionHandle;
  }
  
  public void disconnect() {
    if (gateway != null && sessionHandle != null) {
      gateway.disconnect(sessionHandle);
      sessionHandle = null;
      gateway = null;
    }
  }
  
  public List<ResourcingParticipant> getAllParticipants() {
    connect();
    
    List engineParticipants;
    
    try {
      engineParticipants = gateway.getParticipants(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
    LinkedList<ResourcingParticipant> participantList = new LinkedList<ResourcingParticipant>();
    
    for (Object engineParticipant: engineParticipants) {
      Participant participant = (Participant) engineParticipant;

      participantList.add(
          new ResourcingParticipant(
              participant.getID(),
              participant.getFullName() + " (" +
              participant.getUserID() + ")"
          )
      );
    }

    disconnect();

    return participantList;
  }
  
  public List<ResourcingRole> getAllRoles() {
    connect();
   
    List engineRoles;
    
    try {
      engineRoles = gateway.getRoles(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
    LinkedList<ResourcingRole> registeredRoles = new LinkedList<ResourcingRole>();

    for (Object engineRole: engineRoles) {
      Role role = (Role) engineRole;

      registeredRoles.add(
          new ResourcingRole(
              role.getID(),
              role.getName()
          )
      );
    }

    disconnect();
    
    return registeredRoles;
  }
  
  public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
    connect();
    
    List engineAllocators;
    
    try {
      engineAllocators = gateway.getAllocators(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
    LinkedList<AllocationMechanism> resultsList = new LinkedList<AllocationMechanism>();
    
    for (Object engineAllocator: engineAllocators) {
      AbstractAllocator allocator = (AbstractAllocator) engineAllocator;

      resultsList.add(
          new AllocationMechanism(
              allocator.getName(),
              allocator.getDisplayName(),
              allocator.getDescription()
          )
      );
    }
    
    disconnect();

    return resultsList;
  }

  public List<ResourcingFilter> getRegisteredResourcingFilters() {
    connect();
    
    List engineFilters;
    
    try {
      engineFilters = gateway.getFilters(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
    LinkedList<ResourcingFilter> resultsList = new LinkedList<ResourcingFilter>();
    
    for (Object engineFilter: engineFilters) {
      AbstractFilter filter = (AbstractFilter) engineFilter;

      resultsList.add(
          new ResourcingFilter(
              filter.getName(),
              filter.getDisplayName(),
              filter.getParams()
          )
      );
    }
    disconnect();

    return resultsList;
  }

  
  public boolean testConnection() {
    return testConnection(
        prefs.get(
            "resourcingServiceURI", 
            DEFAULT_RESOURCING_SERVICE_URI
        ),    
        prefs.get(
            "resourcingServiceUserID", 
            DEFAULT_RESOURCING_SERVICE_USERID
        ),    
        prefs.get(
            "resourcingServiceUserPassword", 
            DEFAULT_RESOURCING_SERVICE_USER_PASSWORD
        )
    );
  }
  
  public boolean testConnection(String serviceURI, String userID, String password) {
    String testSessionID = "";
     try {
       testSessionID = tryConnect(serviceURI, userID, password);
       gateway.disconnect(testSessionID);
     } catch (Exception e) {
       e.printStackTrace();
       testSessionID = "";
     }
     return (!testSessionID.startsWith("<failure>"));
  }
}
