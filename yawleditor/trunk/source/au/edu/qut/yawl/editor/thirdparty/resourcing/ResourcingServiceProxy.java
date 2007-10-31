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

public class ResourcingServiceProxy implements ResourcingtServiceProxyInterface {
  
  private transient static final ResourcingServiceProxy INSTANCE 
    = new ResourcingServiceProxy();

  private ResourcingtServiceProxyInterface availableImplementation;
  private ResourcingtServiceProxyInterface unavailableImplementation;

  public static ResourcingServiceProxy getInstance() {
    return INSTANCE; 
  }
    
  private ResourcingServiceProxy() {}
  
  private ResourcingtServiceProxyInterface getImplementation() {
    if (getAvailableImplementation().isDatabaseConnectionAvailable()) {
      return getAvailableImplementation();
    } 
    return getUnavailableImplementation();
  }
  
  private ResourcingtServiceProxyInterface getAvailableImplementation() {
    if (availableImplementation == null) {
      availableImplementation = new AvailableResourcingServiceProxyImplementation();
    }
    return availableImplementation;
  }
  
  private ResourcingtServiceProxyInterface getUnavailableImplementation() {
    if (unavailableImplementation == null) {
      unavailableImplementation = new UnavailableResourcingServiceProxyImplementation();
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
}
