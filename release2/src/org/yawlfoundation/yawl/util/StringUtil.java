/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.util;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
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
        StringBuilder temp = new StringBuilder();
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
     * Converts a string to all lower case, and capitalises the first letter of the string
     * @param s unformated string.
     * @return The formated string.
     */
    public static String capitalise(String s) {
        if ((s == null) || (s.length() == 0)) return s;
        char[] chars = s.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
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
        SimpleDateFormat fmt = null;

        /**
         * Set format depending upon whether we have a timestamp component to the calendar.
         * Ok, this is slightly flawed as an assumption as we could be bang on midnight.......
         */
        if ((calendar.get(Calendar.HOUR) == 0) && (calendar.get(Calendar.MINUTE) == 0)
                && (calendar.get(Calendar.SECOND) == 0))
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
     * formats a long time value into a string of the form 'ddd:hh:mm:ss'
     * @param time the time value (in milliseconds)
     * @return the formatted time string
     */
    public static String formatTime(long time) {
        long secsPerHour = 60 * 60 ;
        long secsPerDay = 24 * secsPerHour ;

        long millis = time % 1000;
        time /= 1000;
        long days = time / secsPerDay ;
        time %= secsPerDay ;
        long hours = time / secsPerHour ;
        time %= secsPerHour ;
        long mins = time / 60 ;
        time %= 60 ;

        return String.format("%d:%02d:%02d:%02d.%04d", days, hours, mins, time, millis) ;
    }

    /**
     * Converts the throwable object into the standard Java stack trace format.
     *
     * @param t Throwable to convert to a String
     * @return String representation of Throwable t
     */
    public static String convertThrowableToString(Throwable t)
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
        StringBuilder sb = new StringBuilder(50);
        sb.append('<').append(wrapTag);
        if (core != null) {
            sb.append('>').append(core).append("</").append(wrapTag).append('>');
        }
        else {
            sb.append("/>");
        }
        return sb.toString();
    }

    public static String wrapEscaped(String core, String wrapTag) {
        return wrap(JDOMUtil.encodeEscapes(core), wrapTag);
    }


    /**
     * Removes an outer set of xml tags from an xml string, if possible
     * @param xml the xml string to strip
     * @return the stripped xml string
     */
    public static String unwrap(String xml) {
        if (xml != null) {
            int start = xml.indexOf('>') + 1;
            int end = xml.lastIndexOf('<');
            if (end > start) {
                return xml.substring(start, end);
            }
        }
        return xml;
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
        for (char digit : digits) {
            if ((digit < '0') || (digit > '9')) return false;
        }
        return true;
    }


    public static File stringToFile(String fileName, String contents) {
        return stringToFile(new File(fileName), contents);
    }


    public static File stringToTempFile(String contents) {
        try {
            return stringToFile(
                    File.createTempFile(
                            RandomStringUtils.randomAlphanumeric(12), null), contents);
        }
        catch (IOException e) {
            return null;
        }
    }


    public static File stringToFile(File f, String contents) {
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f));
            buf.write(contents, 0, contents.length());
            buf.close();
        }
        catch (IOException ioe) {
            f = null;
        }
        return f;
    }


    public static String fileToString(File f) {
        if (f.exists()) {
            try {
                int bufsize = (int) f.length();
                InputStream fis = new FileInputStream(f);
                return streamToString(fis, bufsize);
            }
            catch (Exception e) {
                return null;
            }
        }
        else return null;
    }

    public static String streamToString(InputStream is) {
        return streamToString(is, 32768);  // default bufsize
    }


    public static String streamToString(InputStream is, int bufSize) {
        try {

            // read reply into a buffered byte stream - to preserve UTF-8
            BufferedInputStream inStream = new BufferedInputStream(is);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(bufSize);
            byte[] buffer = new byte[bufSize];
            int bytesRead;

            while ((bytesRead = inStream.read(buffer, 0, bufSize)) > 0) {
                outStream.write(buffer, 0, bytesRead);
            }

            outStream.close();
            inStream.close();

            // convert the bytes to a UTF-8 string
            return outStream.toString("UTF-8");

        }
        catch (IOException ioe) {
            return null;
        }
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


    public static String getRandomString(int length) {
        return RandomStringUtils.random(length);
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null) || (s.length() == 0);
    }


    public static int strToInt(String s, int def) {
        if (isNullOrEmpty(s)) return def;      // short circuit
        try {
            return new Integer(s);
        }
        catch (NumberFormatException e) {
            return def;
        }
    }


    public static long strToLong(String s, long def) {
        if (isNullOrEmpty(s)) return def;      // short circuit
        try {
            return new Long(s);
        }
        catch (NumberFormatException e) {
            return def;
        }
    }


    public static double strToDouble(String s, double def) {
        if (isNullOrEmpty(s)) return def;      // short circuit
        try {
            return new Double(s);
        }
        catch (NumberFormatException e) {
            return def;
        }
    }


    public static Duration strToDuration(String s) {
        if (s != null) {
            try {
                return DatatypeFactory.newInstance().newDuration(s) ;
            }
            catch (DatatypeConfigurationException dce) {
                // nothing to do - null will be returned
            }
            catch (IllegalArgumentException dce) {
                // nothing to do - null will be returned
            }
        }
        return null;
    }

    
    public static boolean isValidDurationString(String s) {
        try {
            DatatypeFactory.newInstance().newDuration(s) ;
            return true;
        }
        catch (DatatypeConfigurationException dce) {
            return false;
        }
    }


    public static long durationToMSecs(Duration d, long def) {
        return (d != null) ? d.getTimeInMillis(new Date()) : def;
    }

    public static long durationToMSecs(Duration d) {
        return durationToMSecs(d, 0);
    }

    public static long durationStrToMSecs(String s) {
        Duration d = strToDuration(s);
        return (d != null) ? durationToMSecs(d) : 0;
    }

    public static long xmlDateToLong(String s) {
        if (s == null) return -1;
        try {
            XMLGregorianCalendar cal =
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
            return cal.toGregorianCalendar().getTimeInMillis();
        }
        catch (DatatypeConfigurationException dce) {
            return -1;
        }
    }

    public static String longToDateTime(long time) {
        GregorianCalendar gregCal = new GregorianCalendar();
        gregCal.setTimeInMillis(time);
        try {
            XMLGregorianCalendar cal =
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
            return cal.toXMLFormat();
        }
        catch (DatatypeConfigurationException dce) {
            return null;
        }
    }


    public static int find(String toSearch, String toFind, int start, boolean ignoreCase) {
        if ((toSearch == null) || (toFind == null)) return -1;
        if (ignoreCase) {
            toSearch = toSearch.toUpperCase();
            toFind = toFind.toUpperCase();
        }
        return find(toSearch, toFind, start);
    }


    public static int find(String toSearch, String toFind, int start) {
        if ((toSearch == null) || (toFind == null)) return -1;
        if ((toSearch.length() < 2048) || (toFind.length() < 4)) {
            return toSearch.indexOf(toFind, start);
        }
        if (start < 0) start = 0;
        final int lastCharToFindIndex = toFind.length() - 1;
        final char lastCharToFind = toFind.charAt(toFind.length() - 1);

        int[] skipTable = new int[255];
        for (int i = 0; i < 255; i++) skipTable[i] = toFind.length();
        for (int i = 0; i < lastCharToFindIndex; i++) {
            skipTable[toFind.charAt(i) & 255] = lastCharToFindIndex - i;
        }

        for (int i = start + lastCharToFindIndex; i < toSearch.length();
             i += skipTable[toSearch.charAt(i) & 255]) {

            if (toSearch.charAt(i) != lastCharToFind) {
                while ((i += skipTable[toSearch.charAt(i) & 255]) < toSearch.length()
                        && toSearch.charAt(i) != lastCharToFind);

                if (i < toSearch.length()) {
                    int j = i - 1;
                    int index = i - toFind.length() + 1;
                    for (int k = lastCharToFindIndex - 1;
                         j > index && toSearch.charAt(j) == toFind.charAt(k); j--, k--);

                    if (j == index) return index;
                }
                else break;
            }
        }
        return -1;
    }

    public static int find(String toSearch, String toFind) {
        return find(toSearch, toFind, 0);
    }


    public static List<Integer> findAll(String toSearch, String toFind, boolean ignoreCase) {
        if (ignoreCase) {
            if (! ((toSearch == null) || (toFind == null))) {
                toSearch = toSearch.toUpperCase();
                toFind = toFind.toUpperCase();
            }    
        }
        return findAll(toSearch, toFind);
    }

    
    public static List<Integer> findAll(String toSearch, String toFind) {
        List<Integer> foundList = new ArrayList<Integer>();
        int start = 0;
        while (start > -1) {
            start = find(toSearch, toFind, start);
            if (start > -1) foundList.add(start++);
        }
        return foundList;
    }
    
    
    public static String repeat(char c, int count) {
        char[] chars = new char[count];
        for (int i=0; i < count; i++) chars[i] = c;
        return new String(chars);
    }


    public static String join(List<String> strList, char separator) {
        if (strList == null || strList.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : strList) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(s);
        }
        return sb.toString();
    }


    public static List<String> splitToList(String s, String separator) {
        if (isNullOrEmpty(s)) return Collections.emptyList();
        return Arrays.asList(s.split(separator));
    }

}
