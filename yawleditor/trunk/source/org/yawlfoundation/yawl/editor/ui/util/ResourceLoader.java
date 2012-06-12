/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.util;

import javax.swing.*;
import java.io.*;

public class ResourceLoader {

    public static JLabel getImageAsJLabel(String imageFile) {
        return new JLabel(getImageAsIcon(imageFile));
    }
    /**
     *
     * @param imageFile
     * @return
     */
    public static ImageIcon getImageAsIcon(String imageFile) {
        try {
            InputStream in = ResourceLoader.class.getResourceAsStream(imageFile);
            final byte[] imageByteBuffer = convertToByteArray(in);
            in.close();
            return new ImageIcon(imageByteBuffer);
        } catch (Exception e) {
            return null;
        }
    }

    public static ImageIcon getExternalImageAsIcon(String imageFile) {
        try {
            FileInputStream in = new FileInputStream(imageFile);
            final byte[] imageByteBuffer = convertToByteArray(in);
            in.close();
            return new ImageIcon(imageByteBuffer);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] convertToByteArray(final InputStream is) throws IOException {
        final int BUF_SIZE = 16384;
        BufferedInputStream inStream = new BufferedInputStream(is);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead = 0;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.flush();
        return outStream.toByteArray();
    }
}

