/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.util;

import org.imgscalr.Scalr;
import org.yawlfoundation.yawl.editor.core.util.FileUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Michael Adams
 * @date 25/10/2013
 */
public class IconList {

    private boolean _loaded;
    private Icon _brokenPath;

    private static IconList INSTANCE = new IconList();
    private static List<IconPair> _icons = new ArrayList<IconPair>();

    public static final String INTERNAL_PATH =
            "org/yawlfoundation/yawl/editor/ui/resources/taskicons";


    private IconList() { }

    public static IconList getInstance() { return INSTANCE; }


    public void load() { new LoaderThread().run(); }

    public List<IconPair> getList() { return _icons; }

    public int getSize() { return _icons.size(); }

    public String getName(int index) { return _icons.get(index).getName(); }

    public Icon getIcon(int index) { return _icons.get(index).getIcon(); }

    public Icon getIcon(String name) {
        for (IconPair pair : _icons) {
            if (pair.matches(name)) {
                return pair.getIcon();
            }
        }
        return _brokenPath;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (IconPair pair : _icons) {
            names.add(pair.getName());
        }
        return names;
    }

    public List<String> getShortNames() {
        List<String> names = new ArrayList<String>();
        for (IconPair pair : _icons) {
            names.add(pair.getShortName());
        }
        return names;
    }


    public List<Icon> getIcons() {
        List<Icon> icons = new ArrayList<Icon>();
        for (IconPair pair : _icons) {
            icons.add(pair.getIcon());
        }
        return icons;
    }

    public List<Icon> getDropDownIcons() {
        List<Icon> icons = new ArrayList<Icon>();
        for (IconPair pair : _icons) {
            icons.add(pair.getMenuIcon());
        }
        return icons;
    }


    /*******************************************************************************/

    public class IconPair implements Comparable<IconPair> {

        private String _name;
        private Icon _icon;
        private Icon _menuIcon;

        public IconPair(String name, Icon icon, Icon menuIcon) {
            _name = name;
            _icon = icon;
            _menuIcon = menuIcon;
        }

        public String getName() { return _name; }

        public Icon getIcon() { return _icon; }

        public Icon getMenuIcon() { return _menuIcon; }

        public String getShortName() {
            int start = _name.lastIndexOf('/');
            int end = _name.lastIndexOf('.');

            return (start > -1 && end > -1) ? _name.substring(start+1, end) : _name;
        }

        public boolean matches(String name) {
            return name != null && (name.equals(_name) ||
                    _name.equals(INTERNAL_PATH + '/' + name) ||         // most end here
                    _name.equalsIgnoreCase(INTERNAL_PATH + '/' + name));  // legacy only
        }

        public int compareTo(IconPair other) {

            // 'None' must always be at the top of the list
            if (_name.equals("None")) return -1;
            if (other.getName().equals("None")) return 1;
            return _name.compareTo(other.getName());
        }
    }


    /*******************************************************************************/

    class LoaderThread extends Thread {

        public void run() {
            addIcon("None", null, null);
            getInternalIcons();
            getExternalIcons();
            Collections.sort(_icons);
            _loaded = true;
        }

        private void getInternalIcons() {
            String jarPath = FileUtil.getHomeDir() + "YAWLEditor-icons.jar";
            try {
                JarFile jarFile = new JarFile(jarPath);
                Enumeration enu = jarFile.entries();
                while (enu.hasMoreElements()) {
                    JarEntry entry = (JarEntry) enu.nextElement();
                    if (entry != null && entry.getName().endsWith(".png")) {
                        BufferedImage image = ImageIO.read(jarFile.getInputStream(entry));
                        addIcon(entry.getName(), new ImageIcon(image),
                                getScaledIcon(image, 12));
                    }
                }
            }
            catch (IOException ioe) {
                // can't load internal icons
            }
        }


        private void getExternalIcons() {
            String path = UserSettings.getTaskIconsFilePath();
            if (path != null) {
                File dir = new File(path);
                File[] externals = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File file, String s) {
                        final List<String> okay = Arrays.asList("png", "gif", "jpg", "jpeg");
                        return okay.contains(s.substring(s.lastIndexOf('.') + 1));
                    }
                });

                if (externals != null) {
                    for (File file : externals) {
                        try {
                            BufferedImage image = ImageIO.read(file);
                            addIcon(file.getName(), new ImageIcon(image),
                                    getScaledIcon(image, 12));
                        }
                        catch (IOException ioe) {
                            // ignore this file
                        }
                    }
                }
            }
        }


        private Icon getScaledIcon(BufferedImage image, int size) {
            return new ImageIcon(Scalr.resize(image, size));
        }


        private void addIcon(String name, Icon icon, Icon menuIcon) {
            if (name.endsWith("brokenpath.png")) {
                _brokenPath = icon;
            }
            else _icons.add(new IconPair(name, icon, menuIcon));
        }

    }

}
