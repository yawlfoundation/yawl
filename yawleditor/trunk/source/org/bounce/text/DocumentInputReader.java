/*
 * $Id: XMLInputReader.java,v 1.4 2008/01/28 21:02:14 edankert Exp $
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * A Reader for XML input, which can handle escape characters.
 * 
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.4 $, $Date: 2008/01/28 21:02:14 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class DocumentInputReader extends Reader {

    private static final int BUFFERLEN = 10240;

    private final char buffer[] = new char[BUFFERLEN];

    private DocumentInputStream stream = null;

    public long pos = 0;

    private long chpos = 0x100000000L;
    private int pushBack = -1;
    private int lastChar = -1;
    private int currentIndex = 0;
    private int numChars = 0;

    // test this
    
    /**
     * Constructs the new input stream reader out of the Xml input strem.
     * 
     * @param document the XML input stream.
     * @throws UnsupportedEncodingException 
     * 
     * @throws UnsupportedEncodingException
     */
    public DocumentInputReader(Document document) {
        stream = new DocumentInputStream(document);
    }

    /**
     * Sets the scan range of the reader.
     * 
     * @param start
     *            the start position.
     * @param end
     *            the end position.
     */
    public void setRange( int start, int end) throws IOException {
        stream.setRange( start, end);

        pos = 0;
        chpos = 0x100000000L;
        pushBack = -1;
        lastChar = -1;
        currentIndex = 0;
        numChars = 0;
    }
    
    /**
     * Reads one character from the stream and increases the index.
     * 
     * @return the character or -1 for an eof.
     * 
     * @throws IOException
     */
    public int read() throws IOException {
        lastChar = readInternal();

        return lastChar;
    }

    /**
     * Returns the last read character.
     * 
     * @return the last read character or -1 for an eof.
     */
    public int getLastChar() {
        return lastChar;
    }

    // The implementation of the read method.
    private int readInternal() throws IOException {
        int i;
        label0: {
            pos = chpos;
            chpos++;
            i = pushBack;

            if ( i == -1) {
                if ( currentIndex >= numChars) {
                    numChars = stream.read( buffer);

                    if ( numChars == -1) {
                        i = -1;
                        break label0;
                    }

                    currentIndex = 0;
                }
                i = buffer[currentIndex++];
            } else {
                pushBack = -1;
            }
        }

        switch ( i) {
        case 10: // '\n'
            chpos += 0x100000000L;
            return 10;

        case 13: // '\r'
            if ( (i = getNextChar()) != 10)
                pushBack = i;
            else
                chpos++;
            chpos += 0x100000000L;
            return 10;
        }

        return i;
    }

    // Returns the next character from the stream
    private int getNextChar() throws IOException {
        if ( currentIndex >= numChars) {
            numChars = stream.read( buffer);

            if ( numChars == -1) {
                return -1;
            }

            currentIndex = 0;
        }

        return buffer[currentIndex++];
    }

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public int read(char[] ac, int i, int j) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static class DocumentInputStream extends InputStream {

	    private Segment segment = null;

	    private Document document = null;

	    private int end = 0; // end position

	    private int pos = 0; // pos in document

	    private int index = 0; // index into array of the segment

	    /**
	     * Constructs a stream for the document.
	     * 
	     * @param doc
	     *            the document with Xml Information.
	     */
	    public DocumentInputStream( Document doc) {
	        this.segment = new Segment();
	        this.document = doc;

	        end = document.getLength();
	        pos = 0;

            try {
				loadSegment();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    /**
	     * Sets the new range to be scanned for the stream and loads the necessary
	     * information.
	     * 
	     * @param start
	     *            the start of the segment.
	     * @param end
	     *            the end of the segment.
	     */
	    public void setRange( int start, int end) throws IOException {
	        this.end = end;
	        pos = start;

            loadSegment();
	    }

	    /**
	     * Reads the next byte of data from this input stream. The value byte is
	     * returned as an <code>int</code> in the range <code>0</code> to
	     * <code>255</code>. If no byte is available because the end of the
	     * stream has been reached, the value <code>-1</code> is returned.
	     * 
	     * @return the next byte of data, or <code>-1</code> if the end of the
	     *         stream is reached.
	     *         
	     * @throws IOException
	     */
	    public int read() throws IOException {
	        if ( index >= segment.offset + segment.count) {
	            if ( pos >= end) {
	                // no more data
	                return -1;
	            }

	            loadSegment();
	        }

	        return segment.array[index++];
	    }

	    public int read(char[] chars) throws IOException {
	    	return read(chars, 0, chars.length);
	    }

	public int read(char[] chars, int i, int j) throws IOException {
	    if(chars == null)
	        throw new NullPointerException();
	    if(i < 0 || j < 0 || j > chars.length - i)
	        throw new IndexOutOfBoundsException();
	    if(j == 0)
	        return 0;
	    int k = read();
	    if(k == -1)
	        return -1;
	    chars[i] = (char)k;
	    int i1 = 1;
	    do
	    {
	        try
	        {
	            if(i1 >= j)
	                break;
	            int l = read();
	            if(l == -1)
	                break;
	            chars[i + i1] = (char)l;
	            i1++;
	            continue;
	        }
	        catch(IOException ioexception) { }
	        break;
	    } while(true);
	    return i1;
	}

	// Loads the segment with new information if necessary...
	    private void loadSegment() throws IOException {
	        try {
	            int n = Math.min( 1024, end - pos);

	            document.getText( pos, n, segment);
	            pos += n;

	            index = segment.offset;
	        } catch ( BadLocationException e) {
	            throw new IOException( "Bad location");
	        }
	    }
	}
}
