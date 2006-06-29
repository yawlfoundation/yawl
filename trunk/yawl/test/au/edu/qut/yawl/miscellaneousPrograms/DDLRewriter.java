/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.miscellaneousPrograms;

import java.io.*;
import java.net.URL;

/**
 * 
 * @author Lachlan Aldred
 * Date: 15/04/2005
 * Time: 17:57:17
 * This file remains the property of the Queensland University of 
 * Technology.
 * You do not have permission to use, view, execute or modify the source outside the terms
 * of the YAWL licence.
 * For more information about the YAWL licence refer to the 'downloads' section under
 * http://www.yawl-system.com
 */
public class DDLRewriter {
    public static void main(String[] args) throws IOException {
        DDLRewriter ddlRewriter = new DDLRewriter();
        URL fileURL = ddlRewriter.getClass().getResource("ResourceTaxonomyORM.DDL");
        File originalFile = new File(fileURL.getFile());
        BufferedReader reader = new BufferedReader(new FileReader(originalFile));

        String line = null;
        StringBuffer newContents = new StringBuffer();
        while((line = reader.readLine() ) != null){
            System.out.println("line = " + line);
            if(line.indexOf("\"") != -1){
                String newLine = rewriteQuotes(line);
                System.out.println("\tnewLine = " + newLine);
                newContents.append( newLine + "\r\n");
            } else {
                newContents.append(line + "\r\n");
            }
        }

        File output = new File(originalFile.getParentFile(), "output.txt");
        FileWriter write = new FileWriter(output);
        write.write(newContents.toString());
        write.flush();
        write.close();
    }


    private static String rewriteQuotes(String chars) {
        String newLine = null;
        System.out.println("\trewriteQuotes: chars = " + chars);
        int firstQuoteIDX = chars.indexOf('"');
        int secondQuoteIDX = chars.indexOf('"', firstQuoteIDX + 1);
        int thirdQuoteIDX = chars.indexOf('"', secondQuoteIDX + 1);
        newLine = chars.substring(0, firstQuoteIDX) +
                rewrite(chars.substring(firstQuoteIDX + 1, secondQuoteIDX));

        if (thirdQuoteIDX != -1){
            newLine += rewriteQuotes(chars.substring(secondQuoteIDX + 1, chars.length()));
        }
        else {
            newLine += chars.substring(secondQuoteIDX + 1, chars.length());
        }


        return newLine;
    }


    private static String rewrite(String quotedChars) {
        char [] chars = quotedChars.toCharArray();
        String result = "";

        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if(aChar == ' '){
                result += ("" + chars[i+1]).toUpperCase();
                i++;
            } else {
                result += aChar;
            }
        }

        return result;
    }
}
