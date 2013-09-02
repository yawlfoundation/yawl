package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryEditorPane;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.data.DataVariableComboBox;
import org.yawlfoundation.yawl.editor.ui.swing.data.DialogMode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class XQueryUpdateDialog extends AbstractDoneDialog implements ActionListener {

    private AbstractDoneDialog parent;

    private boolean firstAppearance = true;

 //   private ExtendedAttribute attribute;
    private DialogMode mode;

    private XQueryEditorPane xQueryEditor;

    private JCheckBox useXQuery;
    private JButton inputVariableQueryButton;
    protected DataVariableComboBox inputVariableComboBox;

    private String _text;

    public XQueryUpdateDialog(AbstractDoneDialog parent, DialogMode mode) {
        super("XQuery", true);
        this.parent = parent;
        this.mode = mode;
        setContentPanel(getPredicatePanel());

        getDoneButton().addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(useXQuery.isSelected())
                        {
                            _text = "${" + xQueryEditor.getText() + "}" ;
                        }
                        else
                        {
                            _text = xQueryEditor.getText();
                        }
                    }
                }
        );
    }

    public String showDialog() {
        setVisible(true);
        return _text;
    }

//    public void setExtendedAttribute(ExtendedAttribute attribute) {
//        this.attribute = attribute;
//        populateInputVariableComboBox();
//
//        String value = URLDecoder.decode(attribute.getValue());
//        if(value.startsWith("${"))
//        {
//            value = value.substring(2);
//            value = value.substring(0, value.length() - 1);
//            useXQuery.setSelected(true);
//            xQueryEditor.setValidating(true);
//        }
//        else
//        {
//            useXQuery.setSelected(false);
//            xQueryEditor.setValidating(false);
//        }
//
//        _text = value;
//        xQueryEditor.setText(value);
//    }

    protected void makeLastAdjustments() {
        pack();
        setSize(430,240);
        JUtilities.setMinSizeToCurrent(this);
    }

    public void setVisible(boolean isVisible) {
        if (isVisible) {
            if (firstAppearance) {
                this.setLocationRelativeTo(parent);
                firstAppearance = false;
            }

            if(mode == DialogMode.TASK) useXQuery.setVisible(true);
            else
            {
                useXQuery.setVisible(false);
                useXQuery.setSelected(true);
                xQueryEditor.setValidating(true);
            }
        }
        super.setVisible(isVisible);
    }

    private JPanel getPredicatePanel() {

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel panel = new JPanel(gbl);
        panel.setBorder(new EmptyBorder(12,12,0,11));

        // TODO: make variable query widget set a package for this and ParameterUpdateDialog

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.weightx = 0.333;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;

        gbc.insets = new Insets(0,0,5,5);

        useXQuery = new JCheckBox("Use XQuery");
        useXQuery.setMnemonic('X');
        useXQuery.addActionListener(this);
        panel.add(useXQuery, gbc);

        JLabel inputVariableLabel = new JLabel("Variable:");
        inputVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
        inputVariableLabel.setDisplayedMnemonic('v');

        panel.add(inputVariableLabel, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,5,5,5);
        panel.add(getInputVariableComboBox(),gbc);
        inputVariableLabel.setLabelFor(inputVariableComboBox);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(getNewInputVariableQueryButton(),gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;

        panel.add(getXQueryEditor(),gbc);

        return panel;
    }

    private void populateInputVariableComboBox() {
        inputVariableComboBox.setEnabled(false);

//        inputVariableComboBox.setNet(
//                attribute.getDecomposition()
//        );

        if (inputVariableComboBox.getItemCount() > 0) {
            inputVariableComboBox.setEnabled(true);
            inputVariableQueryButton.setEnabled(true);
        } else {
            inputVariableComboBox.setEnabled(false);
            inputVariableQueryButton.setEnabled(false);
        }
    }

    private JComboBox getInputVariableComboBox() {
        inputVariableComboBox = new DataVariableComboBox(0);
        return inputVariableComboBox;
    }

    private JButton getNewInputVariableQueryButton() {
        inputVariableQueryButton = new JButton("XPath Expression");
        inputVariableQueryButton.setToolTipText("Generates an XPath expression returning this variable");
        inputVariableQueryButton.setMnemonic(KeyEvent.VK_X);
        inputVariableQueryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    xQueryEditor.getDocument().insertString(
                            xQueryEditor.getCaretPosition(),
//                            XMLUtilities.getXPathPredicateExpression(inputVariableComboBox.getSelectedVariable()),
                            "", null
                    );
                } catch (Exception e) {
                    xQueryEditor.setText(
                            xQueryEditor.getText() +
//                            XMLUtilities.getXPathPredicateExpression(inputVariableComboBox.getSelectedVariable())
                        ""
                    );
                }
            }
        });
        inputVariableQueryButton.addActionListener(this);

        return inputVariableQueryButton;
    }

    private XQueryEditorPane getXQueryEditor() {
        xQueryEditor = new XQueryEditorPane(" = 'true'");
        xQueryEditor.setMinimumSize(new Dimension(400,400));
        return xQueryEditor;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource().equals(useXQuery))
        {
            xQueryEditor.setValidating(useXQuery.isSelected());

            if(!useXQuery.isSelected())
            {
      //          xQueryEditor.hideProblemTable();
            }
        }
        else if (e.getSource().equals(inputVariableQueryButton))
        {
            useXQuery.setSelected(true);
            xQueryEditor.setValidating(true);
        }
    }
}
