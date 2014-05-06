/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.RedoAction;
import org.yawlfoundation.yawl.editor.ui.actions.UndoAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.*;
import org.yawlfoundation.yawl.editor.ui.actions.net.align.*;
import org.yawlfoundation.yawl.editor.ui.actions.net.zoom.ZoomActualSizeAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.zoom.ZoomInAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.zoom.ZoomOutAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.zoom.ZoomSelectedElementsAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.*;
import org.yawlfoundation.yawl.editor.ui.actions.view.ToolbarGridToggleAction;
import org.yawlfoundation.yawl.editor.ui.actions.view.ToolbarTipToggleAction;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.*;

public class ToolBarMenu extends YToolBar {

    public ToolBarMenu() {
        super("YAWLEditor ToolBar");
    }


    public AbstractButton getButtonWithAction(Action action) {
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) c;
                if (button.getAction().equals(action)) {
                    return button;
                }
            }
        }
        return null;
    }


    protected void buildInterface() {
        setMargin(new Insets(3, 2, 2, 0));
        add(new YAWLToolBarButton(new CreateSpecificationAction()));
        add(new YAWLToolBarButton(new OpenSpecificationAction()));
        add(new YAWLToolBarButton(new SaveSpecificationAction()));
        add(new YAWLToolBarButton(new SaveSpecificationAsAction()));
        add(new YAWLToolBarButton(new CloseSpecificationAction()));

        addSeparator();
        add(new YAWLToolBarButton(new DownloadSpecificationAction()));
        add(new YAWLToolBarButton(new UploadSpecificationAction()));

        addSeparator();
        add(new YAWLToolBarButton(UndoAction.getInstance()));
        add(new YAWLToolBarButton(RedoAction.getInstance()));
        add(new YAWLToolBarButton(DeleteAction.getInstance()));

        addSeparator();
        add(new YAWLToolBarButton(new ValidateSpecificationAction()));
        add(new YAWLToolBarButton(new AnalyseSpecificationAction()));

        addSeparator();
        add(new YAWLToolBarButton(new CreateNetAction()));
        add(new YAWLToolBarButton(new RemoveNetAction()));

        addSeparator();
        add(new YAWLToggleToolBarButton(ViewCancellationSetAction.getInstance()));
        add(new YAWLToolBarButton(AddToVisibleCancellationSetAction.getInstance()));
        add(new YAWLToolBarButton(RemoveFromVisibleCancellationSetAction.getInstance()));

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
        add(new YAWLToolBarButton(ZoomActualSizeAction.getInstance()));
        add(new YAWLToolBarButton(ZoomOutAction.getInstance()));
        add(new YAWLToolBarButton(ZoomInAction.getInstance()));
        add(new YAWLToolBarButton(ZoomSelectedElementsAction.getInstance()));

        addSeparator();
        YAWLToggleToolBarButton gridButton = new YAWLToggleToolBarButton(
                new ToolbarGridToggleAction());
        gridButton.setSelected(UserSettings.getShowGrid());
        add(gridButton);

        YAWLToggleToolBarButton tipButton = new YAWLToggleToolBarButton(
                new ToolbarTipToggleAction());
        tipButton.setSelected(UserSettings.getShowToolTips());
        add(tipButton);
    }

}
