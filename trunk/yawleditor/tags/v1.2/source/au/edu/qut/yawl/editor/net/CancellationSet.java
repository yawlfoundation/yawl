/*
 * Created on 23/01/2004
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.net;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;

import java.io.Serializable;
import java.util.HashSet;

public class CancellationSet implements Serializable, Cloneable {
  private YAWLTask triggeringTask;
  private HashSet  setMembers;
  
  public CancellationSet() {
  	this.triggeringTask = null;
  	setMembers = new HashSet();
  }
 
  public CancellationSet(YAWLTask triggeringTask) {
    this.triggeringTask = triggeringTask;
    setMembers = new HashSet();
  }

  public YAWLTask getTriggeringTask() {
    return this.triggeringTask;
  }
  
  public void setTriggeringTask(YAWLTask triggeringTask) {
  	this.triggeringTask = triggeringTask;
  }

  public HashSet getSetMembers() {
    return setMembers;
  }

  public void setSetMembers(HashSet setMembers) {
  	this.setMembers = setMembers;
  }

  public boolean addMember(YAWLCell newSetMember) {
    if (newSetMember == this.triggeringTask) {
      return false;
    }
    if (newSetMember instanceof YAWLTask || newSetMember instanceof Condition || 
        newSetMember instanceof YAWLFlowRelation) {
      return setMembers.add(newSetMember);
    }
    return false;
  }
  
  public boolean removeMember(YAWLCell oldSetMember) {
    return setMembers.remove(oldSetMember);
  }
  
  public boolean contains(YAWLCell cell) {
    return setMembers.contains(cell);
  }
  
  public int size() {
    return setMembers.size();
  }
  
  public Object clone() {
    // Cancellation set members will NOT be copied.
    return new CancellationSet();
  }
}
