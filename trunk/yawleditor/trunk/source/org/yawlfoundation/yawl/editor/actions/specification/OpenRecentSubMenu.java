package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.swing.menu.YAWLMenuItem;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModelListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationFileModel;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Author: Michael Adams
 * Creation Date: 25/02/2009
 */
public class OpenRecentSubMenu extends JMenu implements SpecificationFileModelListener {

    private static Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
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
                "/org/yawlfoundation/yawl/editor/resources/menuicons/open_recent.png");
    }

    
    private void createMenuItems() {
        items = new YAWLMenuItem[8];
        for (int i = 0; i < 8; i++) {
            YAWLMenuItem item = new YAWLMenuItem(new OpenRecentSpecificationAction());
            item.setVisible(false);
            add(item);
            items[i] = item;
        }
    }


    private void loadMenuItems() {
        List<String> recentList = loadRecentFileList();
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


    private static List<String> loadRecentFileList() {
        List<String> recentFileList = new ArrayList<String>();
        String key = "openRecent" ;
        for (int i=0; i<8; i++) {
            String fileName = prefs.get(key + i, null);
            if (fileName == null) fileName="";
            recentFileList.add(fileName);
        }
        return recentFileList;
    }

    private void pushRecentFile(String fullFileName) {
      List<String> recentList = loadRecentFileList();
      int duplicatePos = recentList.indexOf(fullFileName);
      int start = (duplicatePos > -1) ? duplicatePos - 1 : 6 ;
      String key = "openRecent" ;
      for (int i=start; i>=0; i--) {
          String fileName = recentList.get(i);
          if ((fileName != null) && (fileName.length() > 0))  {
              prefs.put(key + (i+1), fileName);
          }
      }
      prefs.put(key + 0, fullFileName);
   }

    
   public void addRecentFile(String fullFileName) {
       pushRecentFile(fullFileName);
       loadMenuItems();
   }


    public void specificationFileModelStateChanged(int state) {
        switch(state) {
            case SpecificationFileModel.IDLE: {
                setEnabled(true);
                break;
            }
            case SpecificationFileModel.EDITING: {
                setEnabled(false);
                break;
            }
            case SpecificationFileModel.BUSY: {
                setEnabled(false);
                break;
            }
        }
    }

}
