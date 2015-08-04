/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.swingWorklist;

import org.apache.log4j.Logger;
import org.jdom.input.JDOMParseException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.gui.YAdminGUI;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 15/05/2003
 * Time: 15:00:22
 * 
 */
public class YWorklistGUI extends JPanel implements ActionListener, ListSelectionListener {
    private YWorklistModel _worklistModel;
    private JTable _availableTable;
    private JTable _activeTable;
    private JButton _applyButton;
    private String _applyCommand = "Apply for Task";
    private JButton _completeButton;
    private String _completionCommand = "Register Completion";
    private JButton _newInstanceButton;
    private String _newInstanceCommand = "Create new Instance";
    private JButton _cancelTaskButton;
    private String _suspendTaskCommand = "Suspend Task";
    private JButton _viewDataButton;
    private String _viewDataCommand = "View/edit data";
    private JButton _updateListsButton;
    private String _updateListsCommand = " Update Lists ";
    private int _rowSelected = -1;
    private String _userName;
    private String _newInstanceData;
    private JFrame _frame;

    // Log4J Logger
    private static final Logger logger = Logger.getLogger(YWorklistGUI.class);

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public YWorklistGUI() {
        super();
    }

    public YWorklistGUI(String userName, YWorklistModel worklistModel, JFrame frame) {
        _frame = frame;
        _worklistModel = worklistModel;
        _userName = userName;

        setBackground(YAdminGUI._apiColour);
        JPanel worklistsPanel = new JPanel(new GridLayout(2, 1));
        setLayout(new BorderLayout());
        _applyButton = new JButton(_applyCommand);
        _applyButton.setBackground(new Color(204, 255, 0));
        _applyButton.setPreferredSize(new Dimension(160, 30));
        _applyButton.addActionListener(this);
        _completeButton = new JButton(_completionCommand);
        _completeButton.setBackground(new Color(255, 100, 100));
        _completeButton.addActionListener(this);
        _newInstanceButton = new JButton(_newInstanceCommand);
        _newInstanceButton.addActionListener(this);
        _cancelTaskButton = new JButton(_suspendTaskCommand);
        _cancelTaskButton.addActionListener(this);
        _viewDataButton = new JButton(_viewDataCommand);
        _viewDataButton.setBackground(new Color(150, 150, 255));
        _viewDataButton.addActionListener(this);
        //do available work panel
        YWorkListPanel availablePanel =
                new YWorkListPanel(
                        _worklistModel.getAvaliableModel(),
                        "Scheduled Tasks",
                        new Dimension(300, 75),
                        _applyButton,
                        null,
                        null,
                        null);
        _availableTable = availablePanel.getMyTable();
        worklistsPanel.add(availablePanel);
        //do active tasks panel
        YWorkListPanel activePanel =
                new YWorkListPanel(
                        _worklistModel.getActiveTasksModel(),
                        _userName + "'s Active Tasks",
                        new Dimension(300, 75),
                        _completeButton,
                        _cancelTaskButton,
                        _newInstanceButton,
                        _viewDataButton);
        _activeTable = activePanel.getMyTable();
        worklistsPanel.add(activePanel);
        //do bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        _updateListsButton = new JButton(_updateListsCommand);
        bottomPanel.add(_updateListsButton, BorderLayout.EAST);
        bottomPanel.setBackground(YAdminGUI._apiColour);
        bottomPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 18));
        _updateListsButton.addActionListener(this);
        add(worklistsPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);


        final TopPopupMenu topPopup = new TopPopupMenu(this);
        _availableTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowSelected = _availableTable.rowAtPoint(e.getPoint());

                    try {
                        applyForWorkItem(rowSelected);
                    } catch (YPersistenceException e2) {
                        logError("Failure to apply for work item", e2);
                        logger.fatal("Failure to apply for work item", e2);
                        System.exit(99);
                    }


                    _worklistModel.refreshLists(_userName);
                }
                //right click
                else {
                    maybeShowPopup(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger() && e.getButton() != MouseEvent.BUTTON1) {
                    _rowSelected = _availableTable.rowAtPoint(e.getPoint());
                    topPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        final WorkListPopupMenu popupMenu = new WorkListPopupMenu(this);
        //       getContentPane().add(_popupMenu);
        _activeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowSelected = _activeTable.rowAtPoint(e.getPoint());
                    createApplicationXPage(rowSelected);
                }
                //right click
                else {
                    maybeShowPopup(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger() && e.getButton() != MouseEvent.BUTTON1) {
                    _rowSelected = _activeTable.rowAtPoint(e.getPoint());
                    popupMenu.setVisibityOfMenuItems(_rowSelected);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        ListSelectionModel rowSM = _activeTable.getSelectionModel();
        rowSM.addListSelectionListener(this);

        _worklistModel.refreshLists(_userName);
    }

    private void createApplicationXPage(int rowSelected) {
        Object[] data = _worklistModel.getActiveTableData(
                (String) _activeTable.getValueAt(rowSelected, 0),
                (String) _activeTable.getValueAt(rowSelected, 1));
        new YApplicationXForm(_frame, YWorklistGUI.this, data, _worklistModel);
    }


    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        String command = e.getActionCommand();
        int rowSel = _availableTable.getSelectedRow();
        if (command.equals(_applyCommand)) {
            if (rowSel == -1) {
                rowSel = _rowSelected;
            }

            try {
                applyForWorkItem(rowSel);
            } catch (YPersistenceException e2) {
                logError("Failure to apply for work item", e2);
                logger.fatal("Failure to apply for work item", e2);
                System.exit(99);
            }


            _worklistModel.refreshLists(_userName);
        } else {
            rowSel = _activeTable.getSelectedRow();
            if (rowSel == -1) {
                rowSel = _rowSelected;
            }
            if (command.equals(_completionCommand)) {
                if (rowSel >= 0) {
                    String caseID = (String) _activeTable.getValueAt(rowSel, 0);
                    String taskID = (String) _activeTable.getValueAt(rowSel, 1);
                    completeWorkItem(caseID, taskID);
                    _worklistModel.refreshLists(_userName);
                }
            } else if (command.equals(_newInstanceCommand)) {
                if (rowSel >= 0) {
                    try {
                        String caseID = (String) _activeTable.getValueAt(rowSel, 0);
                        String taskID = (String) _activeTable.getValueAt(rowSel, 1);
                        //two messages sent upwards, one must complete before the next starts
                        YParameter param = _worklistModel.getMIUniqueParam(taskID);
                        String miUniqueParamStr = Marshaller.presentParam(param);
                        new MIUniqueInputDialog(_frame, this, miUniqueParamStr);
                        _worklistModel.createNewInstance(caseID, taskID, _newInstanceData);
                        //this will be a synchronous call to a WebSevice on the Engine
                        _newInstanceButton.setEnabled(_worklistModel.allowsDynamicInstanceCreation(caseID, taskID));
                        _worklistModel.refreshLists(_userName);
                    } catch (YPersistenceException e1) {
                        logError("Failure to create new instance", e1);
                        System.exit(99);
                    }
                }
            } else if (command.equals(_suspendTaskCommand)) {
                try {
                    if (rowSel >= 0) {
                        String caseID = (String) _activeTable.getValueAt(rowSel, 0);
                        String taskID = (String) _activeTable.getValueAt(rowSel, 1);
                        _worklistModel.rollBackActiveTask(caseID, taskID);
                        _worklistModel.refreshLists(_userName);
                    }
                } catch (YPersistenceException e1) {
                    logError("Failure to create new instance", e1);
                    System.exit(99);
                }
            } else if (command.equals(_viewDataCommand)) {
                if (rowSel >= 0) {
                    createApplicationXPage(rowSel);
                }
            } else if (command.equals(_updateListsCommand)) {
                _worklistModel.refreshLists(_userName);
            }
        }
        _rowSelected = -1;
    }


    protected void completeWorkItem(String caseID, String taskID) {
        _worklistModel.attemptToFinishActiveJob(caseID, taskID);
        _newInstanceButton.setEnabled(false);
        _worklistModel.refreshLists(_userName);
    }

    private void applyForWorkItem(int rowSel) throws YPersistenceException {
        if (rowSel >= 0) {
            String caseID = (String) _availableTable.getValueAt(rowSel, 0);
            String taskID = (String) _availableTable.getValueAt(rowSel, 1);
            try {
                _worklistModel.applyForWorkItem(caseID, taskID);
            } catch (YSchemaBuildingException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;
        ListSelectionModel lsm =
                (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
            //no rows are selected
        } else {
            int selectedRow = lsm.getMinSelectionIndex();
            //selectedRow is selected
            String caseID = (String) _activeTable.getValueAt(selectedRow, 0);
            String taskID = (String) _activeTable.getValueAt(selectedRow, 1);
            if (_worklistModel.allowsDynamicInstanceCreation(caseID, taskID)) {
                _newInstanceButton.setEnabled(true);
            } else {
                _newInstanceButton.setEnabled(false);
            }
        }
    }


    class WorkListPopupMenu extends JPopupMenu {
        JMenuItem _completionItem = new JMenuItem(_completionCommand);
        JMenuItem _viewDataItem = new JMenuItem(_viewDataCommand);
        JMenuItem _newInstanceItem = new JMenuItem(_newInstanceCommand);
        JMenuItem _cancelItem = new JMenuItem(_suspendTaskCommand);


        public WorkListPopupMenu(YWorklistGUI ref) {
            super("File");
            add(_completionItem);
            _completionItem.addActionListener(ref);
            add(_viewDataItem);
            _viewDataItem.addActionListener(ref);
            add(_newInstanceItem);
            _newInstanceItem.addActionListener(ref);
            add(_cancelItem);
            _cancelItem.addActionListener(ref);
        }

        public void setVisibityOfMenuItems(int rowSelected) {
        }
    }

    class TopPopupMenu extends JPopupMenu {
        JMenuItem _applyItem = new JMenuItem(_applyCommand);
        YWorklistGUI _ref;

        public TopPopupMenu(YWorklistGUI ref) {
            add(_applyItem);
            _applyItem.addActionListener(ref);
        }
    }

    class MIUniqueInputDialog extends JDialog implements ActionListener {
        private YWorklistGUI _worklistGui;
        final JTextPane dataPane;

        public MIUniqueInputDialog(JFrame owner, YWorklistGUI worklist, String miUniqueParamStr) {
            super(owner, "Require Extra Information About new Instance", true);
            Container c = getContentPane();
            c.setLayout(new BorderLayout());
            _worklistGui = worklist;
            dataPane = new JTextPane();
            dataPane.setText(miUniqueParamStr);
            c.add(new JScrollPane(dataPane), BorderLayout.CENTER);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    _worklistGui.setNewInstanceData(dataPane.getText());
                    dispose();
                }
            });
            JButton addInstanceButton = new JButton("Add This instance");
            addInstanceButton.addActionListener(this);
            c.add(addInstanceButton, BorderLayout.SOUTH);
            setSize(700, 500);
            setLocation(150, 50);
            show();
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            _worklistGui.setNewInstanceData(dataPane.getText());
            dispose();
        }


    }


    private void setNewInstanceData(String newInstanceData) {
        _newInstanceData = newInstanceData;
    }

    public void reportGeneralProblem(Exception e) {
        String message;
        if (e instanceof IOException || e instanceof JDOMParseException) {
            message = "There was a problem parsing your input data.  \n" +
                    "Perhaps check that the XML is well formed.";
        } else if (e instanceof YQueryException) {
            message = e.getMessage();
        } else {
            message = e.getMessage();
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                message,
                "Problem with data input",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display a MsgBox() style dialog to report a runtime error.
     *
     * @param e The exception to be reported
     *
     */
    private void logError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), message, JOptionPane.ERROR_MESSAGE);
        logger.error(message, e);
    }


}


