/*
 * Created on 17/05/2005
 * YAWLEditor v1.1-2
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

public interface OrganisationDatabaseProxyInterface {
  
  public static final String DEFAULT_DATABASE_URI = 
    "jdbc:postgresql://localhost/";
  
  public static final String DEFAULT_DATABASE_NAME = "yawl";
  public static final String DEFAULT_DATABASE_USER = "postgres";
  public static final String DEFAULT_DATABASE_USER_PASSWORD = "admin";
  
  public boolean isDatabaseConnectionAvailable();
  public void connect();
  public void disconnect();
  
  public HashMap getAllHumanResourceNames();
  public List getAllRoles();
  
  public String getQueryFromResourceMapping(ResourceMapping mapping);
  public String getQueryToIdentifyResource(String resourceIdentifier);
  public String getQueryToIdentifyRole(String roleIdentifier);
}
