/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.util;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtil
{
    private static final String     TIMESTAMP_DELIMITER = " ";
    private static final String     DATE_DELIMITER = "-";
    private static final String     TIME_DELIMITER = ":";
    private static final String     TIME_FORMAT = "HH" + TIME_DELIMITER + "mm" + TIME_DELIMITER + "ss";
    private static final String     DATE_FORMAT = "yyyy" + DATE_DELIMITER + "MM" + DATE_DELIMITER + "dd";
    private static final String     TIMESTAMP_FORMAT = DATE_FORMAT + TIMESTAMP_DELIMITER + TIME_FORMAT;



    /**
     * Utility routine to replace one token with another within a string object.
     *
     * @param buffer       String object to be manipulated
     * @param fromToken    Token to be replaced
     * @param toToken      Token used in replacement
     * @return             String object holding modified String
     */
    public static String replaceTokens(String buffer, String fromToken, String toToken)
    {
        /*
         * Note: We don't use the StringTokenizer class as it doesn't cope with '\n' substrings
         * correctly.
         */
        int old = 0;
        StringBuffer temp = new StringBuffer();
        while (true)
        {
            int pos = buffer.indexOf(fromToken, old);

            if (pos != -1)
            {
                String subText = buffer.substring(old, pos);
                temp.append(subText + toToken);
                old = pos + fromToken.length();
            }
            else
            {
                String subText = buffer.substring(old);
                temp.append(subText);
                break;
            }
        }
        return temp.toString();
    }

    /**
     * Utility routine to return the date supplied as an ISO formatted string.
     *
     * @param date          Date object to be formatted
     * @return              String object holding ISO formatted representation of date supplied
     */
    public static String getISOFormattedDate(Date date)
    {
        SimpleDateFormat fmt = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return fmt.format(date);
    }

    /**
     * Utility routine to return a debug message suitable for logging. It basically prefixes the supplied message
     * with the current timestamp in ISO format.
     *
     * @param msg           Body of debug message to be prefixed with the current timestamp
     * @return              String object holding debug message prefixed with ISO formatted current timestamp
     */
    public static String getDebugMessage(String msg)
    {
        return getISOFormattedDate(new Date()) + " " + msg;
    }

    /**
     * Utility method to take a string and return the string in revserse sequence.
     *
     * @param   inputString String to be reversed
     * @return  Reversed string
     */
    public static String reverseString(String inputString)
    {
        char[] inputChars = new char[inputString.length()];
        char[] outputChars = new char[inputString.length()];

        inputString.getChars(0, inputString.length() ,inputChars, 0);
        int pointer = inputChars.length -1;


        for(int i=0;i<=inputChars.length -1 ;i++)
        {
            outputChars[pointer] = inputChars[i];
            pointer --;
        }

        return new String(outputChars);
    }

    /**
     * Removes all white space from a string.
     * @param string String to remove white space from
     * @return Resulting whitespaceless string.
     */
    public static String removeAllWhiteSpace(String string)
    {
        Pattern p = Pattern.compile("[\\s]");
        Matcher m;
        do
        {
            m = p.matcher(string);
            if (m.find())
            {
                string = m.replaceAll("");
            }
        }while(m.find());

        return string;
    }

    /**
     * Formats a postcode into standard Royal Mail format
     * @param postcode
     * @return Postcode correctly formatted
     */
    public static String formatPostCode(String postcode)
    {
        if(postcode == null) return null;
        postcode = removeAllWhiteSpace(postcode).toUpperCase();
        if(postcode.length() < 3) return postcode;
        else return postcode.substring(0,postcode.length()-3) + " " + postcode.substring(postcode.length()-3, postcode.length());
    }

    /**
     * Formats a sortcode into the common form nn-nn-nn
     * @param sortcode
     * @return Sortcode correctly formatted
     */
    public static String formatSortCode(String sortcode)
    {
        return sortcode.substring(0,2) + "-" + sortcode.substring(2,4) + "-" + sortcode.substring(4,6);
    }

    /**
     * Converts a string to all lower case, and capitailises the first letter of the string
     * @param string unformated string.
     * @return The formated string.
     */
    public static String capitalise(String string)
    {
        String result = "";
        StringTokenizer tokens = new StringTokenizer(string.toLowerCase(), " ");

        while (tokens.hasMoreTokens())
        {
            String token = tokens.nextToken();
            result += token.substring(0, 1).toUpperCase() + token.substring(1) + " ";
        }

        return result;
    }

    /**
     * Utility routine that takes in a Calendar referece and returns a date/time stamp suitable for use
     * in a Portlets environment.
     * @param calendar
     * @return Date/timestamp suitable for display.
     * @deprecated Use TimeUtil.formatUIDate
     */
    public static String formatUIDate(Calendar calendar)
    {
        String dateTimestamp = null;
        SimpleDateFormat fmt = null;

        /**
         * Set format depending upon whether we have a timestamp component to the calendar.
         * Ok, this is slightly flawed as an assumption as we could be bang on midnight.......
         */
        if ((calendar.get(Calendar.HOUR) == 0) && (calendar.get(Calendar.MINUTE) == 0) && (calendar.get(Calendar.SECOND) == 0))
        {
            fmt = new SimpleDateFormat("dd-MMM-yy");
        }
        else
        {
            fmt = new SimpleDateFormat("dd-MMM-yy hh:mm a");
        }

        return fmt.format(calendar.getTime());
    }

    /**
     * Utility routine which takes a decimal value as a string (e.g. 0.25 equating to 25p) and returns the
     * value in UI currency format (e.g. £0.25).
     * @return  A formatted currency
     */
    public static String formatDecimalCost(BigDecimal value)
    {
        Currency currency = Currency.getInstance(Locale.getDefault());
        NumberFormat fmt = DecimalFormat.getInstance();
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return currency.getSymbol() + fmt.format(value);
    }

    /**
     * Converts the throwable object into the standard Java strack trace format.
     *
     * @param t Throwable to convert to a String
     * @return String representation of Throwable t
     */
    public static final String convertThrowableToString(Throwable t)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        t.printStackTrace(writer);
        writer.flush();
        return baos.toString();
    }

    /**
     * Esacpes all HTML entities and "funky accents" into the HTML 4.0 encodings, replacing
     * new lines with "&lt;br&gt;", tabs with four "&amp;nbsp;" and single spaces with "&amp;nbsp;".
     * @param string to escape
     * @return escaped string
     */
    public static String formatForHTML(String string)
    {
        string = StringEscapeUtils.escapeHtml(string);
        string = string.replaceAll("\n", "<br>");
        string = string.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        string = string.replaceAll(" ", "&nbsp;");
        return string;
    }

    /** encases a string with a pair of xml tags
     *
     * @param core the text to encase
     * @param wrapTag the name of the tag to encase the text
     * @return the encased string (e.g. "<wrapTag>core</wrapTag>")
     */
    public static String wrap(String core, String wrapTag) {
        if (core != null)
            return String.format("<%s>%s</%s>", wrapTag, core, wrapTag) ;
        else
            return String.format("<%s/>", wrapTag);
    }

    public static String wrapEscaped(String core, String wrapTag) {
        return wrap(JDOMUtil.encodeEscapes(core), wrapTag);
    }


    /**
     * Removes an outer set of xml tags from an xml string, if possible
     * @param inputXML the xml string to strip
     * @return the stripped xml string
     */
    public static String unwrap(String inputXML) {
        if (inputXML != null) {
            int beginClipping = inputXML.indexOf(">") + 1;
            int endClipping = inputXML.lastIndexOf("<");
            if (beginClipping >= 0 && endClipping >= 0 && endClipping > beginClipping) {
                inputXML = inputXML.substring(beginClipping, endClipping);
            }
        }
        return inputXML;
    }
    
    /**
     * Encodes reserved characters in an xml string
     * @param s the string to encode
     * @return the newly encoded string
     */
    public static String xmlEncode(String s) {
        if (s == null) return s;
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }

    /**
     * Decodes reserved characters in an xml string
     * @param s the string to decode
     * @return the newly decoded string
     */
    public static String xmlDecode(String s) {
        if (s == null) return s;
        try {
            return URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }

    public static boolean isIntegerString(String s) {
        if (s == null) return false;
        char[] digits = s.toCharArray() ;
        for (int i=0; i < digits.length; i++) {
            if ((digits[i] < '0') || (digits[i] > '9')) return false;
        }
        return true;
    }


    public static File stringToFile(String fileName, String contents) {
        File f = new File(fileName);
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f));
            buf.write(contents, 0, contents.length());
            buf.close();
        }
        catch (IOException ioe) {
            f = null;
        }
        finally {
            if (f != null) f.delete();
        }
        return f;
    }


    public static String fileToString(File f) {
        if (f.exists()) {

            try {
                int bufsize = (int) f.length();
                FileInputStream fis = new FileInputStream(f) ;

                // read into buffered byte stream - to preserve UTF-8
                BufferedInputStream inStream = new BufferedInputStream(fis);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream(bufsize);
                byte[] buffer = new byte[bufsize];

                // read chunks from the input stream and write them out
                int bytesRead = 0;
                while ((bytesRead = inStream.read(buffer, 0, bufsize)) > 0) {
                    outStream.write(buffer, 0, bytesRead);
                }
                outStream.flush();

                // convert the bytes to a UTF-8 string
                return outStream.toString("UTF-8");
            }
            catch (Exception e) {
                return null;
            }
        }
        else return null;
    }


    public static String fileToString(String filename) {
        return fileToString(new File(filename));
    }


    public static String extract(String source, String pattern) {
        String extracted = null;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        if (m.find()) {
            extracted = m.group();
        }
        return extracted;
    }
}
