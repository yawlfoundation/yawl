/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util.syntax;

import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.3 $
 * @created May 12, 2005
 * @author Mitchell J. Friedman
 */
public class JeditHelper {
  private static final Log LOG = LogFactory.getLog(JeditHelper.class);
	public static void removeListeners(Component component) {

		if (null == component)
			return;

		StringBuffer buf = new StringBuffer();
		buf.append("JeditHelper.removeListeners: ");

		ComponentListener[] componentListener = component.getComponentListeners();
		if (null != componentListener) {
			if (componentListener.length > 0) {
				buf.append("component=");
				buf.append(componentListener.length);

				for (int ii=0; ii<componentListener.length; ii++) {
					component.removeComponentListener( componentListener[ii]);
				}
			}
		}

		FocusListener[] focusListener = component.getFocusListeners();
		if (null != focusListener) {
			if (focusListener.length > 0) {
				buf.append(";focus=");
				buf.append(focusListener.length);

				for (int ii=0; ii<focusListener.length; ii++) {
					component.removeFocusListener( focusListener[ii]);
				}
			}
		}

		MouseListener[] mouseListener = component.getMouseListeners();
		if (null != mouseListener) {
			if (mouseListener.length > 0) {
				buf.append(";mouse=");
				buf.append(mouseListener.length);

				for (int ii=0; ii<mouseListener.length; ii++) {
					component.removeMouseListener( mouseListener[ii]);
				}
			}
		}

		MouseMotionListener[] mouseMotionListener = component.getMouseMotionListeners();
		if (null != mouseMotionListener) {
			if (mouseMotionListener.length > 0) {
				buf.append(";mouseMotion=");
				buf.append(mouseMotionListener.length);

				for (int ii=0; ii<mouseMotionListener.length; ii++) {
					component.removeMouseMotionListener( mouseMotionListener[ii]);
				}
			}
		}

		KeyListener[] keyListener = component.getKeyListeners();
		if (null != keyListener) {
			if (keyListener.length > 0) {
				buf.append(";key=");
				buf.append(keyListener.length);

				for (int ii=0; ii<keyListener.length; ii++) {
					component.removeKeyListener( keyListener[ii]);
				}
			}
		}
/*
		PropertyChangeListener[] propertyChangeListener = component.getPropertyChangeListeners();
		if (null != propertyChangeListener) {
			if (propertyChangeListener.length > 0) {
				buf.append(";propertyChange=");
				buf.append(propertyChangeListener.length);

				for (int ii=0; ii<propertyChangeListener.length; ii++) {
					component.removePropertyChangeListener( propertyChangeListener[ii]);
				}
			}
		}
*/


		if (buf.length() > 30) {
			// if nothing deleted then not worth logging

			buf.append(";class=");
			buf.append(component.getClass().getName());
			LOG.debug(buf.toString());
		}
	}
}
