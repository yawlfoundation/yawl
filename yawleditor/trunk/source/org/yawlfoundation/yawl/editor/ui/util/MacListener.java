package org.yawlfoundation.yawl.editor.ui.util;


import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationAdapter;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.DefaultApplication;
import org.yawlfoundation.yawl.editor.ui.actions.AboutEditorAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.PrintSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 * Listens for events from the osx menu bar
 *
 * @author Michael Adams
 * @date 21/11/2013
 */
public class MacListener extends ApplicationAdapter {

    private Application application;


    public MacListener() {
        application = new DefaultApplication();
        application.addApplicationListener(this);
        application.addPreferencesMenuItem();
        application.setEnabledPreferencesMenu(true);
        application.addAboutMenuItem();
        application.setEnabledAboutMenu(true);
        setIcon();
        setLaF();
    }


    public void handleAbout(ApplicationEvent event) {
        new AboutEditorAction().actionPerformed(null);
        event.setHandled(true);
    }


    public void handleOpenFile(ApplicationEvent event) {
        FileOperations.open(event.getFilename());
    }


    public void handlePreferences(ApplicationEvent event) {
        new PreferencesDialog().setVisible(true);
    }


    public void handlePrintFile(ApplicationEvent event) {
        new PrintSpecificationAction().actionPerformed(null);
    }


    public void handleQuit(ApplicationEvent event) {
        FileOperations.exit();
    }


    private void setIcon() {
        String path = "/org/yawlfoundation/yawl/editor/ui/resources/yawlLogo.png";
        InputStream in = getClass().getResourceAsStream(path);
        try {
            application.setApplicationIconImage(ImageIO.read(in));
        }
        catch (IOException ioe) {
            // fuggetaboutit
        }
    }


    private void setLaF() {
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            resetClipboardMasks();
        }
        catch (UnsupportedLookAndFeelException ulafe) {
            // accept the default
        }
    }


    private static void resetClipboardMasks() {
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        im = (InputMap) UIManager.get("EditorPane.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
    }

}
