/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.swingWorklist;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.InterfaceAManagement;
import au.edu.qut.yawl.engine.InterfaceBClient;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.gui.YAdminGUI;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.swingWorklist.util.ParamsDefinitions;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.YParametersSchema;
import org.apache.log4j.Logger;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private Logger logger = Logger.getLogger(this.getClass());
    private YWorklistTableModel _availableWork;
    private YWorklistTableModel _myActiveTasks;
    private DateFormat _formatter;
    private static ParamsDefinitions _paramsDefinitions = new ParamsDefinitions();

    // Reference to engine's management interface (used for accessing specifcation definiions)
    private static InterfaceAManagement _engineManagement =  EngineFactory.createYEngine();

    // Reference to engine's client interface (used for worklist driving)
    private static InterfaceBClient _engineClient = EngineFactory.createYEngine();

    private String _username;
    private YWorklistGUI _gui;
    private JFrame _frame;


    public YWorklistModel(String userName, JFrame frame) {
        _frame = frame;
        _username = userName;
        _availableWork = new YWorklistTableModel(new String[]{
            "Case ID", "Task ID", "Description", "Status", "Enablement Time", "Firing Time"});
        _myActiveTasks = new YWorklistTableModel(new String[]{
            "Case ID", "Task ID", "Description", "Enablement Time", "Firing Time", "Start Time"});
        _formatter = new SimpleDateFormat("MMM dd H:mm:ss");
        _gui = new YWorklistGUI(userName, this, frame);
    }



    //####################################################################################
    //                    INTERFACE TO LOCAL WORKLIST
    //####################################################################################



    private void addEnabledWorkItem(YWorkItem workItem) {

        Logger.getLogger(this.getClass()).debug("addEnabledWorkItem: " + workItem.getIDString());

        String caseIDStr = workItem.getCaseID().toString();
        String taskID = workItem.getTaskID();
        String specificationID = workItem.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getId();
        if (null == taskDescription) {
            taskDescription = taskID;
        }
        _availableWork.addRow(caseIDStr + taskID,
                new Object[]{caseIDStr, taskID, taskDescription, "Enabled",
                             _formatter.format(workItem.getEnablementTime()), ""});
    }


    private void addFiredWorkItem(YWorkItem workItem) {
        String caseIDStr = workItem.getCaseID().toString();
        String taskID = workItem.getTaskID();
        String specificationID = workItem.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getId();
        if (null == taskDescription) {
            taskDescription = taskID;
        }
        _availableWork.addRow(caseIDStr + taskID,
                new Object[]{caseIDStr,
                             taskID,
                             taskDescription,
                             "Fired",
                             _formatter.format(workItem.getEnablementTime()),
                             _formatter.format(workItem.getFiringTime())});
    }




    private void addStartedWorkItem(YWorkItem item) {
        String caseIDStr = item.getCaseID().toString();
        String taskID = item.getTaskID();
        String specificationID = item.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specificationID, taskID);
        String taskDescription = task.getDecompositionPrototype().getId();
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
                    (allowsDynamicInstanceCreation) ? Boolean.TRUE : Boolean.FALSE,
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
    public void applyForWorkItem(String caseID, String taskID) throws YSchemaBuildingException, YPersistenceException {
        Set workItems = _engineClient.getAvailableWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.startWorkItem(item, _username);

                } catch (YStateException e) {
                    logger.error("State Exception", e);
                    reportGeneralProblem(e);
                } catch (YDataStateException e) {
                    logger.error("Bad Specification");e.printStackTrace();
                    new SpecificationQueryProcessingValidationErrorBox(
                            _frame,
                            item,
                            e);
                } catch (YQueryException e) {
                    logger.error("YQueryException",e);
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
                    _engineClient.completeWorkItem(item, outputData, false);
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
                    reportGeneralProblem(e);
                }
            }
        }
    }

    private void reportGeneralProblem(Exception e) {
        _gui.reportGeneralProblem(e);
    }


    public void rollBackActiveTask(String caseID, String taskID) throws YPersistenceException {
        Set workItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getCaseID().toString().equals(caseID) &&
                    item.getTaskID().equals(taskID)) {
                try {
                    _engineClient.suspendWorkItem(item.getIDString(), _username);
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
//        _worklistManager.informRemotePartnerOfcurrentState(userName);
    }

    private void updateSelf() {
        Set availableWorkItems = _engineClient.getAvailableWorkItems();
        for (Iterator iterator = availableWorkItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getStatus().equals(YWorkItem.Status.Enabled)) {
                addEnabledWorkItem(item);
            } else if (item.getStatus().equals(YWorkItem.Status.Fired)) {
                addFiredWorkItem(item);
            }
        }
        Set allWorkItems = _engineClient.getAllWorkItems();
        for (Iterator iterator = allWorkItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if (item.getStatus().equals(YWorkItem.Status.Executing)) {
                if (item.getUserWhoIsExecutingThisItem().equals(_username)) {
                    addStartedWorkItem(item);
                }
            }
            if (_paramsDefinitions.getParamsForTask(item.getTaskID()) == null) {
                YTask task = _engineClient.getTaskDefinition(item.getSpecificationID(), item.getTaskID());
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
        String specID = item.getSpecificationID();
        YTask task = _engineClient.getTaskDefinition(specID, item.getTaskID());
        return Marshaller.getOutputParamsInXML(
                params,
                task.getDecompositionPrototype().getRootDataElementName());
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
        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        screenSize.setSize(screenSize.getWidth(), screenSize.getHeight() - insets.bottom);
        setSize(screenSize);
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
        YTask task =  EngineFactory.createYEngine().getTaskDefinition(
                item.getSpecificationID(),
                item.getTaskID());
        String taskName = task.getName();

        String[] text = {
            item.getSpecificationID(),
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
        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = this.getSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
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
