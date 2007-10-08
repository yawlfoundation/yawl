/*
 * Created on 1/01/2004, 09:01:47
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

package au.edu.qut.yawl.editor.net;

public class PrettyOutputStateManager {
  private NetGraph net;
  
  private double rememberedScale;
  private boolean rememberedGridVisibility;
  private boolean rememberedPortVisibility;
  private Object[] rememberedSelectionSet;

  public PrettyOutputStateManager(NetGraph net) {
    this.net = net;
  }
  
  public void makeGraphOutputReady() {
    rememberCurrentNetGraphState();
    makeStateOfNetGraphOutputFriendly();
  }
  
  private void rememberCurrentNetGraphState() {
    rememberedScale = net.getScale();
    rememberedGridVisibility = net.isGridVisible();
    rememberedPortVisibility = net.isPortsVisible();
    rememberedSelectionSet = 
      net.getSelectionModel().getSelectionCells();
  }

  private void makeStateOfNetGraphOutputFriendly() {
    net.setScale(1);
    net.setGridVisible(false);
    net.setPortsVisible(false);
    net.getSelectionModel().clearSelection();
  }
  
  public void revertNetGraphToPreviousState() {
    net.setScale(rememberedScale);
    net.setGridVisible(rememberedGridVisibility);
    net.setPortsVisible(rememberedPortVisibility);
    net.getSelectionModel().addSelectionCells(rememberedSelectionSet);
  }
}
