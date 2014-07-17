/*
 * $Id: XMLView.java,v 1.4 2008/04/16 19:36:18 edankert Exp $
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

package org.bounce.text.xml;

import org.bounce.text.SyntaxHighlightingScanner;
import org.bounce.text.SyntaxHighlightingView;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;
import java.io.IOException;

/**
 * The XML View uses the XML scanner to determine the style (font, color) of the
 * text that it renders.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 * @version $Revision: 1.4 $, $Date: 2008/04/16 19:36:18 $
 */
public class XMLView extends SyntaxHighlightingView {
	/**
	 * Construct a colorized view of xml text for the element. Gets the current
	 * document and creates a new Scanner object.
	 * 
	 * @param context
	 *            the styles used to colorize the view.
	 * @param elem
	 *            the element to create the view for.
	 * @throws IOException
	 *             input/output exception while reading document
	 */
	public XMLView(XMLScanner scanner, StyleContext context, Element elem) throws IOException {
		super(scanner, context, elem);
	}

	// Update the scanner to point to the '<' begin token.
	protected void updateScanner(SyntaxHighlightingScanner scanner, Document doc, int start, int end) {
		XMLViewUtilities.updateScanner(scanner, doc, start, end);
	}

	@Override
	protected boolean isErrorHighlighting() {
		Object errorHighlighting = getDocument().getProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE);

		if (errorHighlighting != null) {
			return (Boolean) errorHighlighting;
		}

		return false;
	}
}