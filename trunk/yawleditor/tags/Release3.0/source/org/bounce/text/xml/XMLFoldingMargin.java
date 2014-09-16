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
package org.bounce.text.xml;

import org.bounce.text.FoldingMargin;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.xml.stream.events.XMLEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * Line folding margin for a JTextComponent.
 * 
 * <pre>
 * JEditorPane editor = new JEditorPane();
 * JScrollPane scroller = new JScrollPane(editor);
 *
 * // Add the number margin as a Row Header View
 * XMLFoldingMargin margin = new XMLFoldingMargin(editor);
 * scroller.setRowHeaderView(margin);
 * </pre>
 * 
 * @author Edwin Dankert <edankert@gmail.com>
 */

public class XMLFoldingMargin extends FoldingMargin {
	private static final long serialVersionUID = 8489615051963807472L;

	private XMLScanner scanner = null;

	/**
	 * Convenience constructor for Text Components
	 */
	public XMLFoldingMargin(JTextComponent editor) throws IOException {
		super(editor);

		initScanner(editor.getDocument());
		
		editor.addPropertyChangeListener("document", new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				Object prop = event.getNewValue();
				
				if (prop instanceof Document) {
					try {
						initScanner((Document)prop);
					} catch (IOException e) {
						// This will just not set the scanner object 
					}
				}
			}
		});
	}
	
	private void initScanner(Document document) throws IOException {
		scanner = new XMLScanner(document);
	}

	protected int getFoldClosingLine(int start, int end) {
		Element element = editor.getDocument().getDefaultRootElement().getElement(start);
		int tagStart = getStartTagLocation(element.getStartOffset(), element.getEndOffset());
		
		if (tagStart != -1) {
			Element endElement = editor.getDocument().getDefaultRootElement().getElement(end);
			int tagEnd = getEndTagLocation(tagStart, endElement.getEndOffset());

			if (tagStart >= element.getStartOffset() && tagStart < element.getEndOffset() && tagEnd > element.getEndOffset()) {
				return editor.getDocument().getDefaultRootElement().getElementIndex(tagEnd);
			}
		}
		
		return start;
	}

	private int getStartTagLocation(int offset, int end){
		scanner.setValid(false);
		XMLViewUtilities.updateScanner(scanner, editor.getDocument(), offset, end);

		try {
			scanner.getNextTag();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (scanner.getEventType() == XMLEvent.START_ELEMENT) {
			return scanner.getStartOffset();
		}
		
		return -1;
	}

	private int getEndTagLocation(int startTagLocation, int endOffset) {
		scanner.setValid(false);
		XMLViewUtilities.updateScanner(scanner, editor.getDocument(), startTagLocation, endOffset);

		int startTags = 1;

		do {
			int event = -1;

			try {
				event = scanner.getNextTag();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (event == XMLEvent.START_ELEMENT) {
				startTags++;
			} else if (event == XMLEvent.END_ELEMENT) {
				startTags--;
			}
		} while (startTags > 0 && scanner.getEndOffset() < endOffset && scanner.token != null);
		
		if (startTags == 0) {
			return scanner.getStartOffset();
		}
		
		return endOffset;
	}
}
