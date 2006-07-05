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

package au.edu.qut.yawl.editor.thirdparty.orgdatabase;

import java.util.prefs.Preferences;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

import java.util.Properties;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.postgresql.Driver;

public class AvailableOrganisationDatabaseImplementation implements OrganisationDatabaseProxyInterface {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static Connection databaseConnection = null;
  
  protected static final Driver DRIVER = new Driver();  // for the DriverManager
  
  private static final int RESOURCE_COLUMN_ID = 1;
  private static final int RESOURCE_COLUMN_FIRSTNAME = 2;
  private static final int RESOURCE_COLUMN_SURNAME = 3;
//  private static final int RESOURCE_COLUMN_TYPE = 4;
  
  private static final int ROLE_COLUMN_NAME = 1;

  public boolean isDatabaseConnectionAvailable() {
    try {
      connect();
      databaseConnection.close();
    }catch(Exception e){
      return false;
    }
    return true;
  }
  
  public void connect() {
    String databaseURI = 
      prefs.get("organisationDatabaseURI", 
                DEFAULT_DATABASE_URI) + 
      prefs.get("organisationDatabaseName", 
                DEFAULT_DATABASE_NAME);
                
    Properties databaseProperties = new Properties();
    
    databaseProperties.setProperty(
        "user",
         prefs.get("organisationDatabaseUserID", 
                   DEFAULT_DATABASE_USER)
    );

    databaseProperties.setProperty(
        "password",
         prefs.get("organisationDatabaseUserPassword", 
                   DEFAULT_DATABASE_USER_PASSWORD)
    );
    
    try {
      databaseConnection = DriverManager.getConnection(databaseURI, databaseProperties);
      
      // System.out.println("Connection established");
    }catch(SQLException e){
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          e.getMessage(),
          "Organisation database connection error",
          JOptionPane.ERROR_MESSAGE);
      databaseConnection = null;
    }
  }
  
  public void disconnect() {
    try {
      databaseConnection.close();
    }catch(SQLException e) {
      databaseConnection = null;
    }
  }
  
  public HashMap getAllHumanResourceNames() {
    connect();
    
    HashMap resultsMap = new HashMap();
    try {
      Statement statement = databaseConnection.createStatement();
      ResultSet results = statement.executeQuery(
          "SELECT ID,GivenName,SurName FROM ResSerPosID WHERE IsOfResourceType = 'Human';"
      );

      while(results.next()) {
        resultsMap.put(
            results.getString(RESOURCE_COLUMN_FIRSTNAME).trim() + " " +
            results.getString(RESOURCE_COLUMN_SURNAME).trim(),
            results.getString(RESOURCE_COLUMN_ID)
        );
      }
      statement.close();
    
    } catch (Exception e) {
      // nothing needs doing.
    } finally {
      disconnect();
    }

    return resultsMap;
  }
  
  public List getAllRoles() {
    connect();
    
    LinkedList resultsList = new LinkedList();
    try {
      Statement statement = databaseConnection.createStatement();
      ResultSet results = statement.executeQuery("SELECT RoleName FROM Role;");

      while(results.next()) {
        resultsList.add(
            results.getString(ROLE_COLUMN_NAME).trim()
        );
      }
      statement.close();
    
    } catch (Exception e) {
      // nothing needs doing
    } finally {
      disconnect();
    }

    return resultsList;
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
    return "select ID FROM ResSerPosID WHERE IsOfResourceType = 'Human' and ID='" + resourceIdentifier.trim() + "'";
  }

  public String getQueryToIdentifyRole(String roleIdentifier) {
    return "select HResID from HResPerformsRole where RoleName ='" + roleIdentifier.trim() + "'";
  }
}
