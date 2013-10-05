package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 26/08/13
 */
public abstract class PropertyDialog extends JDialog {

    private JButton btnOK;
    private JButton btnCancel;

    protected static final String MENU_ICON_PATH =
            "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/";


    public PropertyDialog(Window parent) {
        this(parent, true);
    }

    public PropertyDialog(Window parent, boolean createContent) {
        super(parent);
        setModal(true);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        if (createContent) add(getContent());
    }


    public JButton getOKButton() { return  btnOK; }

    public JButton getCancelButton() { return  btnCancel; }


    protected abstract JPanel getContent();



    protected JPanel getButtonBar(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        btnCancel = createButton("Cancel", listener);
        panel.add(btnCancel);
        btnOK = createButton("OK", listener);
        btnOK.setEnabled(false);
        panel.add(btnOK);
        return panel;
    }


    protected JButton createButton(String caption, ActionListener listener) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(75,25));
        btn.addActionListener(listener);
        return btn;
    }


    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(MENU_ICON_PATH + iconName + ".png");
    }

}
