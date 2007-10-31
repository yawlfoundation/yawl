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

package au.edu.qut.yawl.editor.thirdparty.resourcing;

import java.util.prefs.Preferences;

import au.edu.qut.yawl.editor.YAWLEditor;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;


import java.sql.Connection;

public class AvailableResourcingServiceProxyImplementation implements ResourcingtServiceProxyInterface {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static Connection databaseConnection = null;

  public boolean isDatabaseConnectionAvailable() {
    try {
      connect();
      databaseConnection.close();
    }catch(Exception e){
      return false;
    }
    return true;
  }
  
  public void connect() {}
  
  public void disconnect() {}
  
  public HashMap getAllHumanResourceNames() {
    connect();
    
    HashMap resultsMap = new HashMap();

    return resultsMap;
  }
  
  public List getAllRoles() {
    connect();
    
    LinkedList resultsList = new LinkedList();

    return resultsList;
  }
}
