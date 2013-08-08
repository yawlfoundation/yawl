/*
 * Created on 05/10/2003
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

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.actions.net.*;
import org.yawlfoundation.yawl.editor.ui.actions.specification.CreateNetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;

import java.awt.event.KeyEvent;

class NetMenu extends YAWLOpenSpecificationMenu {

    public NetMenu() {
        super("Net", KeyEvent.VK_N);
    }

    protected void buildInterface() {
        add(new YAWLMenuItem(new CreateNetAction()));
        add(new YAWLMenuItem(new RemoveNetAction()));
        add(new YAWLMenuItem(new SetRootNetAction()));
        addSeparator();
        add(new YAWLMenuItem(new ExportNetToPngAction()));
        addSeparator();
        add(new YAWLMenuItem(new NetBackgroundColourAction()));
        add(new YAWLMenuItem(new NetBackgroundImageAction()));
        addSeparator();
        add(new YAWLMenuItem(new PrintNetAction()));
        addSeparator();
        add(new YAWLMenuItem(new RepositoryAddAction(null, Repo.NetDecomposition, null)));
        add(new YAWLMenuItem(new RepositoryGetAction(null, Repo.NetDecomposition, null)));
        add(new YAWLMenuItem(new RepositoryRemoveAction(null, Repo.NetDecomposition, null)));
        addSeparator();
        add(new ProcessConfigurationMenu());
    }
}
