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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuItem;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 25/02/2009
 */
public class OpenRecentSubMenu extends JMenu implements FileStateListener {

    private static OpenRecentSubMenu INSTANCE = null;
    private YAWLMenuItem[] items;

    public static OpenRecentSubMenu getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OpenRecentSubMenu() ;
        }
        return INSTANCE ;
    }

    private OpenRecentSubMenu() {
        super("Open Recent");
        setMnemonic(KeyEvent.VK_R);
        setIcon(ResourceLoader.getMenuIcon("open_recent"));
        createMenuItems();
        loadMenuItems();
        Publisher.getInstance().subscribe(this);
    }

    
    private void createMenuItems() {
        items = new YAWLMenuItem[8];
        for (int i = 0; i < 8; i++) {
            YAWLMenuItem item = new YAWLMenuItem(new OpenRecentSpecificationAction());
            item.setMnemonic(0x31 + i);
            item.setAccelerator(MenuUtilities.getAcceleratorKeyStroke("" + (i+1)));
            item.setVisible(false);
            add(item);
            items[i] = item;
        }
    }


    private void loadMenuItems() {
        List<String> recentList = UserSettings.loadRecentFileList();
        filterFileList(recentList);
        for (int i = 0; i < recentList.size(); i++) {
            String fileName = recentList.get(i) ;
            if ((fileName != null) && (fileName.length() > 0)) {
                ((OpenRecentSpecificationAction) items[i].getAction()).setFileName(fileName);
                items[i].setVisible(true);
                items[i].setToolTipText(fileName);
            }
            else items[i].setVisible(false);
        }
    }


    private void filterFileList(List<String> fileNameList) {
        List<String> invalidList = new ArrayList<String>();
        for (int i = 0; i < fileNameList.size(); i++) {
            String filename = fileNameList.get(i);
            if (! new File(filename).exists()) {
                invalidList.add(filename);
                UserSettings.removeRecentFile(i);
            }
        }
        fileNameList.removeAll(invalidList);
    }


    public void addRecentFile(String fullFileName) {
        UserSettings.pushRecentFile(fullFileName);
        loadMenuItems();
    }


    public void specificationFileStateChange(FileState state) {
        setEnabled(state == FileState.Closed);
    }

}
