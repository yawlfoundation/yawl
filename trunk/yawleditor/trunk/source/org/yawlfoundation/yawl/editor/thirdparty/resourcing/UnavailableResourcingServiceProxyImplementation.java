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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UnavailableResourcingServiceProxyImplementation implements ResourcingServiceProxyInterface {
  public boolean isDatabaseConnectionAvailable() {
    return false;    
  }
  
  public void connect() {
    // deliberately does nothing.
  }

  public void disconnect() {
    // deliberately does nothing.
  }
  
  public List<ResourcingParticipant>getAllParticipants() {
    return new LinkedList<ResourcingParticipant>();
  }
  
  public List<ResourcingRole> getAllRoles() {
   return new LinkedList<ResourcingRole>();
  }
  
  public boolean testConnection() {
    return false;
  }
  
  public boolean testConnection(String serviceURI, String userID, String password) {
    return false;
  }

  public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
    LinkedList<AllocationMechanism> mechanisms = new LinkedList<AllocationMechanism>();

    mechanisms.add(AllocationMechanism.RANDOM_MECHANISM);
    //mechanisms.add(AllocationMechanism.ROUND_ROBIN_MECHANISM);
    mechanisms.add(AllocationMechanism.SHORTEST_QUEUE_MECHANISM);
    
    return mechanisms;
  }

  public List<ResourcingFilter> getRegisteredResourcingFilters() {
    /* -- I've been using this for internal testing. Leaving it here briefly for further testing.
    LinkedList<ResourcingFilter> filters = new LinkedList<ResourcingFilter>();
    
    ResourcingFilter orgFilter = new ResourcingFilter("OrgFilter", "Organisational Group");

    HashMap<String,String> orgFilterParams = new HashMap<String, String>();
    orgFilterParams.put("OrgGroup", "");
    orgFilterParams.put("Position", "");
    
    orgFilter.setParameters(orgFilterParams);
    
    filters.add(
        orgFilter
    );

    ResourcingFilter capabilityFilter = new ResourcingFilter("CapabilityFilter", "Capability");

    HashMap<String,String> capabilityFilterParams = new HashMap<String, String>();
    capabilityFilterParams.put("Capability", "");

    capabilityFilter.setParameters(
      capabilityFilterParams    
    );
    
    filters.add(
        capabilityFilter
    );

    return filters;
    */
    return new LinkedList<ResourcingFilter>();
  }

  public Map<String, String> getRegisteredCodelets() {
    return null ;
  }  
}