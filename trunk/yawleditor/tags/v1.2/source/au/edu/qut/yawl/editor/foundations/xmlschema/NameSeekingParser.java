/*
 * Created on 25/06/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.foundations.xmlschema;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

public class NameSeekingParser extends SAXParser {
  private static final NameSeekingContentHandler handler = new NameSeekingContentHandler();
  
  public NameSeekingParser() {
    setErrorHandler(new DoNothingErrorHandler());
    setContentHandler(handler);
  }
  
  public String getName() {
    return handler.getName();
  }
}

class NameSeekingContentHandler extends DefaultHandler {
  
  private String name = "";
  
  public NameSeekingContentHandler() {}
  
  public void startDocument() {
    name = "";
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attribs) {
    if (name.equals("") && localName.equals("element")) {
      name = new String(attribs.getValue(uri,"name"));
    }
  }
  
  public String getName() {
    return name;
  }
}