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

import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

public class OrganisationDatabaseProxy implements OrganisationDatabaseProxyInterface {
  
  private transient static final OrganisationDatabaseProxy INSTANCE 
    = new OrganisationDatabaseProxy();

  private OrganisationDatabaseProxyInterface availableImplementation;
  private OrganisationDatabaseProxyInterface unavailableImplementation;

  public static OrganisationDatabaseProxy getInstance() {
    return INSTANCE; 
  }
    
  private OrganisationDatabaseProxy() {}
  
  private OrganisationDatabaseProxyInterface getImplementation() {
    if (getAvailableImplementation().isDatabaseConnectionAvailable()) {
      return getAvailableImplementation();
    } 
    return getUnavailableImplementation();
  }
  
  private OrganisationDatabaseProxyInterface getAvailableImplementation() {
    if (availableImplementation == null) {
      availableImplementation = new AvailableOrganisationDatabaseImplementation();
    }
    return availableImplementation;
  }
  
  private OrganisationDatabaseProxyInterface getUnavailableImplementation() {
    if (unavailableImplementation == null) {
      unavailableImplementation = new UnavailableOrganisationDatabaseImplementation();
    }
    return unavailableImplementation;
  }

  public boolean isDatabaseConnectionAvailable() {
    return getImplementation().isDatabaseConnectionAvailable();
  }

  public void connect() {
    getImplementation().connect();
  }

  public void disconnect() {
    getImplementation().disconnect();
  }
  
  public HashMap getAllHumanResourceNames() {
    return getImplementation().getAllHumanResourceNames();
  }
  
  public List getAllRoles() {
    return getImplementation().getAllRoles();
  }

  public String getQueryFromResourceMapping(ResourceMapping mapping) {
    return getImplementation().getQueryFromResourceMapping(mapping);
  }

  public String getQueryToIdentifyResource(String resourceIdentifier) {
    return getImplementation().getQueryToIdentifyResource(resourceIdentifier);
  }
  
  public String getQueryToIdentifyRole(String roleIdentifier) {
    return getImplementation().getQueryToIdentifyRole(roleIdentifier);
  }
}
