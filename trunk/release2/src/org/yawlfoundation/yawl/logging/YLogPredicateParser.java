package org.yawlfoundation.yawl.logging;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.SaxonUtil;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 17/04/2009
 *
 * other services can extend this to do their own pre parsing
 *
 */
public class YLogPredicateParser {

    public YLogPredicateParser() {}

    public String parse(String s) {
        if (s == null) return null;
        String[] words = s.split("\\s+");                      // split on whitespace
        for (int i=0; i < words.length; i++) {
            String word = words[i];
            if (isDelimited(word)) {
                words[i] = valueOf(word);
            }
        }
        return StringUtils.join(words, " ");
    }


    private boolean isDelimited(String s) {
        return s.startsWith("${") && s.endsWith("}");
    }


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


    protected String dateTimeString(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(time);
    }


    protected String getAttributeValue(Map<String, String> map, String s) {
        if (map == null) return null;
        String key = extractKey(s);
        return map.get(key);
    }


    protected String evaluateExpression(String s, Element data) {
        String result;
        String expression = extractKey(s);
        Document dataDoc = new Document((Element) data.clone());
        try {
            result = SaxonUtil.evaluateQuery(expression, dataDoc);
        }
        catch (SaxonApiException sae) {
            result = "_evaluation_error_";
        }
        return result;
    }


    protected String namesToCSV(Set<String> names) {
        if (names == null) return "Nil";
        String csv = "" ;
        for (String name : names) {
             csv += name + ", ";
        }
        return csv;
    }

    
    private String extractKey(String s) {
        return s.substring(s.lastIndexOf(":") + 1, s.length() - 1);
    }
    
}
