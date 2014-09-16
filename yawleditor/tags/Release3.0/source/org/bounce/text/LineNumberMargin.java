/*
 * $Id: LineNumberMargin.java,v 1.2 2008/04/16 19:36:18 edankert Exp $
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Line number margin for a JTextComponent.
 * 
 * <pre>
 * JEditorPane editor = new JEditorPane();
 * JScrollPane scroller = new JScrollPane(editor);
 *
 * // Add the number margin as a Row Header View
 * LineNumberMargin margin = new LineNumberMargin(editor);
 * scroller.setRowHeaderView(margin);
 * </pre>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class LineNumberMargin extends JComponent {
	private static final long serialVersionUID = 1421386204383391804L;
    
	// Metrics of this LineNumber component
	private FontMetrics fontMetrics = null;
	private JTextComponent editor = null;

	private int lines = 0;

	/**
	 * Convenience constructor for Text Components
	 */
	public LineNumberMargin(JTextComponent editor) {
		this.editor = editor;
		
		setBackground(UIManager.getColor("control"));
		setForeground(UIManager.getColor("textText"));
		setFont(editor.getFont());
		
		editor.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent evt) {
				if (getLines() != lines) {
					revalidate();
					repaint();
					
					lines = getLines();
				}
			}
		});
		
		setBorder(new CompoundBorder(
						new MatteBorder(0, 0, 0, 1, UIManager.getColor("controlShadow")), 
						new EmptyBorder(0, 1, 0, 1)));
		
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				LineNumberMargin.this.mouseClicked(e);
			}
		});
	}

	/**
	 * Receives all mouse-click events in the margin.
	 * 
	 * @param event the mouse event.
	 */
	protected void mouseClicked(MouseEvent event) {
		selectLineForOffset(event.getY());
		LineNumberMargin.this.editor.requestFocusInWindow();
	}
	
	/**
	 * @return the preferred dimension.
	 */
	public Dimension getPreferredSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + getMarginwidth() + getInsets().right, editor.getPreferredSize().height);		
		}
		
        return null;
	}

	/**
	 * @return the maximum dimension.
	 */
	public Dimension getMaximumSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + getMarginwidth() + getInsets().right, editor.getPreferredSize().height);		
		}
		
        return null;
	}
	
	/**
	 * @return the minimum dimension.
	 */
	public Dimension getMinimumSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + getMarginwidth() + getInsets().right, editor.getPreferredSize().height);		
		}
        
        return null;
	}

	private int getMarginwidth() {
		int lines = getLines();
		int width = 0;
		
		if (fontMetrics != null) {
			if (lines >= 1000000) {
				width = fontMetrics.stringWidth("9999999");
			} else if (lines >= 100000) {
				width = fontMetrics.stringWidth("999999");
		} else if (lines >= 10000) {
				width = fontMetrics.stringWidth("99999");
			} else if (lines >= 1000) {
				width = fontMetrics.stringWidth("9999");
			} else {
				width = fontMetrics.stringWidth( "999");
			}
		}
		
		return width;
	}

	public void setFont(Font font) {
		super.setFont(font);
		
		if (font != null) {
			fontMetrics = getFontMetrics(font);
		}
	}

	public void paintComponent(Graphics g) {
		if (fontMetrics != null) {
			Rectangle bounds = g.getClipBounds();
			
			// Paint the background
			g.setColor(getBackground());
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

			// Determine the number of lines to draw in the foreground.
			g.setColor(getForeground());

			FontMetrics editorMetrics = getFontMetrics(editor.getFont());
			int startLine = getLineNumber(bounds.y);
			int endLine = getLineNumber(bounds.y + bounds.height);

			if (endLine < getLines()) {
				endLine = endLine + 1;
			}

			for (int line = startLine; line < endLine; line++) {
				String lineNumber = String.valueOf(line+1);
				
				try {
					int start = getLineStart(line);
					
					if (start != -1) { 
						g.drawString(lineNumber, getInsets().left + (getMarginwidth() - fontMetrics.stringWidth(lineNumber)), start + (editorMetrics.getHeight() - editorMetrics.getMaxDescent()));
					}
				} catch ( Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
    private int getLines() {
        return editor.getDocument().getDefaultRootElement().getElementCount();
    }
    
    private int getLineStart(int i) throws BadLocationException {
        Element line = editor.getDocument().getDefaultRootElement().getElement(i);
        Rectangle result = editor.modelToView(line.getStartOffset());
        
        if (result != null) {
            return result.y;
        }
        
        return -1;
    }
    
    private int getLineNumber(int y) {
        int pos = editor.viewToModel(new Point(0, y));
        
        return editor.getDocument().getDefaultRootElement().getElementIndex(pos);
    }
    
    private void selectLineForOffset(int y) {
    	int pos = editor.viewToModel(new Point(0, y));
        
    	if (pos >= 0) {
            Element root = editor.getDocument().getDefaultRootElement();
            Element elem = root.getElement(root.getElementIndex(pos));
    
            if (elem != null) {
                int start = elem.getStartOffset();
                int end = elem.getEndOffset();
                
                editor.select(start, Math.max(end-1, 0));
            }
        }
    }
}
