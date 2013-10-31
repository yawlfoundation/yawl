/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.util;

import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLUtilities {
  
  private static final String XML_QUOTE_PREFIX = "<![CDATA[";
  private static final String XML_QUOTE_SUFFIX = "]]>";
  
  private static final char[] XML_SPECIAL_CHARACTERS = {
      '<','>','\"','\'','\\','&'
  };
  

  /**
   * A convenience method that allows for XML fragments to be stored in
   * larger XML documents without these fragments interfering with the 
   * containing document.
   * @param xmlFragment The fragment of XML to be quoted.
   * @return A fragment of XML, quoted so it can be included within other
   *         XML documents without interference.
   */
  
  public static String quoteXML(String xmlFragment) {
    if (!xmlFragment.startsWith(XML_QUOTE_PREFIX)) {
      return (XML_QUOTE_PREFIX + xmlFragment + XML_QUOTE_SUFFIX);
    }
    return xmlFragment;
  }

  /**
   * A convenience method that allows for XML fragments, previously quoted
   * with the {@link #quoteXML(String)} method to be  unqoted again.
   * @param xmlFragment The fragment of XML to be unquoted.
   * @return A fragment of XML, no longer quoted.
   */
  
  public static String unquoteXML(String xmlFragment) {
    if (xmlFragment.startsWith(XML_QUOTE_PREFIX)) {
      return(xmlFragment.substring(XML_QUOTE_PREFIX.length(), 
             xmlFragment.length() - XML_QUOTE_SUFFIX.length()));
    }
    return xmlFragment;
  }
  
  /**
   * Returns whether the string supplied could be used as a valid XML name.
   * @param name The string to test for XML name validity.
   * @return true if the string can be used as a valid XML name, false otherwise.
   */
  public static boolean isValidXMLName(String name) {
      if (name == null) return false;

      // ensure that XML standard reserved names are not used
      String trimmedName = name.trim();
      if (trimmedName.length() == 0 || trimmedName.toUpperCase().startsWith("XML")) {
          return false;
      }

      // test that name starts with a valid XML name-starting character
      char firstChar = trimmedName.charAt(0);
      if (! (Character.isLetter(firstChar) || firstChar == '_')) return false;

      // test that remainder name chars are a valid XML name characters
      boolean currentCharacterValid;

      if (name.trim().length()>0) {
          for(int i = 1; i < trimmedName.length(); i++) {
              char c = trimmedName.charAt(i);
              currentCharacterValid = Character.isLetter(c) ||
                      Character.isDigit(c);

              if (c == '_' || c == '-' || c == '.') {
                  currentCharacterValid = true;
              }

              if (!currentCharacterValid) {
                  return false;
              }
          }
      }
      return true;
  }
  
  /**
   * Returns a variant of the supplied string with all invalid XML name characters
   * removed. A special case is the the space character, which is converted to the '_'
   * character instead.
   * @param name A string that could be used as an XML name.
   * @return A valid XML name equivalent of name.
   */
  
  public static String toValidXMLName(String name) {
      if (name == null) return null;

      StringBuilder s = new StringBuilder(name.length());
      for (char c : name.toCharArray()) {
          if (c == ' ') {
              s.append('_');
          }
          else if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
              s.append(c);
          }
      }
      return s.toString();
  }


  /**
   * Tests the supplied character to determine whether it is a 
   * special XML Character. If so, it returns true.
   * @param character A character to be tested.
   * @return whether the character is a special XML character or not.
   */

  public static boolean isSpecialXMLCharacter(final char character) {
      for (char XML_SPECIAL_CHARACTER : XML_SPECIAL_CHARACTERS) {
          if (character == XML_SPECIAL_CHARACTER) {
              return true;
          }
      }
    return false;
  }
  
  /**
   * Quotes the special XML characters present in oldString so the
   * return string can be embedded within an XML document without 
   * problems.
   * @param oldString A string that needs its XML special characters quoted.
   * @return A string safe for inclusing within XML.
   */
  
  public static String quoteSpecialCharacters(final String oldString) {
    // small mod of method from http://www.javapractices.com/Topic96.cjp
    
    // TODO: Fix string quoting between editor and engine.
    
    // needed now only by initial value special character quoting.
    
    final StringBuffer quotedString = new StringBuffer();
    
    if (oldString == null) {
      return null;
    }
    
    for(int i = 0; i < oldString.length(); i++) {
      if (oldString.charAt(i) == '<') {
        quotedString.append("&lt;");
      }
      else if (oldString.charAt(i) == '>') {
        quotedString.append("&gt;");
      } 
      else if (oldString.charAt(i) == '\"') {
        quotedString.append("&quot;");
      }
      else if (oldString.charAt(i) == '\'') {
        quotedString.append("&apos;");
      }
      else if (oldString.charAt(i) == '\\') {
         quotedString.append("&#092;");
      }
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

  /**
   * A convenience method that returns an XPath/XQuery expression
   * that will retrieve the entire XML element of the specificed
   * variable in the engine at runtime (including the variable's 
   * opening and closing element tags).
   * 
   * This expression is expected to be evaluated with a set of 
   * XML tags, requiring the "{" and "}" characters for approapriate 
   * scoping.
   * @param variable The variable to return an XQuery expression for.
   * @return XPath/XQuery expression for the variable element.
   */
  
  public static String getTagEnclosedEntireVariableXQuery(YVariable variable) {
    return "{" + getEntireVariableXQuery(variable) + "}";
  }

  /**
   * A convenience method that returns an XPath/XQuery expression
   * that will retrieve the entire XML element of the specificed
   * variable in the engine at runtime (including the variable's 
   * opening and closing element tags).
   * 
   * This expression is not expected to be evaluated with a set of 
   * XML tags, thus, it does not require the "{" and "}" characters 
   * for approapriate scoping.
   * @param variable The variable to return an XQuery expression for.
   * @return XPath/XQuery expression for the variable element.
   */
  
  public static String getEntireVariableXQuery(YVariable variable) {
//      String scopeId = variable.getScope().getDecomposition().getLabelAsElementName();
//      return "/" + scopeId + "/" + variable.getName();
      return "";
  }

  /**
   * A convenience method that returns an XQuery expression that
   * retrieves the value of the variable specified. If the variable
   * is a simple XMLSchema data type, it should end in "/text()". 
   * Complex types should end in "*". It does not return the enclosing
   * element tags of this variable, just the innards.
   *
   * This expression is expected to be evaluated with a set of 
   * XML tags, requiring the "{" and "}" characters for appropriate 
   * scoping.
   * @param variable A data variable in the specification
   * @return An XPath/XQuery expression that will retrieve the 
   *        variable's content at engine run-time.
   */

  
//  public static String getTagEnclosedVariableContentXQuery(YVariable variable) {
//    return "{" + getVariableContentXQuery(variable) + "}";
//  }
  
  /**
   * A convenience method that generates an XPath expression for the given variable,
   * converting the expression to a number automatically if the variable is a number.
   * @param variable The variable to build an XPath expression for.
   * @return XPath expression for the variable.
   */
  
//  public static String getXPathPredicateExpression(YVariable variable) {
//    // TODO: This won't work for user-defined simple type enumerations based on number simple types.
//    //       Can I use the "restriction" tag of simple enumerated types to see if it's a number?
//
//    if (variable.isNumberType()) {
//      return "number(" + getVariableContentXQuery(variable) + ")";
//    }
//    return getVariableContentXQuery(variable);
//  }
  
  /**
   * A convenience method that returns an XQuery expression that
   * retrieves the value of the variable specified. If the variable
   * is a simple XMLSchema data type, it should end in "/text()". 
   * Complex types should end in "*". It does not return the enclosing
   * element tags of this variable, just the innards.
   *
   * This expression is not expected to be evaluated with a set of 
   * XML tags, thus, it does not require the "{" and "}" characters 
   * for approapriate scoping.
   * @param variable A data variable in the specification
   * @return An XPath/XQuery expression that will retrieve the 
   *        variable's content at engine run-time.
   */

  
  /**
   * Returns a piece of XML where opening and closing tags using <code>variableName</code>
   * encloses <code>content</code>. The resulting string will only be valid XML if <code>content</code>
   * was valid XML initially.
   * @param variableName The name to use in the opening and closing element tags
   * @param content The content to be enclosed within these tags
   * @return An XML fragment using <code>variableName</code> as an enclosing XML element.
   */
  
  public static String getTaggedOutputVariableWithContent(String variableName, String content) {
    StringBuilder taggedQuery = new StringBuilder();
    if (variableName != null) {
      taggedQuery.append("<" + variableName + ">");
    }
    if (content != null && !content.trim().equals("")) {
      taggedQuery.append(content);
    }
    if (variableName != null) {
      taggedQuery.append("</" + variableName + ">");
    }

    return taggedQuery.toString();
  }
  
  /**
   * Takes a full file path string and returns a valid XML Name equivalent
   * via the {@link #toValidXMLName(String)} method.
   * @param fullFilePath as a String
   * @return A valid XML name conversion of that file name.
   */
  public static String fileNameToURI(String fullFilePath) {
    if (fullFilePath == null || fullFilePath.trim().equals("")) {
      fullFilePath = "";
    }
    File theFile = new File(fullFilePath);
    String sansExtn = FileUtilities.stripFileExtension(theFile.getName());
    return toValidXMLName(sansExtn);
  }
  
  /**
   * Simple regular-expression based method to strip the outermost tags from a fragment of XML.
   * Assumes that the fragment begins and ends with tags.
   * @param xmlFragment
   * @return the xmlFragment string with the outermost tags removed.
   */
  
  public static String stripOutermostTags(String xmlFragment) {
    
    if (xmlFragment == null) {
      return null;
    }
    
    Pattern tagContainingPattern = 
      Pattern.compile(
          "^<.*?>(.*)</.*?>$"
    );

    if (tagContainingPattern == null) {
      return xmlFragment;
    }
    
    Matcher tagContainingMatcher = tagContainingPattern.matcher(xmlFragment);
    
    if (tagContainingMatcher == null) {
      return xmlFragment;
    }

    if (tagContainingMatcher.find()) {
      tagContainingMatcher.group();
      return tagContainingMatcher.replaceAll("$1");
    }
    return xmlFragment;  // *shrug* no tags in the fragment, apparently.
  }


    public static String formatXML(String xml, boolean prettify, boolean wrap) {
        if ((xml != null) && (xml.trim().startsWith("<"))) {
            String temp = wrap ? StringUtil.wrap(xml, "temp") : xml;
            XNode node = new XNodeParser(true).parse(temp);
            if (node != null) {
                if (prettify) {
                    temp = node.toPrettyString(1,3);
                    return wrap ? StringUtil.unwrap(temp).substring(1) : temp;  // lead \n
                }
                else {                
                    temp = node.toString();
                    return wrap ? StringUtil.unwrap(temp) : temp;
                }
            }
        }
        return xml;
    }

}
