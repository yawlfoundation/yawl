/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.repository.action;

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.core.repository.RepoDescriptor;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.repository.dialog.DescriptorListDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RepositoryGetAction extends YAWLBaseAction {

    Repo selectedRepo;
    Component caller;
    JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Load From Repository");
        putValue(Action.NAME, "Load From Repository...");
        putValue(Action.LONG_DESCRIPTION, "Load From Repository");
        putValue(Action.SMALL_ICON, getPNGIcon("repo_get"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("alt L"));
    }

    public RepositoryGetAction(JDialog owner, Repo repo) {
        this.owner = owner;
        selectedRepo = repo;
    }

    public RepositoryGetAction(JDialog owner, Repo repo, Component component) {
        this(owner, repo);
        caller = component;
    }

    public void actionPerformed(ActionEvent event) {
        DescriptorListDialog dialog = new DescriptorListDialog(owner, selectedRepo,
                DescriptorListDialog.GET_ACTION);
        dialog.setVisible(true);
        RepoDescriptor descriptor = dialog.getSelection();
        if (descriptor != null) {
            YRepository repo = YRepository.getInstance();
            String name = descriptor.getName();
            switch (selectedRepo) {
                case TaskDecomposition:
                    loadTaskDecomposition(name);
                    break;
                case NetDecomposition: break;
                case ExtendedAttributes:
                    ((ExtendedAttributesDialog) owner).loadAttributes(
                            repo.getExtendedAttributesRepository().get(name));
                    break;
                case DataDefinition: {
                    String text = repo.getDataDefinitionRepository().get(name);
                    ((DataTypeDialogToolBarMenu) caller).insertText(text, true);
                    break;
                }
            }
        }
    }

    private void loadTaskDecomposition(String name) {
        try {
            YAWLServiceGateway gateway = YRepository.getInstance()
                    .getTaskDecompositionRepository().get(name);
            if (gateway != null) {
                SpecificationModel.getHandler().getControlFlowHandler()
                        .addTaskDecomposition(gateway);
            }
        }
        catch (YSyntaxException yse) {
            // ?
        }
    }
}