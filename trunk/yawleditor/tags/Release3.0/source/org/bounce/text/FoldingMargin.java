/*
 * $Id: XMLViewUtilities.java,v 1.5 2009/01/22 22:14:59 edankert Exp $
 *
 * Copyright (c) 2002 - 2009, Edwin Dankert
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base folding margin for a JTextComponent. Just implement the
 * getLastFoldLine() method.
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 */

public abstract class FoldingMargin extends JComponent {
	private static final long serialVersionUID = 1L;

	// Set right/left margin
	private static final int ICON_WIDTH = 9;

	// heights and widths
	private int lineHeight = 16;
	private int start = -1;
	private int end = -1;

	// Metrics of this LineNumber component
	private FontMetrics fontMetrics = null;

	protected JTextComponent editor = null;

	/**
	 * Convenience constructor for Text Components
	 */
	public FoldingMargin(JTextComponent editor) throws IOException {
		this.editor = editor;

		this.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1,
                UIManager.getColor("controlShadow")), new EmptyBorder(0, 1, 0, 1)));

		editor.addPropertyChangeListener("document", new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				Object prop = event.getNewValue();
				
				if (prop instanceof Document) {
					init((Document)prop);
				}
			}
		});

		init(editor.getDocument());

		setBackground(UIManager.getColor("control")); // editor.getBackground());
		setForeground(UIManager.getColor("textText"));

		setFont(editor.getFont());

		editor.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent evt) {
				Element e = FoldingMargin.this.editor.getDocument().getDefaultRootElement();
				unfold(e.getElementIndex(evt.getDot()));

				repaint();
			}
		});

		this.addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				FoldingMargin.this.start = -1;
				FoldingMargin.this.end = -1;

				repaint();
			}

			public void mouseClicked(MouseEvent e) {
				FoldingMargin.this.editor.requestFocusInWindow();
				toggleFold(getLineNumber(e.getY()));
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				int start = getLineNumber(e.getY());
				boolean update = FoldingMargin.this.start == -1;

				if (start != FoldingMargin.this.start) {
					Rectangle visible = getVisibleRect(); 
					int end = getLastFoldLine(start, Math.min(getLineNumber(visible.y + visible.height) + 2, getLines() - 1));

					if (end != -1) {
						FoldingMargin.this.start = start;
						FoldingMargin.this.end = end;
					} else {
						FoldingMargin.this.start = -1;
						FoldingMargin.this.end = -1;
					}

					if (update) {
						repaint();
					}
				}
			}
		});
	}

	private void init(Document document) {
		document.putProperty(Fold.FOLD_LIST_ATTRIBUTE, new ArrayList<Fold>());
		document.addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent documentevent) {
				updateFolds();
			}

			public void insertUpdate(DocumentEvent documentevent) {
				updateFolds();
			}

			public void removeUpdate(DocumentEvent documentevent) {
				updateFolds();
			}

		});
	}
	
	@SuppressWarnings("unchecked")
	private List<Fold> getFolds() {
		List<Fold> folds = (List<Fold>) editor.getDocument().getProperty(Fold.FOLD_LIST_ATTRIBUTE);
		
		if (folds == null) {
			folds = Collections.EMPTY_LIST;
		}

		return folds;
	}

	private void toggleFold(int line) {
		if (isFolded(line + 1)) {
			unfold(line + 1);
		} else {
			int end = getLastFoldLine(line, getLines() - 1);

			if (end != -1) {
				fold(editor.getDocument().getDefaultRootElement().getElement(line), editor.getDocument().getDefaultRootElement().getElement(end));
			}
		}
	}

	public void setVisible(boolean visible) {
		if (visible != isVisible()) {
			super.setVisible(visible);
			cleanupFolds();
		}
	}

	private void cleanupFolds() {
		List<Fold> folds = getFolds();

		for (int i = 0; i < folds.size(); i++) {
			Fold f = folds.get(i);

			f.cleanup();
		}

		folds.clear();
	}

	public Dimension getPreferredSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + ICON_WIDTH + getInsets().right, editor.getPreferredSize().height);
		}

		return null;
	}

	public Dimension getMaximumSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + ICON_WIDTH + getInsets().right, editor.getPreferredSize().height);
		}

		return null;
	}

	public Dimension getMinimumSize() {
		if (isVisible()) {
			return new Dimension(getInsets().left + ICON_WIDTH + getInsets().right, editor.getPreferredSize().height);
		}

		return null;
	}

	public void setFont(Font font) {
		super.setFont(font);

		if (font != null) {
			fontMetrics = getFontMetrics(font);
			lineHeight = fontMetrics.getHeight();
		}
	}

	/**
	 * The line height defaults to the line height of the font for this
	 * component. The line height can be overridden by setting it to a positive
	 * non-zero value.
	 */
	private int getLineHeight() {
		return lineHeight;
	}

	public void paintComponent(Graphics g) {
		if (fontMetrics != null) {
			int lineHeight = getLineHeight();
			Rectangle bounds = g.getClipBounds();

			// Paint the background
			g.setColor(getBackground());
			g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

			// Determine the number of lines to draw in the foreground.
			g.setColor(getForeground());

			int startLine = getLineNumber(bounds.y);
			if (startLine > 0) {
				startLine--;
			}

			int endLine = getLineNumber(bounds.y + bounds.height);

			if (endLine < getLines()) {
				endLine++;
			}

			int line = startLine;

			while (line < endLine) {
				try {
					int start = getLineStart(line);

					if (start != -1) {
						if (isFolded(line + 1)) {
							drawClosedFold(g, getInsets().left, start + ((lineHeight - ICON_WIDTH) / 2));

						} else if (getLastFoldLine(line, Math.min(getLineNumber(bounds.y + getVisibleRect().height) + 2, getLines() - 1)) != -1) {
							drawOpenFold(g, getInsets().left, start + ((lineHeight - ICON_WIDTH) / 2));
						} else if (line > this.start && line < end) {
							drawLine(g, getInsets().left, start);
						} else if (line == end) {
							drawEnd(g, getInsets().left, start);
						}
					}
					line = getNextLineNumber(line);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private void fold(Element start, Element end) {
		if (isVisible()) {
			List<Fold> folds = getFolds();

			Fold fold = new Fold(start, end);
			int startIndex = getNextFoldIndex(fold.getStart());

			if (startIndex != -1) {
				List<Fold> oldFolds = new ArrayList<Fold>(folds);

				for (int i = startIndex; i < oldFolds.size(); i++) {
					Fold f = (Fold) oldFolds.get(i);

					if (fold.contains(f.getStart())) {
						fold.add(f);
						folds.remove(f);
					} else if (f.getStart() > fold.getEnd()) {
						break;
					}
				}
			}

			addFold(fold);

			Element e = editor.getDocument().getDefaultRootElement();
			int index = e.getElementIndex(editor.getCaretPosition());

			if (isFolded(index)) {
				editor.setCaretPosition(start.getEndOffset() - 1);
			}

			fireFoldsUpdated();
		}
	}

	private void fireFoldsUpdated() {
		editor.getDocument().putProperty(Fold.FOLDS_UPDATED_ATTRIBUTE, true);

		editor.revalidate();
		editor.repaint();

		getParent().invalidate();
		getParent().repaint();
	}

	private void unfold(int line) {
		if (isVisible()) {
			Fold f = getFold(line);

			if (f != null) {
				List<Fold> folds = getFolds();
				folds.remove(f);

				f.remove(line, line);
				List<Fold> children = f.getChildren();

				for (int j = 0; j < children.size(); j++) {
					addFold((Fold) children.get(j));
				}

				f.shallowCleanup();
				fireFoldsUpdated();
			}
		}
	}

	private Fold getFold(int line) {
		List<Fold> folds = getFolds();

		if (isVisible() && folds != null) {
			int start = 0;
			int end = folds.size() - 1;

			while (end >= start) {
				int index = (((end - start) / 2) + start);

				Fold fold = folds.get(index);

				if (line >= fold.getEnd()) {
					start = index + 1;
				} else if (line <= fold.getStart()) {
					end = index - 1;
				} else {
					return fold;
				}
			}
		}

		return null;
	}

	private int getNextFoldIndex(int line) {
		List<Fold> folds = getFolds();

		if (isVisible() && folds != null) {
			int start = 0;
			int end = folds.size() - 1;
			Fold lastFold = null;
			int lastIndex = -1;

			while (end >= start) {
				int index = (((end - start) / 2) + start);
				Fold fold = folds.get(index);

				lastFold = fold;
				lastIndex = index;

				if (line >= fold.getEnd()) {
					start = index + 1;
				} else if (line <= fold.getStart()) {
					end = index - 1;
				} else {
					return index;
				}
			}

			if (lastFold == null || lastFold.getStart() > line) {
				return lastIndex;
			} else {
				return lastIndex + 1;
			}
		}

		return -1;
	}

	private boolean isFolded(int line) {
		return getFold(line) != null;
	}

	private void addFold(Fold fold) {
		List<Fold> folds = getFolds();
		int index = getNextFoldIndex(fold.getStart());

		if (index != -1 && index < folds.size()) {
			folds.add(index, fold);
			return;
		}

		folds.add(fold);
	}

	private void updateFolds() {
		if (isVisible()) {
			List<Fold> folds = getFolds();
			List<Fold> oldFolds = new ArrayList<Fold>(folds);

			// Update the folds, to make sure no line has been deleted.
			for (Fold fold : oldFolds) {
				if (!fold.isValid()) {
					fold.update();
					folds.remove(fold);

					List<Fold> children = fold.getChildren();
					for (Fold child : children) {
						addFold(child);
					}
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

	private int getNextLineNumber(int line) throws BadLocationException {
		line++;

		Fold fold = getFold(line);

		if (fold != null) {
			line = fold.getEnd();
		}

		return line;
	}

	private int getLastFoldLine(int start, int limit) {
		int closing = getFoldClosingLine(start, limit);

		if (closing > start + 1) {
			return closing;
		}

		return -1;
	}

	/**
	 * Return the closing line of the fold, the first fold should be located on
	 * the first line.
	 * 
	 * If no fold start can be found on the start line, no attempt should be
	 * taken to find the a fold start in subsequent lines and the start line
	 * should be returned.
	 * 
	 * The closing fold should be located on the line returned however the line
	 * returned should not be higher than the limit. If the closing fold cannot
	 * be found before the limit, the limit should be returned.
	 * 
	 * @param start
	 *            the begin line of the fold.
	 * @param limit
	 *            the end-line limit after which there is no need to parse the
	 *            document anymore.
	 * @return the last line of the fold, not bigger than the end-line limit.
	 */
	protected abstract int getFoldClosingLine(int start, int limit);

	protected void drawLine(Graphics g, int x, int y) {
		g.drawLine(x + 4, y, x + 4, y + getLineHeight());
	}

	protected void drawEnd(Graphics g, int x, int y) {
		g.drawLine(x + 4, y, x + 4, y + (getLineHeight() / 2));
		g.drawLine(x + 4, y + (getLineHeight() / 2), x + 8, y + (getLineHeight() / 2));
	}

	protected void drawOpenFold(Graphics g, int x, int y) {
		Polygon polygon = new Polygon();
		polygon.addPoint(0, 1);
		polygon.addPoint(8, 1);
		polygon.addPoint(8, 9);
		polygon.addPoint(0, 9);
		polygon.translate(x, y);

		g.drawPolygon(polygon);

		g.drawLine(x + 2, y + 5, x + 6, y + 5);
	}

	protected void drawClosedFold(Graphics g, int x, int y) {
		Polygon polygon = new Polygon();
		polygon.addPoint(0, 1);
		polygon.addPoint(8, 1);
		polygon.addPoint(8, 9);
		polygon.addPoint(0, 9);
		polygon.translate(x, y);

		g.drawPolygon(polygon);

		g.drawLine(x + 2, y + 5, x + 6, y + 5);
		g.drawLine(x + 4, y + 3, x + 4, y + 7);
	}
}
