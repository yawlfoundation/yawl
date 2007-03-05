/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.xpath;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.chiba.xml.xforms.BindingResolver;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Provides the XForms Core Function Library
 * <a href="http://www.w3.org/TR/2002/CR-xforms-20021112#expr-lib">[7.5]</a>.
 *
 * @author <a href="mailto:flaviocosta@users.sourceforge.net">Flavio Costa</a>
 * @author <a href="mailto:mark@markdimon.com">Mark Dimon</a>
 * @author <a href="mailto:joernt@users.sourceforge.net">Joern Turner</a>
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id: XFormsExtensionFunctions.java,v 1.1 2004/11/13 00:20:24 joernt Exp $
 */
public class XFormsExtensionFunctions {
    private static Category LOGGER = Category.getInstance(XFormsExtensionFunctions.class);

    /**
     * Used by date/time functions to format
     * dates according to XML Schema format.
     */
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Used by date/time functions to format date-times
     * according to XML Schema (ISO-8601) UTC format.
     */
    private static final DateFormat dateTimeFormatZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Used by date/time functions to format date-times
     * according to General time zone (z in SimpleDateFormat).
     */
    private static final DateFormat dateTimeFormatGtz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

    /**
     * Make sure class cannot be instantiated.
     */
    private XFormsExtensionFunctions() {
    }

    // Make sure class cannot be instantiated

    /**
     * The if() Function [7.6.2].
     * <p/>
     * Function if evaluates the first parameter as boolean, returning
     * the second parameter when true, otherwise the third parameter.
     *
     * @param bool the boolean parameter.
     * @param s1   the string to return when bool evaluates to true.
     * @param s2   the string to return when bool evaluates to false.
     * @return s1 when bool evaluates to true, otherwise s2.
     */
    public static String IF(boolean bool, String s1, String s2) {
        return bool
                ? s1
                : s2;
    }

    // number functions

    /**
     * The avg() Function [7.7.1].
     * <p/>
     * Function avg returns the arithmetic average of the result of
     * converting the string-values of each node in the argument node-set
     * to a number. The sum is computed with sum(), and divided with div
     * by the value computed with count().
     *
     * @param context the expression context.
     * @param nodeset the node-set.
     * @return the computed node-set average.
     */
    public static double avg(ExpressionContext context, List nodeset) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return Double.NaN;
        }

        JXPathContext rootContext = context.getJXPathContext();
        rootContext.getVariables().declareVariable("nodeset", nodeset);

        Double value = (Double) rootContext.getValue("sum($nodeset) div count($nodeset)", Double.class);

        return value.doubleValue();
    }

    // boolean functions

    /**
     * The boolean-from-string() Function [7.6.1].
     * <p/>
     * Function boolean-from-string returns true if the required parameter
     * string is "true" or "1", or false if parameter string is "false", or
     * "0". This is useful when referencing a Schema xsd:boolean datatype in
     * an XPath expression. If the parameter string matches none of the above
     * strings, according to a case-insensitive comparison, processing stops
     * with an exception.
     *
     * @param value the string value.
     * @return the boolean value.
     * @throws XFormsException if the specified value does not match a boolean value.
     */
    public static boolean boolean_from_string(String value)
            throws XFormsException {
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }

        throw new XFormsException("invalid value expression when evaluating boolean_from_string('" + value +
                "')");
    }

    /**
     * The count-non-empty() Function [7.7.4].
     * <p/>
     * Function count-non-empty returns the number of non-empty nodes
     * in argument node-set. A node is considered non-empty if it is
     * convertible into a string with a greater-than zero length.
     *
     * @param nodeset the node-set.
     * @return the count of non-empty nodes in the node-set.
     */
    public static int count_non_empty(List nodeset) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return 0;
        }

        int count = 0;
        Iterator iterator = nodeset.iterator();

        while (iterator.hasNext()) {
            String value = iterator.next().toString();

            if (value.length() > 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * returns true if the 'value' string ends with 'suffix".
     */
    public static boolean endsWith(String value, String suffix) {
        if ((value == null) || (suffix == null)) {
            return false;
        }

        return value.endsWith(suffix);
    }

    /**
     * The index() Function [7.7.5].
     * <p/>
     * Function index takes a string argument that is the idref of a repeat
     * and returns the current 1-based position of the repeat index for the
     * identified repeat — see 9.3.1 The repeat Element for details on repeat
     * and its associated repeat index. If the specified argument does not
     * identify a repeat, processing stops with an exception.
     *
     * @param context the expression context.
     * @param idref   the repeat id.
     * @return the specified repeat index.
     * @throws XFormsException if any error occurred during repeat index lookup.
     */
    public static int index(ExpressionContext context, String idref)
            throws XFormsException {
        JXPathContext rootContext = context.getJXPathContext();


        while (rootContext != null) {
            Object rootNode = rootContext.getContextBean();

            if (rootNode instanceof XFormsElement) {
                XFormsElement element = (XFormsElement) rootNode;
                Repeat repeat = (Repeat) element.getModel().getContainer().lookup(idref);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("index for Element: " + element.getId() + " evaluated to " + repeat.getIndex());
                }
                return repeat.getIndex();
            }

            rootContext = rootContext.getParentContext();
        }

        throw new XFormsException("invalid expression context when evaluating index('" + idref + "')");
    }

    // node-set functions

    /**
     * The instance() Function [7.10.1].
     * <p/>
     * An XForms Model can contain more that one instance. This function allows
     * access to instance data, within the same XForms Model, but outside the
     * instance data containing the context node.
     * <p/>
     * The argument is converted to a string as if by a call to the string function.
     * This string is treated as an IDREF, which is matched against instance elements
     * in the containing document. If a match is located, and the matching instance
     * data is associated with the same XForms Model as the current context node,
     * this function returns a node-set containing just the root element node (also
     * called the document element node) of the referenced instance data. In all
     * other cases, an empty node-set is returned.
     *
     * @param context the expression context.
     * @param idref   the instance id.
     * @return the specified instance.
     * @throws XFormsException if any error occurred during instance lookup.
     */
    public static Object instance(ExpressionContext context, String idref) throws XFormsException {
        JXPathContext rootContext = context.getJXPathContext();

        while (rootContext != null) {
            Object rootNode = rootContext.getContextBean();

//does not work cause rootnode is no XFormsElement
            if (rootNode instanceof XFormsElement) {
                Object instance = ((XFormsElement) rootNode).getModel().getContainer().lookup(idref);

                if (instance != null && instance instanceof Instance) {
                    Pointer pointer = ((Instance) instance).getPointer(BindingResolver.OUTERMOST_CONTEXT);
                    return pointer;
                }
            }

            rootContext = rootContext.getParentContext();
        }

        throw new XFormsException("invalid expression context when evaluating instance('" + idref + "')");
    }

    /**
     * The max() Function [7.7.3].
     * <p/>
     * Function max returns the maximum value of the result of converting
     * the string-values of each node in argument node-set to a number.
     * "Maximum" is determined with the &gt; operator. If the parameter is
     * an empty node-set, the return value is NaN.
     *
     * @param context the expression context.
     * @param nodeset the node-set.
     * @return the computed node-set maximum.
     */
    public static double max(ExpressionContext context, List nodeset) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return Double.NaN;
        }

        JXPathContext rootContext = context.getJXPathContext();
        Object max = nodeset.get(0);

        for (int index = 1; index < nodeset.size(); index++) {
            Object current = nodeset.get(index);
            rootContext.getVariables().declareVariable("max", max);
            rootContext.getVariables().declareVariable("current", current);

            boolean more = ((Boolean) rootContext.getValue("number($current) > number($max)", Boolean.class)).booleanValue();

            if (more) {
                max = current;
            }
        }

        return (new Double(max.toString())).doubleValue();
    }

    /**
     * The min() Function [7.7.2].
     * <p/>
     * Function min returns the minimum value of the result of converting
     * the string-values of each node in argument node-set to a number.
     * "Minimum" is determined with the &lt; operator. If the parameter is
     * an empty node-set, the return value is NaN.
     *
     * @param context the expression context.
     * @param nodeset the node-set.
     * @return the computed node-set minimum.
     */
    public static double min(ExpressionContext context, List nodeset) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return Double.NaN;
        }

        JXPathContext rootContext = context.getJXPathContext();
        Object min = nodeset.get(0);

        for (int index = 1; index < nodeset.size(); index++) {
            Object current = nodeset.get(index);
            rootContext.getVariables().declareVariable("min", min);
            rootContext.getVariables().declareVariable("current", current);

            boolean less = ((Boolean) rootContext.getValue("number($current) < number($min)", Boolean.class)).booleanValue();

            if (less) {
                min = current;
            }
        }

        return (new Double(min.toString())).doubleValue();
    }

    // date and time functions

    /**
     * The now() Function [7.9.1].
     * <p/>
     * The now function returns the current system date and time as a string
     * value in the canonical XML Schema xsd:dateTime format. If time zone
     * information is available, it is included (normalized to UTC). If no
     * time zone information is available, an implementation default is used.
     *
     * @return the current system date in Java formatting.
     */
    public static String now() {
        return dateTimeFormatZ.format(new Date());
    }

    /**
     * The days-from-date() Function [7.10.2].
     * <p/>
     * This function returns a whole number of days, according to the
     * following rules:
     * <p/>
     * If the string parameter represents a legal lexical xsd:date or
     * xsd:dateTime, the return value is equal to the number of days
     * difference between the specified date and 1970-01-01. Hour, minute,
     * and second components are ignored. Any other input parameter causes
     * a return value of NaN.
     *
     * @param date String representation of the date.
     * @return The whole number of days (the decimal part is always 0).
     */
    public static double days_from_date(String date) {

        try {
            Date providedDate = dateFormat.parse(date);
            double diff = providedDate.getTime() / (1000 * 60 * 60 * 24);
            return diff > 0 ? Math.ceil(diff) : Math.floor(diff);
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    /**
     * The seconds-from-dateTime() Function [7.10.3]
     * <p/>
     * This function returns a possibly fractional number of
     * seconds, according to the following rules:
     * <p/>
     * If the string parameter represents a legal lexical xsd:dateTime,
     * the return value is equal to the number of seconds difference
     * between the specified dateTime and 1970-01-01T00:00:00Z. If
     * no time zone is specified, an implementation default is used.
     * Any other input string parameter causes a return value of NaN.
     *
     * @param dateTime String representation of the date-time.
     * @return The number of seconds as descripted.
     */
    public static double seconds_from_dateTime(String dateTime) {

        try {
            return ISO_8601toDate(dateTime).getTime() / 1000;
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    /**
     * Converts a ISO-8601 string representation of a date
     * to a Date object, taking care of the differences
     * between this format and the one used by
     * SimpleDateFormat.
     *
     * @param dateTime Date in ISO-8601 format.
     * @return Date object parsed from the string.
     */
    private static Date ISO_8601toDate(String dateTime) throws ParseException {

        //already in UTC
        if (dateTime.endsWith("Z")) {
            return dateTimeFormatZ.parse(dateTime);
        }

        int positionT = dateTime.indexOf('T');

        int positionZ = dateTime.indexOf('+', positionT);
        if (positionZ == -1) {
            positionZ = dateTime.indexOf('-', positionT);
        }

        //no timezone specified, defaults to UTC
        if (positionZ == -1) {
            return dateTimeFormatZ.parse(dateTime + 'Z');
        }

        //convert to General time zone to preserve the timezone provided
        return dateTimeFormatGtz.parse(dateTime.substring(0, positionZ)
                + "GMT"
                + dateTime.substring(positionZ));
    }

    // string functions

    /**
     * The property() Function [7.8.1].
     * <p/>
     * Function property returns the XForms property named by the string parameter.
     * <p/>
     * The following properties are available for reading (but not modification).
     * <p/>
     * <b>version</b> version is defined as the string "1.0" for XForms 1.0
     * <p/>
     * <b>conformance-level</b> conformance-level strings are defined in 12 Conformance.
     *
     * @param name the property name.
     * @return the property value.
     */
    public static String property(String name) {
        if ("version".equals(name)) {
            return "1.0";
        }

        if ("conformance-level".equals(name)) {
            // todo: read from config
            return "basic";
        }

        return null;
    }
}

//end of class

