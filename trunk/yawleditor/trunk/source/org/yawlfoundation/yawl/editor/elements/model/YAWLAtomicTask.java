/*
 * Created on 30/01/2004, 09:04:27
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2004 Lindsay Bradford
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
 *
 */

package org.yawlfoundation.yawl.editor.elements.model;

import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;

public interface YAWLAtomicTask {
  public void setWSDecomposition(WebServiceDecomposition decomposition);
  public WebServiceDecomposition getWSDecomposition();
  
  public void setIconPath(String iconPath);
  public String getIconPath();
  
  public String getLabel();
  
  public void setResourceMapping(ResourceMapping resourceMapping);
  public ResourceMapping getResourceMapping();
}
