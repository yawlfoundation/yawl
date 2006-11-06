/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.icon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JComponent;


/**
 * This is a modified class of ProgressIcon for purposes of displaying WIZ-BANG
 * stuff in capsela.
 * 
 * This code is taken from:
 * http://www.javaworld.com/javaworld/jw-03-1996/jw-03-animation-p3.html
 * 
 * @author Arthur van Hoff
 * @author Dean Mao
 * @author Daniel Gredler
 * @created April 3, 2003
 * @see "http://www.javaworld.com/javaworld/jw-03-1996/jw-03-animation-p3.html"
 */
public class AnimatedIcon extends JComponent implements Runnable {

  private int numFrames;

  private Thread animator;

  private Image[] frames;

  private int frame;

  private int delay;

  private Dimension offDimension;

  private Image offImage;

  private Graphics offGraphics;

  /**
   * Creates an animated icon with the given frames, displaying the given number
   * of frames per second.
   * @param iconFrames the frames of the animation.
   * @param framesPerSecond the number of frames to display per second.
   */
  public AnimatedIcon(Object [] iconFrames, int framesPerSecond) {
    delay = (framesPerSecond > 0) ? (1000 / framesPerSecond) : 100;

    numFrames = iconFrames.length;
    frames = new Image[numFrames];
    ImageIcon frame = null;
    for (int i = 0; i < numFrames; i++) {
      frame = (ImageIcon) iconFrames[i];
      frames[i] = frame.getImage();
    }

    Dimension dim = new Dimension(frame.getIconWidth(), frame.getIconHeight());
    setSize(dim);
    setPreferredSize(dim);
  }

  /**
   * This method is called when the applet becomes visible on the screen. Create
   * a thread and start it.
   */
  public synchronized void start() {
    if (animator == null) {
      animator = new Thread(this);
      animator.start();
    }
  }

  /**
   * This method is called by the thread that was created in the start method.
   * It does the main animation.
   */
  public void run() {
    // Remember the starting time
    long tm = System.currentTimeMillis();
    while (Thread.currentThread() == animator) {
      // Display the next frame of animation.
      repaint();

      // Delay depending on how far we are behind.
      try {
        tm += delay;
        Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
      } catch (InterruptedException e) {
        break;
      }

      // Advance the frame
      frame++;
    }
  }

  /**
   * This method is called when the applet is no longer visible. Set the
   * animator variable to null so that the thread will exit before displaying
   * the next frame.
   */
  public synchronized void stop() {
    animator = null;
    offImage = null;
    offGraphics = null;
  }

  /**
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  public void paint(Graphics g) {
    try {
      // Create the offscreen graphics context
      Dimension d = this.getSize();
      if ((offGraphics == null) || (d.width != offDimension.width) || (d.height != offDimension.height)) {
        offDimension = d;
        offImage = createImage(d.width, d.height);
        offGraphics = offImage.getGraphics();
      }

      // Paint the frame into the image
      if (offGraphics != null) {
      offGraphics.drawImage(frames[frame % numFrames], 0, 0, Color.WHITE, null);

      // Paint the image onto the screen
      g.drawImage(offImage, 0, 0, null);
      }
    } catch (Exception e) {
      // don't do anything here!
    }
  }
}