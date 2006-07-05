/*
 * Created on 09/10/2003
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

package au.edu.qut.yawl.editor.actions.palette;

import javax.swing.Action;

import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.menu.Palette;

public class FlowRelationAction extends YAWLPaletteAction implements TooltipTogglingWidget {
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Flow Relation");
    putValue(Action.LONG_DESCRIPTION, "Add a new Flow Relation");
    putValue(Action.SMALL_ICON, getPaletteIconByName("PaletteFlowRelation"));
    setIdentifier(Palette.FLOW_RELATION);
  }

  public String getEnabledTooltipText() {
    return " Add a Flow Relation ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification, and selected net to use the palette ";
  }
}
