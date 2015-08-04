package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/09/2014
 */
public class IdGenerator {

    private static final Set<String> _usedIDs = new HashSet<String>();


    protected static void clear() { _usedIDs.clear(); }


    protected static String uniquify(String id) {
        char[] idChars = id.toCharArray();
        StringBuilder cleanChars = new StringBuilder(idChars.length);

        // only letter, digit, underscore or dash allowed for an id
        for (char c : idChars) {
            if (Character.isJavaIdentifierPart(c) && c != '$') {
                cleanChars.append(c);
            }
        }
        String cleanid = cleanChars.toString();
        int suffix = 0;
        while (_usedIDs.contains(cleanid + ++suffix)) ;
        String result = cleanid + suffix;
        _usedIDs.add(result);
        return result;
    }




}
