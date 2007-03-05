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
import org.apache.log4j.Category;
import org.chiba.xml.xforms.Container;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.connector.ModelItemCalculator;
import org.chiba.xml.xforms.connector.ModelItemValidator;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides chiba extension functions. These functions are implemented and
 * tested for use with JXPath.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @author Terence Jacyno
 * @version $Id: ChibaExtensionFunctions.java,v 1.16 2004/11/18 16:07:49 unl Exp $
 */
public class ChibaExtensionFunctions {
    private static Category LOGGER = Category.getInstance(ChibaExtensionFunctions.class);

    /**
     * Prevents outside instantiation
     */
    private ChibaExtensionFunctions() {
        // NOP
    }

    /**
     * Returns the calculation result of an external calculator.
     *
     * @param expressionContext the expression context.
     * @param uri the calculator uri.
     * @return the calculation result of an external calculator.
     * @throws XFormsException if any error occurred during the calculation.
     * @deprecated use custom extension functions instead
     */
    public static String calculate(ExpressionContext expressionContext, String uri) throws XFormsException {
        JXPathContext context = expressionContext.getJXPathContext();

        while (context != null) {
            Object contextBean = context.getContextBean();

            if (contextBean instanceof XFormsElement) {
                // get hook from jxpath to chiba
                XFormsElement xFormsElement = (XFormsElement) contextBean;
                Container container = xFormsElement.getModel().getContainer();
                Element contextElement = xFormsElement.getElement();
                Node instanceNode = (Node) expressionContext.getContextNodePointer().getNode();

                ModelItemCalculator calculator = container.getConnectorFactory().createModelItemCalculator(uri, contextElement);
                return calculator.calculate(instanceNode);
            }

            context = context.getParentContext();
        }

        throw new XFormsException("invalid expression context when evaluating calculate('" + uri + "')");
    }

    /**
     * Returns the validation result of an external validator.
     *
     * @param expressionContext the expression context.
     * @param uri the calculator uri.
     * @return the validation result of an external validator.
     * @throws XFormsException if any error occurred during the validation.
     * @deprecated use custom extension functions instead
     */
    public static boolean validate(ExpressionContext expressionContext, String uri) throws XFormsException {
        JXPathContext context = expressionContext.getJXPathContext();

        while (context != null) {
            Object contextBean = context.getContextBean();

            if (contextBean instanceof XFormsElement) {
                // get hook from jxpath to chiba
                XFormsElement xFormsElement = (XFormsElement) contextBean;
                Container container = xFormsElement.getModel().getContainer();
                Element contextElement = xFormsElement.getElement();
                Node instanceNode = (Node) expressionContext.getContextNodePointer().getNode();

                ModelItemValidator validator = container.getConnectorFactory().createModelItemValidator(uri, contextElement);
                return validator.validate(instanceNode);
            }

            context = context.getParentContext();
        }

        throw new XFormsException("invalid expression context when evaluating validate('" + uri + "')");
    }


    private static Map m_regexPatterns = new HashMap();

    /**
     * <code>Regexp</code> is a utility class providing the functionality
     * present within the EXSLT Regular Expressions definition (<a
     * href="http://www.exslt.org/regexp" target="_top">http://www.exslt.org/regexp</a>).
     * <br><br> This is a contribution from Terence Jacyno.
     */
    public static boolean match(String input, String regex, String flags) {

        String regexKey = ((flags == null) || (flags.indexOf('i') == -1))
                ? "s " + regex : "i " + regex;

        Pattern pattern = (Pattern) m_regexPatterns.get(regexKey);

        if (pattern == null) {
            pattern = (regexKey.charAt(0) == 'i')
                    ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(regex);
            m_regexPatterns.put(regexKey, pattern);
        }

        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * custom extension function to get the size of a local file.
     *
     * @param expressionContext
     * @param nodeset nodeset must contain a single node that has a filename or
     * path as value. The value will be resolved against the baseURI of the
     * processor to find the file.
     * @return the size of the file as String
     *         <p/>
     *         todo: revisit code structure - fileSize and fileDate functions
     *         only differ in one line of code
     */
    public static String fileSize(ExpressionContext expressionContext, List nodeset) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return "Error: Nodeset does not exist";
        }
        JXPathContext rootContext = expressionContext.getJXPathContext();

        while (rootContext != null) {
            Object rootNode = rootContext.getContextBean();

            if (rootNode instanceof Instance) {
                //get the Context
                Instance instance = (Instance) rootNode;
                String baseUri = instance.getModel().getContainer().getProcessor().getBaseURI();
                String path;
                try {
//                    uri = new URI(baseUri).getPath();
                    path = new URI(baseUri).getPath().substring(1);
                }
                catch (URISyntaxException e) {
                    return "Error: base URI not valid: " + baseUri;
                }

                File file = new File(path, (String) nodeset.get(0));
                if (!file.exists() || file.isDirectory()) {
                    LOGGER.info("File " + file.toString() + " does not exist or is directory");
                    return "";
                }

                return "" + file.length();
            }
            rootContext = rootContext.getParentContext();
        }
        return "";
    }

    /**
     * custom extension function to get the lastModified Date of a local file.
     *
     * @param expressionContext
     * @param nodeset must contain a single node that has a filename or path as
     * value. The value will be resolved against the baseURI of the processor to
     * find the file.
     * @param format a format pattern conformant with to
     * java.text.SimpleDateFormat. If an empty string is passed the format
     * defaults to "dd.MM.yyyy H:m:s".
     * @return the formatted lastModified Date of the file
     * @see java.text.SimpleDateFormat
     */
    public static String fileDate(ExpressionContext expressionContext, List nodeset, String format) {
        if ((nodeset == null) || (nodeset.size() == 0)) {
            return "Error: Nodeset does not exist";
        }
        JXPathContext rootContext = expressionContext.getJXPathContext();

        while (rootContext != null) {
            Object rootNode = rootContext.getContextBean();

            if (rootNode instanceof Instance) {
//                //get the Context
//                Instance instance = (Instance) rootNode;
//                String baseUri = instance.getModel().getContainer().getProcessor().getBaseURI();
//
//                File file = new File(baseUri,(String) nodeset.get(0));
//                if(!file.exists()){
//                    LOGGER.info("File " + file.toString() + " does not exist");
//                    return "";
//                }
                //get the Context
                Instance instance = (Instance) rootNode;
                String baseUri = instance.getModel().getContainer().getProcessor().getBaseURI();
                String path;
                try {
//                    uri = new URI(baseUri).getPath();
                    path = new URI(baseUri).getPath().substring(1);
                }
                catch (URISyntaxException e) {
                    return "Error: base URI not valid: " + baseUri;
                }

                File file = new File(path, (String) nodeset.get(0));
                if (!file.exists() || file.isDirectory()) {
                    LOGGER.info("File " + file.toString() + " does not exist or is directory");
                    return "";
                }

                return formatDateString(file, format);
            }
            rootContext = rootContext.getParentContext();
        }
        return "Error: Calculation failed";
    }


    private static String formatDateString(File file, String format) {
        long modified = file.lastModified();
        Calendar calendar = new GregorianCalendar(Locale.getDefault());
        calendar.setTimeInMillis(modified);
        SimpleDateFormat simple = null;
        String result;
        if (format.equals("")) {
            //default format
            simple = new SimpleDateFormat("dd.MM.yyyy H:m:s");
        }
        else {
            //custom format
            try {
                simple = new SimpleDateFormat(format);
            }
            catch (IllegalArgumentException e) {
//                result = "Error: illegal Date format string";
                //todo: do something better
            }
        }
        result = simple.format(calendar.getTime());
        return result;
    }


}

// end of class


