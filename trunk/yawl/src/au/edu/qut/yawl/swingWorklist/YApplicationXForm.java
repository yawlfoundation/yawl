/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.swingWorklist;

import au.edu.qut.yawl.engine.gui.YAdminGUI;
import au.edu.qut.yawl.exceptions.YPersistenceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private boolean _allowsInstanceCreation;
    private String _inputData;
    private String _outputData;
    private JTextPane _outputDataTextPane;
    private String _keepCommand = "Keep for Later";
    private String _releaseCommand = "Release WorkItem";
    private YWorklistModel _model;
    private YWorklistGUI _worklistGUI;


    YApplicationXForm(JFrame frame, YWorklistGUI worklistGUI, Object[] workItemData, final YWorklistModel model) throws YPersistenceException {
        super(frame, "Form: " + workItemData[2], true);
        _worklistGUI = worklistGUI;
        _caseID = (String) workItemData[0];
        _taskID = (String) workItemData[1];
        _description = (String) workItemData[2];
        _enableMentTime = (String) workItemData[3];
        _firingTimeTime = (String) workItemData[4];
        _startTime = (String) workItemData[5];
        _allowsInstanceCreation = ((Boolean) workItemData[6]).booleanValue();
        _inputData = (String) workItemData[7];
        if (workItemData.length > 8) {
            _outputData = (String) workItemData[8];
        } else {
            _outputData = model.getOutputSkeletonXML(_caseID, _taskID);
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

        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());
        screenSize.setSize(screenSize.getWidth(), screenSize.getHeight() - insets.bottom);
        setSize(screenSize);
        show();
    }

    private void storeData() {
        _model.setActiveTableData(new Object[]{
                _caseID,
                _taskID,
                _description,
                _enableMentTime,
                _firingTimeTime,
                _startTime,
                (_allowsInstanceCreation) ? Boolean.TRUE : Boolean.FALSE,
                _inputData,
                _outputDataTextPane.getText()});
    }

    private void this_windowClosing() {
        dispose();
    }


    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(YAdminGUI._apiColour);
        JPanel rightPanel = new JPanel(new GridLayout(4, 2));
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(YAdminGUI._apiColour);
        rightPanel.setBackground(YAdminGUI._apiColour);
        rightPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(), "Work Item Attributes"),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        String[] text = {_caseID, _taskID, _description, _startTime};
        String[] labels = {"Case ID", "Task ID", "Task Description", "Task Started"};
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
        JPanel p = new JPanel();
        p.setBackground(YAdminGUI._apiColour);
        p.add(keepButton);
        p.add(releaseButton);
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
        } else if (command.equals(_releaseCommand))
			try {
				{
				    storeData();
				    this_windowClosing();
				    _worklistGUI.completeWorkItem(_caseID, _taskID);
				}
			} catch (YPersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    }
}
