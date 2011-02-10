/*
 * Created on 16/07/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
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

package org.yawlfoundation.yawl.editor.foundations;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.util.StringUtil;

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
    String trimmedName = name.trim();

    boolean currentCharacterValid;
    
    if (name == null || trimmedName.length() == 0) {
      return false;
    }

    // ensure that XML standard reserved names are not used
    
    if(trimmedName.toUpperCase().startsWith("XML")) {
      return false;
    }

    // test that name starts with a valid XML name-starting character
    
    if(!Character.isUpperCase(trimmedName.charAt(0)) &&
       !Character.isLowerCase(trimmedName.charAt(0)) && 
       trimmedName.charAt(0) != '_') {
      return false; 
    }
    
    // test that remainder name chars are a valid XML name characters 
    
    if (name.trim().length()>0) {
      for(int i = 1; i < trimmedName.length(); i++) {
        currentCharacterValid = false;
        
        if (Character.isUpperCase(trimmedName.charAt(i)) ||
            Character.isLowerCase(trimmedName.charAt(i)) || 
            Character.isDigit(trimmedName.charAt(i))) {
          currentCharacterValid = true;
        }

        if (trimmedName.charAt(i) == '_' || trimmedName.charAt(i) == '-' ||
            trimmedName.charAt(i) == '.') {
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
    
    char[] nameAsCharArray = name.toCharArray();
    char[] newNameAsCharArray = new char[nameAsCharArray.length];
    
    boolean currentCharacterValid = false;
    int j = 0;

    for(int i = 0; i < nameAsCharArray.length; i++) {
      currentCharacterValid = false;
      
      if (nameAsCharArray[i] == ' ') {
        nameAsCharArray[i] = '_';
      }

      if (Character.isLetterOrDigit(nameAsCharArray[i])) {
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

  /**
   * Tests the supplied character to determine whether it is a 
   * special XML Character. If so, it returns true.
   * @param character A character to be tested.
   * @return whether the character is a special XML character or not.
   */

  public static boolean isSpecialXMLCharacter(final char character) {
    for (int i = 0; i < XML_SPECIAL_CHARACTERS.length; i++) {
      if (character == XML_SPECIAL_CHARACTERS[i]) {
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
  
  public static String getTagEnclosedEntireVariableXQuery(DataVariable variable) {
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
  
  public static String getEntireVariableXQuery(DataVariable variable) {
    String scopeId = variable.getScope().getDecomposition().getLabelAsElementName();
    String xQuery = "/" + scopeId + "/" + variable.getName();

    return xQuery;
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

  
  public static String getTagEnclosedVariableContentXQuery(DataVariable variable) {
    return "{" + getVariableContentXQuery(variable) + "}";
  }
  
  /**
   * A convenience method that generates an XPath expression for the given variable,
   * converting the expression to a number automatically if the variable is a number.
   * @param variable The variable to build an XPath expression for.
   * @return XPath expression for the variable.
   */
  
  public static String getXPathPredicateExpression(DataVariable variable) {
    // TODO: This won't work for user-defined simple type enumerations based on number simple types.
    //       Can I use the "restriction" tag of simple enumerated types to see if it's a number?
    
    if (variable.isNumberType()) {
      return "number(" + getVariableContentXQuery(variable) + ")";
    }
    return getVariableContentXQuery(variable);
  }
  
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
  
  public static String getVariableContentXQuery(DataVariable variable) {
    
    String variableContent = null;
    
    if (variable.isYInternalType()) {
      variableContent = "*";
    }
    else  if (DataVariable.isBaseDataType(variable.getDataType())) {
      variableContent = "text()";
    }
    else {
      switch(YAWLEngineProxy.getInstance().getDataTypeComplexity(variable.getDataType())) {
        case YAWLEngineProxy.COMPLEX_DATA_TYPE_COMPLEXITY: {
          variableContent = "*";
          break;
        }
        case YAWLEngineProxy.SIMPLE_DATA_TYPE_COMPLEXITY: {
          variableContent = "text()";
          break;
        }
        case YAWLEngineProxy.UNRECOGNISED_DATA_TYPE_COMPLEXITY: {
          variableContent = "text()";
          break;
        }
      }
    }
    
    return getEntireVariableXQuery(variable) + "/" + variableContent;
  }
  
  /**
   * Returns a piece of XML where opening and closing tags using <code>variableName</code>
   * encloses <code>content</code>. The resulting string will only be valid XML if <code>content</code>
   * was valid XML initially.
   * @param variableName The name to use in the opening and closing element tags
   * @param content The content to be enclosed within these tags
   * @return An XML fragment using <code>variableName</code> as an enclosing XML element.
   */
  
  public static String getTaggedOutputVariableWithContent(String variableName, String content) {
    StringBuffer taggedQuery = new StringBuffer();
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
    return toValidXMLName(theFile.getName());
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
                    temp = node.toPrettyString();
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
