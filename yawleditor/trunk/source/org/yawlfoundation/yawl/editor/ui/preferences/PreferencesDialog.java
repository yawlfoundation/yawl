package org.yawlfoundation.yawl.editor.ui.preferences;

import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.misc.IconPackagerButtonBarUI;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 23/09/13
 */
public class PreferencesDialog extends JDialog {

    private JPanel _mainPanel;
    private JPanel _currentPanel;


    protected static final String MENU_ICON_PATH =
            "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/";



    public PreferencesDialog() {
        super(YAWLEditor.getInstance());
        init();
    }


    private void init() {
        setModal(true);
        setResizable(true);
//        setLocationRelativeTo(parent);
        setTitle("YAWL Editor Preferences");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(getContent());
        setPreferredSize(new Dimension(600, 500));
        pack();
    }


    private JPanel getContent() {
        _mainPanel = new JPanel(new BorderLayout());
        JButtonBar toolbar = new JButtonBar(JButtonBar.VERTICAL);
        toolbar.setUI(new IconPackagerButtonBarUI());
        _mainPanel.add(toolbar, BorderLayout.WEST);
        populateToolbar(toolbar);
        return _mainPanel;
    }


    private void populateToolbar(JButtonBar toolbar) {
        ButtonGroup group = new ButtonGroup();
        addConnectionPreferences(toolbar, group);
        addAnalysisPreferences(toolbar, group);
        addOtherPreferences(toolbar, group);
    }


    private void showContent(JPanel content) {
        if (_currentPanel != null) {
            _mainPanel.remove(_currentPanel);
        }
        _currentPanel = content;
        _mainPanel.add(content, BorderLayout.CENTER);
        _mainPanel.revalidate();
        repaint();
     }


    private void addConnectionPreferences(JButtonBar toolbar, ButtonGroup group) {
        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new Label("connection"));
        JToggleButton button = makeButton("Connect", "link", connectionPanel);
        toolbar.add(button);
        group.add(button);
        button.setSelected(true);
        showContent(connectionPanel);
    }

    private void addAnalysisPreferences(JButtonBar toolbar, ButtonGroup group) {
         JPanel analysisPanel = new AnalysisPanel();
         JToggleButton button = makeButton("Analysis", "cd-accept", analysisPanel);
         toolbar.add(button);
         group.add(button);
     }

    private void addOtherPreferences(JButtonBar toolbar, ButtonGroup group) {
        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new Label("others"));
        JToggleButton button = makeButton("Others", "folder-open-edit", connectionPanel);
        toolbar.add(button);
        group.add(button);
    }

    private JToggleButton makeButton(String caption, String iconName,
                                     final JPanel content) {
        JToggleButton button = new JToggleButton(caption);
        button.setIcon(getMenuIcon(iconName));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                showContent(content);
            }
        });
        return button;
    }


    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(MENU_ICON_PATH + iconName + ".png");
    }


}
