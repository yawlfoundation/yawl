/*
 * @(#)PlainView.java	1.71 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.bounce.text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Implements View interface for a simple multi-line text view that has text in
 * one font and color. The view represents each child element as a line of text.
 * 
 * @author Timothy Prinzing
 * @version 1.71 01/23/03
 * @see View
 */
public class FoldingPlainView extends PlainView {

	/**
	 * Constructs a new PlainView wrapped on an element.
	 * 
	 * @param elem
	 *            the element
	 */
	public FoldingPlainView(Element elem) {
		super(elem);
	}

	/**
	 * Renders a line of text, suppressing whitespace at the end and expanding
	 * any tabs. This is implemented to make calls to the methods
	 * <code>drawUnselectedText</code> and <code>drawSelectedText</code> so
	 * that the way selected and unselected text are rendered can be customized.
	 * 
	 * @param lineIndex
	 *            the line to draw >= 0
	 * @param g
	 *            the <code>Graphics</code> context
	 * @param x
	 *            the starting X position >= 0
	 * @param y
	 *            the starting Y position >= 0
	 * @see #drawUnselectedText
	 * @see #drawSelectedText
	 */
	protected void drawLine(int lineIndex, Graphics g, int x, int y) {
		Element line = getElement().getElement(lineIndex);
		Element elem;

		try {
			if (line.isLeaf()) {
				drawElement(lineIndex, line, g, x, y);
			} else {
				// this line contains the composed text.
				int count = line.getElementCount();

				for (int i = 0; i < count; i++) {
					elem = line.getElement(i);
					x = drawElement(lineIndex, elem, g, x, y);
				}
			}
		} catch (BadLocationException e) {
			throw new RuntimeException("State Invariant Error", e);
		}
	}

	/**
	 * Provides a way to determine the next visually represented model location
	 * at which one might place a caret. Some views may not be visible, they
	 * might not be in the same order found in the model, or they just might not
	 * allow access to some of the locations in the model.
	 * 
	 * @param pos
	 *            the position to convert >= 0
	 * @param a
	 *            the allocated region in which to render
	 * @param direction
	 *            the direction from the current position that can be thought of
	 *            as the arrow keys typically found on a keyboard. This will be
	 *            one of the following values:
	 *            <ul>
	 *            <li>SwingConstants.WEST
	 *            <li>SwingConstants.EAST
	 *            <li>SwingConstants.NORTH
	 *            <li>SwingConstants.SOUTH
	 *            </ul>
	 * @return the location within the model that best represents the next
	 *         location visual position
	 * @exception BadLocationException
	 * @exception IllegalArgumentException
	 *                if <code>direction</code> doesn't have one of the legal
	 *                values above
	 */
	public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet) throws BadLocationException {
		int result = -1;

		if (direction == WEST || direction == EAST) {
			result = super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);

			int index = getElement().getElementIndex(result);

			if (!isVisible(index)) {
				if (direction == WEST) {
					Element e = getPreviousVisibleElement(index);
					result = e.getEndOffset() - 1;
				} else if (direction == EAST) {
					Element e = getNextVisibleElement(index);
					result = e.getStartOffset();
				}
			}
		} else if (direction == NORTH || direction == SOUTH) {
			JTextComponent target = (JTextComponent) getContainer();
			Caret c = (target != null) ? target.getCaret() : null;

			Point mcp;

			if (c != null) {
				mcp = c.getMagicCaretPosition();
			} else {
				mcp = null;
			}

			int x;

			if (mcp == null) {
				Rectangle loc = target.modelToView(pos);
				x = (loc == null) ? 0 : loc.x;
			} else {
				x = mcp.x;
			}

			if (direction == NORTH) {
				result = getPositionAbove(target, pos, x);
			} else {
				result = getPositionBelow(target, pos, x);
			}
		}

		return result;
	}

	private Element getNextVisibleElement(int index) {
		Fold fold = getFold(index);

		if (fold != null) {
			return getElement().getElement(fold.getEnd());
		}

		return null;
	}

	private Element getPreviousVisibleElement(int index) {
		Fold fold = getFold(index);

		if (fold != null) {
			return getElement().getElement(fold.getStart());
		}

		return null;
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

	private int drawElement(int lineIndex, Element elem, Graphics g, int x, int y) throws BadLocationException {
		int p0 = elem.getStartOffset();
		int p1 = elem.getEndOffset();
		p1 = Math.min(getDocument().getLength(), p1);

		if (lineIndex == 0) {
			x += firstLineOffset;
		}

		if (sel0 == sel1) {
			// no selection
			x = drawUnselectedText(g, x, y, p0, p1);
		} else if ((p0 >= sel0 && p0 <= sel1) && (p1 >= sel0 && p1 <= sel1)) {
			x = drawSelectedText(g, x, y, p0, p1);
		} else if (sel0 >= p0 && sel0 <= p1) {
			if (sel1 >= p0 && sel1 <= p1) {
				x = drawUnselectedText(g, x, y, p0, sel0);
				x = drawSelectedText(g, x, y, sel0, sel1);
				x = drawUnselectedText(g, x, y, sel1, p1);
			} else {
				x = drawUnselectedText(g, x, y, p0, sel0);
				x = drawSelectedText(g, x, y, sel0, p1);
			}
		} else if (sel1 >= p0 && sel1 <= p1) {
			x = drawSelectedText(g, x, y, p0, sel1);
			x = drawUnselectedText(g, x, y, sel1, p1);
		} else {
			x = drawUnselectedText(g, x, y, p0, p1);
		}

		return x;
	}

	/**
	 * Renders the given range in the model as normal unselected text. Uses the
	 * foreground or disabled color to render the text.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the starting X coordinate >= 0
	 * @param y
	 *            the starting Y coordinate >= 0
	 * @param p0
	 *            the beginning position in the model >= 0
	 * @param p1
	 *            the ending position in the model >= 0
	 * @return the X location of the end of the range >= 0
	 * @exception BadLocationException
	 *                if the range is invalid
	 */
	protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
		g.setColor(unselected);
		Document doc = getDocument();
		Segment s = SegmentCache.getSharedSegment();
		doc.getText(p0, p1 - p0, s);
		int ret = Utilities.drawTabbedText(s, x, y, g, this, p0);
		SegmentCache.releaseSharedSegment(s);
		return ret;
	}

	/**
	 * Renders the given range in the model as selected text. This is
	 * implemented to render the text in the color specified in the hosting
	 * component. It assumes the highlighter will render the selected
	 * background.
	 * 
	 * @param g
	 *            the graphics context
	 * @param x
	 *            the starting X coordinate >= 0
	 * @param y
	 *            the starting Y coordinate >= 0
	 * @param p0
	 *            the beginning position in the model >= 0
	 * @param p1
	 *            the ending position in the model >= 0
	 * @return the location of the end of the range
	 * @exception BadLocationException
	 *                if the range is invalid
	 */
	protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
		g.setColor(selected);
		Document doc = getDocument();
		Segment s = SegmentCache.getSharedSegment();
		doc.getText(p0, p1 - p0, s);
		int ret = Utilities.drawTabbedText(s, x, y, g, this, p0);
		SegmentCache.releaseSharedSegment(s);
		return ret;
	}

	/**
	 * Checks to see if the font metrics and longest line are up-to-date.
	 * 
	 * @since 1.4
	 */
	protected void updateMetrics() {
		Component host = getContainer();
		Font f = host.getFont();
		// FoldingMargin margin =
		// ((JEditorPane)getContainer()).getFoldingMargin();

		if (font != f) {
			// The font changed, we need to recalculate the
			// longest line.
			calculateLongestLine();
			tabSize = getTabSize() * metrics.charWidth('m');
		}

		if (foldsUpdated()) {
			calculateLongestLine();
		}
	}
	
	// maybe there should-be a folds list, listening to fold events???
	private boolean foldsUpdated() {
		Object updated = getDocument().getProperty(Fold.FOLDS_UPDATED_ATTRIBUTE);
		getDocument().putProperty(Fold.FOLDS_UPDATED_ATTRIBUTE, false);
		
		if (updated instanceof Boolean) {
			return (Boolean)updated; 
		}

		return false;
	}

	// ---- View methods ----------------------------------------------------

	/**
	 * Determines the preferred span for this view along an axis.
	 * 
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @return the span the view would like to be rendered into >= 0. Typically
	 *         the view is told to render into the span that is returned,
	 *         although there is no guarantee. The parent may choose to resize
	 *         or break the view.
	 * @exception IllegalArgumentException
	 *                for an invalid axis
	 */
	public float getPreferredSpan(int axis) {
		updateMetrics();

		switch (axis) {
		case View.X_AXIS:
			return getLineWidth(longLine);

		case View.Y_AXIS:
			return getVisibleElementCount() * metrics.getHeight(); 
		default:
			throw new IllegalArgumentException("Invalid axis: " + axis);
		}
	}

	/**
	 * Renders using the given rendering surface and area on that surface. The
	 * view may need to do layout and create child views to enable itself to
	 * render into the given allocation.
	 * 
	 * @param g
	 *            the rendering surface to use
	 * @param a
	 *            the allocated region to render into
	 * 
	 * @see View#paint
	 */
	public void paint(Graphics g, Shape a) {
		// long time = System.currentTimeMillis();

		Shape originalA = a;
		// a = adjustPaintRegion(a);
		Rectangle alloc = (Rectangle) a;
		tabBase = alloc.x;
		JTextComponent host = (JTextComponent) getContainer();
		g.setFont(host.getFont());
		sel0 = host.getSelectionStart();
		sel1 = host.getSelectionEnd();
		unselected = (host.isEnabled()) ? host.getForeground() : host.getDisabledTextColor();
		Caret c = host.getCaret();
		selected = c.isSelectionVisible() ? host.getSelectedTextColor() : unselected;

		updateMetrics();

		// If the lines are clipped then we don't expend the effort to
		// try and paint them. Since all of the lines are the same height
		// with this object, determination of what lines need to be repainted
		// is quick.
		Rectangle clip = g.getClipBounds();

		int fontHeight = metrics.getHeight();
		int heightBelow = (alloc.y + alloc.height) - (clip.y + clip.height);
		int heightAbove = clip.y - alloc.y;

		int linesBelow = Math.max(0, heightBelow / fontHeight);
		int linesAbove = Math.max(0, heightAbove / fontHeight);
		int linesTotal = alloc.height / fontHeight;

		if (alloc.height % fontHeight != 0) {
			linesTotal++;
		}

		// update the visible lines
		Rectangle lineArea = lineToRect(a, convertFromVisibleIndex(linesAbove));

		int y = lineArea.y + metrics.getAscent();
		int x = lineArea.x;
		Element map = getElement();

		int lineCount = getVisibleElementCount(); // map.getElementCount();
		int endLine = convertFromVisibleIndex(Math.min(lineCount, linesTotal - linesBelow));

		Highlighter h = host.getHighlighter();
		LayeredHighlighter dh = (h instanceof LayeredHighlighter) ? (LayeredHighlighter) h : null;

		for (int line = convertFromVisibleIndex(linesAbove); line < endLine; line++) {
			if (isVisible(line)) {
				if (dh != null) {
					Element lineElement = map.getElement(line);

					if (line == lineCount) {
						dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset(), originalA, host, this);
					} else {
						dh.paintLayeredHighlights(g, lineElement.getStartOffset(), lineElement.getEndOffset() - 1, originalA, host, this);
					}
				}

				drawLine(line, g, x, y);
				y += fontHeight;

				if (line == 0) {
					// This should never really happen, in so far as if
					// firstLineOffset is non 0, there should only be one
					// line of text.
					x -= firstLineOffset;
				}
			}
		}
	}

	/**
	 * Should return a shape ideal for painting based on the passed in Shape
	 * <code>a</code>. This is useful if painting in a different region. The
	 * default implementation returns <code>a</code>.
	 */
	// Shape adjustPaintRegion(Shape a) {
	// return a;
	// }
	private boolean isVisible(int line) {
		return !(getFold(line) != null);
	}

	private int getVisibleElementCount() {
		Element map = getElement();
		int result = map.getElementCount();

		List<Fold> folds = getFolds();

		if (folds != null) {
			for (int i = 0; i < folds.size(); i++) {
				result = result - ((((Fold) folds.get(i)).getEnd() - ((Fold) folds.get(i)).getStart()) - 1);
			}
		}

		return result;
	}

	private List<Fold> getFolds() {
		return getFolds((JEditorPane) getContainer());
	}

	/**
	 * Provides a mapping from the document model coordinate space to the
	 * coordinate space of the view mapped to it.
	 * 
	 * @param pos
	 *            the position to convert >= 0
	 * @param a
	 *            the allocated region to render into
	 * @return the bounding box of the given position
	 * @exception BadLocationException
	 *                if the given position does not represent a valid location
	 *                in the associated document
	 * @see View#modelToView
	 */
	public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
		// line coordinates
		Document doc = getDocument();
		Element map = getElement();
		int lineIndex = getElementIndex(pos);

		if (!isVisible(lineIndex)) {
			return null;
		}

		Rectangle lineArea = null;

		lineArea = lineToRect(a, lineIndex);

		// determine span from the start of the line
		tabBase = lineArea.x;
		Element line = map.getElement(lineIndex);

		int p0 = line.getStartOffset();
		Segment s = SegmentCache.getSharedSegment();
		doc.getText(p0, pos - p0, s);
		int xOffs = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, p0);
		SegmentCache.releaseSharedSegment(s);

		// fill in the results and return
		lineArea.x += xOffs;
		lineArea.width = 1;
		lineArea.height = metrics.getHeight();

		return lineArea;
	}

	private int getElementIndex(int pos) {
		Element map = getElement();
		int index = map.getElementIndex(pos);

		// if ( index > 10 && index < 100) {
		// index = index + 90;
		// } else if ( index > 100) {
		// index = index - 90;
		// }

		return index;
	}

	/**
	 * Provides a mapping from the view coordinate space to the logical
	 * coordinate space of the model.
	 * 
	 * @param fx
	 *            the X coordinate >= 0
	 * @param fy
	 *            the Y coordinate >= 0
	 * @param a
	 *            the allocated region to render into
	 * @return the location within the model that best represents the given
	 *         point in the view >= 0
	 * @see View#viewToModel
	 */
	public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
		// PENDING(prinz) properly calculate bias
		bias[0] = Position.Bias.Forward;

		Rectangle alloc = a.getBounds();
		Document doc = getDocument();
		int x = (int) fx;
		int y = (int) fy;

		if (y < alloc.y) {
			// above the area covered by this icon, so the the position
			// is assumed to be the start of the coverage for this view.
			return getStartOffset();
		} else if (y > alloc.y + alloc.height) {
			// below the area covered by this icon, so the the position
			// is assumed to be the end of the coverage for this view.
			return getEndOffset() - 1;
		} else {
			// positioned within the coverage of this view vertically,
			// so we figure out which line the point corresponds to.
			// if the line is greater than the number of lines contained, then
			// simply use the last line as it represents the last possible place
			// we can position to.
			Element map = doc.getDefaultRootElement();
			int lineIndex = Math.abs((y - alloc.y) / metrics.getHeight());

			lineIndex = convertFromVisibleIndex(lineIndex);

			if (lineIndex >= map.getElementCount()) {
				return getEndOffset() - 1;
			}

			Element line = map.getElement(lineIndex);

			// int dx = 0;
			if (lineIndex == 0) {
				alloc.x += firstLineOffset;
				alloc.width -= firstLineOffset;
			}

			if (x < alloc.x) {
				// point is to the left of the line
				return line.getStartOffset();
			} else if (x > alloc.x + alloc.width) {
				// point is to the right of the line
				return line.getEndOffset() - 1;
			} else {
				// Determine the offset into the text
				try {
					int p0 = line.getStartOffset();
					int p1 = line.getEndOffset() - 1;
					Segment s = SegmentCache.getSharedSegment();
					doc.getText(p0, p1 - p0, s);
					tabBase = alloc.x;
					int offs = p0 + Utilities.getTabbedTextOffset(s, metrics, tabBase, x, this, p0);
					SegmentCache.releaseSharedSegment(s);
					return offs;
				} catch (BadLocationException e) {
					// should not happen
					return -1;
				}
			}
		}
	}

	// convert from the currently visible lines to all lines
	private int convertFromVisibleIndex(int index) {
		List<Fold> folds = getFolds();

		if (folds != null && folds.size() > 0) {
			for (int i = 0; i < folds.size(); i++) {
				Fold fold = (Fold) folds.get(i);

				if (fold.getStart() < index) {
					index = index + ((fold.getEnd() - fold.getStart()) - 1);
				} else {
					break;
				}
			}
		}

		return index;
	}

	// convert from all lines to only the currently visible lines
	private int convertFromAllIndex(int index) {
		List<Fold> folds = getFolds();

		if (folds != null && folds.size() > 0) {
			int oldIndex = index;

			for (int i = 0; i < folds.size(); i++) {
				Fold fold = folds.get(i);

				if (fold.getStart() < oldIndex) {
					index = index - ((fold.getEnd() - fold.getStart()) - 1);
				} else {
					break;
				}
			}
		}

		return index;
	}

	/**
	 * Gives notification that something was inserted into the document in a
	 * location that this view is responsible for.
	 * 
	 * @param changes
	 *            the change information from the associated document
	 * @param a
	 *            the current allocation of the view
	 * @param f
	 *            the factory to use to rebuild if the view has children
	 * @see View#insertUpdate
	 */
	public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
		updateDamage(changes, a, f);
	}

	/**
	 * Gives notification that something was removed from the document in a
	 * location that this view is responsible for.
	 * 
	 * @param changes
	 *            the change information from the associated document
	 * @param a
	 *            the current allocation of the view
	 * @param f
	 *            the factory to use to rebuild if the view has children
	 * @see View#removeUpdate
	 */
	public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
		updateDamage(changes, a, f);
	}

	/**
	 * Gives notification from the document that attributes were changed in a
	 * location that this view is responsible for.
	 * 
	 * @param changes
	 *            the change information from the associated document
	 * @param a
	 *            the current allocation of the view
	 * @param f
	 *            the factory to use to rebuild if the view has children
	 * @see View#changedUpdate
	 */
	public void changedUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
		updateDamage(changes, a, f);
	}

	/**
	 * Sets the size of the view. This should cause layout of the view along the
	 * given axis, if it has any layout duties.
	 * 
	 * @param width
	 *            the width >= 0
	 * @param height
	 *            the height >= 0
	 */
	public void setSize(float width, float height) {
		super.setSize(width, height);
		updateMetrics();
	}

	// --- TabExpander methods ------------------------------------------

	/**
	 * Returns the next tab stop position after a given reference position. This
	 * implementation does not support things like centering so it ignores the
	 * tabOffset argument.
	 * 
	 * @param x
	 *            the current position >= 0
	 * @param tabOffset
	 *            the position within the text stream that the tab occurred at >=
	 *            0.
	 * @return the tab stop, measured in points >= 0
	 */
	public float nextTabStop(float x, int tabOffset) {
		if (tabSize == 0) {
			return x;
		}

		int ntabs = (((int) x) - tabBase) / tabSize;
		return tabBase + ((ntabs + 1) * tabSize);
	}

	// --- local methods ------------------------------------------------

	/*
	 * Repaint the region of change covered by the given document event. Damages
	 * the line that begins the range to cover the case when the insert/remove
	 * is only on one line. If lines are added or removed, damages the whole
	 * view. The longest line is checked to see if it has changed.
	 * 
	 * @since 1.4
	 */
	protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f) {
		Component host = getContainer();
		updateMetrics();
		Element elem = getElement();
		DocumentEvent.ElementChange ec = changes.getChange(elem);

		Element[] added = (ec != null) ? ec.getChildrenAdded() : null;
		Element[] removed = (ec != null) ? ec.getChildrenRemoved() : null;
		if (((added != null) && (added.length > 0)) || ((removed != null) && (removed.length > 0))) {
			// lines were added or removed...
			if (added != null) {
				int currWide = getLineWidth(longLine);

				for (int i = 0; i < added.length; i++) {
					int w = getLineWidth(added[i]);
					if (w > currWide) {
						currWide = w;
						longLine = added[i];
					}
				}
			}
			if (removed != null) {
				for (int i = 0; i < removed.length; i++) {
					if (removed[i] == longLine) {
						calculateLongestLine();
						break;
					}
				}
			}
			preferenceChanged(null, true, true);
			host.repaint();
		} else {
			Element map = getElement();
			int line = getElementIndex(changes.getOffset());
			damageLineRange(line, line, a, host);

			if (changes.getType() == DocumentEvent.EventType.INSERT) {
				// check to see if the line is longer than current
				// longest line.
				int w = getLineWidth(longLine);
				Element e = map.getElement(line);
				if (e == longLine) {
					preferenceChanged(null, true, false);
				} else if (getLineWidth(e) > w) {
					longLine = e;
					preferenceChanged(null, true, false);
				}
			} else if (changes.getType() == DocumentEvent.EventType.REMOVE) {
				if (map.getElement(line) == longLine) {
					// removed from longest line... recalc
					calculateLongestLine();
					preferenceChanged(null, true, false);
				}
			}
		}
	}

	/**
	 * Repaint the given line range.
	 * 
	 * @param host
	 *            the component hosting the view (used to call repaint)
	 * @param a
	 *            the region allocated for the view to render into
	 * @param line0
	 *            the starting line number to repaint. This must be a valid line
	 *            number in the model.
	 * @param line1
	 *            the ending line number to repaint. This must be a valid line
	 *            number in the model.
	 * @since 1.4
	 */
	protected void damageLineRange(int line0, int line1, Shape a, Component host) {
		if (a != null) {
			Rectangle area0 = lineToRect(a, line0);
			Rectangle area1 = lineToRect(a, line1);
			if ((area0 != null) && (area1 != null)) {
				Rectangle damage = area0.union(area1);
				host.repaint(damage.x, damage.y, damage.width, damage.height);
			} else {
				host.repaint();
			}
		}
	}

	/**
	 * Determine the rectangle that represents the given line.
	 * 
	 * @param a
	 *            the region allocated for the view to render into
	 * @param line
	 *            the line number to find the region of. This must be a valid
	 *            line number in the model.
	 * @since 1.4
	 */
	protected Rectangle lineToRect(Shape a, int line) {
		Rectangle r = null;
		updateMetrics();

		if (metrics != null) {
			Rectangle alloc = a.getBounds();
			if (line == 0) {
				alloc.x += firstLineOffset;
				alloc.width -= firstLineOffset;
			}

			line = convertFromAllIndex(line);

			r = new Rectangle(alloc.x, alloc.y + (line * metrics.getHeight()), alloc.width, metrics.getHeight());
		}
		return r;
	}

	/**
	 * Iterate over the lines represented by the child elements of the element
	 * this view represents, looking for the line that is the longest. The
	 * <em>longLine</em> variable is updated to represent the longest line
	 * contained. The <em>font</em> variable is updated to indicate the font
	 * used to calculate the longest line.
	 */
	public void calculateLongestLine() {
		Component c = getContainer();
		font = c.getFont();
		metrics = c.getFontMetrics(font);
		// Document doc = getDocument();
		Element lines = getElement();
		int n = lines.getElementCount();
		int maxWidth = -1;

		for (int i = 0; i < n; i++) {
			if (isVisible(i)) {
				Element line = lines.getElement(i);
				int w = getLineWidth(line);

				if (w > maxWidth) {
					maxWidth = w;
					longLine = line;
				}
			}
		}
	}

	/**
	 * Calculate the width of the line represented by the given element. It is
	 * assumed that the font and font metrics are up-to-date.
	 */
	private int getLineWidth(Element line) {
		int p0 = line.getStartOffset();
		int p1 = line.getEndOffset();
		int w;
		Segment s = SegmentCache.getSharedSegment();

		try {
			line.getDocument().getText(p0, p1 - p0, s);
			w = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, p0);
		} catch (BadLocationException ble) {
			w = 0;
		}

		SegmentCache.releaseSharedSegment(s);

		return w;
	}

	// --- member variables -----------------------------------------------

	/**
	 * Font metrics for the current font.
	 */
	protected FontMetrics metrics;

	/**
	 * The current longest line. This is used to calculate the preferred width
	 * of the view. Since the calculation is potentially expensive we try to
	 * avoid it by stashing which line is currently the longest.
	 */
	Element longLine;

	/**
	 * Font used to calculate the longest line... if this changes we need to
	 * recalculate the longest line
	 */
	Font font;

	Segment lineBuffer;
	int tabSize;
	int tabBase;

	int sel0;
	int sel1;
	Color unselected;
	Color selected;

	/**
	 * Offset of where to draw the first character on the first line. This is a
	 * hack and temporary until we can better address the problem of text
	 * measuring. This field is actually never set directly in PlainView, but by
	 * FieldView.
	 */
	int firstLineOffset;
	
	/**
	 * Determines the starting row model position of the row that contains the
	 * specified model position. The component given must have a size to compute
	 * the result. If the component doesn't have a size a value of -1 will be
	 * returned.
	 * 
	 * @param c
	 *            the editor
	 * @param offs
	 *            the offset in the document >= 0
	 * @return the position >= 0 if the request can be computed, otherwise a
	 *         value of -1 will be returned.
	 * @exception BadLocationException
	 *                if the offset is out of range
	 */
	private static final int getRowStart(JTextComponent c, int offs) throws BadLocationException {
		Rectangle r = c.modelToView(offs);
		if (r == null) {
			return -1;
		}

		int lastOffs = offs;
		int y = r.y;

		while ((r != null) && (y == r.y)) {
			offs = lastOffs;
			lastOffs--;

			if ((lastOffs >= 0)) {
				Rectangle temp = null;

				try {
					temp = c.modelToView(lastOffs);
				} catch (Exception x) {
					temp = null;
				}

				if (temp != null) {
					r = temp;
				} else {
					List<Fold> folds = getFolds(c);
					Document doc = c.getDocument();
					Element root = doc.getDefaultRootElement();
					int index = root.getElementIndex(lastOffs);

					for (int i = 0; i < folds.size(); i++) {
						Fold fold = (Fold) folds.get(i);

						if (fold.contains(index)) {
							lastOffs = root.getElement(fold.getStart()).getEndOffset();
							break;
						}
					}
				}
			} else {
				r = null;
			}
		}

		return offs;
	}

	/**
	 * Determines the ending row model position of the row that contains the
	 * specified model position. The component given must have a size to compute
	 * the result. If the component doesn't have a size a value of -1 will be
	 * returned.
	 * 
	 * @param c
	 *            the editor
	 * @param offs
	 *            the offset in the document >= 0
	 * @return the position >= 0 if the request can be computed, otherwise a
	 *         value of -1 will be returned.
	 * @exception BadLocationException
	 *                if the offset is out of range
	 */
	private static final int getRowEnd(JTextComponent c, int offs) throws BadLocationException {
		Rectangle r = c.modelToView(offs);

		if (r == null) {
			return -1;
		}

		int n = c.getDocument().getLength();
		int lastOffs = offs;
		int y = r.y;

		while (r != null && y == r.y) {
			offs = lastOffs;
			lastOffs++;

			if ((lastOffs <= n)) {
				Rectangle temp = null;

				try {
					temp = c.modelToView(lastOffs);
				} catch (Exception x) {
					temp = null;
				}

				if (temp != null) {
					r = temp;
				} else {
					List<Fold> folds = getFolds(c);
					Document doc = c.getDocument();
					Element root = doc.getDefaultRootElement();
					int index = root.getElementIndex(lastOffs);

					for (int i = 0; i < folds.size(); i++) {
						Fold fold = (Fold) folds.get(i);

						if (fold.contains(index)) {
							lastOffs = root.getElement(fold.getEnd()).getStartOffset() - 1;
							break;
						}
					}
				}
			} else {
				r = null;
			}
		}

		return offs;
	}

	@SuppressWarnings("unchecked")
	private static final List<Fold> getFolds(JTextComponent c) {
		List<Fold> folds = (List<Fold>)c.getDocument().getProperty(Fold.FOLD_LIST_ATTRIBUTE);
		
		if (folds == null) {
			folds = Collections.EMPTY_LIST;
		}
		
		return folds;
	}

	/**
	 * Determines the position in the model that is closest to the given view
	 * location in the row above. The component given must have a size to
	 * compute the result. If the component doesn't have a size a value of -1
	 * will be returned.
	 * 
	 * @param c
	 *            the editor
	 * @param offs
	 *            the offset in the document >= 0
	 * @param x
	 *            the X coordinate >= 0
	 * @return the position >= 0 if the request can be computed, otherwise a
	 *         value of -1 will be returned.
	 * @exception BadLocationException
	 *                if the offset is out of range
	 */
	private static final int getPositionAbove(JTextComponent c, int offs, int x) throws BadLocationException {
		int lastOffs = getRowStart(c, offs) - 1;
		if (lastOffs < 0) {
			return -1;
		}
		int bestSpan = Short.MAX_VALUE;
		int y = 0;
		Rectangle r = null;

		if (lastOffs >= 0) {
			r = c.modelToView(lastOffs);
			y = r.y;
		}

		while ((r != null) && (y == r.y)) {
			int span = Math.abs(r.x - x);
			if (span < bestSpan) {
				offs = lastOffs;
				bestSpan = span;
			}

			lastOffs -= 1;
			try {
				r = (lastOffs >= 0) ? c.modelToView(lastOffs) : null;
			} catch (Exception e) {
				break;
			}
		}
		return offs;
	}

	/**
	 * Determines the position in the model that is closest to the given view
	 * location in the row below. The component given must have a size to
	 * compute the result. If the component doesn't have a size a value of -1
	 * will be returned.
	 * 
	 * @param c
	 *            the editor
	 * @param offs
	 *            the offset in the document >= 0
	 * @param x
	 *            the X coordinate >= 0
	 * @return the position >= 0 if the request can be computed, otherwise a
	 *         value of -1 will be returned.
	 * @exception BadLocationException
	 *                if the offset is out of range
	 */
	private static final int getPositionBelow(JTextComponent c, int offs, int x) throws BadLocationException {
		int lastOffs = getRowEnd(c, offs) + 1;
		if (lastOffs <= 0) {
			return -1;
		}
		int bestSpan = Short.MAX_VALUE;
		int n = c.getDocument().getLength();
		int y = 0;
		Rectangle r = null;
		if (lastOffs <= n) {
			r = c.modelToView(lastOffs);
			y = r.y;
		}
		while ((r != null) && (y == r.y)) {
			int span = Math.abs(x - r.x);
			if (span < bestSpan) {
				offs = lastOffs;
				bestSpan = span;
			}
			lastOffs += 1;
			try {
				r = (lastOffs <= n) ? c.modelToView(lastOffs) : null;
			} catch (Exception e) {
				break;
			}
		}
		return offs;
	}

	/**
	 * SegmentCache caches <code>Segment</code>s to avoid continually creating
	 * and destroying of <code>Segment</code>s. A common use of this class would
	 * be:
	 * <pre>
	 *   Segment segment = segmentCache.getSegment();
	 *   // do something with segment
	 *   ...
	 *   segmentCache.releaseSegment(segment);
	 * </pre>
	 *
	 * @version 1.3 12/03/01
	 */
	private static class SegmentCache {
	    /**
	     * A global cache.
	     */
	    private static SegmentCache sharedCache = new SegmentCache();

	    /**
	     * A list of the currently unused Segments.
	     */
	    private List<Segment> segments;


	    /**
	     * Returns the shared SegmentCache.
	     */
	    public static SegmentCache getSharedInstance() {
	        return sharedCache;
	    }

	    /**
	     * A convenience method to get a Segment from the shared
	     * <code>SegmentCache</code>.
	     */
	    public static Segment getSharedSegment() {
	        return getSharedInstance().getSegment();
	    }

	    /**
	     * A convenience method to release a Segment to the shared
	     * <code>SegmentCache</code>.
	     */
	    public static void releaseSharedSegment(Segment segment) {
	        getSharedInstance().releaseSegment(segment);
	    }



	    /**
	     * Creates and returns a SegmentCache.
	     */
	    public SegmentCache() {
	        segments = new ArrayList<Segment>(11);
	    }

	    /**
	     * Returns a <code>Segment</code>. When done, the <code>Segment</code>
	     * should be recycled by invoking <code>releaseSegment</code>.
	     */
	    public Segment getSegment() {
	        synchronized(this) {
	            int size = segments.size();

	            if (size > 0) {
	                return (Segment)segments.remove(size - 1);
	            }
	        }
	        return new CachedSegment();
	    }

	    /**
	     * Releases a Segment. You should not use a Segment after you release it,
	     * and you should NEVER release the same Segment more than once, eg:
	     * <pre>
	     *   segmentCache.releaseSegment(segment);
	     *   segmentCache.releaseSegment(segment);
	     * </pre>
	     * Will likely result in very bad things happening!
	     */
	    public void releaseSegment(Segment segment) {
	        if (segment instanceof CachedSegment) {
	            synchronized(this) {
	                segment.array = null;
	                segment.count = 0;
	                segments.add(segment);
	            }
	        }
	    }


	    /**
	     * CachedSegment is used as a tagging interface to determine if
	     * a Segment can successfully be shared.
	     */
	    private static class CachedSegment extends Segment {
	    }
	}
}
