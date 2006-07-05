/*
 * Created on 16/07/2004
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
 */

package au.edu.qut.yawl.editor.foundations;

import au.edu.qut.yawl.editor.data.DataVariable;

public class XMLUtilities {
  
  private static final String XML_QUOTE_PREFIX = "<![CDATA[";
  private static final String XML_QUOTE_SUFFIX = "]]>";

  public static String quoteXML(String xmlFragment) {
    if (!xmlFragment.startsWith(XML_QUOTE_PREFIX)) {
      return (XML_QUOTE_PREFIX + xmlFragment + XML_QUOTE_SUFFIX);
    }
    return xmlFragment;
  }
  
  public static String unquoteXML(String xmlFragment) {
    if (xmlFragment.startsWith(XML_QUOTE_PREFIX)) {
      return(xmlFragment.substring(XML_QUOTE_PREFIX.length(), 
             xmlFragment.length() - XML_QUOTE_SUFFIX.length()));
    }
    return xmlFragment;
  }
  
  // Returns a variant of a string with all invalid XML element characters removed
  // Space characters are mapped to _ characters instead.
  
  public static String toValidElementName(String name) {

    char[] nameAsCharArray = name.toCharArray();
    char[] newNameAsCharArray = new char[nameAsCharArray.length];
    
    boolean currentCharacterValid = false;
    int j = 0;

    for(int i = 0; i < nameAsCharArray.length; i++) {
      currentCharacterValid = false;
      
      if (nameAsCharArray[i] == ' ') {
        nameAsCharArray[i] = '_';
      }

      if (nameAsCharArray[i] >= '0' && nameAsCharArray[i] <= '9') {
        currentCharacterValid = true;
      }
      
      if (nameAsCharArray[i] >= 'a' && nameAsCharArray[i] <= 'z') {
        currentCharacterValid = true;
      }

      if (nameAsCharArray[i] >= 'A' && nameAsCharArray[i] <= 'Z') {
        currentCharacterValid = true;
      }
      
      if (nameAsCharArray[i] == '_' || nameAsCharArray[i] == '-' ||
          nameAsCharArray[i] == '.') {
        currentCharacterValid = true;
      }

      if (currentCharacterValid) {
        newNameAsCharArray[j] = nameAsCharArray[i];
        j++;
      }
    }

    String validElementName = new String(newNameAsCharArray);
    validElementName = validElementName.trim();
    
    return validElementName;
  }
  
  public static String quoteSpecialCharacters(final String oldString) {
    // small mod of method from http://www.javapractices.com/Topic96.cjp
    
    // TODO: Fix string quoting between editor and engine.
    
    final StringBuffer quotedString = new StringBuffer();
    
    for(int i = 0; i < oldString.length(); i++) {
    
      if (oldString.charAt(i) == '<') {
        quotedString.append("&lt;");
      }
      else if (oldString.charAt(i) == '>') {
        quotedString.append("&gt;");
      } /* -- Not mandatory for XML elements, but causes
           exceptions in Beta4 engine XML generation if left out */
      else if (oldString.charAt(i) == '\"') {
        quotedString.append("&quot;");
      }/* -- Not mandatory for XML Elelemts --
      else if (oldString.charAt(i) == '\'') {
        quotedString.append("&apos;");
      }
      else if (oldString.charAt(i) == '\\') {
         quotedString.append("&#092;");
      }*/
      else if (oldString.charAt(i) == '&') {
         quotedString.append("&amp;");
      }
      else {
        //the char is not a special one
        //add it to the result as is
        quotedString.append(oldString.charAt(i));
      }
    }
    return quotedString.toString();
  }
  
  public static String getVariablePathXQuery(DataVariable variable) {
    String variableContent = DataVariable.isSimpleDataType(variable.getDataType()) ? "text()" : "*";
    String scopeId = variable.getScope().getDecomposition().getLabelAsElementName();
    String xQuery = "/" + scopeId + "/" + variable.getName() + "/" + variableContent;
    if (variable.isNumberType()) {
      xQuery = "number(" + xQuery + ")";
    }
     return xQuery;
  }
  
  // This is a quick-fix that should eventually be handled by the engine methods 
  // Task.setDataBindingForInputParam() and Task.setDataBindingForOutputParam()
  
  public static String getTaggedOutputVariableQuery(DataVariable variable, String query) {
    StringBuffer taggedQuery = new StringBuffer();
    if (variable != null) {
      taggedQuery.append("<" + variable.getName() + ">");
    }
    if (query != null && !query.trim().equals("")) {
      taggedQuery.append("{" + query + "}");
    }
    if (variable != null) {
      taggedQuery.append("</" + variable.getName() + ">");
    }

    return taggedQuery.toString();
  }
}
