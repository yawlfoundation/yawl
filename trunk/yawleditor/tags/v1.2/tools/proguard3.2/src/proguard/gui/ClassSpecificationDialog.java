/* $Id: ClassSpecificationDialog.java,v 1.2 2004/08/15 12:39:30 eric Exp $
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.Border;

import java.util.List;

import proguard.ClassSpecification;
import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;

/**
 * This <code>JDialog</code> allows the user to enter a String.
 *
 * @author Eric Lafortune
 */
class ClassSpecificationDialog extends JDialog
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


    private JTextArea commentsTextArea = new JTextArea(4, 20);

    private JRadioButton keepClassesAndMembersRadioButton  = new JRadioButton(GUIResources.getMessage("keep"));
    private JRadioButton keepClassMembersRadioButton       = new JRadioButton(GUIResources.getMessage("keepClassMembers"));
    private JRadioButton keepClassesWithMembersRadioButton = new JRadioButton(GUIResources.getMessage("keepClassesWithMembers"));

    private JRadioButton[] publicRadioButtons;
    private JRadioButton[] finalRadioButtons;
    private JRadioButton[] interfaceRadioButtons;
    private JRadioButton[] abstractRadioButtons;

    private JTextField classNameTextField        = new JTextField(20);
    private JTextField extendsClassNameTextField = new JTextField(20);

    private ClassMemberSpecificationsPanel classMembersPanel;

    private int returnValue;


    public ClassSpecificationDialog(JFrame owner, boolean fullKeepOptions)
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
        cancelButtonConstraints.gridwidth = GridBagConstraints.REMAINDER;;
        cancelButtonConstraints.weighty   = 1.0;
        cancelButtonConstraints.anchor    = GridBagConstraints.SOUTHEAST;
        cancelButtonConstraints.insets    = okButtonConstraints.insets;

        GridBagLayout layout = new GridBagLayout();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

        // Create the comments panel.
        JPanel commentsPanel = new JPanel(layout);
        commentsPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                 GUIResources.getMessage("comments")));

        JScrollPane commentsScrollPane = new JScrollPane(commentsTextArea);
        commentsScrollPane.setBorder(classNameTextField.getBorder());

        commentsPanel.add(commentsScrollPane, constraintsLastStretch);

        // Create the keep option panel.
        ButtonGroup keepButtonGroup = new ButtonGroup();
        keepButtonGroup.add(keepClassesAndMembersRadioButton);
        keepButtonGroup.add(keepClassMembersRadioButton);
        keepButtonGroup.add(keepClassesWithMembersRadioButton);

        JPanel keepOptionPanel = new JPanel(layout);
        keepOptionPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                   GUIResources.getMessage("keepTitle")));

        keepOptionPanel.add(keepClassesAndMembersRadioButton,  constraintsLastStretch);
        keepOptionPanel.add(keepClassMembersRadioButton,       constraintsLastStretch);
        keepOptionPanel.add(keepClassesWithMembersRadioButton, constraintsLastStretch);

        // Create the access panel.
        JPanel accessPanel = new JPanel(layout);
        accessPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                               GUIResources.getMessage("access")));

        accessPanel.add(Box.createGlue(),         labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("required")),   labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("not")),        labelConstraints);
        accessPanel.add(new JLabel(GUIResources.getMessage("dontCare")), labelConstraints);
        accessPanel.add(Box.createGlue(),         constraintsLastStretch);

        publicRadioButtons    = addRadioButtonTriplet("Public",    accessPanel);
        finalRadioButtons     = addRadioButtonTriplet("Final",     accessPanel);
        interfaceRadioButtons = addRadioButtonTriplet("Interface", accessPanel);
        abstractRadioButtons  = addRadioButtonTriplet("Abstract",  accessPanel);

        // Create the class name panel.
        JPanel classNamePanel = new JPanel(layout);
        classNamePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                  GUIResources.getMessage("class")));

        classNamePanel.add(classNameTextField, constraintsLastStretch);

        // Create the extends class name panel.
        JPanel extendsClassNamePanel = new JPanel(layout);
        extendsClassNamePanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                         GUIResources.getMessage("extendsImplementsClass")));

        extendsClassNamePanel.add(extendsClassNameTextField, constraintsLastStretch);


        // Create the class member list panel.
        classMembersPanel = new ClassMemberSpecificationsPanel(this, fullKeepOptions);
        classMembersPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                                     GUIResources.getMessage("classMembers")));

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
        mainPanel.add(commentsPanel,         panelConstraints);
        if (fullKeepOptions)
        {
            mainPanel.add(keepOptionPanel,       panelConstraints);
        }
        mainPanel.add(accessPanel,           panelConstraints);
        mainPanel.add(classNamePanel,        panelConstraints);
        mainPanel.add(extendsClassNamePanel, panelConstraints);
        mainPanel.add(classMembersPanel,     stretchPanelConstraints);

        mainPanel.add(okButton,              okButtonConstraints);
        mainPanel.add(cancelButton,          cancelButtonConstraints);

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
     * Sets the ClassSpecification to be represented in this dialog.
     */
    public void setClassSpecification(ClassSpecification classSpecification)
    {
        String  className         = classSpecification.className;
        String  extendsClassName  = classSpecification.extendsClassName;
        boolean markClassFiles    = classSpecification.markClassFiles;
        boolean markConditionally = classSpecification.markConditionally;
        String  comments          = classSpecification.comments;
        List    keepFieldOptions  = classSpecification.fieldSpecifications;
        List    keepMethodOptions = classSpecification.methodSpecifications;

        // Set the comments text area.
        commentsTextArea.setText(comments == null ? "" : comments);

        // Figure out the proper keep radio button and set it.
        JRadioButton keepOptionRadioButton =
            markConditionally ? keepClassesWithMembersRadioButton :
            markClassFiles    ? keepClassesAndMembersRadioButton  :
                                keepClassMembersRadioButton;

        keepOptionRadioButton.setSelected(true);

        // Set the access radio buttons.
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,    publicRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_FINAL,     finalRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_INTERFACE, interfaceRadioButtons);
        setClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,  abstractRadioButtons);

        // Set the class name text fields.
        classNameTextField       .setText(className        == null ? "*" : ClassUtil.externalClassName(className));
        extendsClassNameTextField.setText(extendsClassName == null ? ""  : ClassUtil.externalClassName(extendsClassName));

        // Set the keep class member option list.
        classMembersPanel.setClassMemberSpecifications(keepFieldOptions, keepMethodOptions);
    }


    /**
     * Returns the ClassSpecification currently represented in this dialog.
     */
    public ClassSpecification getClassSpecification()
    {
        String  comments          = commentsTextArea.getText();
        String  className         = classNameTextField.getText();
        String  extendsClassName  = extendsClassNameTextField.getText();
        boolean markClassFiles    = !keepClassMembersRadioButton.isSelected();
        boolean markConditionally = keepClassesWithMembersRadioButton.isSelected();

        ClassSpecification classSpecification =
            new ClassSpecification(0,
                                    0,
                                    className.equals("") ||
                                    className.equals("*")       ? null : ClassUtil.internalClassName(className),
                                    extendsClassName.equals("") ? null : ClassUtil.internalClassName(extendsClassName),
                                    markClassFiles,
                                    markConditionally,
                                    comments.equals("")         ? null : comments);

        // Also get the access radio button settings.
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_PUBLIC,    publicRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_FINAL,     finalRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_INTERFACE, interfaceRadioButtons);
        getClassSpecificationRadioButtons(classSpecification, ClassConstants.INTERNAL_ACC_ABSTRACT,  abstractRadioButtons);

        // Get the keep class member option lists.
        classSpecification.fieldSpecifications  = classMembersPanel.getClassMemberSpecifications(true);
        classSpecification.methodSpecifications = classMembersPanel.getClassMemberSpecifications(false);

        return classSpecification;
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
    private void setClassSpecificationRadioButtons(ClassSpecification classSpecification,
                                                    int                 flag,
                                                    JRadioButton[]      radioButtons)
    {
        int index = (classSpecification.requiredSetAccessFlags   & flag) != 0 ? 0 :
                    (classSpecification.requiredUnsetAccessFlags & flag) != 0 ? 1 :
                                                                                 2;
        radioButtons[index].setSelected(true);
    }


    /**
     * Updates the access flag of the given keep option, based on the given radio
     * button triplet.
     */
    private void getClassSpecificationRadioButtons(ClassSpecification classSpecification,
                                                    int                 flag,
                                                    JRadioButton[]      radioButtons)
    {
        if      (radioButtons[0].isSelected())
        {
            classSpecification.requiredSetAccessFlags   |= flag;
        }
        else if (radioButtons[1].isSelected())
        {
            classSpecification.requiredUnsetAccessFlags |= flag;
        }
    }
}
