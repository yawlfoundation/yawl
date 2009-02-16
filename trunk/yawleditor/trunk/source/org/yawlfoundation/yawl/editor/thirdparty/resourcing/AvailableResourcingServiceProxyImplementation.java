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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class AvailableResourcingServiceProxyImplementation implements ResourcingServiceProxyInterface {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static String sessionHandle = null;

  private ResourceGatewayClientAdapter gateway;

  private String serviceURI ;
  
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
        if (sessionHandle.startsWith("<failure>")) sessionHandle = null;  
      }
    } catch (Exception e) {
      //e.printStackTrace();
      sessionHandle = null;
    }
  }
  
  private String tryConnect(String uri, String userID, String password) {    
    if ((userID == null) || (userID.length() == 0))
       return "<failure>No userid specified.</failure>";
    else if ((password == null) || (password.length() == 0))
       return "<failure>No password specified.</failure>";
    else {
      if ((gateway == null) || (serviceURI == null) || (! uri.equals(serviceURI))) {
        serviceURI = uri;
        gateway = new ResourceGatewayClientAdapter(serviceURI);
      }
      return gateway.connect(userID, password) ;
    }
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
    
    List engineParticipants = null;
    LinkedList<ResourcingParticipant> participantList = new LinkedList<ResourcingParticipant>();

    try {
      engineParticipants = gateway.getParticipants(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return participantList;
    }
    
    if (engineParticipants != null) {
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
    }

    disconnect();

    return participantList;
  }
  
  public List<ResourcingRole> getAllRoles() {
    connect();
   
    List engineRoles = null;
    LinkedList<ResourcingRole> registeredRoles = new LinkedList<ResourcingRole>();

    try {
      engineRoles = gateway.getRoles(sessionHandle);
    } catch (Exception e) {
      e.printStackTrace();
      return registeredRoles;
    }
    
    if ( engineRoles != null) {
      for (Object engineRole: engineRoles) {
        Role role = (Role) engineRole;

        registeredRoles.add(
          new ResourcingRole(
              role.getID(),
              role.getName()
          )
        );
      }
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


  public List getCapabilities() {
      List result = null;
      connect();

      try {
          result = gateway.getCapabilities(sessionHandle);
      }
      catch (IOException ioe) {
          // do nothing
      }

      disconnect();
      return result;
  }

    public List getPositions() {
        List result = null;
        connect();

        try {
            result = gateway.getPositions(sessionHandle);
        }
        catch (IOException ioe) {
            // do nothing
        }

        disconnect();
        return result;
    }

    public List getOrgGroups() {
        List result = null;
        connect();

        try {
            result = gateway.getOrgGroups(sessionHandle); 
        }
        catch (IOException ioe) {
            // do nothing
        }

        disconnect();
        return result;
    }


    public Map<String, String> getRegisteredCodelets() {
        connect();

        try {
           return gateway.getCodeletMap(sessionHandle);
        }
        catch (Exception e) {
           e.printStackTrace();
            return null;
        }
    }


    // get live object id lists from resource service
    public List<String> getAllParticipantIDs() {
      List<ResourcingParticipant> liveList = getAllParticipants();
      List<String> result = new ArrayList<String>();
      for (ResourcingParticipant p : liveList) {
        result.add(p.getId());
      }
      return result;
    }


    public List<String> getAllRoleIDs() {
      List<ResourcingRole> liveRList = ResourcingServiceProxy.getInstance().getAllRoles();
      List<String> result = new ArrayList<String>();
      for (ResourcingRole r : liveRList) {
        result.add(r.getId());
      }
      return result;
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
      return ConnectionTester.testConnection(serviceURI, 0 , 1000);
//    String testSessionID = "";
//     try {
//       testSessionID = tryConnect(serviceURI, userID, password);
//       gateway.disconnect(testSessionID);
//     } catch (Exception e) {
//       e.printStackTrace();
//       testSessionID = "";
//     }
//     return (testSessionID.length() > 0) && (! testSessionID.startsWith("<failure>"));
  }

  public boolean checkConnection() {
    if ((gateway == null) || (sessionHandle == null)) return false ;
    return gateway.checkConnection(sessionHandle);
  }
}
