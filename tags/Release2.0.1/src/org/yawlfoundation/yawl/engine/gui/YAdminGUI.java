/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.gui;


import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.authentication.User;
import org.yawlfoundation.yawl.authentication.UserList;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceADesign;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagement;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClientObserver;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBInterop;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.swingWorklist.YWorklistGUI;
import org.yawlfoundation.yawl.swingWorklist.YWorklistModel;
import org.yawlfoundation.yawl.swingWorklist.YWorklistTableModel;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * 
 * @author Lachlan Aldred
 * Date: 16/06/2003
 * Time: 12:29:13
 * 
 */
public class YAdminGUI extends JPanel implements InterfaceBClientObserver,
        ActionListener,
        ItemListener {
    private JButton _loadWorkFlowButton;
    private JButton _unloadWorkFlowButton;
//    private JCheckBox _ignoreErrorsCheckBox;
//    private JCheckBox _oldFormatCheckBox;
    private JButton _startCaseButton;
    private JButton _exportToXMLButton;
    private JButton _cancelCaseButton;
    private JButton _inspectStateButton;
    private JButton _suspendCaseButton;
    private JButton _resumeCaseButton;
    private JButton _dumpButton;
    private String _loadWorkflowCommand = "Load Workflow";
    private String _unloadWorkflowCommand = "Unload Workflow";
    private String _exportToXMLCommand = "Export to XML";
    private String _createWorklistCommand = "Create Worklist";
    private String _reannounceEnabledWorkItems = "Reannounce";
    private String _startCaseCommand = "Start Case";
    private String _cancelCaseCommand = "Cancel";
    private String _inspectStateCommand = "Inspect";
    private String _suspendCaseCommand = "Suspend";
    private String _resumeCaseCommand = "Resume";
    private String _dumpCommand = "Dump";
    private JTable _loadedSpecificationsTable;
    private JButton _createWorklistButton;
    private JButton _reannounceButton;
    private YWorklistTableModel _loadedSpecificationsTableModel;
    private JTable _activeWorklistsTable;
    private YWorklistTableModel _activeWorklistsTableModel;
    private static boolean _quickyLoad = false;
    private boolean journalising = false;

    //engine state attributes
    private YWorklistTableModel _activeCasesTableModel;
    private JTable _activeCasesTable;
    private JFileChooser _fileChooser;
    public static Color _apiColour = new Color(192, 192, 170);
    private boolean _ignoreErrors = /*true*/false;
    private static boolean _networked;
//    private YAWLEngine _engine;
    private boolean _oldFormat = true;
    private JFrame _frame;
    private TabbedEngineGUI _tabbedGUI;

    // Log4J Logger
    private static final Logger logger = Logger.getLogger(YAdminGUI.class);

    // Engine references (via set interfaces)
    private InterfaceADesign _engineDesign;
    private InterfaceAManagement _engineManagement;
    private InterfaceBClient _engineClient;
    private InterfaceBClientObserver _engineClientObserver;
    private InterfaceBInterop _engineInterop;

    // RTE_TOOLIT
    private static Hashtable<String, File> specTestDataDirs = new Hashtable<String, File>();

    private YAdminGUI() {
        super();
    }


    /**
     * Creates a new, initially invisible <code>Frame</code> with the
     * specified title.
     * <p>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @exception java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see java.awt.Component#setSize
     * @see java.awt.Component#setVisible
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public YAdminGUI(YSplash splash, JFrame frame, TabbedEngineGUI tabbedGUI, boolean journalise, boolean generateUIMetaData) {
        this();

        logger.debug("Initialising");

        this.journalising = journalise;

        _tabbedGUI = tabbedGUI;
        _frame = frame;
        setBackground(_apiColour);
        setLayout(new BorderLayout(20, 20));

        _loadWorkFlowButton = new JButton(_loadWorkflowCommand);
        _loadWorkFlowButton.setBackground(new Color(150, 150, 255));
        _loadWorkFlowButton.addActionListener(this);
        _unloadWorkFlowButton = new JButton(_unloadWorkflowCommand);
        _unloadWorkFlowButton.setBackground(new Color(150, 150, 255));
        _unloadWorkFlowButton.addActionListener(this);

        _startCaseButton = new JButton(_startCaseCommand);
        _startCaseButton.setBackground(new Color(150, 150, 255));
        _startCaseButton.addActionListener(this);
        _startCaseButton.setIcon(null);

        _exportToXMLButton = new JButton(_exportToXMLCommand);
        _exportToXMLButton.setBackground(new Color(150, 150, 255));
        _exportToXMLButton.addActionListener(this);

        _createWorklistButton = new JButton(_createWorklistCommand);
        _createWorklistButton.setBackground(new Color(150, 150, 255));
        _createWorklistButton.addActionListener(this);

        _reannounceButton = new JButton(_reannounceEnabledWorkItems);
        _reannounceButton.setBackground(new Color(150, 150, 255));
        _reannounceButton.addActionListener(this);

        _cancelCaseButton = new JButton(_cancelCaseCommand);
        _cancelCaseButton.setBackground(new Color(150, 150, 255));
        _cancelCaseButton.addActionListener(this);
        _inspectStateButton = new JButton(_inspectStateCommand);
        _inspectStateButton.setBackground(new Color(150, 150, 255));
        _inspectStateButton.addActionListener(this);

        _suspendCaseButton = new JButton(_suspendCaseCommand);
        _suspendCaseButton.setBackground(new Color(150, 150, 255));
        _suspendCaseButton.addActionListener(this);
        _resumeCaseButton = new JButton(_resumeCaseCommand);
        _resumeCaseButton.setBackground(new Color(150, 150, 255));
        _resumeCaseButton.addActionListener(this);

        _dumpButton = new JButton(_dumpCommand);
        _dumpButton.setBackground(new Color(150, 150, 255));
        _dumpButton.addActionListener(this);

        splash.setProgress(10);
        _loadedSpecificationsTableModel =
                new YWorklistTableModel(new String[]{"SpecificationID", "Root Net Name"});
        _loadedSpecificationsTable = new JTable(_loadedSpecificationsTableModel);
        JPanel loadedWorkflowsPanel = new JPanel(new BorderLayout());
        loadedWorkflowsPanel.setBackground(_apiColour);
        loadedWorkflowsPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(),
                                "Loaded Workflows"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JScrollPane scrollPane = new JScrollPane(_loadedSpecificationsTable);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        loadedWorkflowsPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        buttonPanel.setBackground(_apiColour);
        buttonPanel.add(_loadWorkFlowButton);
        buttonPanel.add(_startCaseButton);
        buttonPanel.add(_exportToXMLButton);
//        _ignoreErrorsCheckBox = new JCheckBox("Ignore Errors");
//        _ignoreErrorsCheckBox.addItemListener(this);
//        _ignoreErrorsCheckBox.setSelected(false);
//        _oldFormatCheckBox = new JCheckBox("Old Format");
//        _oldFormatCheckBox.addItemListener(this);
//        _oldFormatCheckBox.setSelected(true);

        splash.setProgress(20);
//        buttonPanel.add(_ignoreErrorsCheckBox);
//        buttonPanel.add(_oldFormatCheckBox);
        buttonPanel.add(_unloadWorkFlowButton);
        buttonPanel.add(_dumpButton);

        loadedWorkflowsPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(loadedWorkflowsPanel, BorderLayout.NORTH);
        _activeCasesTableModel = new YWorklistTableModel(new String[]{"SpecificationID", "CaseID"});
        _activeCasesTable = new JTable(_activeCasesTableModel);
        JPanel activeCasesPanel = new JPanel(new BorderLayout());
        activeCasesPanel.setBackground(_apiColour);
        activeCasesPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Active Cases"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        scrollPane = new JScrollPane(_activeCasesTable);
        scrollPane.setPreferredSize(new Dimension(350, 100));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        activeCasesPanel.add(scrollPane, BorderLayout.CENTER);
        buttonPanel = new JPanel(new GridLayout(1, 3));
        splash.setProgress(30);
        buttonPanel.setBackground(_apiColour);
        buttonPanel.add(_inspectStateButton);
        buttonPanel.add(_cancelCaseButton);
        buttonPanel.add(_suspendCaseButton);
        buttonPanel.add(_resumeCaseButton);
        activeCasesPanel.add(buttonPanel, BorderLayout.SOUTH);


        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(_apiColour);

        bottomPanel.add(activeCasesPanel, BorderLayout.WEST);
        _activeWorklistsTableModel = new YWorklistTableModel(new String[]{"UserName"});
        _activeWorklistsTable = new JTable(_activeWorklistsTableModel);
        JPanel activeWorklistsPanel = new JPanel(new BorderLayout());
        activeWorklistsPanel.setBackground(_apiColour);
        activeWorklistsPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(),
                                "Active Worklists"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        scrollPane = new JScrollPane(_activeWorklistsTable);
        scrollPane.setPreferredSize(new Dimension(350, 100));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        activeWorklistsPanel.add(scrollPane, BorderLayout.CENTER);
        buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.setBackground(_apiColour);
        buttonPanel.add(_createWorklistButton);
        buttonPanel.add(_reannounceButton);
        activeWorklistsPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.add(activeWorklistsPanel, BorderLayout.EAST);
        splash.setProgress(40);
        add(bottomPanel);
        _fileChooser = setUpChooser();

        _loadedSpecificationsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowSelected = _loadedSpecificationsTable.rowAtPoint(e.getPoint());
                    startCase(rowSelected);
                }
            }
        });
        splash.setProgress(50);
        _activeWorklistsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int rowSelected = _activeWorklistsTable.rowAtPoint(e.getPoint());
                    String userName = (String) _activeWorklistsTableModel.getValueAt(rowSelected, 0);
                    new YWorklistModel(userName, _frame);
                }
            }
        });
        splash.setProgress(60);
        setSize(getPreferredSize());

        /**
         * AJH: Changed to force display onto primary screen in a dual-head X environment
         */
        Dimension guiSize = getPreferredSize();
        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());

        setLocation(screenWidth.intValue() / 2 - (guiSize.width / 2),
                    screenHeight.intValue() / 2 - (guiSize.height / 2));
        setVisible(true);

        /**
         * Create engine instance (plus references vai the various interfaces)
         */
        try {
            _engineManagement = YEngine.getInstance(journalising);
            _engineClient = YEngine.getInstance(journalising);

            /**
             * AJH: Indicateto YEngine if we are to generate UI metadata in a tasks input XML doclet
             */
            YEngine obj = (YEngine)_engineClient;
            obj.setGenerateUIMetaData(generateUIMetaData);

        } catch (YPersistenceException e) {
            logger.fatal("Failure to instanciate engine", e);
            logError("Failure to instanciate engine", e);
            System.exit(99);
        }
        splash.setProgress(80);
        //AJH: Load up the loaded specs into UI
        {
            try {
                Set specs = _engineManagement.getLoadedSpecifications();
                Iterator iter = specs.iterator();
                while (iter.hasNext()) {
                    YSpecification spec = _engineManagement.getSpecification((YSpecificationID) iter.next());
                    logger.debug("Loading spec " + spec.getID());
                    _loadedSpecificationsTableModel.addRow(spec.getSpecificationID(), new Object[]{spec.getSpecificationID(), spec.getRootNet().getID()});

                    // Load up any active cases
                    Set cases = _engineManagement.getCasesForSpecification(spec.getSpecificationID());
                    logger.debug("Loading " + cases.size() + " active cases for this specification");

                    Iterator iterCases = cases.iterator();
                    while (iterCases.hasNext()) {
                        YIdentifier caseID = (YIdentifier) iterCases.next();
                        addCase(spec.getID(), caseID.get_idString());
                    }
                }
            } catch (YPersistenceException e) {
                logError("Failure to load specifications", e);
                System.exit(99);
            }
        }
        splash.setProgress(100);
        _engineClient.registerInterfaceBObserver(this);
//        _engine.setGUI(this);

        if (_quickyLoad) {
            quickyLoad();
        }
    }

    public void loadWorklists() {
        //AJH: Load up the worklists
        {
            try {
                Set users = _engineManagement.getUsers();
                Iterator iter = users.iterator();
                while (iter.hasNext()) {
                    User user = (User) iter.next();
                    attemptToCreateWorklist(user.getUserID());
                }
            } catch (Exception e) {
                logError("Failure to load specifications", e);
                System.exit(99);
            }

        }
    }

    /**
     * Display a MsgBox() style dialog to report a runtime error.
     *
     * @param e The exception to be reported
     *
     */
    private void logError(String message, Exception e) {
        if (e == null)
        {
            JOptionPane.showMessageDialog(this, message, "Log Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
        JOptionPane.showMessageDialog(this, e.getMessage(), message, JOptionPane.ERROR_MESSAGE);
        logger.error(message, e);
    }
    }


    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command == _createWorklistCommand) {
            /**
             * Create a new worklist entry
             */
            String userName = JOptionPane.showInputDialog(
                    this, "Worklist User Name", "Create Worklist", JOptionPane.QUESTION_MESSAGE);

            try {
                attemptToCreateWorklist(userName);
            } catch (Exception err) {
                logError("Failure to create worklist for user '" + userName + "'", err);
            }


        } else if (command == _loadWorkflowCommand) {
            /**
             * Load a new process specification
             */
            int returnVal = _fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = _fileChooser.getSelectedFile();
                attemptToLoadSpecificationFile(selectedFile);

            }
        } else if (command == _unloadWorkflowCommand) {
            /**
             * Unload a process specification
             */
            int rowSelected = _loadedSpecificationsTable.getSelectedRow();
            if (rowSelected >= 0) {
                YSpecificationID specID = (YSpecificationID) _loadedSpecificationsTable.getValueAt(rowSelected, 0);

                try {
                    _engineManagement.unloadSpecification(specID);
                    _loadedSpecificationsTableModel.removeRow(specID);
                } catch (YStateException e1) {
                    logError("Specification could not be unloaded - " + e1.getMessage(), e1);
                    e1.printStackTrace();
                } catch (YPersistenceException ex) {
                    logError("Failure to unload specifcation", ex);
                }
            }
        } else if (command == _startCaseCommand) {
            /**
             * Start a new case
             */
            int selectedRow = _loadedSpecificationsTable.getSelectedRow();
            startCase(selectedRow);
        } else if (command == _cancelCaseCommand) {
            /**
             * Cancel a case
             */
            try {
                int selectedRow = _activeCasesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String caseIDStr = (String) _activeCasesTable.getValueAt(selectedRow, 1);
                    YIdentifier id = _engineManagement.getCaseID(caseIDStr);
                    if (id != null) {
                        _engineManagement.cancelCase(id);
                        _activeCasesTableModel.removeRow(id.toString());
                    }
                }
            } catch (YAWLException e2) {
                logError("Failure to cancel case", e2);
            }
        } else if (command == _inspectStateCommand) {
            /**
             * Report the state of a case
             */
            try {
                int selectedRow = _activeCasesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String caseIDStr = (String) _activeCasesTable.getValueAt(selectedRow, 1);
                    YIdentifier id = _engineManagement.getCaseID(caseIDStr);
                    if (id != null) {
                        String textToDisplay = _engineManagement.getStateTextForCase(id);
                        new YInspectStateDialog(_frame, textToDisplay);
                    }
                }
            } catch (YPersistenceException ex) {
                logError("Failure whilst obtaining state of case", ex);
            }
        } else if (command == _suspendCaseCommand) {
            /**
             * Suspend a case
             */
            try {
                int selectedRow = _activeCasesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String caseIDStr = (String) _activeCasesTable.getValueAt(selectedRow, 1);
                    YIdentifier id = _engineManagement.getCaseID(caseIDStr);
                    if (id != null) {
                        _engineManagement.suspendCase(id);
                    }
                }
            } catch (YAWLException e2) {
                logError("Failure to suspend case", e2);
            }
        } else if (command == _resumeCaseCommand) {
            /**
             * Resume a case
             */
            try {
                int selectedRow = _activeCasesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String caseIDStr = (String) _activeCasesTable.getValueAt(selectedRow, 1);
                    YIdentifier id = _engineManagement.getCaseID(caseIDStr);
                    if (id != null) {
                        _engineManagement.resumeCase(id);
                    }
                }
            } catch (YAWLException e2) {
                logError("Failure to resume case", e2);
            }
        } else if (command == _exportToXMLCommand) {
            int selectedRow = _loadedSpecificationsTable.getSelectedRow();
            saveXML(selectedRow);
        }
        else if (command == _reannounceEnabledWorkItems) {
            try
            {
                _engineManagement.reannounceEnabledWorkItems();
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(this, e2.getMessage(), "Error re-announcing workitems", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (command == _dumpCommand)
        {
            try
            {
                _engineManagement.dump();
            }
            catch (Exception e2)
            {
                JOptionPane.showMessageDialog(this, e2.getMessage(), "Error dumping engine tables", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void attemptToLoadSpecificationFile(File selectedFile) {
        YSpecification spec = null;
        Preferences prefs =
                Preferences.userNodeForPackage(YAdminGUI.class);
        if (selectedFile != null) {
            prefs.put(
                    "lastUsedDirectory",
                    _fileChooser.getCurrentDirectory().getAbsolutePath()
            );
        }
        List errorMessages = new ArrayList();
        List<YSpecificationID> newSpecIDs = null;
        try {
            String specStr = StringUtil.fileToString(selectedFile);
            newSpecIDs = _engineManagement.addSpecifications(specStr, _ignoreErrors, errorMessages);
        } catch (Exception e) {
            logError("Failure to load specification", e);
            return;
        }

        if (newSpecIDs.size() == 0 || errorMessages.size() > 0) {
            StringBuffer errorMessageStr = new StringBuffer();

            Iterator iterator = errorMessages.iterator();
            while (iterator.hasNext()) {
                YVerificationMessage message = (YVerificationMessage) iterator.next();
                errorMessageStr.append(
                        "\r\n" + message.getStatus() +
                        ": " + message.getMessage());
            }
            JOptionPane.showMessageDialog(this,
                    "The workflow you loaded contains: " + errorMessageStr,
                    "Error Loading Workflow",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (YVerificationMessage.containsNoErrors(errorMessages)) {
            for (YSpecificationID specID : newSpecIDs)
            {
                spec = _engineManagement.getSpecification(specID);
                _loadedSpecificationsTableModel.addRow(specID, new Object[]{specID, spec.getRootNet().getID()});
            }
        }

        /**
         * AJH: Create a directory to hold the test data files if required
         */
        try
        {
            File dataDir = new File(selectedFile.getParentFile().getAbsolutePath() + File.separator + spec.getSpecificationID().getSpecName() + "_DAT");
            if (!dataDir.exists())
            {
                if (!dataDir.mkdir())
                {
                    throw new IOException("Failure to create RTE_TOOLKIT directory - " + dataDir.getAbsolutePath());
    }

                logger.info("RTE_TOOKIT directory created - " + dataDir.getAbsolutePath());
            }
            else
            {
                logger.info("Reading test data files from " + dataDir.getAbsolutePath());
            }
            specTestDataDirs.put(spec.getSpecificationID().getSpecName(), dataDir);
        }
        catch (IOException e)
        {
             logError("Failure to create RTE_TOOLKIT directory", e);
        }
    }

    public static File getSpecTestDataDirectory(String specID)
    {
        return specTestDataDirs.get(specID);
    }


    private void attemptToCreateWorklist(String userName) throws YPersistenceException, YAuthenticationException {
        Set users = _engineManagement.getUsers();
        User user = null;
        boolean userfound = false;
        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            user = (User) iterator.next();
            if (user.getUserID().equals(userName)) {
                userfound = true;
            }
        }
        if (!userfound) {
            user = UserList.getInstance().addUser(userName, "password", false);
            _engineManagement.storeObject(user);
        } else if (userName == null) {
            return;//do nothing - user must have chosen cancel
        }
        {
            YWorklistModel model = new YWorklistModel(userName, _frame);
            YWorklistGUI worklistGUI = model.getGUI();
            _activeWorklistsTableModel.addRow(userName, new Object[]{userName});

            _tabbedGUI.addWorklistPanel(userName, worklistGUI);
        }
    }


    private void saveXML(int selectedRow) {
        if (selectedRow >= 0) {

            try {
                YSpecificationID specID = (YSpecificationID) _loadedSpecificationsTable.getValueAt(selectedRow, 0);
                YSpecification spec = _engineManagement.getSpecification(specID);
                String specAsXML = YMarshal.marshal(spec);
                int returnVal = _fileChooser.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File toWriteXMLHere = _fileChooser.getSelectedFile();
                    if (toWriteXMLHere.exists()) {
                        int optionChosen = JOptionPane.showConfirmDialog(
                                this,
                                "The file chosen already exists.\nSave anyway?",
                                "User Warning",
                                JOptionPane.ERROR_MESSAGE);
                        if (optionChosen == JOptionPane.CANCEL_OPTION) {
                            saveXML(selectedRow);
                        } else {
                            toWriteXMLHere.delete();
                        }
                    }
                    try {
                        FileWriter fileWriter = new FileWriter(toWriteXMLHere);
                        fileWriter.write(specAsXML);
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                logError("Failure to save specification XML", e);
            }
        }
    }


    private void startCase(int selectedRow) {
        if (selectedRow >= 0) {
            YSpecificationID specID = (YSpecificationID) _loadedSpecificationsTable.getValueAt(selectedRow, 0);
            try {
                //todo AJH - IS this where we pass in the input params ???????
//                String caseIDStr = _engineClient.startCase(specID).toString();
                /**
                 * AJH: If we have a data file defined for the initial net data, get the XML and pass into the
                 * case launcher
                 */
                File initCaseData = new File(getSpecTestDataDirectory(specID.getSpecName()), "starting_net.xml");
                if (initCaseData.exists())
                {
                    logger.info("Loading initial case net data from " + initCaseData.getAbsolutePath());
                    _engineClient.launchCase("",specID.getSpecName(), StringUtil.fileToString(initCaseData), null);
                }
                else
                {
                    _engineClient.launchCase("", specID.getSpecName(), "", null);
                }
            } catch (Exception e) {
                logError("Failure to start case", e);
            }
        }
    }



    public void addCase(String specID, String caseIDStr) {
        _activeCasesTableModel.addRow(caseIDStr, new Object[]{specID, caseIDStr});
    }


    private JFileChooser setUpChooser() {
        Preferences prefs =
                Preferences.userNodeForPackage(YAdminGUI.class);

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if (extension != null) {
                    if (extension.equals("xml") ||
                            extension.equals("yawl") ||
                            extension.equals("new") ||
                            extension.equals("old")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "YAWL Files";
            }

            private String getExtension(File f) {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                return ext;
            }
        });
        chooser.setCurrentDirectory(
                new File(prefs.get("lastUsedDirectory",
                        System.getProperty("user.dir"))
                )
        );
        return chooser;
    }


    public void removeCase(String caseIDStr) {
        if (_activeCasesTableModel.getRowMap().containsKey(caseIDStr)) {
            _activeCasesTableModel.removeRow(caseIDStr);
            _activeCasesTableModel.fireTableDataChanged();
        }
    }

    public void addWorkItem(YAWLServiceReference yawlService, YWorkItem item)
    {
        //todo - Perhaps refresh the tasklist panels
    }

    public void removeWorkItem(YAWLServiceReference yawlService, YWorkItem item)
    {
        //todo - Perhaps refresh the tasklist panels
    }


    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent e) {
//        if(e.getSource() == _ignoreErrorsCheckBox){
//            if(e.getStateChange() == ItemEvent.SELECTED){
//                _ignoreErrors = true;
//            }
//            else if(e.getStateChange() == ItemEvent.DESELECTED){
//                _ignoreErrors = false;
//            }
//        } else if(e.getSource() == _oldFormatCheckBox){
//            if(e.getStateChange() == ItemEvent.SELECTED){
//                _oldFormat = true;
//            }
//            else if(e.getStateChange() == ItemEvent.DESELECTED){
//                _oldFormat = false;
//            }
//        }
    }


    private void quickyLoad() {
        File newFile = new File(System.getProperty("user.dir")
                + File.separator + "test" + File.separator + "org"
                + File.separator + "org" + File.separator + "yawlfoundation"
                + File.separator + "yawl" + File.separator + "engine"
                + File.separator + "ImproperCompletion.xml");
        attemptToLoadSpecificationFile(newFile);
        startCase(0);

        try {
            attemptToCreateWorklist("admin");
        } catch (Exception e) {
            logError("Failed to add user 'admin'", e);
        }
    }


    private class YInspectStateDialog extends JDialog {
        public YInspectStateDialog(JFrame parent, String textToDisplay) {
            super(parent, "Inspect State", false);
            Container c = this.getContentPane();
            JTextPane statePane = new JTextPane();
            statePane.setText(textToDisplay);
            statePane.setEditable(false);
            JScrollPane scroller = new JScrollPane(statePane);
            c.add(scroller);
            repaint();
            this.setSize(500, 300);
            show();
        }
    }

    public static void main(String[] args) {
        boolean journalise = false;
        boolean generateUIMetaData = false;
        for (int i = 0; i < args.length; i++) {
            logger.debug(args[i]);
            if (args[i].equalsIgnoreCase("quickyLoad")) {
                _quickyLoad = true;
            } else if (args[i].equalsIgnoreCase("networked")) {
                _networked = true;
            }
            // If we have a -p flag, assuming a journalising engine
            if (args[i].equalsIgnoreCase("-p")) {
                journalise = true;
                logger.info("Running engine in persistent mode");
            }

            // If we have a -uim flag, assuming generation of UI metadata
            if (args[i].equalsIgnoreCase("-uim")) {
                generateUIMetaData = true;
                logger.info("Engine will generate UI metadata");
            }

        }

        new YAdminGUI(null, null, null, journalise, generateUIMetaData);
    }    

}
