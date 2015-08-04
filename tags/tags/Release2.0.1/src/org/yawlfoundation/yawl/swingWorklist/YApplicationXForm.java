/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.swingWorklist;

import org.yawlfoundation.yawl.engine.gui.YAdminGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/10/2003
 * Time: 17:21:09
 * 
 */
public class YApplicationXForm extends JDialog implements ActionListener {
    private String _caseID;
    private String _taskID;
    private String _description;
    private String _enableMentTime;
    private String _firingTimeTime;
    private String _startTime;
    private String _sequence;
    private boolean _allowsInstanceCreation;
    private String _inputData;
    private String _outputData;
    private String _skeletonOutputData;
    private JTextPane _outputDataTextPane;
    private String _keepCommand = "Keep for Later";
    private String _releaseCommand = "Release WorkItem";
    private String _resetCommand = "Set default XML";
    private YWorklistModel _model;
    private YWorklistGUI _worklistGUI;


    YApplicationXForm(JFrame frame, YWorklistGUI worklistGUI, Object[] workItemData, final YWorklistModel model) {
        super(frame, "Form: " + workItemData[2], true);
        _worklistGUI = worklistGUI;
        _caseID = (String) workItemData[0];
        _taskID = (String) workItemData[1];
        _description = (String) workItemData[2];
        _enableMentTime = (String) workItemData[3];
        _firingTimeTime = (String) workItemData[4];
        _startTime = (String) workItemData[5];
        _sequence = (String) workItemData[6];
        _allowsInstanceCreation = ((Boolean) workItemData[7]).booleanValue();
        _inputData = (String) workItemData[8];


        /**
         * AJH: Pre-load output data from test data file
         */
        _skeletonOutputData = model.getOutputSkeletonXML(_caseID, _taskID);
        _outputData = model.getTaskTestData(_caseID, _taskID);

        if (_outputData == null)
        {
            if (workItemData.length > 9) {
                _outputData = (String) workItemData[9];
        } else {
                _outputData = _skeletonOutputData;
        }
        }

        _model = model;
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(YAdminGUI._apiColour);
        p.add(createTopPanel(), BorderLayout.NORTH);
        p.add(createDataPanel(), BorderLayout.CENTER);
        c.add(p, BorderLayout.CENTER);
        c.add(createBottomPanel(), BorderLayout.SOUTH);
        c.setBackground(YAdminGUI._apiColour);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                storeData();
                this_windowClosing();
            }
        });

        /**
         * AJH: Changed to force display onto primary screen in a dual-head X environment
         */
//        Dimension guiSize = getPreferredSize();
//        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
//        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
//        setSize(new Double((screenWidth - guiSize.width) * 2).intValue(), new Double((screenHeight - guiSize.height) * 2).intValue());
//        setLocation(((screenWidth.intValue() - guiSize.width) / 2),0);
//        pack();
        setSize(1000, 800);
        setLocationRelativeTo(null);
        show();
    }

    private void storeData() {
        _model.setActiveTableData(new Object[]{_caseID, _taskID, _description,
                                               _enableMentTime, _firingTimeTime,
                                               _startTime, _sequence, new Boolean(_allowsInstanceCreation),
                                               _inputData, _outputDataTextPane.getText()});
    }

    private void this_windowClosing() {
        dispose();
    }


    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(YAdminGUI._apiColour);
        JPanel rightPanel = new JPanel(new GridLayout(5, 2));
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(YAdminGUI._apiColour);
        rightPanel.setBackground(YAdminGUI._apiColour);
        rightPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Work Item Attributes"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        String[] text = {_caseID, _taskID, _description, _startTime, _sequence};
        String[] labels = {"Case ID", "Task ID", "Task Description", "Task Started", "In Sequence"};
        for (int i = 0; i < text.length; i++) {
            String s = text[i];
            rightPanel.add(new JLabel(labels[i]));
            JTextField t = new JTextField(s);
            t.setEditable(false);
            rightPanel.add(t);
        }
        leftPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextArea explanatoryText = new JTextArea();
        explanatoryText.setText("To the right is a list of the\n" +
                "attributes of the relevant work item.\n" +
                "Below is the data for this item that\n" +
                "can be read/updated.");
        explanatoryText.setEditable(false);
        explanatoryText.setFont(new Font("Arial", Font.BOLD, 12));
        explanatoryText.setForeground(Color.GRAY);
        explanatoryText.setBackground(YAdminGUI._apiColour);
        leftPanel.add(explanatoryText);
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.CENTER);
        return topPanel;
    }


    private JPanel createDataPanel() {
        JPanel dataPanel = new JPanel(new GridLayout(1, 2));
        dataPanel.setBackground(YAdminGUI._apiColour);
        dataPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Work Item Data"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JTextPane inputTextPane = new JTextPane();
        inputTextPane.setContentType("text/xml");
        inputTextPane.setFont(new Font("courier", Font.PLAIN, 12));
        inputTextPane.setText(_inputData);

        inputTextPane.setEditable(false);
        inputTextPane.setBackground(Color.LIGHT_GRAY);
        dataPanel.add(new JScrollPane(inputTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

        _outputDataTextPane = new JTextPane();
        _outputDataTextPane.setContentType("text/xml");
        _outputDataTextPane.setEditable(true);
        _outputDataTextPane.setFont(new Font("courier", Font.PLAIN, 12));
        _outputDataTextPane.setText(_outputData);
        dataPanel.add(
                new JScrollPane(
                        _outputDataTextPane,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        return dataPanel;
    }


    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(YAdminGUI._apiColour);
        JButton keepButton = new JButton(_keepCommand);
        keepButton.setToolTipText("Save your work and continue later.");
        keepButton.addActionListener(this);
        JButton releaseButton = new JButton(_releaseCommand);
        releaseButton.setToolTipText("Send completed work item back to YAWL engine.");
        releaseButton.addActionListener(this);
        JButton setDefaultDataButton = new JButton(_resetCommand);
        setDefaultDataButton.setToolTipText("Reset the template output XML data.");
        setDefaultDataButton.addActionListener(this);
        JPanel p = new JPanel();
        p.setBackground(YAdminGUI._apiColour);
        p.add(keepButton);
        p.add(releaseButton);
        p.add(setDefaultDataButton);
        bottomPanel.add(p, BorderLayout.EAST);
        return bottomPanel;
    }


    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(_keepCommand)) {
            storeData();
            this_windowClosing();
        } else if (command.equals(_releaseCommand)) {
            storeData();
            java.util.List errorMessages = _model.validateData();
            if (errorMessages.size() == 0) {
            this_windowClosing();
            _worklistGUI.completeWorkItem(_caseID, _taskID);
            } else {
                StringBuffer errorMessageStr = new StringBuffer();
                Iterator iterator = errorMessages.iterator();
                errorMessageStr.append(iterator.next());
                while (iterator.hasNext()) {
                    errorMessageStr.append("\t" + iterator.next() + "\r\n");
        }
                JOptionPane.showMessageDialog(this,
                                              "The data provided failed validation:\n " + errorMessages,
                                              "Error during validation",
                                              JOptionPane.ERROR_MESSAGE);
    }
        } else if (command == _resetCommand) {
            _outputDataTextPane.setText(_skeletonOutputData);
}
    }
    /*
        new Object[]{
            caseIDstr,
            taskID,
            description,
            _formatter.format(enablementTime),
            _formatter.format(firingTime),
            _formatter.format(startTime),
            new Boolean(allowsDynamicInstanceCreation),
            xmlData
    */
}
