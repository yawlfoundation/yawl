package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryEditorPane;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 14/08/12
 */
public class MappingDialog extends JDialog implements ActionListener {

    private VariableRow _row;                    // the task input or output row
    private VariableTablePanel _netTablePanel;
    private XQueryEditorPane _xQueryEditor;
    private XQueryEditorPane _miQueryEditor;

    private JRadioButton netVarsButton;
    private JRadioButton gatewayButton;
    private JRadioButton expressionButton;
    private JComboBox netVarsCombo;
    private JComboBox gatewayCombo;
    private JPanel queryPanel;
    private JButton okButton;


    public MappingDialog(VariableTablePanel netTablePanel, VariableRow row) {
        super();
        _row = row;
        _netTablePanel = netTablePanel;
        setTitle(makeTitle());
        add(getContent());
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
        setPreferredSize(new Dimension(426, _row.isMultiInstance() ? 520 : 400));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("netVarRadio")) {
            enableCombos(true, false);
            handleNetVarComboSelection();
        }
        else if (action.equals("gatewayRadio")) {
            enableCombos(false, true);
            handleGatewayComboSelection();
        }
        else if (action.equals("expressionRadio")) {
            enableCombos(false, false);
        }
        else if (action.equals("netVarComboSelection")) {
            handleNetVarComboSelection();
        }
        else if (action.equals("gatewayComboSelection")) {
            handleGatewayComboSelection();
        }
        else {
            if (action.equals("OK")) {
                _row.setMapping(formatQuery(_xQueryEditor.getText(), false));
                if (_row.isMultiInstance()) {
                    _row.setMIQuery(formatQuery(_miQueryEditor.getText(), false));
                }
                _netTablePanel.getVariableDialog().enableApplyButton();
            }
            setVisible(false);
        }
    }


    private String makeTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(YDataHandler.getScopeName(_row.getUsage()))
          .append(" Data Mapping for Task: ")
          .append(_row.getDecompositionID());
        return sb.toString();
    }


    private JPanel getContent() {
        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(7, 7, 7, 7));
        content.add(buildIOPanel());
        content.add(createQueryPanel());
        if (_row.isMultiInstance()) content.add(createMiQueryPanel());
        content.add(createButtonBar());
        initContent();
        return content;
    }


    private void initContent() {
        if (netVarsCombo.getItemCount() == 0) {
            netVarsButton.setEnabled(false);
            netVarsCombo.setEnabled(false);
        }
        if (gatewayCombo.getItemCount() == 0) {
            gatewayButton.setEnabled(false);
            gatewayCombo.setEnabled(false);
        }

        String mapping = _row.getMapping();
        if (mapping == null) {
            if (netVarsButton.isEnabled()) {
                netVarsButton.setSelected(true);
                enableCombos(true, false);
            }
            else {
                setExpressionButton();
            }
        }
        else {
            if (! (initExternalSelection(mapping) || initNetVarSelection(mapping))) {
                setExpressionButton();
            }
        }
        if (expressionButton.isSelected() && ! expressionButton.isVisible()) {
            enableContents(queryPanel, false);
            okButton.setEnabled(false);
        }
        if (_miQueryEditor != null) {
            disableChoicesForMIVariable();
        }
    }


    private void enableCombos(boolean enableNetVarsCombo, boolean enableGatewayCombo) {
        netVarsCombo.setEnabled(enableNetVarsCombo);
        gatewayCombo.setEnabled(enableGatewayCombo);
    }


    private void setExpressionButton() {
        expressionButton.setSelected(true);
        enableCombos(false, false);
    }

    private boolean initExternalSelection(String mapping) {
        if (! mapping.contains("#external:")) return false;
        String gatewayName = mapping.substring(
                mapping.indexOf(':'), mapping.lastIndexOf(':'));
        if (gatewayName != null) {
            for (int i = 0; i < gatewayCombo.getItemCount(); i++) {
                if (gatewayName.equals(gatewayCombo.getItemAt(i))) {
                    enableCombos(false, true);
                    gatewayCombo.setSelectedIndex(i);
                    gatewayButton.setSelected(true);
                    return true;
                }
            }
        }
        return false;          // gateway not in list
    }


    private boolean initNetVarSelection(String mapping) {
        if (_row.isOutput()) {
            enableCombos(true, false);
            netVarsCombo.setSelectedItem(_row.getNetVarForOutputMapping());
            netVarsButton.setSelected(true);
            return true;
        }
        DefaultMapping defMapping = new DefaultMapping(mapping);
        if (defMapping.isCustomMapping()) return false;

        VariableRow row = getVariableRow(defMapping.getVariableName());
        if (row != null && row.getDecompositionID().equals(defMapping.getContainerName())) {
            enableCombos(true, false);
            netVarsCombo.setSelectedItem(row.getName());
            netVarsButton.setSelected(true);
            return true;
        }
        return false;
    }


    private JPanel createQueryPanel() {
        queryPanel = new JPanel(new BorderLayout());
        queryPanel.setBorder(new TitledBorder(makeQueryTitle()));
        queryPanel.add(createAutoFormatPanel(getXQueryEditor()), BorderLayout.NORTH);
        queryPanel.add(_xQueryEditor, BorderLayout.CENTER);
        return queryPanel;
    }

    private JPanel createMiQueryPanel() {
        String title = (_row.isInput() ? "Splitting" : "Joining") + " Query";
        JPanel miQueryPanel = new JPanel(new BorderLayout());
        miQueryPanel.setBorder(new TitledBorder(title));
        miQueryPanel.add(getMiQueryEditor(), BorderLayout.CENTER);
        return miQueryPanel;
    }

    private JPanel createAutoFormatPanel(final XQueryEditorPane editorPane) {
        String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/";
        JPanel content = new JPanel(new BorderLayout());
        JButton btnFormat = new JButton(ResourceLoader.getImageAsIcon(
                  iconPath + "taskicons/AutoFormat.png"));

        btnFormat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = editorPane.getText();
                editorPane.setText(formatQuery(text, true));
            }
        });

        btnFormat.setToolTipText(" Auto-format query ");
        content.add(btnFormat, BorderLayout.EAST);

        if (_row.isOutput()) {
            JPanel outerContent = new JPanel(new BorderLayout());
            JButton btnReset = new JButton(ResourceLoader.getImageAsIcon(
                    iconPath + "menuicons/arrow_undo.png"));

            btnReset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editorPane.setText(createMapping(_row));
                }
            });

            btnReset.setToolTipText(" Reset query to default ");
            content.add(btnReset, BorderLayout.CENTER);
            outerContent.add(content, BorderLayout.EAST);
            return outerContent;
        }

        return content;
    }

    private String formatQuery(String query, boolean prettify) {
        return XMLUtilities.formatXML(query, prettify, true);
    }


    private String makeQueryTitle() {
        return "Mapping for Task " + YDataHandler.getScopeName(_row.getUsage()) +
                " Variable: " + _row.getName();
    }


    private XQueryEditorPane getXQueryEditor() {
        _xQueryEditor = new XQueryEditorPane();
        _xQueryEditor.setPreferredSize(new Dimension(400, 150));
        _xQueryEditor.setValidating(true);
        _xQueryEditor.setText(formatQuery(_row.getMapping(), true));
        return _xQueryEditor;
    }

    private XQueryEditorPane getMiQueryEditor() {
        _miQueryEditor = new XQueryEditorPane();
        _miQueryEditor.setPreferredSize(new Dimension(400, 90));
        _miQueryEditor.setValidating(true);
        _miQueryEditor.setText(formatQuery(_row.getMIQuery(), true));
        return _miQueryEditor;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Cancel"));
        okButton = createButton("OK");
        panel.add(okButton);
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }


    private String createMapping(VariableRow row) {
        StringBuilder s = new StringBuilder();
        s.append('/').append(row.getDecompositionID()).append('/').append(row.getName())
                .append('/').append(getXQuerySuffix(row));
        return s.toString();
    }


    private String getXQuerySuffix(VariableRow row) {
        return SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                row.getDataType());
    }


    private JPanel buildIOPanel() {
        String title = _row.isInput() ? "Input From" : "Output To";
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(title));
        panel.add(buildSelectionPanel(), BorderLayout.CENTER);
        panel.add(buildRadioPanel(), BorderLayout.WEST);
        panel.setPreferredSize(new Dimension(410, 110));
        return panel;
    }

    private JPanel buildRadioPanel() {
        ButtonGroup buttonGroup = new ButtonGroup();
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.setBorder(new EmptyBorder(0,10,0,10));

        netVarsButton = new JRadioButton("Net Variable: ");
        netVarsButton.setMnemonic(KeyEvent.VK_N);
        netVarsButton.setActionCommand("netVarRadio");
        netVarsButton.addActionListener(this);
        buttonGroup.add(netVarsButton);
        radioPanel.add(netVarsButton);

        gatewayButton = new JRadioButton("Data Gateway: ");
        gatewayButton.setMnemonic(KeyEvent.VK_D);
        gatewayButton.setActionCommand("gatewayRadio");
        gatewayButton.addActionListener(this);
        buttonGroup.add(gatewayButton);
        radioPanel.add(gatewayButton);

        // build even if output so spacing is consistent
        expressionButton = new JRadioButton("Expression: ");
        expressionButton.setMnemonic(KeyEvent.VK_E);
        expressionButton.setActionCommand("expressionRadio");
        expressionButton.addActionListener(this);
        buttonGroup.add(expressionButton);
        radioPanel.add(expressionButton);

        if (_row.isOutput()) expressionButton.setVisible(false);

        return radioPanel;
    }


    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel();
        netVarsCombo = buildComboBox(getNetVarNames());
        netVarsCombo.setActionCommand("netVarComboSelection");
        panel.add(netVarsCombo);
        gatewayCombo = buildComboBox(getDataGatewayNames());
        gatewayCombo.setActionCommand("gatewayComboSelection");
        panel.add(gatewayCombo);
        return panel;
    }


    private Vector<String> getNetVarNames() {
        Vector<String> names = new Vector<String>();
        for (VariableRow row : _netTablePanel.getTable().getVariables()) {
            names.add(row.getName());
        }
        return names;
    }


    private Vector<String> getDataGatewayNames() {
        try {
            return new Vector<String>(YConnector.getExternalDataGateways().keySet());
        }
        catch (IOException ioe) {
            return new Vector<String>();
        }
    }

    private JComboBox buildComboBox(Vector<String> items) {
        JComboBox comboBox = new JComboBox(items);
        comboBox.setPreferredSize(new Dimension(250,
                (int) comboBox.getPreferredSize().getHeight()));
        comboBox.addActionListener(this);
        return comboBox;
    }


    private void handleNetVarComboSelection() {
        if (_row.isOutput()) return;
        VariableRow row = getVariableRow((String) netVarsCombo.getSelectedItem());
        if (row != null) {
            _xQueryEditor.setText(createMapping(row));
        }
    }

    private void handleGatewayComboSelection() {
        if (_row.isOutput()) return;
        String expression ="#external:" + gatewayCombo.getSelectedItem() + ":" +
                                            _row.getName();
        _xQueryEditor.setText(expression);
    }

    private VariableRow getVariableRow(String name) {
        for (VariableRow row : _netTablePanel.getTable().getVariables()) {
             if (row.getName().equals(name)) return row;
        }
        return null;
    }


    private void enableContents(JPanel panel, boolean enable) {
        for (Component component : panel.getComponents()) {
            component.setEnabled(enable);
        }
    }


    private void disableChoicesForMIVariable() {
        netVarsButton.setEnabled(false);
        gatewayButton.setEnabled(false);
        expressionButton.setEnabled(false);
        netVarsCombo.setEnabled(false);
        gatewayCombo.setEnabled(false);
    }


}
