/* $Id: ListPanel.java,v 1.9.2.2 2007/01/18 21:31:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * This <code>Jpanel</code> allows the user to move and remove entries in a
 * list and between lists. Extensions of this class should add buttons to add
 * and possibly edit entries, and to set and get the resulting list.
 *
 * @author Eric Lafortune
 */
abstract class ListPanel extends JPanel
{
    protected DefaultListModel listModel = new DefaultListModel();
    protected JList            list      = new JList(listModel);

    protected int firstSelectionButton = 2;


    protected ListPanel()
    {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.gridheight = GridBagConstraints.REMAINDER;
        listConstraints.fill       = GridBagConstraints.BOTH;
        listConstraints.weightx    = 1.0;
        listConstraints.weighty    = 1.0;
        listConstraints.anchor     = GridBagConstraints.NORTHWEST;
        listConstraints.insets     = new Insets(0, 2, 0, 2);

        // Make sure some buttons are disabled or enabled depending on whether
        // the selection is empty or not.
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                enableSelectionButtons();
            }
        });

        add(new JScrollPane(list), listConstraints);

        // something like the following calls are up to the extending class:
        //addAddButton();
        //addEditButton();
        //addRemoveButton();
        //addUpButton();
        //addDownButton();
        //
        //enableSelectionButtons();
    }


    protected void addRemoveButton()
    {
        JButton removeButton = new JButton(GUIResources.getMessage("remove"));
        removeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Remove the selected elements.
                removeElementsAt(list.getSelectedIndices());
            }
        });

        addButton(removeButton);
    }


    protected void addUpButton()
    {
        JButton upButton = new JButton(GUIResources.getMessage("moveUp"));
        upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices.length > 0 &&
                    selectedIndices[0] > 0)
                {
                    // Move the selected elements up.
                    moveElementsAt(selectedIndices, -1);
                }
            }
        });

        addButton(upButton);
    }


    protected void addDownButton()
    {
        JButton downButton = new JButton(GUIResources.getMessage("moveDown"));
        downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[] selectedIndices = list.getSelectedIndices();
                if (selectedIndices.length > 0 &&
                    selectedIndices[selectedIndices.length-1] < listModel.getSize()-1)
                {
                    // Move the selected elements down.
                    moveElementsAt(selectedIndices, 1);
                }
            }
        });

        addButton(downButton);
    }


    /**
     * Adds a button that allows to copy or move entries to another ListPanel.
     *
     * @param buttonText the button text.
     * @param panel      the other ListPanel.
     */
    public void addCopyToPanelButton(String          buttonText,
                                     final ListPanel panel)
    {
        JButton moveButton = new JButton(buttonText);
        moveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int[]    selectedIndices  = list.getSelectedIndices();
                Object[] selectedElements = list.getSelectedValues();

                // Remove the selected elements from this panel.
                removeElementsAt(selectedIndices);

                // Add the elements to the other panel.
                panel.addElements(selectedElements);
            }
        });

        addButton(moveButton);
    }


    protected void addButton(JButton button)
    {
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        buttonConstraints.fill      = GridBagConstraints.HORIZONTAL;
        buttonConstraints.anchor    = GridBagConstraints.NORTHWEST;
        buttonConstraints.insets    = new Insets(0, 2, 0, 2);

        add(button, buttonConstraints);
    }


    /**
     * Returns a list of all right-hand side buttons.
     */
    public List getButtons()
    {
        List list = new ArrayList(getComponentCount()-1);

        // Add all buttons.
        for (int index = 1; index < getComponentCount(); index++)
        {
            list.add(getComponent(index));
        }

        return list;
    }


    protected void addElement(Object element)
    {
        listModel.addElement(element);

        // Make sure it is selected.
        list.setSelectedIndex(listModel.size() - 1);
    }


    protected void addElements(Object[] elements)
    {
        // Add the elements one by one.
        for (int index = 0; index < elements.length; index++)
        {
            listModel.addElement(elements[index]);
        }

        // Make sure they are selected.
        int[] selectedIndices = new int[elements.length];
        for (int index = 0; index < selectedIndices.length; index++)
        {
            selectedIndices[index] =
                listModel.size() - selectedIndices.length + index;
        }
        list.setSelectedIndices(selectedIndices);
    }


    protected void moveElementsAt(int[] indices, int offset)
    {
        // Remember the selected elements.
        Object[] selectedElements = list.getSelectedValues();

        // Remove the selected elements.
        removeElementsAt(indices);

        // Update the element indices.
        for (int index = 0; index < indices.length; index++)
        {
            indices[index] += offset;
        }

        // Reinsert the selected elements.
        insertElementsAt(selectedElements, indices);
    }


    protected void insertElementsAt(Object[] elements, int[] indices)
    {
        for (int index = 0; index < elements.length; index++)
        {
            listModel.insertElementAt(elements[index], indices[index]);
        }

        // Make sure they are selected.
        list.setSelectedIndices(indices);
    }


    protected void setElementAt(Object element, int index)
    {
        listModel.setElementAt(element, index);

        // Make sure it is selected.
        list.setSelectedIndex(index);
    }


    protected void setElementsAt(Object[] elements, int[] indices)
    {
        for (int index = 0; index < elements.length; index++)
        {
            listModel.setElementAt(elements[index], indices[index]);
        }

        // Make sure they are selected.
        list.setSelectedIndices(indices);
    }


    protected void removeElementsAt(int[] indices)
    {
        for (int index = indices.length - 1; index >= 0; index--)
        {
            listModel.removeElementAt(indices[index]);
        }

        // Make sure nothing is selected.
        list.clearSelection();

        // Make sure the selection buttons are properly enabled,
        // since the above method doesn't seem to notify the listener.
        enableSelectionButtons();
    }


    protected void removeAllElements()
    {
        listModel.removeAllElements();

        // Make sure the selection buttons are properly enabled,
        // since the above method doesn't seem to notify the listener.
        enableSelectionButtons();
    }


    /**
     * Enables or disables the buttons that depend on a selection.
     */
    protected void enableSelectionButtons()
    {
        boolean selected = !list.isSelectionEmpty();

        // Loop over all components, except the list itself and the Add button.
        for (int index = firstSelectionButton; index < getComponentCount(); index++)
        {
            getComponent(index).setEnabled(selected);
        }
    }
}
