package org.yawlfoundation.yawl.util;

import org.jdom.Document;
import org.jdom.Element;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

/**
 * Parses strings, replacing substrings within of the form ${expression} with the
 * result of the expression evaluation
 *
 * Author: Michael Adams
 * Date: 02/04/2010
 *
 * Known subclasses: logging.YLogPredicateDecompositionParser,
 *                   logging.YLogPredicateParameterParser,
 *                   logging.YLogPredicateWorkItemParser,
 *                   resourcing.util.LogPredicateParser,
 *                   resourcing.jsf.dynform.DynTextParser
 *
 * Subclasses should override the 'valueOf' method to handle their own pre-parsing.
 */

public class YPredicateParser {

    public YPredicateParser() {}

    /**
     * Parses a string, replacing substrings within of the form ${expression} with the
     * result of the expression evaluation
     * @param s the string to parse
     * @return s, with any embedded expressions replaced with their evaluations
     */
    public String parse(String s) {
        if ((s == null) || (! s.contains("${"))) return s;     // short circuit 

        // split on the points immediately before an instance of "${" or immediately
        // after an instance of "}", preserving all chars.
        String[] phrases = s.split("(?=\\$\\{)|(?<=\\})");

        for (int i=0; i < phrases.length; i++) {
            String phrase = phrases[i];
            if (isDelimited(phrase)) {
                phrases[i] = valueOf(phrase);
            }
        }
        return join(phrases);
    }


    /**
     * Evaluates an expression and returns the result. Subclasses should override this
     * class to do their own expression evaluations before calling this version for
     * any general expression evaluations.
     * @param s the string representing the expression, of the form ${expression}
     * @return the result of the expression evaluation, or the unchanged string if the
     * expression is unrecognised
     */
    protected String valueOf(String s) {
        if (s.equalsIgnoreCase("${now}")) {
            s = dateTimeString(System.currentTimeMillis());
        }
        else if (s.equalsIgnoreCase("${date}")) {
            s = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        }
        else if (s.equalsIgnoreCase("${time}")) {
            s = new SimpleDateFormat("HH:mm:ss.SSS").format(System.currentTimeMillis()); 
        }
        return s ;
    }


    /**
     * Converts a time value to a full date & time string
     * @param time the time value
     * @return a string representing the date & time value
     */
    protected String dateTimeString(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(time);
    }


    /**
     * Extracts a key from the delimited expression passed, then seeks the value
     * corresponding to that key from the map passed
     * @param map a map of String pairs
     * @param s a string representing an expression, of the form ${expression:key},
     * containing the key
     * @return the corresponding key value, or null if the map is null or the map
     * does not contain the key
     */
    protected String getAttributeValue(Map<String, String> map, String s) {
        if (map == null) return null;
        String key = extractKey(stripDelimiters(s));
        return map.get(key);
    }


    /**
     * Transforms a Set of Strings into a string of comma separated values
     * @param names the Set of Strings to transform
     * @return a string containing each of the Strings in the Set, spearated by a
     * comma, or "Nil" if there are no Strings in the Set
     */
    protected String namesToCSV(Set<String> names) {
        if (names == null) return "Nil";
        String csv = "" ;
        for (String name : names) {
             csv += name + ", ";
        }
        return csv;
    }


    /**
     * Evaluates an XQuery embedded within a delimited expression 
     * @param s the delimited expression, either of the form ${query} or ${expression:query}
     * @param data XML'd data that may be referenced by the expression. May be null if
     * the expression doesn't reference any data
     * @return the result of the evaluation, or "__evaluation_error__" if there's a
     * problem evaluating the expression
     */
    protected String evaluateQuery(String s, Element data) {
        String expression = stripDelimiters(s);
        if (expression.startsWith("expression:")) {
            expression = extractKey(expression);
        }
        return evaluateXQuery(expression, data);
    }


    /*******************************************************************************/

    /**
     * Checks if the string passed is a delimited expression of the form ${expression}
     * @param s the string to check
     * @return true if it has the correct form, false if otherwise
     */
    private boolean isDelimited(String s) {
        return s.startsWith("${") && s.endsWith("}");
    }


    /**
     * Transforms the String array passed into a single String
     * @param phrases the String array to join
     * @return the joined Strings
     */
    private String join(String[] phrases) {
        StringBuilder joined = new StringBuilder();
        for (String phrase : phrases) {
            joined.append(phrase);
        }
        return joined.toString();
    }


    /**
     * Evaluates an XQuery
     * @param s the query expression
     * @param data XML'd data that may be referenced by the expression. May be null if
     * the expression doesn't reference any data
     * @return the result of the evaluation, or "__evaluation_error__" if there's a
     * problem evaluating the expression
     */
    private String evaluateXQuery(String s, Element data) {
        try {
            Document dataDoc = new Document((Element) data.clone());
            return SaxonUtil.evaluateQuery(s, dataDoc);
        }
        catch (Exception e) {
            return "__evaluation_error__";
        }
    }


    /**
     * removes the surrounding ${...} from a String
     * @param s the delimited String
     * @return the inner contents of the String with the delimiters removed
     */
    private String stripDelimiters(String s) {
        return s.substring(2, s.length() - 1);    
    }


    /**
     * Extracts the key part of an expression of the form "expression:key"
     * @param s the String containing the key
     * @return the kay part of the String
     */
    private String extractKey(String s) {
        return s.substring(s.lastIndexOf(":") + 1);
    }

}
