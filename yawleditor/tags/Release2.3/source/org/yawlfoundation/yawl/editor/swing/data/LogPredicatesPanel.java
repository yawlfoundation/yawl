package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Author: Michael Adams
 * Creation Date: 25/02/2010
 */
public class LogPredicatesPanel extends JPanel {

    private JTextArea taLogPredicateStarted;
    private JTextArea taLogPredicateCompletion;

    public enum Parent {DataVariable, Decomposition}

    public LogPredicatesPanel(int cols, int rows, Parent parent) {
        super(new BorderLayout());
        constructPanel(cols, rows, parent);
    }

    public String getStartedPredicate() {
        if (! taLogPredicateStarted.isEnabled()) return null;
        String predicate = taLogPredicateStarted.getText();
        return (predicate.length() > 0) ? predicate : null;
    }

    public void setStartedPredicate(String s) {
        taLogPredicateStarted.setText(s);
    }

    public String getCompletionPredicate() {
        if (! taLogPredicateCompletion.isEnabled()) return null;
        String predicate = taLogPredicateCompletion.getText();
        return (predicate.length() > 0) ? predicate : null;
    }

    public void setCompletionPredicate(String s) {
        taLogPredicateCompletion.setText(s);
    }

    public void setStartedPredicateEnabled(boolean enable) {
        taLogPredicateStarted.setEnabled(enable);
        taLogPredicateStarted.setBackground(enable ? Color.WHITE : Color.LIGHT_GRAY);
    }

    public void setCompletionPredicateEnabled(boolean enable) {
        taLogPredicateCompletion.setEnabled(enable);
        taLogPredicateCompletion.setBackground(enable ? Color.WHITE : Color.LIGHT_GRAY);
    }

    private void constructPanel(int cols, int rows, Parent parent) {
        JPanel logPredicatesPanel = new JPanel(new BorderLayout());
        logPredicatesPanel.setBorder(new EmptyBorder(10,10,10,12));

        taLogPredicateStarted = new JTextArea();
        taLogPredicateCompletion = new JTextArea();

        String title = getTitle(parent, "in");
        JPanel startedPanel =
                constructTextPanel(taLogPredicateStarted, cols, rows, title);

        title = getTitle(parent, "out");
        JPanel completionPanel =
                constructTextPanel(taLogPredicateCompletion, cols, rows, title);

        logPredicatesPanel.add(startedPanel, BorderLayout.NORTH);
        logPredicatesPanel.add(completionPanel, BorderLayout.SOUTH);
        logPredicatesPanel.add(new JLabel(" "), BorderLayout.CENTER);     // spacing

        this.add(logPredicatesPanel);
    }


    private String getTitle(Parent parent, String direction) {
        String title = "";
        if (parent == Parent.Decomposition) {
            title = (direction.equals("in")) ? "Start" : "Completion" ;
        }
        else if (parent == Parent.DataVariable) {
            title = (direction.equals("in")) ? "Input" : "Output" ;
        }
        return title;
    }


    private JPanel constructTextPanel(JTextArea textArea, int cols, int rows, String title) {
        JPanel textPanel = new JPanel();
        textPanel.setBorder(new CompoundBorder(new EmptyBorder(0,10,0,12),
                               new TitledBorder("On " + title)));
        textArea.setColumns(cols);
        textArea.setRows(rows);
        textArea.setLineWrap(true);
        JScrollPane startedPane = new JScrollPane(textArea);
        startedPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textPanel.add(startedPane);
        return textPanel;
    }
}
