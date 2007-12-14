/*
 * Created on 27/09/2004
 * YAWLEditor v1.01 
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

package org.yawlfoundation.yawl.editor.data;

public class WebServiceDecomposition extends Decomposition {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_ENGINE_SERVICE_NAME = "Default Engine Worklist";
  
  public static final WebServiceDecomposition DEFAULT_WS_DECOMPOSITION = 
    new WebServiceDecomposition(
      null,
      DEFAULT_ENGINE_SERVICE_NAME
    );
  
  public WebServiceDecomposition() {
    super();
    setYawlServiceID(null);
    setYawlServiceDescription("");
  }
  
  public WebServiceDecomposition(String yawlServiceID,
                                 String yawlServiceDescription) {
    setYawlServiceID(yawlServiceID);
    setYawlServiceDescription(yawlServiceDescription);
  }

  public String getYawlServiceID() {
    return (String) serializationProofAttributeMap.get("yawlServiceID");
  }
  
  public void setYawlServiceID(String yawlServiceID) {
    serializationProofAttributeMap.put("yawlServiceID",yawlServiceID);
  }
  
  public String getYawlServiceDescription() {
    return (String) serializationProofAttributeMap.get("yawlServiceDescription");
  }
  
  public void setYawlServiceDescription(String yawlServiceDescription) {
    serializationProofAttributeMap.put("yawlServiceDescription",yawlServiceDescription);
  }
  
  public boolean invokesWorklist() {
    if (this.getYawlServiceID() == null) {
      return true;
    }
    return false;
  }
}
