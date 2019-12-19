/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.icons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 20/08/2014
 */
public class IconLoader {

    private static final String DEF_EXTN = ".png";
    private static final Map<String, ImageIcon> CACHE = new HashMap<String, ImageIcon>();


    public static ImageIcon get(String name) {
        ImageIcon icon = CACHE.get(name);
        if (icon == null) {
            icon = load(name);
        }
        return icon;
    }


    private static ImageIcon load(String name) {
        ImageIcon icon = null;
        try {

            // animated gifs must be loaded with this constructor
            if (name.endsWith(".gif")) {
                URL url = IconLoader.class.getResource(name);
                icon = new ImageIcon(url);
            }

            // pngs are needed as BufferedImages
            else {
                InputStream stream = IconLoader.class.getResourceAsStream(name + DEF_EXTN);
                if (stream != null) {
                    icon = new ImageIcon(ImageIO.read(stream));
                }
            }
            if (icon != null) CACHE.put(name, icon);
        }
        catch (IOException ignore) {
            // ignore this file
        }
        return icon;
    }

}
