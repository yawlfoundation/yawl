package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Author: Michael Adams
 * Creation Date: 15/08/2009
 */
public class XMLDialogFormatter {

    private static enum Chopper {
        OPEN,  // <
        CLOSE, // </
        BRACE, // {
        NIL }

    public static String format(String s) {
        if (s.trim().startsWith("<")) {
            String formatted = JDOMUtil.formatXMLString(s);
            if (formatted != null) {
                if (formatted.indexOf('{') > -1)  {
                    formatted = formatXPaths(formatted);
                }
                return formatted;
            }
        }
        return s;
    }


    private static String formatXPaths(String s) {
        int tabCount = 0;
        StringBuilder result = new StringBuilder();
        char[] input = s.toCharArray();
        Chopper prevChopper = Chopper.NIL;

        for (int i=0; i<input.length; i++) {
            switch (input[i]) {
                case ' ' : continue;
                case '<' : {
                    boolean closer = (input[i+1] == '/');
                    if (prevChopper == Chopper.OPEN) {
                        if (! closer) tabCount += 2;
                    }
                    if (prevChopper == Chopper.BRACE) {
                        if (closer) tabCount -= 2;
                    }                    
                    if (prevChopper != Chopper.NIL) {
                        result.append(lineBreakAndIndent(tabCount));                        
                    }
                    prevChopper = closer ? Chopper.CLOSE : Chopper.OPEN;
                    break;
                }
                case '{' : {
                    if (prevChopper == Chopper.OPEN) {
                        tabCount += 2;
                    }
                    if (prevChopper != Chopper.NIL) {
                        result.append(lineBreakAndIndent(tabCount));
                    }
                    prevChopper = Chopper.BRACE;
                    break;
                }
                case '\n' : {
                    if (prevChopper != Chopper.NIL) {     // eat multi newlines
                        if (prevChopper == Chopper.OPEN) {
                            tabCount += 2;
                        }
                        else if (prevChopper == Chopper.CLOSE) {
                            tabCount -= 2;
                        }
                        result.append(lineBreakAndIndent(tabCount));
                        prevChopper = Chopper.NIL;
                    }
                    continue;
                 }
            }
            result.append(input[i]);
        }
        return result.toString();
    }


    private static char[] lineBreakAndIndent(int tabCount) {
        char[] result = new char[tabCount + 1];
        result[0] = '\n';
        for (int i=1; i<=tabCount; i++) result[i] = ' ';
        return result;
    }

}
