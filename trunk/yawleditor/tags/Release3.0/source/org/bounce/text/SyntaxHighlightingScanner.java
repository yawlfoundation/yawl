/*
 * $Id: XMLScanner.java,v 1.5 2009/01/22 22:14:59 edankert Exp $
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

import javax.swing.text.Document;
import java.io.IOException;

/**
 * Associates input stream characters with specific styles.
 * <p>
 * <b>Note:</b> The Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.5 $, $Date: 2009/01/22 22:14:59 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public abstract class SyntaxHighlightingScanner {
	protected int start = 0;
	protected long pos = 0;
	protected boolean error = false;

	protected DocumentInputReader in = null;
	protected boolean valid = false;

	/** The last token scanned */
	public String token = null;

	/**
	 * Constructs a scanner for the Document.
	 * 
	 * @param document
	 *            the document containing the XML content.
	 * 
	 * @throws IOException
	 */
	public SyntaxHighlightingScanner(Document document) throws IOException {
		try {
			in = new DocumentInputReader(document);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		in.read();
	}

	public boolean isError() {
		return error;
	}
	
	/**
	 * Returns true when no paint has invalidated the scanner.
	 * 
	 * @return true when no paint has invalidated the output.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Set valid when correct range is set.
	 * 
	 * @param valid
	 *            when correct range set.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Sets the scanning range.
	 * 
	 * @param start
	 *            the start of the range.
	 * @param end
	 *            the end of the range.
	 * 
	 * @throws IOException
	 */
	public void setRange(int start, int end) throws IOException {
		in.setRange(start, end);

		this.start = start;

		token = null;
		pos = 0;

		in.read();
		scan();
	}

	/**
	 * Gets the starting location of the current token in the document.
	 * 
	 * @return the starting location.
	 */
	public final int getStartOffset() {
		return start + (int) pos;
	}

	/**
	 * Gets the end location of the current token in the document.
	 * 
	 * @return the end location.
	 */
	public final int getEndOffset() {
		return start + (int) in.pos;
	}

	/**
	 * Scans the Xml Stream for XML specific tokens.
	 * 
	 * @return the last location.
	 * 
	 * @throws IOException
	 */
	public abstract long scan() throws IOException;
}
