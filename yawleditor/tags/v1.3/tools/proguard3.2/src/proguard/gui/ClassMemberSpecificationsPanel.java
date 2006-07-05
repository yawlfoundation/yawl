/* $Id: ClassMemberSpecificationsPanel.java,v 1.5 2004/08/28 22:50:49 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.gui;

import proguard.*;
import proguard.classfile.util.ClassUtil;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;


/**
 * This <code>ListPanel</code> allows the user to add, edit, move, and remove
 * ClassMemberSpecification entries in a list.
 *
 * @author Eric Lafortune
 */
class ClassMemberSpecificationsPanel extends ListPanel
{
    private ClassMemberSpecificationDialog fieldSpecificationDialog;
    private ClassMemberSpecificationDialog methodSpecificationDialog;


    public ClassMemberSpecificationsPanel(JDialog owner, boolean fullKeepOptions)
    {
        super();

        super.firstSelectionButton = fullKeepOptions ? 3 : 2;

        list.setCellRenderer(new MyListCellRenderer());

        fieldSpecificationDialog  = new ClassMemberSpecificationDialog(owner, true);
        methodSpecificationDialog = new ClassMemberSpecificationDialog(owner, false);

        if (fullKeepOptions)
        {
            addAddFieldButton();
        }
        addAddMethodButton();
        addEditButton();
        addRemoveButton();
        addUpButton();
        addDownButton();

        enableSelectionButtons();
    }


    protected void addAddFieldButton()
    {
        JButton addFieldButton = new JButton(GUIResources.getMessage("addField"));
        addFieldButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fieldSpecificationDialog.setClassMemberSpecification(new ClassMemberSpecification());
                int returnValue = fieldSpecificationDialog.showDialog();
                if (returnValue == ClassMemberSpecificationDialog.APPROVE_OPTION)
                {
                    // Add the new element.
                    addElement(new MyClassMemberSpecificationWrapper(fieldSpecificationDialog.getClassMemberSpecification(),
                                                                  true));
                }
            }
        });

        addButton(addFieldButton);
    }


    protected void addAddMethodButton()
    {
        JButton addMethodButton = new JButton(GUIResources.getMessage("addMethod"));
        addMethodButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                methodSpecificationDialog.setClassMemberSpecification(new ClassMemberSpecification());
                int returnValue = methodSpecificationDialog.showDialog();
                if (returnValue == ClassMemberSpecificationDialog.APPROVE_OPTION)
                {
                    // Add the new element.
                    addElement(new MyClassMemberSpecificationWrapper(methodSpecificationDialog.getClassMemberSpecification(),
                                                                  false));
                }
            }
        });

        addButton(addMethodButton);
    }


    protected void addEditButton()
    {
        JButton editButton = new JButton(GUIResources.getMessage("edit"));
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                MyClassMemberSpecificationWrapper wrapper =
                    (MyClassMemberSpecificationWrapper)list.getSelectedValue();

                ClassMemberSpecificationDialog classMemberSpecificationDialog =
                    wrapper.isField ?
                        fieldSpecificationDialog :
                        methodSpecificationDialog;

                classMemberSpecificationDialog.setClassMemberSpecification(wrapper.classMemberSpecification);
                int returnValue = classMemberSpecificationDialog.showDialog();
                if (returnValue == ClassMemberSpecificationDialog.APPROVE_OPTION)
                {
                    // Replace the old element.
                    wrapper.classMemberSpecification = classMemberSpecificationDialog.getClassMemberSpecification();
                    setElementAt(wrapper,
                                 list.getSelectedIndex());
                }
            }
        });

        addButton(editButton);
    }


    /**
     * Sets the ClassMemberSpecification instances to be represented in this panel.
     */
    public void setClassMemberSpecifications(List fieldSpecifications,
                                             List methodSpecifications)
    {
        listModel.clear();

        if (fieldSpecifications != null)
        {
            for (int index = 0; index < fieldSpecifications.size(); index++)
            {
                listModel.addElement(
                    new MyClassMemberSpecificationWrapper((ClassMemberSpecification)fieldSpecifications.get(index),
                                                          true));
            }
        }

        if (methodSpecifications != null)
        {
            for (int index = 0; index < methodSpecifications.size(); index++)
            {
                listModel.addElement(
                    new MyClassMemberSpecificationWrapper((ClassMemberSpecification)methodSpecifications.get(index),
                                                          false));
            }
        }

        // Make sure the selection buttons are properly enabled,
        // since the clear method doesn't seem to notify the listener.
        enableSelectionButtons();
    }


    /**
     * Returns the ClassMemberSpecification instances currently represented in
     * this panel, referring to fields or to methods.
     *
     * @param isField specifies whether specifications referring to fields or
     *                specifications referring to methods should be returned.
     */
    public List getClassMemberSpecifications(boolean isField)
    {
        int size = listModel.size();
        if (size == 0)
        {
            return null;
        }

        List classMemberSpecifcations = new ArrayList(size);
        for (int index = 0; index < size; index++)
        {
            MyClassMemberSpecificationWrapper wrapper =
                (MyClassMemberSpecificationWrapper)listModel.get(index);

            if (wrapper.isField == isField)
            {
                classMemberSpecifcations.add(wrapper.classMemberSpecification);
            }
        }

        return classMemberSpecifcations;
    }


    /**
     * This ListCellRenderer renders ClassMemberSpecification objects.
     */
    private static class MyListCellRenderer implements ListCellRenderer
    {
        JLabel label = new JLabel();


        // Implementations for ListCellRenderer.

        public Component getListCellRendererComponent(JList   list,
                                                      Object  value,
                                                      int     index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            MyClassMemberSpecificationWrapper wrapper = (MyClassMemberSpecificationWrapper)value;

            ClassMemberSpecification option = wrapper.classMemberSpecification;
            String name = option.name;
            label.setText(wrapper.isField ?
                (name == null ? "<fields>"  : ClassUtil.externalFullFieldDescription(0, option.name, option.descriptor)) :
                (name == null ? "<methods>" : ClassUtil.externalFullMethodDescription("<init>", 0, option.name, option.descriptor)));

            if (isSelected)
            {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            else
            {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            label.setOpaque(true);

            return label;
        }
    }


    /**
     * This class wraps a ClassMemberSpecification, additionally storing whether
     * the option refers to a field or to a method.
     */
    private static class MyClassMemberSpecificationWrapper
    {
        public ClassMemberSpecification classMemberSpecification;
        public boolean                  isField;

        public MyClassMemberSpecificationWrapper(ClassMemberSpecification classMemberSpecification,
                                              boolean               isField)
        {
            this.classMemberSpecification = classMemberSpecification;
            this.isField                  = isField;
        }
    }
}
