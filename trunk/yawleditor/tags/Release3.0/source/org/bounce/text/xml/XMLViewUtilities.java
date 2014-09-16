/*
 * $Id: XMLViewUtilities.java,v 1.5 2009/01/22 22:14:59 edankert Exp $
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
 * @version $Revision: 1.5 $, $Date: 2009/01/22 22:14:59 $
 */
class XMLViewUtilities {
	// Update the scanner to point to the '<' begin token.
	public static void updateScanner(SyntaxHighlightingScanner scanner, Document doc, int start, int end) {
		try {
			if (!scanner.isValid()) {
				scanner.setRange(getTagEnd(doc, start), end);
				scanner.setValid(true);
			}

			while (scanner.getEndOffset() <= start && end > scanner.getEndOffset()) {
				scanner.scan();
			}
		} catch (IOException e) {
			// can't adjust scanner... calling logic
			// will simply render the remaining text.
			// e.printStackTrace();
		}
	}

	// Return the end position of the current tag.
	private static int getTagEnd(Document doc, int p) {
		int elementEnd = 0;

		if (p > 0) {
			try {
				int index;

				String s = doc.getText(0, p);
				int cdataStart = s.lastIndexOf("<![CDATA[");

				if (cdataStart > 0 && cdataStart > s.lastIndexOf("]]>")) {
 					index = s.lastIndexOf(">", cdataStart);
 				} else {
					int commentStart = s.lastIndexOf("<!--");

					if (commentStart > 0 && commentStart > s.lastIndexOf("-->")) {
						index = s.lastIndexOf(">", commentStart);
					} else {
						index = s.lastIndexOf(">");
 					}
 				}
 				
				if (index != -1) {
					elementEnd = index;
				}
			} catch (BadLocationException bl) {
				// empty
			}
		}

		return elementEnd;
	}
}