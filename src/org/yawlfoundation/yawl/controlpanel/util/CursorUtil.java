package org.yawlfoundation.yawl.controlpanel.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

import static antlr.build.ANTLR.root;

/**
 * @author Michael Adams
 * @date 11/11/2015
 */
public class CursorUtil {

    private static final MouseAdapter _mouseAdapter =  new MouseAdapter() {};

    private static RootPaneContainer _root;

    private CursorUtil() {}

    /** Sets cursor for specified component to Wait cursor */
    public static void showWaitCursor() {
        _root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        _root.getGlassPane().addMouseListener(_mouseAdapter);
        _root.getGlassPane().setVisible(true);
    }


    /** Sets cursor for specified component to normal cursor */
    public static void showDefaultCursor() {
        _root.getGlassPane().setCursor(Cursor.getDefaultCursor());
        _root.getGlassPane().removeMouseListener(_mouseAdapter);
        _root.getGlassPane().setVisible(false);
    }


    public static void setContainer(RootPaneContainer root) { _root = root; }

}
