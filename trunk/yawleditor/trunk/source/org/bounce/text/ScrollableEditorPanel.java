/*
 * $Id: ScrollableEditorPanel.java,v 1.2 2008/01/28 21:02:18 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.text;

import javax.swing.*;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;
import java.awt.*;

/**
 * Wrapper panel to force the editor pane to resize when a 
 * Wrapped View has been installed. 
 * 
 * Takes Block Increment and Unit Increment info from editor.
 * 
 * @version $Revision: 1.2 $, $Date: 2008/01/28 21:02:18 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class ScrollableEditorPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 3978147659863437620L;

        private JEditorPane editor = null;
        
        /**
         * Constructs the panel, with the editor in the Center 
         * of the BorderLayout.
         * 
         * @param editor the parent editor.
         */
        public ScrollableEditorPanel( JEditorPane editor) {
            super( new BorderLayout());
            
            this.editor = editor;
            
            add( editor, BorderLayout.CENTER);
        }
        
        /**
         * @see Scrollable#getPreferredScrollableViewportSize()
         */
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        /**
         * Returns the information directly from the editor component.
         * 
         * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
         */
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return editor.getScrollableUnitIncrement( visibleRect, orientation, direction);
        }

        /**
         * Returns the information directly from the editor component.
         * 
         * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
         */
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return editor.getScrollableBlockIncrement( visibleRect, orientation, direction);
        }  

        /**
         * Return true when a Wrapped View is used.
         * 
         * @see Scrollable#getScrollableTracksViewportWidth()
         */
        public boolean getScrollableTracksViewportWidth() {
        	View view = editor.getUI().getRootView( editor).getView(0);
        
            if ( view instanceof WrappedPlainView) {
                return true;
            } else if ( getParent() instanceof JViewport) {
                return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
            }

            return false;
        }

        /**
         * @see Scrollable#getScrollableTracksViewportHeight()
         */
        public boolean getScrollableTracksViewportHeight() {

        	if ( getParent() instanceof JViewport) {
                return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
            }
            
            return false;
        }
    }