package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationFileModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationFileModelListener;
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
public class OpenRecentSubMenu extends JMenu implements SpecificationFileModelListener {

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
        setIcon(getImageAsIcon());
        createMenuItems();
        loadMenuItems();
        SpecificationFileModel.getInstance().subscribe(this);
    }


    private ImageIcon getImageAsIcon() {
        return ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/open_recent.png");
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


    public void specificationFileModelStateChanged(int state) {
        setEnabled(state == SpecificationFileModel.IDLE);
    }

}
