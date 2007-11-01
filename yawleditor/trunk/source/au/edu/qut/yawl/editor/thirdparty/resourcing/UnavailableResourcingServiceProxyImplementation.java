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

import java.util.List;
import java.util.LinkedList;

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
  
  public List<String>getAllParticipants() {
    return new LinkedList<String>();
  }
  
  public List<ResourcingRole> getAllRoles() {
    
    LinkedList<ResourcingRole> roles = new LinkedList<ResourcingRole>();
    
    roles.add(
        new ResourcingRole("1", "Rob")
    );
    roles.add(
        new ResourcingRole("2", "Annette")
    );
    roles.add(
        new ResourcingRole("3", "Reuben")
    );
    roles.add(
        new ResourcingRole("4", "Linds")
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

  public List<String> getRegisteredAllocationMechanisms() {
   /*  
    *  The lables for the standard, guaranteed to be there, allocation
    *  mechanisms are necessarilly redundant.  If the engine ever 
    *  changes the name of one of the standard allocation mechanisms,
    *  the name must also be updated here.
    */
    
    // TODO: Don't understand where/how names are being set on the engine-side
    
    
    LinkedList<String> list = new LinkedList<String>();

    list.add("Round-Robin");
    list.add("Shortest-Queue");
    list.add("Random");
    
    return list;
  }
}
