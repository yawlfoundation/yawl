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

package au.edu.qut.yawl.editor.thirdparty.resourcing;

import java.util.List;

import au.edu.qut.yawl.editor.resourcing.ResourcingRole;

public interface ResourcingServiceProxyInterface {
  
  public static final String DEFAULT_RESOURCING_SERVICE_URI = 
    "http://localhost:8080/resourceService/gateway";

  public static final String DEFAULT_RESOURCING_SERVICE_USERID = 
    "admin";

  public static final String DEFAULT_RESOURCING_SERVICE_USER_PASSWORD = 
    "YAWL";
  
  public void connect();
  public void disconnect();

  public boolean testConnection();
  public boolean testConnection(String serviceURI, String userID, String password);
  
  public List<String> getAllParticipants();
  public List<ResourcingRole> getAllRoles();
  
  public List<String> getRegisteredAllocationMechanisms();
  
}
