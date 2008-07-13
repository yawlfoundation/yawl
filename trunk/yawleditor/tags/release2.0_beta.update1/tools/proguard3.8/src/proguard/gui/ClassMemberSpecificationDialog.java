/* $Id: ClassMemberSpecificationDialog.java,v 1.5.2.2 2007/01/18 21:31:52 eric Exp $
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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.Border;

import proguard.*;
import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.util.*;

/**
 * This <code>JDialog</code> allows the user to enter a String.
 *
 * @author Eric Lafortune
 */
class ClassMemberSpecificationDialog extends JDialog
{
    /**
     * Return value if the dialog is canceled (with the Cancel button or by
     * closing the dialog window).
     */
    public static final int CANCEL_OPTION = 1;

    /**
     * Return value if the dialog is approved (with the Ok button).
     */
    public static final int APPROVE_OPTION = 0;


    private boolean isField;

    private JRadioButton[] publicRadioButtons;
    private JRadioButton[] privateRadioButtons;
    private JRadioButton[] protectedRadioButtons;
    private JRadioButton[] staticRadioButtons;
    private JRadioButton[] finalRadioButtons;

    private JRadioButton[] volatileRadioButtons;
    private JRadioButton[] transientRadioButtons;

    private JRadioButton[] synchronizedRadioButtons;
    private JRadioButton[] nativeRadioButtons;
    private JRadioButton[] abstractRadioButtons;
    private JRadioButton[] strictRadioButtons;

    private JTextField nameTextField      = new JTextField(20);
    private JTextField typeTextField      = new JTextField(20);
    private JTextField argumentsTextField = new JTextField(20);
    private int        returnValue;


    public ClassMemberSpecificationDialog(JDialog owner, boolean isField)
    {
        super(owner, true);
        setResizable(true);

        // Create some constraints that can be reused.
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 2, 1, 2);

        GridBagConstraints constraintsStretch = new GridBagConstraints();
        constraintsStretch.fill    = GridBagConstraints.HORIZONTAL;
        constraintsStretch.weightx = 1.0;
        constraintsStretch.anchor  = GridBagConstraints.WEST;
        constraintsStretch.insets  = constraints.insets;

        GridBagConstraints constraintsLast = new GridBagConstraints();
        constraintsLast.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLast.anchor    = GridBagConstraints.WEST;
        constraintsLast.insets    = constraints.insets;

        GridBagConstraints constraintsLastStretch = new GridBagConstraints();
        constraintsLastStretch.gridwidth = GridBagConstraints.REMAINDER;
        constraintsLastStretch.fill      = GridBagConstraints.HORIZONTAL;
        constraintsLastStretch.weightx   = 1.0;
        constraintsLastStretch.anchor    = GridBagConstraints.WEST;
        constraintsLastStretch.insets    = constraints.insets;

        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelConstraints.fill      = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx   = 1.0;
        panelConstraints.weighty   = 0.0;
        panelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        panelConstraints.insets    = constraints.insets;

        GridBagConstraints stretchPanelConstraints = new GridBagConstraints();
        stretchPanelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        stretchPanelConstraints.fill      = GridBagConstraints.BOTH;
        stretchPanelConstraints.weightx   = 1.0;
        stretchPanelConstraints.weighty   = 1.0;
        stretchPanelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        stretchPanelConstraints.insets    = constraints.insets;

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.CENTER;
        labelConstraints.insets = new Insets(2, 10, 2, 10);

        GridBagConstraints lastLabelConstraints = new GridBagConstraints();
        lastLabelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        lastLabelConstraints.anchor    = GridBagConstraints.CENTER;
        lastLabelConstraints.insets    = labelConstraints.insets;

        GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.weightx = 1.0;
        okButtonConstraints.weighty = 1.0;
        okButtonConstraints.anchor  = GridBagConstraints.SOUTHEAST;
        okButtonConstraints.insets  = new Insets(4, 4, 8, 4);

        GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
        cancelButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;
        cancelButtonConstraints.weighty   = 1.0;
        cancelButtonConstraints.anchor    = GridBagConstraints.SOUTHEAST;
        cancelButtonConstraints.insets    = okButtonConstraints.insets;

        GridBagLayout layout = new GridBagLayout();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

        this.isField = isField;

        // Create the access panel.
        JPanel accessPanel = new JPanel(layout);
        accessPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                               GUIResources.getMessage("access")));

        accessPanel.add(Box.createGlue(),         labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("required")), labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("not")),      labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("dontCare")), labelConstraints);
        accessPanel.add(Box.createGlue(),         constraintsLastStretch);

        publicRadioButtons           = addRadioButtonTriplet("Public",       accessPanel);
        privateRadioButtons          = addRadioButtonTriplet("Private",      accessPanel);
        protectedRadioButtons        = addRadioButtonTriplet("Protected",    accessPanel);
        staticRadioButtons           = addRadioButtonTriplet("Static",       accessPanel);
        finalRadioButtons            = addRadioButtonTriplet("Final",        accessPanel);

        if (isField)
        {
            volatileRadioButtons     = addRadioButtonTriplet("Volatile",     accessPanel);
            transientRadioButtons    = addRadioButtonTriplet("Transient",    accessPanel);
        }
        else
        {
            synchronizedRadioButtons = addRadioButtonTriplet("Synchronized", accessPanel);
            nativeRadioButtons       = addRadioButtonTriplet("Native",       accessPanel);
            abstractRadioButtons     = addRadioButtonTriplet("Abstract",     accessPanel);
            strictRadioButtons       = addRadioButtonTriplet("Strict",       accessPanel);
        }

        // Create the type panel.
        JPanel typePanel = new JPanel(layout);
        typePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                             GUIResources.getMessage(isField ?
                                                                                         "type" :
                                                                                         "returnType")));

        typePanel.add(typeTextField, constraintsLastStretch);

        // Create the name panel.
        JPanel namePanel = new JPanel(layout);
        namePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                             GUIResources.getMessage("name")));

        namePanel.add(nameTextField, constraintsLastStretch);

        // Create the arguments panel.
        JPanel argumentsPanel = new JPanel(layout);
        argumentsPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                  GUIResources.getMessage("arguments")));

        argumentsPanel.add(argumentsTextField, constraintsLastStretch);

        // Create the Ok button.
        JButton okButton = new JButton(GUIResources.getMessage("ok"));
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                returnValue = APPROVE_OPTION;
                hide();
            }
        });

        // Create the Cancel button.
        JButton cancelButton = new JButton(GUIResources.getMessage("cancel"));
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                hide();
            }
        });

        // Add all panels to the main panel.
        JPanel mainPanel = new JPanel(layout);
        mainPanel.add(accessPanel, panelConstraints);
        mainPanel.add(typePanel,   panelConstraints);
        mainPanel.add(namePanel,   panelConstraints);

        if (!isField)
        {
            mainPanel.add(argumentsPanel, panelConstraints);
        }

        mainPanel.add(okButton,     okButtonConstraints);
        mainPanel.add(cancelButton, cancelButtonConstraints);

        getContentPane().add(mainPanel);
    }


    /**
     * Adds a JLabel and three JRadioButton instances in a ButtonGroup to the
     * given panel with a GridBagLayout, and returns the buttons in an array.
     */
    private JRadioButton[] addRadioButtonTriplet(String labelText,
                                                 JPanel panel)
    {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 10, 2, 10);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.insets = labelConstraints.insets;

        GridBagConstraints lastGlueConstraints = new GridBagConstraints();
        lastGlueConstraints.gridwidth = GridBagConstraints.REMAINDER;
        lastGlueConstraints.weightx   = 1.0;

        // Create the radio buttons.
        JRadioButton radioButton0 = new JRadioButton();
        JRadioButton radioButton1 = new JRadioButton();
        JRadioButton radioButton2 = new JRadioButton();

        // Put them in a button group.
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButton0);
        buttonGroup.add(radioButton1);
        buttonGroup.add(radioButton2);

        // Add the label and the buttons to the panel.
        panel.add(new JLabel(labelText), labelConstraints);
        panel.add(radioButton0,          buttonConstraints);
        panel.add(radioButton1,          buttonConstraints);
        panel.add(radioButton2,          buttonConstraints);
        panel.add(Box.createGlue(),      lastGlueConstraints);

        return new JRadioButton[]
        {
             radioButton0,
             radioButton1,
             radioButton2
        };
    }


    /**
     * Sets the ClassMemberSpecification to be represented in this dialog.
     */
    public void setClassMemberSpecification(ClassMemberSpecification classMemberSpecification)
    {
        String name       = classMemberSpecification.name;
        String descriptor = classMemberSpecification.descriptor;

        // Set the access radio buttons.
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,       publicRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PRIVATE,      privateRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PROTECTED,    protectedRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_STATIC,       staticRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_FINAL,        finalRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_VOLATILE,     volatileRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_TRANSIENT,    transientRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_SYNCHRONIZED, synchronizedRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_NATIVE,       nativeRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,     abstractRadioButtons);
        setClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_STRICT,       strictRadioButtons);

        // Set the class name text fields.
        nameTextField.setText(name == null ? "" : name);

        if (isField)
        {
            typeTextField     .setText(descriptor == null ? "" : ClassUtil.externalType(descriptor));
        }
        else
        {
            typeTextField     .setText(descriptor == null ? "" : ClassUtil.externalMethodReturnType(descriptor));
            argumentsTextField.setText(descriptor == null ? "" : ClassUtil.externalMethodArguments(descriptor));
        }
    }


    /**
     * Returns the ClassMemberSpecification currently represented in this dialog.
     */
    public ClassMemberSpecification getClassMemberSpecification()
    {
        String  name      = nameTextField.getText();
        String  type      = typeTextField.getText();
        String  arguments = argumentsTextField.getText();

        if (name.equals("") ||
            name.equals("*"))
        {
            name = null;
        }

        if (type.equals("") ||
            type.equals("*"))
        {
            type = null;
        }

        if (name != null ||
            type != null)
        {
            if (isField)
            {
                if (type == null)
                {
                    type = ClassConstants.EXTERNAL_TYPE_INT;
                }

                type = ClassUtil.internalType(type);
            }
            else
            {
                if (type == null)
                {
                    type = ClassConstants.EXTERNAL_TYPE_VOID;
                }

                type = ClassUtil.internalMethodDescriptor(type, ListUtil.commaSeparatedList(arguments));
            }
        }

        ClassMemberSpecification classMemberSpecification =
            new ClassMemberSpecification(0, 0, name, type);

        // Also get the access radio button settings.
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,       publicRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PRIVATE,      privateRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_PROTECTED,    protectedRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_STATIC,       staticRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_FINAL,        finalRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_VOLATILE,     volatileRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_TRANSIENT,    transientRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_SYNCHRONIZED, synchronizedRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_NATIVE,       nativeRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,     abstractRadioButtons);
        getClassMemberSpecificationRadioButtons(classMemberSpecification, ClassConstants.INTERNAL_ACC_STRICT,       strictRadioButtons);

        return classMemberSpecification;
    }


    /**
     * Shows this dialog. This method only returns when the dialog is closed.
     *
     * @return <code>CANCEL_OPTION</code> or <code>APPROVE_OPTION</code>,
     *         depending on the choice of the user.
     */
    public int showDialog()
    {
        returnValue = CANCEL_OPTION;

        // Open the dialog in the right place, then wait for it to be closed,
        // one way or another.
        pack();
        setLocationRelativeTo(getOwner());
        show();

        return returnValue;
    }


    /**
     * Sets the appropriate radio button of a given triplet, based on the access
     * flags of the given keep option.
     */
    private void setClassMemberSpecificationRadioButtons(ClassMemberSpecification classMemberSpecification,
                                                         int                   flag,
                                                         JRadioButton[]        radioButtons)
    {
        if (radioButtons != null)
        {
            int index = (classMemberSpecification.requiredSetAccessFlags   & flag) != 0 ? 0 :
                        (classMemberSpecification.requiredUnsetAccessFlags & flag) != 0 ? 1 :
                                                                                       2;
            radioButtons[index].setSelected(true);
        }
    }


    /**
     * Updates the access flag of the given keep option, based on the given radio
     * button triplet.
     */
    private void getClassMemberSpecificationRadioButtons(ClassMemberSpecification classMemberSpecification,
                                                         int                   flag,
                                                         JRadioButton[]        radioButtons)
    {
        if (radioButtons != null)
        {
            if      (radioButtons[0].isSelected())
            {
                classMemberSpecification.requiredSetAccessFlags   |= flag;
            }
            else if (radioButtons[1].isSelected())
            {
                classMemberSpecification.requiredUnsetAccessFlags |= flag;
            }
        }
    }
}
