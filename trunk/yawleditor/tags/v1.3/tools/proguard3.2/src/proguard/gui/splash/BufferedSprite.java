/* $Id: BufferedSprite.java,v 1.6 2004/08/15 12:39:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.gui.splash;

import java.awt.*;

/**
 * This Sprite encapsulates another Sprite, which is buffered on an Image.
 *
 * @author Eric Lafortune
 */
public class BufferedSprite implements Sprite
{
    private Image    bufferImage;
    private Graphics bufferGraphics;
    private Color    backgroundColor;
    private Sprite   sprite;

    private long cachedTime = -1;


    /**
     * Creates a new BufferedSprite.
     * @param bufferImage     the Image that is used for the buffering.
     * @param bufferGraphics  the Graphics of the Image.
     * @param backgroundColor the background color that is used for the buffer.
     * @param sprite          the Sprite that is painted in the buffer.
     */
    public BufferedSprite(Image    bufferImage,
                          Graphics bufferGraphics,
                          Color    backgroundColor,
                          Sprite   sprite)
    {

        this.bufferImage     = bufferImage;
        this.bufferGraphics  = bufferGraphics;
        this.backgroundColor = backgroundColor;
        this.sprite          = sprite;
    }


   // Implementation for Sprite.

    public void paint(Graphics graphics, long time)
    {
        Rectangle clip = bufferGraphics.getClipBounds();

        // Do we need to repaint the sprites in the buffer image?
        if (time != cachedTime)
        {
            // Clear the background.
            if (backgroundColor != null)
            {
                bufferGraphics.setColor(backgroundColor);
                bufferGraphics.fillRect(0, 0, clip.width, clip.height);
            }

            // Draw the sprite.
            sprite.paint(bufferGraphics, time);

            cachedTime = time;
        }

        // Draw the buffer image.
        graphics.drawImage(bufferImage, 0, 0, clip.width, clip.height, null);
    }
}
