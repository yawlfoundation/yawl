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

package au.edu.qut.yawl.editor.thirdparty.orgdatabase;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.resourcing.ResourceMapping;


public class UnavailableOrganisationDatabaseImplementation implements OrganisationDatabaseProxyInterface {
  public boolean isDatabaseConnectionAvailable() {
    return false;    
  }
  
  public void connect() {
    // deliberately does nothing.
  }

  public void disconnect() {
    // deliberately does nothing.
  }
  
  public HashMap getAllHumanResourceNames() {
    return new HashMap();
  }
  
  public List getAllRoles() {
    return new LinkedList();
  }
  
  public String getQueryFromResourceMapping(ResourceMapping mapping) {
    switch(mapping.getMappingType()) {
      case ResourceMapping.ALLOCATE_DIRECTLY: case ResourceMapping.AUTHORISED_DIRECTLY: {
        return getQueryToIdentifyResource(mapping.getIdentifier());
      }
      case ResourceMapping.ALLOCATE_TO_ROLE: case ResourceMapping.AUTHORISATION_VIA_ROLE: {
        return getQueryToIdentifyRole(mapping.getIdentifier());
      }
      default: {
        return null;
      }
    }
  }

  public String getQueryToIdentifyResource(String resourceIdentifier) {
    return "SELECT * FROM Resource where ResourceId = '" + resourceIdentifier.trim() + "';";
  }

  public String getQueryToIdentifyRole(String roleIdentifier) {
    return "select Resource.* from Resource,HumanResourcePerformsExtraRole where " +
           "HumanResourcePerformsExtraRole.RoleName = '" + roleIdentifier.trim() + "' " + 
           "and HumanResourcePerformsExtraRole.ResourceId = Resource.ResourceId;";
  }
}
