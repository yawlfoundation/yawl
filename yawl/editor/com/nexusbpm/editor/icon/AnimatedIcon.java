/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.icon;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class AnimatedIcon extends ImageIcon implements Runnable {

  private int numFrames;

  private Thread animator;

  private Image[] frames;

  private int frame;

  private int delay;

  private JComponent host;

  /**
   * Creates an animated icon with the given frames, displaying the given number
   * of frames per second.
   * @param iconFrames the frames of the animation.
   * @param framesPerSecond the number of frames to display per second.
   */
  public AnimatedIcon(Object [] iconFrames, int framesPerSecond, JComponent host) {
    delay = (framesPerSecond > 0) ? (1000 / framesPerSecond) : 100;

    numFrames = iconFrames.length;
    frames = new Image[numFrames];
    ImageIcon frame = null;
    this.host = host;
    for (int i = 0; i < numFrames; i++) {
      frame = (ImageIcon) iconFrames[i];
      frames[i] = frame.getImage();
    }
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
      this.setImage(frames[frame]);
    	host.repaint();
    	frame = (frame + 1) % numFrames;

      // Delay depending on how far we are behind.
      try {
        tm += delay;
        Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  public synchronized void stop() {
    animator = null;
  }

public Image getFrame(int i) {
	return frames[i];
}
}