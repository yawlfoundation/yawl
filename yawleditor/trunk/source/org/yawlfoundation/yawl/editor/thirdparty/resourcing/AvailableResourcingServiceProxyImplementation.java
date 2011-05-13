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
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.resourcing.*;
import org.yawlfoundation.yawl.editor.thirdparty.engine.ServerLookup;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
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
  
  public boolean connect() {
    return connect(
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

   
  public boolean connect(String serviceURI, String userID, String password) {
    try {
      if (!connected()) {
        sessionHandle = tryConnect(serviceURI, userID, password);
        if (sessionHandle.startsWith("<failure>")) sessionHandle = null;
      }
    } catch (Exception e) {
      sessionHandle = null;
    }
    return (sessionHandle != null);  
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
      try {
          if (ServerLookup.isReachable(serviceURI)) gateway.disconnect(sessionHandle);
      }
      catch (IOException ioe) {
          // ignore - service no longer reachable
      }
      sessionHandle = null;
      gateway = null;
    }
  }
  
  public List<ResourcingParticipant> getAllParticipants() {
      List<Participant> engineParticipants;
      LinkedList<ResourcingParticipant> participantList = new LinkedList<ResourcingParticipant>();

      if (connect()) {
          try {
              engineParticipants = gateway.getParticipants(sessionHandle);
          } catch (Exception e) {
              return participantList;
          }

          if (engineParticipants != null) {
              for (Participant participant : engineParticipants) {
                  participantList.add(
                          new ResourcingParticipant(
                                  participant.getID(),
                                  participant.getFullName() + " (" +
                                          participant.getUserID() + ")"
                          )
                  );
              }
          }
      }
      return participantList;
  }

    public List<ResourcingRole> getAllRoles() {
        List engineRoles;
        LinkedList<ResourcingRole> registeredRoles = new LinkedList<ResourcingRole>();
        if (connect()) {

            try {
                engineRoles = gateway.getRoles(sessionHandle);
            } catch (Exception e) {
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
        }
        return registeredRoles;
    }


    public List<ResourcingAsset> getAllNonHumanResources() {
      List<NonHumanResource> engineResources;
      LinkedList<ResourcingAsset> resourcesList = new LinkedList<ResourcingAsset>();

      if (connect()) {
          try {
              engineResources = gateway.getNonHumanResources(sessionHandle);
          } catch (Exception e) {
              return resourcesList;
          }

          if (engineResources != null) {
              for (NonHumanResource resource : engineResources) {
                  resourcesList.add(
                          new ResourcingAsset(resource.getID(), resource.getName()));
              }
          }
      }
      return resourcesList;
    }


    public List<ResourcingCategory> getAllNonHumanCategories() {
        List<NonHumanCategory> engineCategories;
        LinkedList<ResourcingCategory> categoriesList = new LinkedList<ResourcingCategory>();
        if (connect()) {
            try {
                engineCategories = gateway.getNonHumanCategories(sessionHandle);
            } catch (Exception e) {
                return categoriesList;
            }

            if ( engineCategories != null) {
                for (NonHumanCategory category : engineCategories) {
                    categoriesList.add(
                            new ResourcingCategory(category.getID(), category.getName()));
                }
            }
        }
        return categoriesList;
    }


    public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
        List engineAllocators;
        LinkedList<AllocationMechanism> resultsList = new LinkedList<AllocationMechanism>();
        if (connect()) {

            try {
                engineAllocators = gateway.getAllocators(sessionHandle);
            } catch (Exception e) {
                return null;
            }

            for (Object engineAllocator: engineAllocators) {
                AbstractAllocator allocator = (AbstractAllocator) engineAllocator;

                resultsList.add(
                        new AllocationMechanism(
                                allocator.getName(),
                                allocator.getCanonicalName(),
                                allocator.getDisplayName(),
                                allocator.getDescription()
                        )
                );
            }
        }
        return resultsList;
    }

    public List<ResourcingFilter> getRegisteredResourcingFilters() {
        List engineFilters;
        LinkedList<ResourcingFilter> resultsList = new LinkedList<ResourcingFilter>();

        if (connect()) {
            try {
                engineFilters = gateway.getFilters(sessionHandle);
            } catch (Exception e) {
                return null;
            }

            for (Object engineFilter: engineFilters) {
                AbstractFilter filter = (AbstractFilter) engineFilter;

                resultsList.add(
                        new ResourcingFilter(
                                filter.getName(),
                                filter.getCanonicalName(),
                                filter.getDisplayName(),
                                filter.getParams()
                        )
                );
            }
        }
        return resultsList;
    }


    public List getCapabilities() {
        List result = null;
        if (connect()) {

            try {
                result = gateway.getCapabilities(sessionHandle);
            }
            catch (Exception ioe) {
                // do nothing
            }

        }
        return result;
    }

    public List getPositions() {
        List result = null;
        if (connect()) {

            try {
                result = gateway.getPositions(sessionHandle);
            }
            catch (Exception ioe) {
                // do nothing
            }

        }
        return result;
    }

    public List getOrgGroups() {
        List result = null;
        if (connect()) {

            try {
                result = gateway.getOrgGroups(sessionHandle);
            }
            catch (Exception ioe) {
                // do nothing
            }
        }
        return result;
    }


    public Map<String, String> getRegisteredCodelets() {
        Map<String, String> result = null;
        if (connect()) {

            try {
                result = gateway.getCodeletMap(sessionHandle);
            }
            catch (Exception e) {
                // do nothing
            }
        }
        return result;
    }


    public List<DataVariable> getCodeletParameters(String codeletName) {
        List<YParameter> params = null;
        List<DataVariable> varList = new LinkedList<DataVariable>();
        try {
            params = gateway.getCodeletParameters(codeletName, sessionHandle);
        }
        catch (Exception e) {
            // nothing to do
        }
        if (params != null) {
            for (YParameter param : params) {
                DataVariable editorVariable = new DataVariable();
                editorVariable.setDataType(param.getDataTypeName());
                editorVariable.setName(param.getName());
                if (param.isInput()) {
                    editorVariable.setUsage(DataVariable.USAGE_INPUT_ONLY);
                }
                else if (param.isOutput()) {
                    editorVariable.setUsage(DataVariable.USAGE_OUTPUT_ONLY);
                }

                varList.add(editorVariable);
            }

            if (! varList.isEmpty()) {
                List<DataVariable> inputList = new ArrayList<DataVariable>();
                List<DataVariable> outputList = new ArrayList<DataVariable>();
                for (DataVariable variable : varList) {
                    if (variable.getUsage() == DataVariable.USAGE_INPUT_ONLY) {
                        inputList.add(variable);
                    }
                    else {
                        outputList.add(variable);
                    }
                }
                for (DataVariable inputVar : inputList) {
                    for (DataVariable outputVar : outputList) {
                        if (inputVar.equalsIgnoreUsage(outputVar)) {
                            inputVar.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
                            varList.remove(outputVar);
                        }
                    }
                }
                int index = 0;
                for (DataVariable var : varList) {
                    var.setIndex(index++);
                    var.setUserDefined(false);
                }
            }
        }
        return varList;
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
    String testSessionID;
     try {
       testSessionID = tryConnect(serviceURI, userID, password);
       gateway.disconnect(testSessionID);
     } catch (Exception e) {
       testSessionID = "";
     }
     return (testSessionID.length() > 0) && (! testSessionID.startsWith("<failure>"));
  }

  public boolean checkConnection() {
      return !((gateway == null) || (sessionHandle == null))
              && gateway.checkConnection(sessionHandle);
  }
}
