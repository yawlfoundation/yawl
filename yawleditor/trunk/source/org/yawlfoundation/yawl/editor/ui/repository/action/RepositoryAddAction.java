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
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.repository.dialog.AddDialog;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Map;

public class RepositoryAddAction extends YAWLBaseAction {

    Repo selectedRepo;
    Component caller;
    JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Store in Repository");
        putValue(Action.NAME, "Store in Repository...");
        putValue(Action.LONG_DESCRIPTION, "Store in Repository");
        putValue(Action.SMALL_ICON, getPNGIcon("repo_add"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("alt T"));
    }

    public RepositoryAddAction(JDialog owner, Repo repo) {
        this.owner = owner;
        selectedRepo = repo;
    }


    public RepositoryAddAction(JDialog owner, Repo repo, Component component) {
        this(owner, repo);
        caller = component;
    }

    public void actionPerformed(ActionEvent event) {
        AddDialog dialog = new AddDialog(owner, getDefaultText());
        dialog.setVisible(true);
        String name = dialog.getRecordName();
        if (name != null) {
            String description = dialog.getRecordDescription();
            YRepository repo = YRepository.getInstance();
            switch (selectedRepo) {
                case TaskDecomposition:
                    YDecomposition decomposition = getSelectedDecomposition();
                    if (decomposition != null) {
                        repo.getTaskDecompositionRepository().add(
                                name, description, (YAWLServiceGateway) decomposition);
                    }
                    break;
                case NetDecomposition:
                    addCurrentNet(name, description);
                    break;
                case ExtendedAttributes:
                    repo.getExtendedAttributesRepository().add(name, description,
                            ((ExtendedAttributesDialog) owner).getAttributes());
                    break;
                case DataDefinition: {
                    String content = ((DataTypeDialogToolBarMenu) caller).getSelectedText();
                    repo.getDataDefinitionRepository().add(name, description, content);
                    break;
                }
            }
        }
    }

    private String getDefaultText() {
        switch (selectedRepo) {
            case TaskDecomposition:
                YDecomposition selected = getSelectedDecomposition();
                return selected != null ? selected.getID() : null;
            case NetDecomposition:
                return YAWLEditor.getNetsPane().getSelectedYNet().getID();
            case ExtendedAttributes: break;
            case DataDefinition: break;
        }
        return "";
    }

    private void addCurrentNet(String name, String description) {
        YRepository repo = YRepository.getInstance();
        YNet selectedNet = YAWLEditor.getNetsPane().getSelectedYNet();
        addContainedDecompositions(selectedNet);
        repo.getNetRepository().add(name, description, selectedNet);
    }


    private Map<String, String> addContainedDecompositions(YNet net) {
        Map<String, String> changedIDs = new Hashtable<String, String>();
        YRepository repo = YRepository.getInstance();
        String decompDescription = "Stored as required by storage of " +
                net.getID();
        for (YTask task : net.getNetTasks()) {
            YDecomposition decomposition = task.getDecompositionPrototype();
            String currentID = decomposition.getID();
            String newID = null;
            if (decomposition instanceof YAWLServiceGateway) {
                newID = repo.getTaskDecompositionRepository().add(currentID,
                        decompDescription, (YAWLServiceGateway) decomposition);
            }
            else if (decomposition instanceof YNet) {
                addContainedDecompositions((YNet) decomposition);     // sub-net recurse
                newID = repo.getNetRepository().add(decomposition.getID(),
                        decompDescription, (YNet) decomposition);
            }
            if (! currentID.equals(newID)) {
                changedIDs.put(currentID, newID);
            }
        }
        return changedIDs;
    }

    private YDecomposition getSelectedDecomposition() {
        Object cell = YAWLEditor.getNetsPane().getSelectedGraph().getSelectionCell();
        YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
        return vertex != null ? ((YAWLAtomicTask) vertex).getDecomposition() : null;
    }
}