/*
 * $Id: XMLScanner.java,v 1.5 2009/01/22 22:14:59 edankert Exp $
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

import org.bounce.text.DocumentInputReader;
import org.bounce.text.SyntaxHighlightingScanner;
import org.bounce.xml.XMLChar;

import javax.swing.text.Document;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;


/**
 * Associates XML input stream characters with XML specific styles.
 * <p>
 * <b>Note:</b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.5 $, $Date: 2009/01/22 22:14:59 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLScanner extends SyntaxHighlightingScanner {
	private Scanner tagScanner = null;

	private final AttributeScanner ATTRIBUTE_SCANNER = new AttributeScanner(); // done
	private final AttributeNameScanner ATTRIBUTE_NAME_SCANNER = new AttributeNameScanner(); // done
	private final AttributeValueScanner ATTRIBUTE_VALUE_SCANNER = new AttributeValueScanner(XMLStyleConstants.ATTRIBUTE_VALUE); // done
	private final AttributeValueScanner NAMESPACE_VALUE_SCANNER = new AttributeValueScanner(XMLStyleConstants.NAMESPACE_VALUE); // done
	private final EntityReferenceScanner ENTITY_REFERENCE_SCANNER = new EntityReferenceScanner(); // done
	private final WhitespaceScanner WHITESPACE_SCANNER = new WhitespaceScanner(); // done
	private final ElementEndScanner ELEMENT_END_SCANNER = new ElementEndScanner(); // done
	private final TagEndScanner TAG_END_SCANNER = new TagEndScanner(); // done
	private final ElementStartScanner ELEMENT_START_SCANNER = new ElementStartScanner(); // done
	private final ElementNameScanner ELEMENT_NAME_SCANNER = new ElementNameScanner(); // done
	private final ContentScanner CONTENT_SCANNER = new ContentScanner();
	private final EntityTagScanner ENTITY_TAG_SCANNER = new EntityTagScanner();
	private final CommentScanner COMMENT_SCANNER = new CommentScanner();
	private final CDATAScanner CDATA_SCANNER = new CDATAScanner();
	private final TagScanner TAG_SCANNER = new TagScanner();

	/**
	 * Constructs a scanner for the Document.
	 * 
	 * @param document
	 *            the document containing the XML content.
	 * 
	 * @throws IOException
	 */
	public XMLScanner(Document document) throws IOException {
		super(document);
	}

	public int getEventType() {
		if (tagScanner == TAG_SCANNER) {
			if (TAG_SCANNER.scanner == ELEMENT_START_SCANNER) {
				// if (ELEMENT_START_TAG_SCANNER.scanner == ATTRIBUTE_SCANNER) {
				// return TYPE.ATTRIBUTE;
				// }

				if (ELEMENT_START_SCANNER.scanner == TAG_END_SCANNER && TAG_END_SCANNER.emptyElement) {
					return XMLEvent.END_ELEMENT;
				}

				return XMLEvent.START_ELEMENT;
			} else if (TAG_SCANNER.scanner == ELEMENT_END_SCANNER) {
				return XMLEvent.END_ELEMENT;
			} else if (TAG_SCANNER.scanner == COMMENT_SCANNER) {
				return XMLEvent.COMMENT;
			} else if (TAG_SCANNER.scanner == CDATA_SCANNER) {
				return XMLEvent.CDATA;
			}
		} else if (tagScanner == CONTENT_SCANNER) {
			return XMLEvent.CHARACTERS;
		}
		
		if (getStartOffset() == 0) {
			return XMLEvent.START_DOCUMENT;
		}
		
		return XMLEvent.END_DOCUMENT;
	}
	
	public int getNextTag() throws IOException {
		while (true) {
			scan();
			
			if (token == XMLStyleConstants.ELEMENT_NAME) {
				return getEventType();
			} else if (tagScanner == TAG_SCANNER && TAG_SCANNER.scanner == ELEMENT_START_SCANNER && ELEMENT_START_SCANNER.scanner == TAG_END_SCANNER && TAG_END_SCANNER.emptyElement) {
				return XMLEvent.END_ELEMENT;
			} else if (in.getLastChar() == -1) {
				return getEventType();
			}
		}
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
		tagScanner = null;

		super.setRange(start, end);
	}

	/**
	 * Scans the Xml Stream for XML specific tokens.
	 * 
	 * @return the last location.
	 * 
	 * @throws IOException
	 */
	public long scan() throws IOException {
		int character = in.getLastChar();

		if (error && (character == '<' || character == -1)) {
			tagScanner = null;
			token = null;
		}

		error = false;

		if (tagScanner != null && tagScanner.isFinished()) {
			tagScanner = null;
			token = null;
		}

		long l = pos;
		pos = in.pos;

		while (true) {
			if (tagScanner != null) {
				token = tagScanner.scan(in);

				character = in.getLastChar();

				if (getEndOffset() > getStartOffset() || character == -1) {
					break;
				} else if (character == '<') {
					character = in.read();
				}
			} else if (character == '<') {
				character = in.read();

				tagScanner = TAG_SCANNER;
				tagScanner.reset();

			} else if (isContent(character) || character == '&') {
				tagScanner = CONTENT_SCANNER;
				tagScanner.reset();
			} else {
				token = null;
				error = true;
				break;
			}
		}
		
		if (error) {
			if (in.getLastChar() == -1 && getStartOffset() == getEndOffset()) {
				token = null;
				tagScanner = null;
			}
		}
		
		return l;
	}

	/**
	 * A scanner for anything starting with a ' <'.
	 */
	private class TagScanner extends Scanner {
		private Scanner scanner = null;

		/**
		 * returns whether this scanner has finished scanning all it was
		 * supposed to scan.
		 * 
		 * @return true when the scanner is finished.
		 */
		public boolean isFinished() {
			return scanner != null && scanner.isFinished();
		}

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			if (scanner != null) {
				if (scanner.isFinished()) {
					scanner = null;
				} else {
					return scanner.scan(in);
				}
			}

			int character = in.getLastChar();

			if (character == 33) { // '!'
				character = in.read();
				if (character == 45) { // '-'
					character = in.read();
					if (character == 45) { // '-'
						character = in.read();
						scanner = COMMENT_SCANNER;
						scanner.reset();
						return XMLStyleConstants.COMMENT;
					}

				}

				if (character == 91) { // '['
					character = in.read();
					if (character == 67) { // 'C'
						character = in.read();
						if (character == 68) { // 'D'
							character = in.read();
							if (character == 65) { // 'A'
								character = in.read();
								if (character == 84) { // 'T'
									character = in.read();
									if (character == 65) { // 'A'
										character = in.read();
										if (character == 91) { // '['
											character = in.read();
											scanner = CDATA_SCANNER;
											scanner.reset();
											return XMLStyleConstants.CDATA;
										}
									}
								}
							}
						}
					}
				}

				if (scanner == null) {
					scanner = ENTITY_TAG_SCANNER;
				}

				scanner.reset();
				return XMLStyleConstants.SPECIAL;

			} else if (character == '?') { // '?'
				character = in.read();
				scanner = ENTITY_TAG_SCANNER;
				scanner.reset();

				return XMLStyleConstants.SPECIAL;

			} else if (character == '/') { // '/'
				character = in.read();
				scanner = ELEMENT_END_SCANNER;
				scanner.reset();

				return XMLStyleConstants.SPECIAL;

			} else if (character == '>') { // '>'
				character = in.read();
				finished();
				return XMLStyleConstants.SPECIAL;

			} else if (character == '<') { // '>'
				scanner = ELEMENT_START_SCANNER; // for the show
				scanner.reset();

				error = true;
				finished();
				return XMLStyleConstants.SPECIAL;
			} else {
				scanner = ELEMENT_START_SCANNER;
				scanner.reset();

				return XMLStyleConstants.SPECIAL;
			}
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			scanner = null;
		}
	}

	/**
	 * Scans a entity ' <!'.
	 */
	private class EntityTagScanner extends Scanner {

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			int character = in.read();

			while (true) {
				switch (character) {
				case -1:
					// System.err.println("Error ["+pos+"]: eof in entity!");
					finished();
					return XMLStyleConstants.ENTITY;

				case 62: // '>'
					finished();
					return XMLStyleConstants.ENTITY;

				default:
					character = in.read();
					break;

				}
			}
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
		}
	}

	/**
	 * Scans a comment section ' <!--'.
	 */
	private class CommentScanner extends Scanner {
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			int character = in.read();

			while (true) {
				// System.out.print((char)character);

				switch (character) {
				case -1: // EOF
					finished();
					return XMLStyleConstants.COMMENT;

				case 45: // '-'
					character = in.read();
					if (character == 45) { // '-'
						character = in.read();
						if (character == 62) { // '>'
							character = in.read();
							finished();
							tagScanner.finished();
							return XMLStyleConstants.COMMENT;
						}
					}
					break;

				default:
					character = in.read();
					break;

				}
			}
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
		}
	}

	/**
	 * Scans a CDATA section ' <![CDATA['.
	 */
	private class CDATAScanner extends Scanner {
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			int character = in.read();

			while (true) {
				// System.out.print((char)character);

				switch (character) {
				case -1: // EOF
					finished();
					return XMLStyleConstants.CDATA;

				case 93: // ']'
					character = in.read();
					if (character == 93) { // ']'
						character = in.read();
						if (character == 62) { // '>'
							character = in.read();
							finished();
							tagScanner.finished();
							return XMLStyleConstants.CDATA;
						}
					}
					break;

				default:
					character = in.read();
					break;

				}
			}
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
		}
	}

	/**
	 * Scans an element start tag ' <xxx:xxxx yyy:yyyy="yyyyy"
	 * xmlns:hsshhs="sffsfsf">'.
	 */
	private class ElementEndScanner extends Scanner {
		private Scanner scanner = ELEMENT_NAME_SCANNER;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			String token = scanner.scan(in);

			if (scanner.isFinished()) {
				if (scanner == TAG_END_SCANNER) {
					finished();
				} else {
					int character = in.getLastChar();
					
					if (character == '>') {
						scanner = TAG_END_SCANNER;
						scanner.reset();
					} else if (isSpace((char)character)) {
						scanner = WHITESPACE_SCANNER;
						scanner.reset();
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();
						} else {
							character = in.read();
						}
					}
				}
			}
			
			return token;
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			scanner = ELEMENT_NAME_SCANNER;
			scanner.reset();
		}
	}

	/**
	 * Scans an element start tag ' <xxx:xxxx yyy:yyyy="yyyyy"
	 * xmlns:hsshhs="sffsfsf">'.
	 */
	private class ElementStartScanner extends Scanner {
		private Scanner scanner = ELEMENT_NAME_SCANNER;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			String token = scanner.scan(in);

			if (scanner.isFinished()) {
				if (scanner == TAG_END_SCANNER) {
					finished();
				} else {
					int ch = in.getLastChar();
					
					if (ch == '/' || ch == '>') {
						scanner = TAG_END_SCANNER;
						scanner.reset();
					} else if (isSpace((char)ch)) {
						scanner = WHITESPACE_SCANNER;
						scanner.reset();
					} else if (ch == '<') {
						error = true;
						finished();
					} else {
						scanner = ATTRIBUTE_SCANNER;
						scanner.reset();
					}
				}
			}
			
			return token;
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			scanner = ELEMENT_NAME_SCANNER;
			scanner.reset();
		}
	}

	/**
	 * Scans an element start tag ' <xxx:xxxx yyy:yyyy="yyyyy"
	 * xmlns:hsshhs="sffsfsf">'.
	 */
	private class WhitespaceScanner extends Scanner {
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			while (true) {
				if (!isSpace((char) in.read())) {
					finished();
					return XMLStyleConstants.WHITESPACE;
				}
			}
		}
	}

	/**
	 * Scans an element name ' <xxx:xxxx'.
	 */
	private class ElementNameScanner extends Scanner {
		private boolean prefix = false;
		private boolean first = true;
		private boolean nameStart = false;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {

			int character = in.getLastChar();
			
			if (first) {
				first = false;
				
				if (!isNameStart(character)) {
					error = true;

					if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.ELEMENT_NAME;
					}
				}

				character = in.read();
			}
			
			do {
				if (nameStart) {
					nameStart = false;

					if (isNameStart(character)) {
						character = in.read();
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();

							return XMLStyleConstants.ELEMENT_NAME;
						} else {
							character = in.read();
						}
					}
				} else if (character == ':') {
					if (prefix) {
						character = in.read();
						nameStart = true;
						return XMLStyleConstants.SPECIAL;
					}

					prefix = true;
					return XMLStyleConstants.ELEMENT_PREFIX;
				} else if (isName(character)) {
					character = in.read();
				} else if (isSpace(character) || character == '/' || character == '>') {
					finished();
					return XMLStyleConstants.ELEMENT_NAME;
				} else {
					error = true;

					if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.ELEMENT_NAME;
					} else {
						character = in.read();
					}
				}
			} while (true);
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			prefix = false;
			first = true;
			nameStart = false;
		}
	}

	private class TagEndScanner extends Scanner {
		boolean emptyElement = false;
		
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			int character = in.getLastChar();

			do {
				if (character == '/') {
					emptyElement = true;
					character = in.read();
				} else if (character == '>') {
					character = in.read();
					finished();
					return XMLStyleConstants.SPECIAL;
				} else {
					if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.SPECIAL;
					} else {
						character = in.read();
					}

					error = true;
				}
			} while (true);
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			emptyElement = false;
		}
	}

	/**
	 * Scans an elements attribute 'xxx:xxxx="hhhh"' or 'xmlns:xxxx="hhhh"'.
	 */
	private class AttributeScanner extends Scanner {
		private Scanner scanner = ATTRIBUTE_NAME_SCANNER;
		boolean foundEquals = false;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			String token = null;
			int ch = in.getLastChar();
			
			if (ch == '=') {
				in.read();
				foundEquals = true;
				
				token = XMLStyleConstants.SPECIAL;
			} else {
				token = scanner.scan(in);
			}

			if (scanner.isFinished()) {
				if (scanner == ATTRIBUTE_VALUE_SCANNER || scanner == NAMESPACE_VALUE_SCANNER) {
					finished();
				} else {
					ch = in.getLastChar();
					
					if (isSpace((char)ch)) {
						scanner = WHITESPACE_SCANNER;
						scanner.reset();
					} else if (ch == '\'' || ch == '"') {
						if (ATTRIBUTE_NAME_SCANNER.namespace) {
							scanner = NAMESPACE_VALUE_SCANNER;
						} else {
							scanner = ATTRIBUTE_VALUE_SCANNER;
						}

						scanner.reset();

						if (!foundEquals) {
							error = true;
						}
					} else if (ch != '=') {
						error = true;
						finished();
					}
				}
			}
			
			return token;
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			foundEquals = false;
			scanner = ATTRIBUTE_NAME_SCANNER;
			scanner.reset();
		}
	}

	/**
	 * Scans an elements attribute name 'xxx:xxxx' or 'xmlns:xxxx'.
	 */
	private class AttributeNameScanner extends Scanner {
		private boolean prefix = false;
		private boolean firstTime = true;
		private boolean namespace = false;
		private boolean nameStart = false;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			int character = in.getLastChar();
			
			if (firstTime) {
				firstTime = false;

				if (isNameStart(character)) {
					if (character == 'x') {
						character = in.read();
						if (character == 'm') { // 'm'
							character = in.read();
							if (character == 'l') { // 'l'
								character = in.read();
								if (character == 'n') { // 'n'
									character = in.read();
									if (character == 's') { // 's'
										character = in.read();
										namespace = true;
									}
								}	
							}
						}
					}
				} else {
					error = true;

					if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.ATTRIBUTE_NAME;
					} else {
						character = in.read();
					}
				}
			}
			
			do {
				if (nameStart) {
					nameStart = false;

					if (isNameStart(character)) {
						character = in.read();
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();

							if (namespace && prefix) {
								return XMLStyleConstants.NAMESPACE_PREFIX;
							} else if (namespace) {
								return XMLStyleConstants.NAMESPACE_NAME;
							}
							
							return XMLStyleConstants.ATTRIBUTE_NAME;
						} else {
							character = in.read();
						}
					}
				} else if (character == ':') {
					if (prefix) {
						character = in.read();
						nameStart = true;
						return XMLStyleConstants.SPECIAL;
					} else if (namespace) {
						prefix = true;
						return XMLStyleConstants.NAMESPACE_NAME;
					}
					
					prefix = true;
					return XMLStyleConstants.ATTRIBUTE_PREFIX;
				} else if (isSpace(character) || character == '=') {
					finished();
					
					if (namespace && prefix) {
						return XMLStyleConstants.NAMESPACE_PREFIX;
					} else if (namespace) {
						return XMLStyleConstants.NAMESPACE_NAME;
					}
					
					return XMLStyleConstants.ATTRIBUTE_NAME;
				} else if (isName(character)) {
					character = in.read();
				} else {
					error = true;

					if (character == '<' || character == -1) {
						finished();

						if (namespace && prefix) {
							return XMLStyleConstants.NAMESPACE_PREFIX;
						} else if (namespace) {
							return XMLStyleConstants.NAMESPACE_NAME;
						}
						
						return XMLStyleConstants.ATTRIBUTE_NAME;
					} else {
						character = in.read();
					}
				}
			} while (true);
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();

			prefix = false;
			firstTime = true;
			namespace = false;
			nameStart = false;
		}
	}

	/**
	 * Scans an elements attribute '"hhhh"'.
	 */
	private class AttributeValueScanner extends Scanner {
		private Scanner scanner = null;
		private String style = null;
		private int start = -1;
		
		public AttributeValueScanner(String style) {
			this.style = style;
		}
		
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			if (scanner != null && !scanner.isFinished()) {
				return scanner.scan(in);
			} else {
				int character = in.getLastChar();
				
				if (start == -1) {
					start = character;
					character = in.read();
				}
	
				do {
					if (character == start) {
						character = in.read();
						finished();

						return style;
					} else if (isContent(character)) {
						character = in.read();
					} else if (character == '&') {
						scanner = ENTITY_REFERENCE_SCANNER;
						scanner.reset();
	
						return style;
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();
							return style;
						} else {
							character = in.read();
						}
					}
				} while (true);
			}
		}
		
		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			start = -1;
		}
	}

	/**
	 * Scans an elements attribute '"hhhh"'.
	 */
	private class ContentScanner extends Scanner {
		private Scanner scanner = null;

		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
			if (scanner != null && !scanner.isFinished()) {
				return scanner.scan(in);
			} else {
				int character = in.getLastChar();
				
				do {
					if (isContent(character)) {
						character = in.read();
					} else if (character == '&') {
						scanner = ENTITY_REFERENCE_SCANNER;
						scanner.reset();
	
						return XMLStyleConstants.ELEMENT_VALUE;
					} else if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.ELEMENT_VALUE;
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();
							return XMLStyleConstants.ELEMENT_VALUE;
						} else {
							character = in.read();
						}
					}
				} while (true);
			}
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
		}
	}

	/**
	 * Scans an elements attribute '"hhhh"'.
	 */
	private class EntityReferenceScanner extends Scanner {
		private boolean characterReference = false;
		private boolean hexadecimal = false;
		
		/**
		 * @see Scanner#scan(DocumentInputReader)
		 */
		public String scan(DocumentInputReader in) throws IOException {
				int character = in.read();
				if (isNameStart(character)) {
					character = in.read();
				} else if (character == '#') {
					character = in.read();
					characterReference = true;

					if (character == 'x') {
						character = in.read();
						hexadecimal = true;
					}
				} else {
					error = true;
					
					if (character == '<' || character == -1) {
						finished();
						return XMLStyleConstants.ENTITY_REFERENCE;
					} else {
						character = in.read();
					}
				}
				
				do {
					if (characterReference && isCharacterRef(character, hexadecimal)) {
						character = in.read();
					} else if (!characterReference && isName(character)) {
						character = in.read();
					} else if (character == ';') {
						character = in.read();
						finished();
						return XMLStyleConstants.ENTITY_REFERENCE;
					} else {
						error = true;

						if (character == '<' || character == -1) {
							finished();
							return XMLStyleConstants.ENTITY_REFERENCE;
						} else {
							character = in.read();
						}
					}
			} while (true);
		}

		/**
		 * @see Scanner#reset()
		 */
		public void reset() {
			super.reset();
			characterReference = false;
			hexadecimal = false;
		}
	}

	/**
	 * Abstract scanner class..
	 */
	abstract class Scanner {
		private boolean finished = false;

		/**
		 * Scan the input steam for a token.
		 * 
		 * @param in
		 *            the input stream reader.
		 * @return the token.
		 * @throws IOException
		 */
		public abstract String scan(DocumentInputReader in) throws IOException;
		
		/**
		 * The scanner has finished scanning the information, only a reset can
		 * change this.
		 */
		protected void finished() {
			finished = true;
		}

		/**
		 * returns whether this scanner has finished scanning all it was
		 * supposed to scan.
		 * 
		 * @return true when the scanner is finished.
		 */
		public boolean isFinished() {
			return finished;
		}

		/**
		 * Resets all the variables to the start value.
		 */
		public void reset() {
			finished = false;
		}
	}
	
	private static boolean isNameStart(int character) {
		if (character == -1 || character == ':') {
			return false;
		}

		return XMLChar.isNameStart(character);
	}

	private static boolean isName(int character) {
		if (character == -1 || character == ':') {
			return false;
		}

		return XMLChar.isName(character);
	}

	private static boolean isCharacterRef(int character, boolean hex) {
		if (character == -1) {
			return false;
		}

		if (character > '0' && character < '9') {
			return true;
		}
		
		if (hex) {
			if (character == 'a' || character == 'A' 
				|| character == 'b' || character == 'B'
				|| character == 'c' || character == 'C'
				|| character == 'd' || character == 'D'
				|| character == 'e' || character == 'E'
				|| character == 'f' || character == 'F') {
				return true;
			}
		}
		
		return false;
	}

	private static boolean isSpace(int character) {
		if (character == -1) {
			return false;
		}

		return XMLChar.isSpace(character);
	}

	private static boolean isContent(int character) {
		if (character == -1) {
			return false;
		}
		
		if (XMLChar.isContent(character)) {
			return true;
		}

		if (character == ']') {
			return true;
		}

		return XMLChar.isSpace(character);
	}
}
