/*
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a
 *    Package may be copied, such that the Copyright Holder maintains some
 *    semblance of artistic control over the development of the package,
 *    while giving the users of the package the right to use and distribute
 *    the Package in a more-or-less customary fashion, plus the right to
 *    make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the
 *    Copyright Holder, and derivatives of that collection of files created
 *    through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been
 *    modified, or has been modified in accordance with the wishes of the
 *    Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights
 *    for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this
 *    Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of
 *    media cost, duplication charges, time of people involved, and so
 *    on. (You will not be required to justify it to the Copyright Holder,
 *    but only to the computing community at large as a market that must
 *    bear the fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself,
 *    though there may be fees involved in handling the item. It also means
 *    that recipients of the item may redistribute it under the same
 *    conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of
 *    the Standard Version of this Package without restriction, provided
 *    that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications
 *    derived from the Public Domain or from the Copyright Holder. A
 *    Package modified in such a way shall still be considered the Standard
 *    Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way,
 *    provided that you insert a prominent notice in each changed file
 *    stating how and when you changed that file, and provided that you do
 *    at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise
 *        make them Freely Available, such as by posting said modifications
 *        to Usenet or an equivalent medium, or placing the modifications
 *        on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard
 *        Version of the Package.
 *
 *        b) use the modified Package only within your corporation or
 *        organization.
 *
 *        c) rename any non-standard executables so the names do not
 *        conflict with standard executables, which must also be provided,
 *        and provide a separate manual page for each non-standard
 *        executable that clearly documents how it differs from the
 *        Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or
 *    executable form, provided that you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library
 *        files, together with instructions (in the manual page or
 *        equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of
 *        the Package with your modifications.
 *
 *        c) accompany any non-standard executables with their
 *        corresponding Standard Version executables, giving the
 *        non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent),
 *        together with instructions on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of
 *    this Package. You may charge any fee you choose for support of this
 *    Package. You may not charge a fee for this Package itself.  However,
 *    you may distribute this Package in aggregate with other (possibly
 *    commercial) programs as part of a larger (possibly commercial)
 *    software distribution provided that you do not advertise this Package
 *    as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as
 *    output from the programs of this Package do not automatically fall
 *    under the copyright of this Package, but belong to whomever generated
 *    them, and may be sold commercially, and may be aggregated with this
 *    Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package
 *    shall not be considered part of this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or
 *    promote products derived from this software without specific prior
 *    written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 *    IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 *    WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * */
/* $Id */
package org.chiba.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * This abstract class is responsible for calculations on and with Dates,
 * for transformation of these to Strings vice versa.  Main focus of
 * calculations are Startdates(especially Startdates of calendarweeks).
 * Additionally it extracts from a String that represents a Date only the
 * day, the month and the year.  It also gets the calendarweek.
 *
 * @author Sabine Krach
 * @version $Revision: 1.6 $
 */
public abstract class DateUtil {
    /**
     * gets the current Date and returns it as String
     */
    public static String getDate(Locale locale) {
        Calendar cal_date = new GregorianCalendar();
        cal_date = Calendar.getInstance();

        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        String strstart = null;
        strstart = df.format(cal_date.getTime());

        return strstart;
    }

    /**
     * same as above but allows to specify a DateFormat-style.15.06.2001 00:07
     */
    public static String getDate(Locale locale, int style) {
        Calendar cal_date = new GregorianCalendar();
        cal_date = Calendar.getInstance();

        DateFormat df = DateFormat.getDateTimeInstance();
        String strstart = null;
        strstart = df.format(cal_date.getTime());

        return strstart;
    }

    /**
     * Returns the current date and time correctly formated
     */
    public static String getDate() {
        return getDate(Locale.getDefault(), 0);
    }

    /**
     * get the current date and returns it as formated string in the
     * current locale if (and only if) the parameter first is empty or the
     * empty string.
     */
    public static String getDateIfEmpty(String first) {
        if ((first != null) && (first.length() > 0)) {
            return first;
        }

        return getDate();
    }

    /**
     * This method parses the @param String tempdat, according to a specified format style
     * determined by the @param Locale locale to a Date. It sets a Gregorian Calender
     * with this date and gets the value of the day.
     * Needed for StringDate Constructor
     *
     * @param tempdat - a date string
     * @param locale  - the locale to use
     */
    public static String getDay(String tempdat, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        Date d = df.parse(tempdat, new ParsePosition(0));
        Calendar cal_date = new GregorianCalendar();
        cal_date.setTime(d);

        int tag = 0;
        tag = cal_date.get(Calendar.DATE);

        String tagstr = String.valueOf(tag);

        return tagstr;
    }

    /**
     * This method parses the @param String tempdat, according to a specified format style
     * determined by the @param Locale locale to a Date. It sets a Gregorian Calender
     * with this date and gets the value of the month.
     * Needed for StringDate Constructor
     *
     * @param tempdat - a date string
     * @param locale  - the locale to use
     */
    public static String getMonth(String tempdat, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        Date d = df.parse(tempdat, new ParsePosition(0));
        Calendar cal_date = new GregorianCalendar();
        cal_date.setTime(d);

        int monat = 0;
        monat = cal_date.get(Calendar.MONTH) + 1;

        String monatstr = String.valueOf(monat);

        return monatstr;
    }

    /**
     * This method receives the @params Date date,int weeks,Locale locale
     * It sets a Gregorian Calendar with the given Date, with the given locale
     * a format style is determined. By using the @param weeks that specifies the amount
     * of weeks that past, the corresponding week of year is calculated. Based on this value
     * the method getStartDateOfWeek is invoked.
     * getStartDateOfWeek is responsible for calculating the date of the first day of a
     * week of year.
     *
     * @param date
     * @param weeks
     * @param locale
     */
    public static String getStartDate(Date date, int weeks, Locale locale) {
        Calendar cal_date = new GregorianCalendar();
        cal_date.setTime(date);

        //format style
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);

        //get current week
        int startWeek = getWeek(weeks);
        cal_date.set(Calendar.WEEK_OF_YEAR, startWeek);

        String strstart = df.format(cal_date.getTime());
        strstart = getStartDateOfWeek(cal_date.getTime(), locale);

        return strstart;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public static int getTime() {
        Calendar cal = new GregorianCalendar();
        Date d = cal.getTime();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        return hour;
    }

    /**
     * This method subtracts the @param int duration that represents the weeks that past
     * from the current week, and obtains the corresponding week of year.
     *
     * @param duration
     */
    public static int getWeek(int duration) {
        Calendar cal_date = new GregorianCalendar();
        DateFormat df = DateFormat.getInstance();
        int kalwoch = 0;
        cal_date.add(Calendar.WEEK_OF_YEAR, -duration);
        kalwoch = cal_date.get(Calendar.WEEK_OF_YEAR);

        return kalwoch;
    }

    /**
     * This method parses the @param String tempdat, according to a specified format style
     * determined by the @param Locale locale to a Date. It sets a Gregorian Calender
     * with this date and gets the value of the year.
     * Needed for StringDate Constructor
     *
     * @param tempdat - a date string
     * @param locale  - the locale to use
     */
    public static String getYear(String tempdat, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        Date d = df.parse(tempdat, new ParsePosition(0));
        Calendar cal_date = new GregorianCalendar();
        cal_date.setTime(d);

        int jahr = 0;
        jahr = cal_date.get(Calendar.YEAR);

        String jahrstr = String.valueOf(jahr);

        return jahrstr;
    }

    /**
     * This method formats a Date to a String
     *
     * @param d
     * @param locale
     */
    public static String format(Date d, Locale locale) {
        return format(d, locale, DateFormat.FULL);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param d      __UNDOCUMENTED__
     * @param locale __UNDOCUMENTED__
     * @param style  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static String format(Date d, Locale locale, int style) {
        //macht ein Datumsformat
        DateFormat df = DateFormat.getDateInstance(style, locale);

        //formats a Date to a string Corresponding to the given Locale
        String myDate = df.format(d);

        return myDate;
    }

    /**
     * this method parses the @params String day,month,year to ints, builds a Gregorian
     * Calendar with these values to get the corresponding Date that is returned as
     * String by invoking the method format(Date d,Locale locale), that used to parse a
     * Date to a String.
     *
     * @param day
     * @param month
     * @param year
     * @param locale
     */
    public static String format(String day, String month, String year, Locale locale) {
        //parseInt(day,month,year)
        int tag = Integer.parseInt(day);
        int monat = Integer.parseInt(month);
        int jahr = Integer.parseInt(year);

        //buildGregorianCalendar for representation of a Date with jahr,tag,monat-1
        Calendar cal = new GregorianCalendar(jahr, monat - 1, tag);
        Date d = cal.getTime();

        return format(d, locale);
    }

    /**
     * This method parses a @param String dateString to a Date
     *
     * @param dateString
     * @param locale
     */
    public static Date toDate(String dateString, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        Date d = df.parse(dateString, new ParsePosition(0));

        return d;
    }

    /**
     * This method sets a Gregorian Calendar according to the given Date
     *
     * @param selectedDate - it get the day of week, based on this the first
     *                     day of the corresponding week is calculated.
     *                     By using the method format(Date d,Locale locale) a String with the default format
     *                     style is returned.
     * @param locale
     */
    private static String getStartDateOfWeek(Date selectedDate, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
        Calendar cal_date = new GregorianCalendar();
        cal_date.setTime(selectedDate);

        int strDay = cal_date.get(Calendar.DAY_OF_WEEK);

        cal_date.add(Calendar.DATE, -strDay + 1);

        return format(cal_date.getTime(), Locale.getDefault());
    }
}


/*
   $Log: DateUtil.java,v $
   Revision 1.6  2004/08/15 14:14:08  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.5  2003/11/07 00:19:22  joernt
   optimized imports

   Revision 1.4  2003/10/02 15:15:50  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.3  2003/10/01 23:45:00  joernt
   fixed javadoc issues
   Revision 1.2  2003/07/31 02:19:25  joernt
   optimized imports
   Revision 1.1.1.1  2003/05/23 14:54:06  unl
   no message
   Revision 1.5  2002/01/11 19:53:39  eyestep
   used long format
   Revision 1.4  2001/10/30 14:19:06  gregor
   log4jing
   Revision 1.3  2001/09/20 18:20:17  gregor
   added some more helper methods
   Revision 1.2  2001/09/04 11:57:03  gregor
   author && license
 */
