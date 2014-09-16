/*
 * $Id: XMLEditorKit.java,v 1.5 2008/01/28 21:02:14 edankert Exp $
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
package org.bounce.text.xml;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * The XML editor kit supports handling of editing XML content. It supports
 * syntax highlighting, line wrapping, automatic indentation and tag completion.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * <pre><code>
 * JEditorPane editor = new JEditorPane(); 
 * 
 * // Instantiate a XMLEditorKit with wrapping enabled.
 * XMLEditorKit kit = new XMLEditorKit( true); 
 * 
 * // Set the wrapping style.
 * kit.setWrapStyleWord(true);
 * kit.setAutoIndentation(true);
 * kit.setTagCompletion(true);
 * 
 * editor.setEditorKit( kit); 
 * 
 * // Set the font style.
 * editor.setFont( new Font( &quot;Courier&quot;, Font.PLAIN, 12)); 
 * 
 * // Set the tab size
 * editor.getDocument().putProperty( PlainDocument.tabSizeAttribute, new Integer(4));
 * 
 * // Set a style
 * kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color( 255, 0, 0), Font.BOLD);
 * 
 * // Put the editor in a panel that will force it to resize, when a different view is choosen.
 * ScrollableEditorPanel editorPanel = new ScrollableEditorPanel( editor);
 * 
 * JScrollPane scroller = new JScrollPane( editorPanel);
 * 
 * ...
 * </code></pre>
 * 
 * <p>
 * To switch between line wrapped and non wrapped views use:
 * </p>
 * 
 * <pre><code>
 * ...
 * 
 * XMLEditorKit kit = (XMLEditorKit)editor.getEditorKit();
 * kit.setLineWrappingEnabled( false);
 * 
 * // Update the UI and create a new view...
 * editor.updateUI();
 * 
 * ...
 * </code></pre>
 * 
 * @version $Revision: 1.5 $, $Date: 2008/01/28 21:02:14 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLEditorKit extends DefaultEditorKit implements XMLStyleConstants, KeyListener {
	private static final long serialVersionUID = 6303638967973333256L;

	public final static String ERROR_HIGHLIGHTING_ATTRIBUTE = "errorHighlighting";
	
	private boolean tagCompletion = false;
	private boolean autoIndent = false;

	private StyleContext context = null;
	private ViewFactory factory = null;

	/**
	 * Constructs an XMLEditorKit with view factory and Context, but with line
	 * wrapping turned off.
	 */
	public XMLEditorKit() {
		context = new StyleContext();
		
		setStyle(ELEMENT_NAME, new Color(136, 18, 128), Font.PLAIN);
		setStyle(ELEMENT_VALUE, Color.black, Font.PLAIN);
		setStyle(ELEMENT_PREFIX, new Color(136, 18, 128), Font.PLAIN);

		setStyle(ATTRIBUTE_NAME, new Color(153, 69, 0), Font.PLAIN);
		setStyle(ATTRIBUTE_VALUE, new Color(26, 26, 166), Font.PLAIN);
		setStyle(ATTRIBUTE_PREFIX, new Color(153, 69, 0), Font.PLAIN);

		setStyle(NAMESPACE_NAME, new Color(128, 128, 0), Font.PLAIN);
		setStyle(NAMESPACE_VALUE, new Color(63, 95, 191), Font.PLAIN);
		setStyle(NAMESPACE_PREFIX, new Color(128, 128, 0), Font.PLAIN);

		setStyle(ENTITY, new Color(102, 102, 102), Font.PLAIN);
		setStyle(CDATA, new Color(127, 159, 191), Font.PLAIN);
		setStyle(COMMENT, new Color(63, 127, 95), Font.PLAIN);
		setStyle(SPECIAL, new Color(102, 102, 102), Font.PLAIN);

		factory = new XMLViewFactory();
	}

	/**
	 * Get the MIME type of the data that this kit represents support for. This
	 * kit supports the type <code>text/xml</code>.
	 * 
	 * @return the type.
	 */
	public String getContentType() {
		return "text/xml";
	}

	/**
	 * Fetches the XML factory that can produce views for XML Documents.
	 * 
	 * @return the XML factory
	 */
	public ViewFactory getViewFactory() {
		return factory;
	}

	/**
	 * @param enabled true enables the tag completion
	 */
	public final void setTagCompletion(boolean enabled) {
		tagCompletion = enabled;
	}

	/**
	 * @param enabled true enables the auto indentation
	 */
	public final void setAutoIndentation(boolean enabled) {
		autoIndent = enabled;
	}

	/**
	 * Set the style identified by the name.
	 * 
	 * @param token
	 *            the style token
	 * @param foreground
	 *            the foreground color
	 * @param fontStyle
	 *            the font style Plain, Italic or Bold
	 */
	public void setStyle(String token, Color foreground, int fontStyle) {
		Style s = context.getStyle(token);
		
		if (s == null) {
			s = context.addStyle(token, context.new NamedStyle());
		}
			
		StyleConstants.setItalic(s, (fontStyle & Font.ITALIC) > 0);
		StyleConstants.setBold(s, (fontStyle & Font.BOLD) > 0);
		StyleConstants.setForeground(s, foreground);
		
	}
	
	@Override
	public void install(JEditorPane editor) {
		super.install(editor);

		editor.addKeyListener(this);
	}

	@Override
	public void deinstall(JEditorPane editor) {
		super.deinstall(editor);
		
		editor.removeKeyListener(this);
	}

	/**
	 * A simple view factory implementation.
	 */
	class XMLViewFactory implements ViewFactory {
		/**
		 * Creates the XML View.
		 * 
		 * @param elem
		 *            the root element.
		 * @return the XMLView
		 */
		public View create(Element elem) {
			try {
				return new XMLView(new XMLScanner(elem.getDocument()), context, elem);
			} catch (IOException e) {
				// Instead of an IOException, this will return null if the
				// XMLView could not be instantiated.
				// Should never happen.
			}

			return null;
		}
	}
	
	private static void completeTag(Document document, int off) throws BadLocationException {
		StringBuffer endTag = new StringBuffer();

		String text = document.getText(0, off);
		int startTag = text.lastIndexOf('<', off);
		int prefEndTag = text.lastIndexOf('>', off);

		// If there was a start tag and if the start tag is not empty
		// and if the start-tag has not got an end-tag already.
		if ((startTag > 0) && (startTag > prefEndTag) && (startTag < text.length() - 1)) {
			String tag = text.substring(startTag, text.length());
			char first = tag.charAt(1);

			if (first != '/' && first != '!' && first != '?' && !Character.isWhitespace(first)) {
				boolean finished = false;
				char previous = tag.charAt(tag.length() - 1);

				if (previous != '/' && previous != '-') {

					endTag.append("</");

					for (int i = 1; (i < tag.length()) && !finished; i++) {
						char ch = tag.charAt(i);

						if (!Character.isWhitespace(ch)) {
							endTag.append(ch);
						} else {
							finished = true;
						}
					}

					endTag.append(">");
				}
			}
		}

		document.insertString(off, endTag.toString(), null);
	}

	private static void autoIndent(Document document, int off) throws BadLocationException {
		StringBuffer newStr = new StringBuffer("\r\n");
		Element elem = document.getDefaultRootElement().getElement(document.getDefaultRootElement().getElementIndex(off));
		int start = elem.getStartOffset();
		int end = elem.getEndOffset();
		String line = document.getText(start, off - start);
        String following = document.getText(off, end-off);

		boolean finished = false;

		for (int i = 0; (i < line.length()) && !finished; i++) {
			char ch = line.charAt(i);

			if (((ch != '\n') && (ch != '\f') && (ch != '\r')) && Character.isWhitespace(ch)) {
				newStr.append(ch);
			} else {
				finished = true;
			}
		}

		if (isStartElement(line) && ! (following != null && following.startsWith("</"))) {
			newStr.append("\t");
		}

		document.insertString(off, newStr.toString(), null);
	}
	
	// Tries to find out if the line finishes with an element start
	private static boolean isStartElement(String line) {
		boolean result = false;

		int first = line.lastIndexOf("<");
		int last = line.lastIndexOf(">");

		if (last < first) { // In the Tag
			result = true;
		} else {
			int firstEnd = line.lastIndexOf("</");
			int lastEnd = line.lastIndexOf("/>");

			// Last Tag is not an End Tag
			if ((firstEnd != first) && ((lastEnd + 1) != last)) {
				result = true;
			}
		}

		return result;
	}

	public void keyPressed(KeyEvent event) {
		JEditorPane editor = (JEditorPane)event.getSource();
		
		if (event.getKeyChar() == '>' && tagCompletion) {
			try {
				int pos = editor.getCaretPosition();
				completeTag(editor.getDocument(), pos);
				editor.setCaretPosition(pos);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (event.getKeyChar() == '\n' && autoIndent) {
			try {
				autoIndent(editor.getDocument(), editor.getCaretPosition());
				event.consume();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

	}

	public void keyReleased(KeyEvent keyevent) {}

	public void keyTyped(KeyEvent keyevent) {}
}