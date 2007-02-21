/* $Id: FilterDialog.java,v 1.4.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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

/**
 * This <code>JDialog</code> allows the user to enter a String.
 *
 * @author Eric Lafortune
 */
public class FilterDialog extends JDialog
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

    private static final String DEFAULT_FILTER     = "**";
    private static final String DEFAULT_JAR_FILTER = "**.jar";
    private static final String DEFAULT_WAR_FILTER = "**.war";
    private static final String DEFAULT_EAR_FILTER = "**.ear";
    private static final String DEFAULT_ZIP_FILTER = "**.zip";


    private JTextField filterTextField    = new JTextField(40);
    private JTextField jarFilterTextField = new JTextField(40);
    private JTextField warFilterTextField = new JTextField(40);
    private JTextField earFilterTextField = new JTextField(40);
    private JTextField zipFilterTextField = new JTextField(40);
    private int        returnValue;


    public FilterDialog(JFrame owner,
                        String explanation)
    {
        super(owner, true);
        setResizable(true);

        // Create some constraints that can be reused.
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.gridwidth = GridBagConstraints.REMAINDER;
        textConstraints.fill      = GridBagConstraints.HORIZONTAL;
        textConstraints.weightx   = 1.0;
        textConstraints.weighty   = 1.0;
        textConstraints.anchor    = GridBagConstraints.NORTHWEST;
        textConstraints.insets    = new Insets(10, 10, 10, 10);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(1, 2, 1, 2);

        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        textFieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
        textFieldConstraints.fill      = GridBagConstraints.HORIZONTAL;
        textFieldConstraints.weightx   = 1.0;
        textFieldConstraints.anchor    = GridBagConstraints.WEST;
        textFieldConstraints.insets    = labelConstraints.insets;

        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
        panelConstraints.fill      = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx   = 1.0;
        panelConstraints.weighty   = 0.0;
        panelConstraints.anchor    = GridBagConstraints.NORTHWEST;
        panelConstraints.insets    = labelConstraints.insets;

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

        // Create the panel with the explanation.
        JTextArea explanationTextArea = new JTextArea(explanation, 3, 0);
        explanationTextArea.setOpaque(false);
        explanationTextArea.setEditable(false);
        explanationTextArea.setLineWrap(true);
        explanationTextArea.setWrapStyleWord(true);

        // Create the filter labels.
        JLabel filterLabel    = new JLabel(GUIResources.getMessage("nameFilter"));
        JLabel jarFilterLabel = new JLabel(GUIResources.getMessage("jarNameFilter"));
        JLabel warFilterLabel = new JLabel(GUIResources.getMessage("warNameFilter"));
        JLabel earFilterLabel = new JLabel(GUIResources.getMessage("earNameFilter"));
        JLabel zipFilterLabel = new JLabel(GUIResources.getMessage("zipNameFilter"));

        // Create the filter panel.
        JPanel filterPanel = new JPanel(layout);
        filterPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder,
                                                               GUIResources.getMessage("filters")));

        filterPanel.add(explanationTextArea, textConstraints);

        filterPanel.add(filterLabel,         labelConstraints);
        filterPanel.add(filterTextField,     textFieldConstraints);

        filterPanel.add(jarFilterLabel,      labelConstraints);
        filterPanel.add(jarFilterTextField,  textFieldConstraints);

        filterPanel.add(warFilterLabel,      labelConstraints);
        filterPanel.add(warFilterTextField,  textFieldConstraints);

        filterPanel.add(earFilterLabel,      labelConstraints);
        filterPanel.add(earFilterTextField,  textFieldConstraints);

        filterPanel.add(zipFilterLabel,      labelConstraints);
        filterPanel.add(zipFilterTextField,  textFieldConstraints);


        JButton okButton = new JButton(GUIResources.getMessage("ok"));
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                returnValue = APPROVE_OPTION;
                hide();
            }
        });

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
        mainPanel.add(filterPanel,  panelConstraints);
        mainPanel.add(okButton,     okButtonConstraints);
        mainPanel.add(cancelButton, cancelButtonConstraints);

        getContentPane().add(mainPanel);
    }


    /**
     * Sets the filter to be represented in this dialog.
     */
    public void setFilter(String filter)
    {
        filterTextField.setText(filter != null ? filter : DEFAULT_FILTER);
    }


    /**
     * Returns the filter currently represented in this dialog.
     */
    public String getFilter()
    {
        String filter = filterTextField.getText();

        return filter.equals(DEFAULT_FILTER) ? null : filter;
    }


    /**
     * Sets the jar filter to be represented in this dialog.
     */
    public void setJarFilter(String filter)
    {
        jarFilterTextField.setText(filter != null ? filter : DEFAULT_JAR_FILTER);
    }


    /**
     * Returns the jar filter currently represented in this dialog.
     */
    public String getJarFilter()
    {
        String filter = jarFilterTextField.getText();

        return filter.equals(DEFAULT_JAR_FILTER) ? null : filter;
    }


    /**
     * Sets the war filter to be represented in this dialog.
     */
    public void setWarFilter(String filter)
    {
        warFilterTextField.setText(filter != null ? filter : DEFAULT_WAR_FILTER);
    }


    /**
     * Returns the war filter currently represented in this dialog.
     */
    public String getWarFilter()
    {
        String filter = warFilterTextField.getText();

        return filter.equals(DEFAULT_WAR_FILTER) ? null : filter;
    }


    /**
     * Sets the ear filter to be represented in this dialog.
     */
    public void setEarFilter(String filter)
    {
        earFilterTextField.setText(filter != null ? filter : DEFAULT_EAR_FILTER);
    }


    /**
     * Returns the ear filter currently represented in this dialog.
     */
    public String getEarFilter()
    {
        String filter = earFilterTextField.getText();

        return filter.equals(DEFAULT_EAR_FILTER) ? null : filter;
    }


    /**
     * Sets the zip filter to be represented in this dialog.
     */
    public void setZipFilter(String filter)
    {
        zipFilterTextField.setText(filter != null ? filter : DEFAULT_ZIP_FILTER);
    }


    /**
     * Returns the zip filter currently represented in this dialog.
     */
    public String getZipFilter()
    {
        String filter = zipFilterTextField.getText();

        return filter.equals(DEFAULT_ZIP_FILTER) ? null : filter;
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
}
