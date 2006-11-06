/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.icon;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JMenu;


/**
 * This code is taken from:
 * http://www.javaworld.com/javaworld/jw-03-1996/jw-03-animation-p3.html
 * 
 * Modified for the purposes of Capsela.
 * 
 * @author Arthur van Hoff
 * @author Dean Mao
 * @author Daniel Gredler
 * @created April 3, 2003
 * @see "http://www.javaworld.com/javaworld/jw-03-1996/jw-03-animation-p3.html"
 */
public class ProgressIcon extends JMenu implements Runnable {

  private final static int NUM_FRAMES = 8;

  private static ProgressIcon _singleton;

  private Thread animator;

  private Image[] frames;

  private int frame;

  private int delay;

  private Dimension offDimension;

  private Image offImage;

  private Graphics offGraphics;

  /**
   * Gets the instance attribute of the AnimatedIcon class
   * 
   * @return The instance value
   */
  public synchronized static ProgressIcon getInstance() {
    if (_singleton == null) {
      _singleton = new ProgressIcon();
    }
    return _singleton;
  }

  /**
   * Initialize the applet and compute the delay between frames.
   */
  private ProgressIcon() {

    super("  ");
    int fps = 6;
    delay = (fps > 0) ? (1000 / fps) : 100;

    frames = new Image[NUM_FRAMES];
    for (int i = 1; i <= NUM_FRAMES; i++) {
      ImageIcon imageIcon = ApplicationIcon.getIcon("Client.animation.frame" + i);
      frames[i - 1] = imageIcon.getImage();
    }

    setSize(20, 20);
    setPreferredSize(new Dimension(20, 20));
  }

  /**
   * This method is called when the applet becomes visible on the screen. Create
   * a thread and start it.
   */
  public synchronized void start() {
    if (animator == null) {
      animator = new Thread(this, "Capsela Animated Icon");
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
      offGraphics.drawImage(frames[frame % NUM_FRAMES], 0, 0, null);

      // Paint the image onto the screen
      g.drawImage(offImage, 0, 0, null);
      }
    } catch (Exception e) {
      // don't do anything here!
    }
  }
}