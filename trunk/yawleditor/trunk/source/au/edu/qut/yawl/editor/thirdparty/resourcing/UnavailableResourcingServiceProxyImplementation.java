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

package au.edu.qut.yawl.editor.thirdparty.resourcing;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.resourcing.AllocationMechanism;
import au.edu.qut.yawl.editor.resourcing.ResourcingFilter;
import au.edu.qut.yawl.editor.resourcing.ResourcingParticipant;
import au.edu.qut.yawl.editor.resourcing.ResourcingRole;

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
    
    LinkedList<ResourcingParticipant> participants = new LinkedList<ResourcingParticipant>();
    
    participants.add(
        new ResourcingParticipant("1", "Rob Thiem")
    );
    participants.add(
        new ResourcingParticipant("2", "Annette Fraser")
    );
    participants.add(
        new ResourcingParticipant("3", "Reuben Hemls")
    );
    participants.add(
        new ResourcingParticipant("4", "I'm Old Greg!")
    );

    return participants;
    // return new LinkedList<ResourcingParticipant>();
  }
  
  public List<ResourcingRole> getAllRoles() {
    
    LinkedList<ResourcingRole> roles = new LinkedList<ResourcingRole>();
    
    roles.add(
        new ResourcingRole("1", "CEO")
    );
    roles.add(
        new ResourcingRole("2", "CIO")
    );
    roles.add(
        new ResourcingRole("3", "Accountant")
    );
    roles.add(
        new ResourcingRole("4", "Consultant")
    );

    return roles;
    // return new LinkedList<ResourcingRole>();
  }
  
  public boolean testConnection() {
    return false;
  }
  
  public boolean testConnection(String serviceURI, String userID, String password) {
    return false;
  }

  public List<AllocationMechanism> getRegisteredAllocationMechanisms() {
   /*  
    *  The lables for the standard, guaranteed to be there, allocation
    *  mechanisms are necessarilly redundant.  If the engine ever 
    *  changes the name and/or class name of one of the standard 
    *  allocation mechanisms, that detail must also be updated here.
    */
    
    // TODO: Don't understand where/how names are being set on the engine-side
    
    LinkedList<AllocationMechanism> mechanisms = new LinkedList<AllocationMechanism>();
    
    mechanisms.add(
        new AllocationMechanism(
            "RandomChoice",
            "Random Choice",
            "blah on random choice"
        )
    );

    mechanisms.add(
        new AllocationMechanism(
            "RoundRobin",
            "Round-Robin",
            "blah on round-robin"
        )
    );

    mechanisms.add(
        new AllocationMechanism(
            "ShortestQueue",
            "Shortest-Queue",
            "blah on shortest-queue"
        )
    );

    
    return mechanisms;
  }

  public List<ResourcingFilter> getRegisteredResourcingFilters() {
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
    
    // return new LinkedList<ResourcingFilter>();
  }
}