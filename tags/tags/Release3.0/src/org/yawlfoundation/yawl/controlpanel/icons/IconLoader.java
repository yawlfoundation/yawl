package org.yawlfoundation.yawl.controlpanel.icons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 20/08/2014
 */
public class IconLoader {

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
            InputStream stream = IconLoader.class.getResourceAsStream(name + ".png");
            icon = new ImageIcon(ImageIO.read(stream));
            if (icon != null) CACHE.put(name, icon);
        }
        catch (IOException ignore) {
            // ignore this file
        }
        return icon;
    }

}
