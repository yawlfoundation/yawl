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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.RedoAction;
import org.yawlfoundation.yawl.editor.ui.actions.UndoAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.*;
import org.yawlfoundation.yawl.editor.ui.actions.specification.*;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModelListener;

import java.awt.*;

public class ToolBarMenu extends YAWLToolBar implements ProcessConfigurationModelListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YAWLToggleToolBarButton previewProcessConfigurationButton;
  private YAWLToggleToolBarButton applyProcessConfigurationButton;


  public ToolBarMenu() {
    super("YAWLEditor ToolBar");
    ProcessConfigurationModel.getInstance().subscribe(this);
  }

  protected void buildInterface() {
    setMargin(new Insets(3,2,2,0));
    add(new YAWLToolBarButton(new CreateSpecificationAction()));
    add(new YAWLToolBarButton(new OpenSpecificationAction()));
    add(new YAWLToolBarButton(new SaveSpecificationAction()));
    add(new YAWLToolBarButton(new SaveSpecificationAsAction()));
    add(new YAWLToolBarButton(new CloseSpecificationAction()));

    addSeparator();

	add(new YAWLToolBarButton(new ValidateSpecificationAction()));
    add(new YAWLToolBarButton(new AnalyseSpecificationAction()));

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
    addSeparator();
    previewProcessConfigurationButton =
            new YAWLToggleToolBarButton(PreviewConfigurationProcessAction.getInstance());
    add(previewProcessConfigurationButton);
    applyProcessConfigurationButton =
            new YAWLToggleToolBarButton(ApplyProcessConfigurationAction.getInstance());
    add(applyProcessConfigurationButton);
    addSeparator();
    add(new YAWLToolBarButton(ZoomActualSizeAction.getInstance()));
    add(new YAWLToolBarButton(ZoomOutAction.getInstance()));
    add(new YAWLToolBarButton(ZoomInAction.getInstance()));
    add(new YAWLToolBarButton(ZoomSelectedElementsAction.getInstance()));
  }


    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        previewProcessConfigurationButton.setSelected(
                previewState != ProcessConfigurationModel.PreviewState.OFF);

        applyProcessConfigurationButton.setSelected(
                applyState == ProcessConfigurationModel.ApplyState.ON);
    }
}
