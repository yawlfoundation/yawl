/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.swingWorklist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.engine.gui.YAdminGUI;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagement;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClient;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.swingWorklist.util.ParamsDefinitions;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 15/05/2003
 * Time: 16:32:43
 * 
 */
public class YWorklistModel {
    private static final Logger logger = LogManager.getLogger(YWorklistModel.class);
    private YWorklistTableModel _availableWork;
    private YWorklistTableModel _myActiveTasks;
    private Vector inSequenceWorkitemIDs = new Vector();
    private DateFormat _formatter;
    private static ParamsDefinitions _paramsDefinitions = new ParamsDefinitions();

    // Reference to engine's management interface (used for accessing specifcation definiions)
    private static InterfaceAManagement _engineManagement = YEngine.getInstance();

    // Reference to engine's client interface (used for worklist driving)
    private static InterfaceBClient _engineClient = YEngine.getInstance();
    private static String xmlCommentHeader = "<!-- Test data loaded from -";

    private String _username;
    private YWorklistGUI _gui;
    private JFrame _frame;


    public YWorklistModel(String userName, JFrame frame) {
        _frame = frame;
        _username = userName;
        _availableWork = new YWorklistTableModel(new String[]{
            "Case ID", "Task ID", "Description", "Status", "Enablement Time", "Firing Time","Seq"});
        _myActiveTasks = new YWorklistTableModel(new String[]{
            "Case ID", "Task ID", "Description", "Enablement Time", "Firing Time", "Start Time", "Seq"});
        _formatter = new SimpleDateFormat("MMM dd H:mm:ss");
        _gui = new YWorklistGUI(userName, this, frame);
    }



    //####################################################################################
    //                    INTERFACE TO LOCAL WORKLIST
    //####################################################################################

    private void removeUnstartedWorkItem(String caseIDStr, String taskID) {
        _availableWork.removeRow(caseIDStr + taskID);
    }


    private void addEnabledWorkItem(YWorkItem workItem, boolean inSequence) {

        logger.debug("addEnabledWorkItem: {}", workItem.getIDString());

        String caseIDStr = workItem.getCaseID().toString();
        String taskID = workItem.getTaskID();
        YSpecificationID specificationID = workItem.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getID();
        if (null == taskDescription) {
            taskDescription = taskID;
        }
        _availableWork.addRow(caseIDStr + taskID,
                new Object[]{caseIDStr, taskID, taskDescription, "Enabled",
                                           _formatter.format(workItem.getEnablementTime()), "",
                                           inSequence ? "Y" : "N"});
    }


    private void addFiredWorkItem(YWorkItem workItem, boolean inSequence) {
        String caseIDStr = workItem.getCaseID().toString();
        String taskID = workItem.getTaskID();
        YSpecificationID specificationID = workItem.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getID();
        if (null == taskDescription) {
            taskDescription = taskID;
        }
        _availableWork.addRow(caseIDStr + taskID,
                new Object[]{caseIDStr,
                             taskID,
                             taskDescription,
                             "Fired",
                             _formatter.format(workItem.getEnablementTime()),
                                           _formatter.format(workItem.getFiringTime()),
                                           inSequence ? "Y" : "N"});
    }


    private void removeStartedItem(String caseIDStr, String taskID) {
        _myActiveTasks.removeRow(caseIDStr + taskID);
    }


    private void addStartedWorkItem(YWorkItem item, boolean inSequence) {
        String caseIDStr = item.getCaseID().toString();
        String taskID = item.getTaskID();
        YSpecificationID specificationID = item.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getID();
        if (null == taskDescription) {
            taskDescription = taskID;
        }
        boolean allowsDynamicInstanceCreation = true;
        try {
            _engineClient.checkElegibilityToAddInstances(item.getIDString());
        } catch (YAWLException e) {
            allowsDynamicInstanceCreation = false;
        }
        _myActiveTasks.addRow(caseIDStr + taskID,
                new Object[]{
                    caseIDStr,
                    taskID,
                    taskDescription,
                    _formatter.format(item.getEnablementTime()),
                    _formatter.format(item.getFiringTime()),
                    _formatter.format(item.getStartTime()),
                                  inSequence ? "Y" : "N",
                                  new Boolean(allowsDynamicInstanceCreation),
                    item.getDataString(),
                    getOutputSkeletonXML(caseIDStr, taskID)
                });
    }


    Object[] getActiveTableData(String caseIDStr, String taskIDStr) {
        return (Object[]) _myActiveTasks._rows.get(caseIDStr + taskIDStr);
    }

    void setActiveTableData(Object[] data) {
        String workItemID;
        workItemID = (String) data[0] + data[1];
        _myActiveTasks._rows.put(workItemID, data);
    }





    //######################################################################################
    //                          INTERFACE TO GUI
    //######################################################################################

    // MUTATORS ############################################################################
    public void applyForWorkItem(String caseID, String taskID) throws YPersistenceException {
        Set workItems = _engineClient.getAvailableWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.startWorkItem(item, null);

                } catch (YStateException e) {
                    e.printStackTrace();
                    reportGeneralProblem(e);
                } catch (YDataStateException e) {
                    e.printStackTrace();
                    new SpecificationQueryProcessingValidationErrorBox(
                            _frame,
                            item,
                            e);
                } catch (YAWLException e) {
                    e.printStackTrace();
                    reportGeneralProblem(e);
                }
            }
        }
    }


    public void createNewInstance(String caseID, String taskID, String newInstanceData) throws YPersistenceException {
        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.createNewInstance(item, newInstanceData);
                } catch (YStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean allowsDynamicInstanceCreation(String caseID, String taskID) {
        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.checkElegibilityToAddInstances(item.getIDString());
                    return true;
                } catch (YStateException e) {
                    return false;
                }
            }
        }
        return false;
    }


    public void attemptToFinishActiveJob(String caseID, String taskID) {
        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    String outputData = _myActiveTasks.getOutputData(caseID, taskID);

                    /**
                     * AJH: Write the output data into test data file
                     */
                    File testDataDir =  YAdminGUI.getSpecTestDataDirectory(item.getSpecificationID().getKey());
                    File taskInputData = new File(testDataDir, taskID + ".xml");
                    if (!taskInputData.exists())
                    {
                        logger.info("Creating task data file - " + taskInputData.getAbsolutePath());
                        taskInputData.createNewFile();
                    }
                    StringUtil.stringToFile(taskInputData.getAbsolutePath(), outputData);

                    _engineClient.completeWorkItem(item, outputData, null,
                            WorkItemCompletion.Normal);
                } catch (YDataStateException e) {
                    String errors = e.getMessage();
                    if (errors.indexOf("FAILED TO VALIDATE AGAINST SCHEMA =") != -1) {
                        System.out.println(e.getMessage());
                        new SpecificationQueryProcessingValidationErrorBox(_frame, item, e);
                    } else {
                        new UserInputValidationErrorBox(_frame, item, e);
                        System.out.println(e.getMessage());
                    }
                } catch (Exception e) {
                    //todo AJH - Create defalut skeleton at this point????
                    reportGeneralProblem(e);
                }
            }
        }
    }

    /**
     * Return the XML test data for a specified task
     *
     * @param caseID
     * @param taskID
     * @return testData
     */

    public String getTaskTestData(String caseID, String taskID)
    {
        String testData = null;
        File taskInputData = null;

        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                item.getTaskID().equals(taskID)) {
                try {
                    File testDataDir =  YAdminGUI.getSpecTestDataDirectory(item.getSpecificationID().getKey());
                    taskInputData = new File(testDataDir, taskID + ".xml");
                    if (taskInputData.exists())
                    {
                        testData = StringUtil.fileToString(taskInputData);
                    }
                } catch (Exception e) {
                    reportGeneralProblem(e);
                }
            }
        }

        if (testData == null)
        {
            return testData;
        }
        else if (testData.startsWith(xmlCommentHeader))
        {
            return testData;
        }
        else
        {
            return xmlCommentHeader + taskInputData.getName() + " -->\n" + testData;
        }
    }

    private void reportGeneralProblem(Exception e) {
        _gui.reportGeneralProblem(e);
    }


    public void rollBackActiveTask(String caseID, String taskID)
            throws YPersistenceException, YLogException {
        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.rollbackWorkItem(item.getIDString());
                } catch (YStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void refreshLists(String userName) {
        //clear the models
        List keys = new ArrayList();
        keys.addAll(_availableWork._rows.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String id = (String) keys.get(i);
            _availableWork.removeRow(id);
        }
        keys.clear();
        keys.addAll(_myActiveTasks._rows.keySet());
        for (int i = 0; i < keys.size(); i++) {
            String id = (String) keys.get(i);
            _myActiveTasks.removeRow(id);
        }
        //now update them
        updateSelf();
    }

    private void updateSelf() {
        boolean inSequence;
        for (YWorkItem item : _engineClient.getAvailableWorkItems()) {

            inSequence = inSequenceWorkitemIDs.contains(item.getTaskID());

            if (item.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                addEnabledWorkItem(item, inSequence);
            }
            else if (item.getStatus().equals(YWorkItemStatus.statusFired)) {
                addFiredWorkItem(item, inSequence);
            }
        }
        for (YWorkItem item :_engineClient.getAllWorkItems()) {

            inSequence = inSequenceWorkitemIDs.contains(item.getTaskID());

            if (item.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                    addStartedWorkItem(item, inSequence);
            }
            if (_paramsDefinitions.getParamsForTask(item.getTaskID()) == null) {
                YTask task = _engineClient.getTaskDefinition(item.getSpecificationID(),
                        item.getTaskID());
                String paramsAsXML = task.getInformation();
                TaskInformation taskInfo = Marshaller.unmarshalTaskInformation(paramsAsXML);
                YParametersSchema paramsForTask = taskInfo.getParamSchema();
                _paramsDefinitions.setParamsForTask(item.getTaskID(), paramsForTask);
            }
        }
    }


    // ACCESSORS ###########################################################################
    public YWorklistTableModel getAvaliableModel() {
        return this._availableWork;
    }


    public YWorklistTableModel getActiveTasksModel() {
        return this._myActiveTasks;
    }


    public String getOutputSkeletonXML(String caseID, String taskID) {
        YParametersSchema params = _paramsDefinitions.getParamsForTask(taskID);
        YWorkItem item = _engineClient.getWorkItem(caseID + ":" + taskID);
        YSpecificationID specID = item.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specID, item.getTaskID());
        return Marshaller.getOutputParamsInXML(
                params,
                task.getDecompositionPrototype().getRootDataElementName());
    }

    public List validateData() {
        List validationMessages = new Vector();

        return validationMessages;
    }


    public YParameter getMIUniqueParam(String taskID) {
        YParametersSchema p = _paramsDefinitions.getParamsForTask(taskID);
        if (p == null) {
            return null;
        }
        return p.getFormalInputParam();
    }

    public YWorklistGUI getGUI() {
        return _gui;
    }
}


class UserInputValidationErrorBox extends JDialog implements ActionListener {
    private String _okCommand = "  OK  ";

    UserInputValidationErrorBox(Frame parent, YWorkItem item, YDataStateException e) {
        super(parent, "Problem with your input data");
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(YAdminGUI._apiColour);
        p.add(createTopPanel(item), BorderLayout.NORTH);
        p.add(createCentrePanel(e), BorderLayout.CENTER);
        c.add(p, BorderLayout.CENTER);
        c.add(createBottomPanel(), BorderLayout.SOUTH);
        c.setBackground(YAdminGUI._apiColour);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                storeData();
                this_windowClosing();
            }
        });

        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
        setSize(new Double(screenWidth * 0.8).intValue(), new Double(screenHeight * 0.8).intValue());

        Dimension labelSize = this.getSize();
        setLocation(screenWidth.intValue() / 2 - (labelSize.width / 2),
                    screenHeight.intValue() / 2 - (labelSize.height / 2));
        show();
    }

    private void this_windowClosing() {
        dispose();
    }

    private void storeData() {

    }

    private JPanel createTopPanel(YWorkItem item) {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(YAdminGUI._apiColour);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea explanatoryText = new JTextArea();
        explanatoryText.setText(
                "The data you submitted for this work item was \n" +
                "validated against a schema (see below).  For some reason the\n" +
                "this data did not succeed in passing the constrainst set\n" +
                "inside the schema.\n" +
                "Usage Note: If this is causing problems try using the Web server\n" +
                "version of YAWL, which supports automatic forms generation.\n" +
                "Otherwise you could copy the schema from this page and use it\n " +
                "to create a valid output document using an XML development tool.");
        explanatoryText.setEditable(false);
        explanatoryText.setFont(new Font("Arial", Font.BOLD, 12));
        explanatoryText.setForeground(Color.DARK_GRAY);
        explanatoryText.setBackground(YAdminGUI._apiColour);
        leftPanel.add(explanatoryText);
        topPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridLayout(4, 2));

        rightPanel.setBackground(YAdminGUI._apiColour);
        rightPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Work Item Details"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        YTask task = YEngine.getInstance().getTaskDefinition(
                item.getSpecificationID(),
                item.getTaskID());
        String taskName = task.getName();

        String[] text = {
            item.getSpecificationID().toString(),
            taskName,
            item.getIDString(),
            item.getStartTimeStr()};
        String[] labels = {
            "Specification ID",
            "Task Name",
            "WorkItem ID",
            "Task Started"};
        for (int i = 0; i < text.length; i++) {
            String s = text[i];
            rightPanel.add(new JLabel(labels[i]));
            JTextField t = new JTextField(s);
            t.setEditable(false);
            rightPanel.add(t);
        }
        topPanel.add(rightPanel, BorderLayout.CENTER);
        return topPanel;
    }

    private JPanel createCentrePanel(YDataStateException exception) {
        XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
        JPanel centrePanel = new JPanel(new GridLayout(1, 2));

        JPanel schemaPanel = new JPanel(new BorderLayout());
        schemaPanel.setBackground(YAdminGUI._apiColour);
        schemaPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(),
                                "Schema for completing task"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextPane schemaTextPane = new JTextPane();
        schemaTextPane.setContentType("text/xml");
        schemaTextPane.setFont(new Font("courier", Font.PLAIN, 12));
        String schemaXML = xmlOut.outputString(exception.getSchema());

        /**
         * AJH: Trap various XML format errors gracefully.
         */
        try
        {
            String xml = schemaXML.substring(
                    schemaXML.indexOf('<'),
                    schemaXML.lastIndexOf("</xsd:schema>") + 13);
            schemaTextPane.setText(xml);
        }
        catch (Exception e)
        {
            schemaTextPane.setText(schemaXML);
        }


        schemaTextPane.setEditable(false);
        schemaTextPane.setBackground(Color.LIGHT_GRAY);
        JPanel noWrapPanel = new JPanel();
        noWrapPanel.setLayout(new BorderLayout());
        noWrapPanel.add(schemaTextPane);
        schemaPanel.add(new JScrollPane(noWrapPanel));

        JPanel rightPanel = new JPanel(new GridLayout(2, 1));

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(YAdminGUI._apiColour);
        dataPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "The data that failed to validate"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextPane dataTextPane = new JTextPane();
        dataTextPane.setContentType("text/xml");
        dataTextPane.setFont(new Font("courier", Font.PLAIN, 12));
        String data = xmlOut.outputString(exception.get_dataInput());

        /**
         * AJH: Trap various XML format errors gracefully.
         */
        try
        {
            String temp = data.substring(
                    data.lastIndexOf("<?xml"),
                    data.lastIndexOf('>'));
            dataTextPane.setText(temp);
        }
        catch (Exception e)
        {
            dataTextPane.setText(data);
        }

        dataTextPane.setEditable(false);
        dataTextPane.setBackground(Color.LIGHT_GRAY);
        JPanel noWrapPanel2 = new JPanel();
        noWrapPanel2.setLayout(new BorderLayout());
        noWrapPanel2.add(dataTextPane);
        dataPanel.add(new JScrollPane(noWrapPanel2));

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(YAdminGUI._apiColour);
        errorPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "The error message from validation engine"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextPane errorTextPane = new JTextPane();
        errorTextPane.setContentType("text/plain");
        errorTextPane.setFont(new Font("courier", Font.PLAIN, 12));

        /**
         * AJH: Trap various XML format errors gracefully.
         */
        try
        {
            String error = schemaXML.substring(
                    schemaXML.lastIndexOf("ERRORS ="),
                    schemaXML.length());
            errorTextPane.setText(error);
        }
        catch (Exception e)
        {
            // null action !
        }


        errorTextPane.setText(exception.getErrors());

        errorTextPane.setEditable(false);
        errorTextPane.setBackground(Color.LIGHT_GRAY);
        JPanel noWrapPanel3 = new JPanel();
        noWrapPanel3.setLayout(new BorderLayout());
        noWrapPanel3.add(errorTextPane);
        errorPanel.add(new JScrollPane(noWrapPanel3));

        rightPanel.add(dataPanel);
        rightPanel.add(errorPanel);
        centrePanel.add(schemaPanel, BorderLayout.NORTH);
        centrePanel.add(rightPanel, BorderLayout.CENTER);
        return centrePanel;
    }


    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(YAdminGUI._apiColour);

        JButton releaseButton = new JButton(_okCommand);
        releaseButton.setToolTipText("OK");
        releaseButton.addActionListener(this);
        JPanel p = new JPanel();
        p.setBackground(YAdminGUI._apiColour);
        p.add(releaseButton);
        bottomPanel.add(p, BorderLayout.EAST);
        return bottomPanel;
    }

    public void actionPerformed(ActionEvent e) {
        storeData();
        this_windowClosing();
    }
}

class SpecificationQueryProcessingValidationErrorBox extends JDialog implements ActionListener {
    private String _okCommand = "  OK  ";

    SpecificationQueryProcessingValidationErrorBox(Frame parent, YWorkItem item, YDataStateException message) {
        super(parent, "Runtime Problem with Process Specification detected");
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(YAdminGUI._apiColour);
        p.add(createCentrePanel(message), BorderLayout.CENTER);
        c.add(p, BorderLayout.CENTER);
        c.add(createBottomPanel(), BorderLayout.SOUTH);
        c.setBackground(YAdminGUI._apiColour);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                storeData();
                this_windowClosing();
            }
        });
        //setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
//        setSize(new Double((screenWidth - guiSize.width) * 2).intValue(), new Double((screenHeight - guiSize.height) * 2).intValue());
//        setLocation(((screenWidth.intValue() - guiSize.width) / 2),0);
//        pack();
        Dimension labelSize = this.getSize();
        setLocation(screenWidth.intValue() / 2 - (labelSize.width / 2),
                    screenHeight.intValue() / 2 - (labelSize.height / 2));
        show();
    }

    private void this_windowClosing() {
        dispose();
    }

    private void storeData() {

    }

    private JPanel createCentrePanel(YDataStateException exception) {
        JPanel centrePanel = new JPanel(new GridLayout(1, 2));

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setBackground(YAdminGUI._apiColour);
        msgPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Schema for completing task"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextPane msgTextPane = new JTextPane();
        msgTextPane.setContentType("text/plain");
        msgTextPane.setFont(new Font("courier", Font.PLAIN, 12));
        msgTextPane.setForeground(Color.RED);

        msgTextPane.setText(exception.getMessage());
        msgTextPane.setEditable(false);
        msgTextPane.setBackground(Color.LIGHT_GRAY);
        JPanel noWrapPanel = new JPanel();
        noWrapPanel.setLayout(new BorderLayout());
        noWrapPanel.add(msgTextPane);
        msgPanel.add(new JScrollPane(noWrapPanel));

        centrePanel.add(msgPanel, BorderLayout.NORTH);
        return centrePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(YAdminGUI._apiColour);

        JButton releaseButton = new JButton(_okCommand);
        releaseButton.setToolTipText("OK");
        releaseButton.addActionListener(this);
        JPanel p = new JPanel();
        p.setBackground(YAdminGUI._apiColour);
        p.add(releaseButton);
        bottomPanel.add(p, BorderLayout.EAST);
        return bottomPanel;
    }

    public void actionPerformed(ActionEvent e) {
        storeData();
        this_windowClosing();
    }
}