package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.update.ChecksumsReader;
import org.yawlfoundation.yawl.controlpanel.update.UpdateChecker;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Michael Adams
 * @date 26/08/2014
 */
public class AboutDialog extends JDialog {

    private static final String DEFAULT_YEAR = "2014";
    private static final Color BACK_COLOUR = new Color(254,254,240);


    public AboutDialog(Point appLocation, Point click) {
        super();
        init();
        setLocation(appLocation.x + click.x, appLocation.y + click.y);
    }


    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setContentPane(createContent());
        addKeyListener(this);
        pack();
    }


    private JPanel createContent() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(12,12,12,12)));
        panel.setBackground(BACK_COLOUR);
        panel.add(getLabel());
        addMouseListener(panel);
        return panel;
    }


    private JLabel getLabel() {
        ChecksumsReader props = new ChecksumsReader(getLocalCheckSumFile());
        StringBuilder s = new StringBuilder();
        s.append("YAWL Control Panel ");
        s.append(getVersion(props));

        String build = getBuild(props);
        if (build != null) s.append(" (build ").append(build).append(')');

        s.append("<br><br>");
        s.append(getCopyrightText(props));

        JLabel label = new JLabel(StringUtil.wrap(s.toString(), "HTML"));
        addMouseListener(label);
        return label;
    }


    private String getVersion(ChecksumsReader props) {
        String version = props.getVersion();
        return version != null ? version : YControlPanel.VERSION;
    }


    private String getBuild(ChecksumsReader props) {
        XNode node = props.getControlPanelNode();
        return node != null ? node.getChildText("build") : null;
    }


    private String getCopyrightText(ChecksumsReader props) {
        String buildDate = null;
        XNode node = props.getControlPanelFileNode();
        if (node != null) {
            buildDate = node.getAttributeValue("timestamp");
        }
        String year = buildDate != null ? buildDate.substring(0,4) : DEFAULT_YEAR;
        return "\u00a9 " + year + " The YAWL Foundation";
    }



    private void addMouseListener(Component c) {
        c.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setVisible(false);
            }
        });
    }


    private void addKeyListener(Component c) {
        c.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                setVisible(false);
            }
        });
    }


    private File getLocalCheckSumFile() {
        return new File(FileUtil.buildPath(TomcatUtil.getCatalinaHome(), "yawllib",
                UpdateChecker.CHECKSUM_FILE));
    }


}
