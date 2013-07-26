package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class FilterListPanel extends JPanel implements ActionListener {

    private JTextArea txtExpression;

    private static final String iconPath =
            "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";



    public FilterListPanel(String title, Vector<String> items) {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(title));
        setContent(items);
    }


    public void setExpression(String expression) { txtExpression.setText(expression); }


    public String getExpression() {
        String expression = txtExpression.getText();
        if (endsWithOperator(expression)) {
            expression = undo(expression);               // chop the operator from end
        }
        return expression;
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("And")) {
            if (! endsWithOperator(txtExpression.getText())) txtExpression.append(" & ");
        }
        else if (action.equals("Or")) {
            if (! endsWithOperator(txtExpression.getText())) txtExpression.append(" | ");
        }
        else if (action.equals("Clear")) {
            txtExpression.setText("");
        }
        else if (action.equals("Undo")) {
            txtExpression.setText(undo(txtExpression.getText()));
        }
    }


    private void setContent(Vector<String> items) {
        add(createList(items), BorderLayout.NORTH);
        add(createTextArea(), BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.SOUTH);
    }


    private JScrollPane createList(Vector<String> items) {
        final JList list = new JList(items);
        list.setPreferredSize(new Dimension(175, 150));
        list.setDragEnabled(true);
        list.setEnabled(! items.isEmpty());

        // copies item on double-click
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && canAppendItem()) {
                    txtExpression.append((String) list.getSelectedValue());
                }
            }
        });

        return new JScrollPane(list);
    }


    private JPanel createTextArea() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,0,0,0));
        txtExpression = new JTextArea();
        txtExpression.setLineWrap(true);
        txtExpression.setWrapStyleWord(true);
        txtExpression.setTransferHandler(new FilterTransferHandler());
        JScrollPane pane = new JScrollPane(txtExpression);
        pane.setPreferredSize(new Dimension(210, 75));
        panel.add(createExpressionTitle());
        panel.add(pane);
        return panel;
    }


    private JPanel createExpressionTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Filter Expression"), BorderLayout.WEST);
        return panel;
    }


    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(createToolBarButton("and", "And", " And "));
        toolbar.add(createToolBarButton("or", "Or", " Or "));
        toolbar.add(createToolBarButton("undo", "Undo", " Undo "));
        toolbar.add(createToolBarButton("cross", "Clear", " Clear "));
        return toolbar;
    }


    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    private String undo(String text) {
        boolean alphaHit = false;
        int i;
        for (i=text.length()-1; i>-0; i--) {
            char c = text.charAt(i);
            if (c == '&' || c == '|') {
                if (! alphaHit) {
                    return text.substring(0, i-1);   // lop off operator and lead space
                }
                else {
                    return text.substring(0, i+2);   // lop off word, leave trailing space
                }
            }
            else if (c != ' ') {
                alphaHit = true;
            }
        }
        return "";               // no operator found, must be single word, so remove
    }


    private boolean endsWithOperator(String s) {
        for (int i=s.length()-1; i >=0; i--) {
            char c = s.charAt(i);
            if (c == '&' || c == '|') return true;
            else if (c != ' ') return false;
        }
        return false;
    }

    private boolean canAppendItem() {
        String currentText = txtExpression.getText();
        return StringUtil.isNullOrEmpty(currentText) || endsWithOperator(currentText);
    }


    /****************************************************************************/

    class FilterTransferHandler extends TransferHandler {

        public boolean importData(TransferHandler.TransferSupport support) {
            if (! canImport(support)) {
                return false;
            }
            try {
                String data = (String) support.getTransferable().getTransferData(
                        DataFlavor.stringFlavor);
                txtExpression.append(data);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return canAppendItem();
        }

    }

}
