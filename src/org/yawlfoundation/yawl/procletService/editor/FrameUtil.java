/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.procletService.editor;

import java.util.*;
import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class FrameUtil {

  private static final String BUTTON_OK = "ok";
  private static final String BUTTON_CANCEL = "cancel";
  private static final String BUTTON_YES = "yes";
  private static final String BUTTON_NO = "no";

  private static final String BUTTON_ADD = "add";
  private static final String BUTTON_EDIT = "edit";
  private static final String BUTTON_DELETE = "delete";

  /**
   * fillList
   *
   * @param anList List
   * @param anJList JList
   */
  public static void fillList(Collection anList, JList anJList) {
    DefaultListModel listModel = new DefaultListModel();
    if (anList != null && anJList != null) {
      Object el = null;
      Iterator it = anList.iterator();
      while (it.hasNext()) {
        el = it.next();
        listModel.addElement(el);
      }
    }
    anJList.setModel(listModel);
    if (listModel.size() > 0) {
      anJList.setSelectedIndex(0);
    }
  }

  public static void iniList(JList anJList) {
    anJList.setModel(new DefaultListModel());
  }

  public static void addToList(JList list, Object object) {
    ListModel model = list.getModel();
    if (model == null) {
      model = new DefaultListModel();
      list.setModel(model);
    }
    ;
    if (model instanceof DefaultListModel) {
      DefaultListModel listModel = (DefaultListModel) model;
      listModel.addElement(object);
    }
  }

  public static void removeFromList(JList list, Object object) {
    ListModel model = list.getModel();
    if (model == null) {
      model = new DefaultListModel();
      list.setModel(model);
    }
    ;
    if (model instanceof DefaultListModel) {
      DefaultListModel listModel = (DefaultListModel) model;
      listModel.removeElement(object);
    }
  }

  public static void removeFromList(JList list, int index) {
    ListModel model = list.getModel();
    if (model == null) {
      model = new DefaultListModel();
      list.setModel(model);
    }
    ;
    if (model instanceof DefaultListModel) {
      DefaultListModel listModel = (DefaultListModel) model;
      listModel.removeElementAt(index);
    }
  }

  /**
   * getSelecetdList
   *
   * @param anJList JList
   * @return Object
   */
  public static Object getSelecetdList(JList anJList) {
    return anJList.getSelectedValue();
  }

  /**
   * getSelecetdList
   *
   * @param anJList JList
   * @return Object
   */
  public static Object[] getSelecetdAllList(JList anJList) {
    return anJList.getSelectedValues();
  }

  public static int indexList(JList list, Object object) {
    ListModel model = list.getModel();
    if (model == null) {
      model = new DefaultListModel();
      list.setModel(model);
    }
    ;
    if (model instanceof DefaultListModel) {
      DefaultListModel listModel = (DefaultListModel) model;
      return listModel.indexOf(object);
    }
    return -1;
  }

  public static void readOnly(JTextComponent c, JComponent parent) {
    c.setEditable(false);
    c.setForeground(parent.getForeground());
    c.setBackground(parent.getBackground());
  }

  /**
   * getSelecetdList
   *
   * @param anJList JList
   * @return Object
   */
  public static Object getItemList(JList anJList, int index) {
    return anJList.getModel().getElementAt(index);
  }

  /**
   *
   * @param list JList
   * @param selected Object
   */
  public static void setSelectedList(JList list, Object selected) {
    if (list == null) {
      return;
    }
    if (list.getModel().getSize() == 0) {
      return;
    }
    if (selected == null) {
      list.setSelectedIndex(0);
    }
    else {
      list.setSelectedValue(selected, true);
    }
  }

  /**
   * fillList
   *
   * @param anJList JList
   * @return Object
   */
  protected static Object[] getSelectedMultipleList(JList anJList) {
    Object[] el = null;
    el = anJList.getSelectedValues();
    return el;
  }

  public static void addToComboBox(JComboBox list, Object object) {
    ComboBoxModel model = list.getModel();
    if (model == null) {
      model = new DefaultComboBoxModel();
      list.setModel(model);
    }
    ;
    if (model instanceof DefaultComboBoxModel) {
      DefaultComboBoxModel listModel = (DefaultComboBoxModel) model;
      listModel.addElement(object);
    }
  }

  public static void fillComboBox(JComboBox combo, List list) {
    ComboBoxModel model = combo.getModel();
    if (model == null) {
      model = new DefaultComboBoxModel();
      combo.setModel(model);
    }
    ;
    if (model instanceof DefaultComboBoxModel) {
      DefaultComboBoxModel listModel = (DefaultComboBoxModel) model;
      listModel.removeAllElements();
      for (int i = 0; i < list.size(); i++) {
        listModel.addElement(list.get(i));
      }
    }
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonOk(JButton anButton) {
    anButton.setText(BUTTON_OK);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonCancel(JButton anButton) {
    anButton.setText(BUTTON_CANCEL);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonYes(JButton anButton) {
    anButton.setText(BUTTON_YES);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonNo(JButton anButton) {
    anButton.setText(BUTTON_NO);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonAdd(JButton anButton) {
    anButton.setText(BUTTON_ADD);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonEdit(JButton anButton) {
    anButton.setText(BUTTON_EDIT);
  }

  /**
   * setButtonOk
   *
   * @param anButton JButton
   */
  public static void setButtonDelete(JButton anButton) {
    anButton.setText(BUTTON_DELETE);
  }

  public static void setSize(JComponent componenet, Dimension dimension) {
    componenet.setMinimumSize(dimension);
    componenet.setMaximumSize(dimension);
    componenet.setPreferredSize(dimension);
  }

}

