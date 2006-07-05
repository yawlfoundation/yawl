/*
 * Created on 06/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.swing.menu;

import java.awt.Insets;

import au.edu.qut.yawl.editor.actions.RedoAction;
import au.edu.qut.yawl.editor.actions.UndoAction;
import au.edu.qut.yawl.editor.actions.net.DecreaseSizeAction;
import au.edu.qut.yawl.editor.actions.net.DeleteAction;
import au.edu.qut.yawl.editor.actions.net.IncreaseSizeAction;
import au.edu.qut.yawl.editor.actions.net.AlignTopAction;
import au.edu.qut.yawl.editor.actions.net.AlignBottomAction;
import au.edu.qut.yawl.editor.actions.net.AlignMiddleAction;
import au.edu.qut.yawl.editor.actions.net.AlignLeftAction;
import au.edu.qut.yawl.editor.actions.net.AlignCentreAction;
import au.edu.qut.yawl.editor.actions.net.AlignRightAction;
import au.edu.qut.yawl.editor.actions.net.AddToVisibleCancellationSetAction;
import au.edu.qut.yawl.editor.actions.net.RemoveFromVisibleCancellationSetAction;

import au.edu.qut.yawl.editor.actions.specification.CreateNetAction;
import au.edu.qut.yawl.editor.actions.net.RemoveNetAction;
import au.edu.qut.yawl.editor.actions.specification.CreateSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.OpenSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.SaveSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.SaveSpecificationAsAction;
import au.edu.qut.yawl.editor.actions.specification.CloseSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.ValidateSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.AnalyseSpecificationAction;
import au.edu.qut.yawl.editor.actions.specification.ExportToEngineFormatAction;
import au.edu.qut.yawl.editor.actions.specification.ImportFromEngineFormatAction;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class ToolBarMenu extends YAWLToolBar {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ToolBarMenu() {
    super("YAWLEditor ToolBar");
  }

  protected void buildInterface() {
    setMargin(new Insets(3,2,2,0));
    add(new YAWLToolBarButton(new CreateSpecificationAction()));
    add(new YAWLToolBarButton(new OpenSpecificationAction()));
    add(new YAWLToolBarButton(new SaveSpecificationAction()));
		add(new YAWLToolBarButton(new SaveSpecificationAsAction()));
    add(new YAWLToolBarButton(new CloseSpecificationAction()));
    if (YAWLEngineProxy.engineLibrariesAvailable()) {
      addSeparator();
	  add(new YAWLToolBarButton(new ValidateSpecificationAction()));
      add(new YAWLToolBarButton(new AnalyseSpecificationAction()));
      add(new YAWLToolBarButton(new ExportToEngineFormatAction()));
      add(new YAWLToolBarButton(new ImportFromEngineFormatAction()));
    }
    addSeparator();
    add(new YAWLToolBarButton(new CreateNetAction()));
		add(new YAWLToolBarButton(new RemoveNetAction()));
    addSeparator();
    add(new YAWLToolBarButton(UndoAction.getInstance()));
    add(new YAWLToolBarButton(RedoAction.getInstance()));
    add(new YAWLToolBarButton(DeleteAction.getInstance()));
    addSeparator();
		add(new YAWLToolBarButton(AlignTopAction.getInstance()));
		add(new YAWLToolBarButton(AlignMiddleAction.getInstance()));
		add(new YAWLToolBarButton(AlignBottomAction.getInstance()));
		add(new YAWLToolBarButton(AlignLeftAction.getInstance()));
		add(new YAWLToolBarButton(AlignCentreAction.getInstance()));
		add(new YAWLToolBarButton(AlignRightAction.getInstance()));
		addSeparator();
    add(new YAWLToolBarButton(IncreaseSizeAction.getInstance()));
    add(new YAWLToolBarButton(DecreaseSizeAction.getInstance()));
		addSeparator();
    add(new YAWLToolBarButton(AddToVisibleCancellationSetAction.getInstance()));
    add(new YAWLToolBarButton(RemoveFromVisibleCancellationSetAction.getInstance()));
  }
}
