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

package au.edu.qut.yawl.editor.foundations;

import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ResourceLoader {
  static final int MAX_IMAGE_SIZE = 131072;

  public static JLabel getImageAsJLabel(String imageFile) {
    return new JLabel(getImageAsIcon(imageFile));
  }
  
  public static ImageIcon getImageAsIcon(String imageFile) {
	try {
      InputStream in = ResourceLoader.class.getResourceAsStream(imageFile);
      final byte[] imageByteBuffer = convertToByteArray(in);
      in.close();
      return new ImageIcon(imageByteBuffer);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static byte[] convertToByteArray(final InputStream is) {
    int       read = 0;
    int       totalRead = 0;

    byte[] byteArray        = new byte[MAX_IMAGE_SIZE];

    try {
      while ((read = is.read(byteArray,totalRead,MAX_IMAGE_SIZE - totalRead)) >= 0) {
      	totalRead += read;
      }
    } catch (Exception e) { return null; }

    byte[] finalByteArray = new byte[totalRead];
    System.arraycopy(byteArray,0,finalByteArray,0, totalRead);
    return finalByteArray;
  }
}

