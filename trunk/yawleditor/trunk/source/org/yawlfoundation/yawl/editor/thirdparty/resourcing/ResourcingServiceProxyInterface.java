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

package org.yawlfoundation.yawl.editor.thirdparty.resourcing;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingFilter;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingParticipant;
import org.yawlfoundation.yawl.editor.resourcing.ResourcingRole;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public interface ResourcingServiceProxyInterface {
  
    public static final Preferences prefs =
      Preferences.userNodeForPackage(YAWLEditor.class);

  public static final String DEFAULT_RESOURCING_SERVICE_URI = 
    "http://localhost:8080/resourceService/gateway";

  public static final String DEFAULT_RESOURCING_SERVICE_USERID = "editor";

  public static final String DEFAULT_RESOURCING_SERVICE_USER_PASSWORD = "yEditor";
  
  public boolean connect();
  public void disconnect();

  public boolean testConnection();
  public boolean testConnection(String serviceURI, String userID, String password);
  
  public List<ResourcingParticipant> getAllParticipants();
  public List<ResourcingRole> getAllRoles();
  public List<String> getAllParticipantIDs();
  public List<String> getAllRoleIDs();

  
  public List<ResourcingFilter> getRegisteredResourcingFilters();
  
  public List<AllocationMechanism> getRegisteredAllocationMechanisms();

  public List getCapabilities();
  public List getPositions();
  public List getOrgGroups();

  public Map<String, String> getRegisteredCodelets() ;

   public List<DataVariable> getCodeletParameters(String codeletName);

}
