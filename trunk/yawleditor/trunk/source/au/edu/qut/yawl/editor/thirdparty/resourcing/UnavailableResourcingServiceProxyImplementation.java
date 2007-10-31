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

public class UnavailableResourcingServiceProxyImplementation implements ResourcingtServiceProxyInterface {
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
}
