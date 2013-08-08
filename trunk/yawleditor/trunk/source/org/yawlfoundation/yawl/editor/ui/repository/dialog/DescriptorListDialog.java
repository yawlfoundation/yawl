package org.yawlfoundation.yawl.editor.ui.repository.dialog;

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.core.repository.RepoDescriptor;
import org.yawlfoundation.yawl.editor.ui.repository.listModel.RepositoryListModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class DescriptorListDialog extends JDialog implements ActionListener, CaretListener {

    private JButton btnOK;
    private JList listBox;
    private JTextField filterField;
    private JTextArea txtDescription;
    private int action;

    public static final int GET_ACTION = 0;
    public static final int REMOVE_ACTION = 1;


    public DescriptorListDialog(JDialog owner, Repo repo, int action) {
        super(owner);
        this.action = action;
        initialise();
        add(getContent());
        listBox.setModel(new RepositoryListModel(repo));
        setTitle((action == GET_ACTION ? "Load" : "Remove") + " from Repository");
        setPreferredSize(new Dimension(295, 500));
        pack();
    }


    public RepositoryListModel getListModel() {
        return (RepositoryListModel) listBox.getModel();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Cancel")) {
            listBox.clearSelection();
        }
        setVisible(false);
    }

    public void caretUpdate(CaretEvent caretEvent) {
        btnOK.setEnabled(false);
        listBox.clearSelection();
        txtDescription.setEditable(true);
        txtDescription.setText("");
        txtDescription.setEditable(false);
        getListModel().filter(filterField.getText());
    }

    public RepoDescriptor getSelection() {
        int selectedIndex = listBox.getSelectedIndex();
        return selectedIndex > -1 ? getListModel().getSelection(selectedIndex) : null;
    }

    public java.util.List<RepoDescriptor> getSelections() {
        return getListModel().getSelections(listBox.getSelectedIndices());
    }


    private void initialise() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }


    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createMainContentPanel(), BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createMainContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createFilterPanel(), BorderLayout.NORTH);
        content.add(createListBox(), BorderLayout.CENTER);
        content.add(createDescriptionPanel(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterField = new JTextField();
        filterField.setPreferredSize(new Dimension(230, 25));
        filterField.addCaretListener(this);
        filterPanel.add(filterField);
        return filterPanel;
    }

    private JPanel createListBox() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Name"));
        listBox = new JList();
        if (action == GET_ACTION) {
            listBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        listBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    int index = listBox.getSelectedIndex();
                    if (index > -1) {
                        txtDescription.setEditable(true);
                        txtDescription.setText( getListModel().getDescriptionAt(index));
                        txtDescription.setEditable(false);
                        btnOK.setEnabled(true);
                        listBox.grabFocus();
                        listBox.setSelectedIndex(index);
                    }
                }
                else if (mouseEvent.getClickCount() == 2) {
                    setVisible(false);
                }
            }
        });

        JScrollPane pane = new JScrollPane(listBox);
        pane.setPreferredSize(new Dimension(280, 300));
        panel.add(pane);
        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Description"));
        txtDescription = new JTextArea();
        txtDescription.setWrapStyleWord(true);
        txtDescription.setLineWrap(true);
        txtDescription.setEditable(false);
        JScrollPane pane = new JScrollPane(txtDescription);
        pane.setPreferredSize(new Dimension(300, 100));
        panel.add(pane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton("Cancel"));
        btnOK = createButton("OK");
        btnOK.setEnabled(false);
        panel.add(btnOK);
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


}
