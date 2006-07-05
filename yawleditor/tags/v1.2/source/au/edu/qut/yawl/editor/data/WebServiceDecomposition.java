/*
 * Created on 27/09/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.data;

public class WebServiceDecomposition extends Decomposition {
  private String serviceDescriptionURI = "";
  private String serviceOperation = "";
  private String yawlServiceID = null;
  private String yawlServiceDescription = "";
  
  public static final String DEFAULT_ENGINE_SERVICE_NAME = "Default Engine Worklist";
  
  public static final WebServiceDecomposition DEFAULT_WS_DECOMPOSITION = 
    new WebServiceDecomposition(
      "",
      "",
      null,
      DEFAULT_ENGINE_SERVICE_NAME
    );
  
  public WebServiceDecomposition() {
    super();
  }
  
  public WebServiceDecomposition(String serviceDecsriptionURI, 
                                 String serviceOperation, 
                                 String yawlServiceID,
                                 String yawlServiceDescription) {
    setServiceDescriptionURI(serviceDescriptionURI);
    setServiceOperation(serviceOperation);
    setYawlServiceID(yawlServiceID);
    setYawlServiceDescription(yawlServiceDescription);
  }
  
  public String getServiceDescriptionURI() {
    return this.serviceDescriptionURI;
  }
  
  public void setServiceDescriptionURI(String serviceDescriptionURI) {
    this.serviceDescriptionURI = serviceDescriptionURI;
  }
  
  public String getServiceOperation() {
    return this.serviceOperation;
  }
  
  public void setServiceOperation(String serviceOperation) {
    this.serviceOperation = serviceOperation;
  }

  public String getYawlServiceID() {
    return this.yawlServiceID;
  }
  
  public void setYawlServiceID(String yawlServiceID) {
    this.yawlServiceID = yawlServiceID;
  }
  
  public String getYawlServiceDescription() {
    return this.yawlServiceDescription;
  }
  
  public void setYawlServiceDescription(String yawlServiceDescription) {
    this.yawlServiceDescription = yawlServiceDescription;
  }
}
