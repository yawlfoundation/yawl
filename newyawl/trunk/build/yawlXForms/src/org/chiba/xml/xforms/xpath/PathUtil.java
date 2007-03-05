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

import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.Model;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is a utility class which contains some static methods which
 * deal with the manipulation of canonical xpathes (as defined by XForms).
 */
public class PathUtil {
    private static final String INSTANCE_FUNCTION_NAME = "instance(";

    /**
     * checks, if xpath uses a xforms:instance() function to select the target instance.
     *
     * @param xpath the xpath to be checked
     * @return true, if the xpath uses the xforms:instance() function
     */
    public static boolean hasInstanceFunction(String xpath) {
        if (xpath.indexOf(INSTANCE_FUNCTION_NAME) != -1) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Test if absoloute reference or not
     * @author mark dimon
     */
    public static boolean isAbsoluteReference(String ref) {
        if ((ref != null) && !ref.equals("") && (ref.charAt(0) == '/')) {
            return true;
        }

        return false;
    }

    /**
     * returns only the attribute part of a xpath without the '@' sign
     *
     * @param path the xpath to check - must be a locationPath expression in Abbreviated syntax
     * @return the attribute name without the '@' sign
     */
    public static String getAttribute(String path) {
        if (path.indexOf('@') != -1) {
            return path.substring(path.indexOf('@') + 1);
        }

        return null;
    }

    /**
     * returns true if a reference (probably a complex, multistep one)
     * contains an attribute reference (i.e. one which begins with '@')
     */
    public static boolean isAttributeRef(String ref) {
        return (((ref != null) && (ref.length() > 1))
                ? (ref.indexOf('@') != -1)
                : false);
    }

    /**
     * returns true if a step is an attribute reference (i.e. begins with an '@')
     */
    public static boolean isAttributeStep(String ref) {
        return (((ref != null) && (ref.length() > 1))
                ? (ref.charAt(0) == '@')
                : false);
    }

    /**
     * returns the first step of a canonical xpath
     */
    public static String getFirstStep(String ref) {
        return (String) getSteps(ref).firstElement();
    }

    /**
     * returns true, if the xpath contains a positional
     * predicate e.g. '[1]'
     *
     * @param ref - the xpath to test
     * @return - returns true, if the xpath contains a positional predicate
     */
    public static boolean isIndexedStep(String ref) {
        if (ref != null) {
            return (ref.indexOf('[') != -1);
        }

        return false;
    }

    /**
     * returns true in case a xpath step expression contains a namespace prefix.
     *
     * @param step - a xpath step expression to test
     * @return - returns true in case a xpath step expression contains a namespace prefix.
     */
    public static boolean isNamespaced(String step) {
        if (step.indexOf(":") == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * tokenizes a binding-expression into XPath steps.
     *
     * @return - returns an array where each entry represents one step
     *         in the binding-expression, reading from left to right.
     */
    public static Vector getSteps(String bindingExpr) {
        Vector steps = new Vector(10);
        StringTokenizer st = new StringTokenizer(bindingExpr, "/");

        while (st.hasMoreTokens()) {
            steps.add(st.nextToken());
        }

        return steps;
    }

    /**
     * return the path to an instance element or attribute
     * eg,  /data/mylist[2]/mydetails[1]/myname
     * eg,  /data/mylist[2]/mydetails[1]/myname/@myattr
     */
    public static String calcIndexedPathTo(Node instance, Node ui) {
        Node instanceParent;
        Node uiParent;
        String path = "";

        // handel attr
        if (instance.getNodeType() == Node.ATTRIBUTE_NODE) {
            path = "/@" + instance.getNodeName();
            instance = ((Attr) instance).getOwnerElement();
        }

        if (ui.getNodeType() == Node.ATTRIBUTE_NODE) {
            ui = ((Attr) ui).getOwnerElement();
        }

        instanceParent = instance.getParentNode();
        uiParent = ui.getParentNode();

        while (instanceParent != null) {
            if (instanceParent.getNodeType() == Node.DOCUMENT_NODE) {
                //Elemene = ((Doument)parent).getDocumentElement();
                String name = instance.getNodeName();
                path = "/" + name + path;
            } else {
                String name = instance.getNodeName();
                NodeList siblings = ((Element) instanceParent).getElementsByTagName(name);

                if (ui.getNodeName().equals("xforms:repeat")) {
                    int my_pos = DOMUtil.getCurrentListPosition(instance, siblings);
                    path = "/" + name + "[" + my_pos + "]" + path;
                } else {
                    path = "/" + name + path;
                }
            }

            instance = instanceParent;
            instanceParent = instance.getParentNode();
            ui = uiParent;
            uiParent = ui.getParentNode();
        }

        return path;
    }

    /**
     * returns the last element of a binding expression.  If the last
     * element is a 'normal' step element, this method returns the same
     * result as lastStep(String), but if the last element is an attribute
     * reference (beginning with '@') then the last two elements are
     * returned, e.g. for a/b/c/d the last element is 'd', but for a/b/c/@d
     * it is 'c/@d'.
     *
     * @param ref the reference to check.  If null the return value is null.
     */
    public static String lastElement(String ref) {
        if (ref != null) {
            int pos = ref.lastIndexOf('@');

            if (pos == -1) {
                return lastStep(ref);
            } else if (pos > 1) {
                int lspos = ref.lastIndexOf('/', pos - 2);

                if (lspos >= 0) {
                    return ref.substring(lspos + 1);
                }
            }

            return ref;
        }

        return null;
    }

    /**
     * returns the last step of a binding Expression.  if nodeset has only one
     * step, the return value is identical to the input
     *
     * @param ref a slash seperated binding expression
     * @return the last step (without trailing /)
     */
    public static String lastStep(String ref) {
        int pos = ref.lastIndexOf("/");

        return ((pos != -1)
                ? ref.substring(pos + 1)
                : ref);
    }

    /**
     * returns the local part of a stepname, that is without predicat
     */
    public static String localStepName(String step) {
        int pos = step.indexOf("[");

        if (pos != -1) {
            return step.substring(0, pos);
        }

        return step;
    }

    /**
     * returns the path to a instance node (i.e. the part of a binding
     * expression before the last element ('/')).  If no path exists
     * (because the binding expression contains only one step or begins
     * with a '/') the returnvalue is null
     */
    public static String pathTo(String ref) {
        if (ref != null) {
            int pos = ref.lastIndexOf("/");

            if ((pos == -1) || (pos == 0)) {
                return null;
            }

            return ref.substring(0, pos);
        }

        return null;
    }

    /**
     * @return - strips the positional predicate from an indexed step
     */
    public static String stepBasePart(String step) {
        if (step != null) {
            int pos = step.indexOf('[');

            if (pos != -1) {
                return step.substring(0, pos);
            } else {
                return step;
            }
        }

        return null;
    }

    /**
     * strips the last predicate from an xpath locationpath expression
     *
     * @param locationPath a xpath locationpath expression
     * @return the locationPath with stripped last predicate
     */
    public static String stripLastPredicate(String locationPath) {
        if (locationPath != null) {
            int pos = locationPath.lastIndexOf('[');

            if (pos != -1) {
                return locationPath.substring(0, pos);
            } else {
                return locationPath;
            }
        }
        return null;
    }

    /**
     * @return - returns the index from an positional predicate as int
     */
    public static int stepIndex(String ref) {
        if (ref != null) {
            int pos = ref.indexOf('[');

            if (pos != -1) {
                String stepIdx = ref.substring(pos + 1, ref.length() - 1);

                return Integer.parseInt(stepIdx);
            } else {
                return 1;
            }
        }

        return 0;
    }

    /**
     * strips the first part of a canonical xpath and
     * returns the rest of the path e.g.<br>
     * '/address/street' as input will result in 'street'
     * as output.
     * <p/>
     * In case, aRef does not contain steps, this method
     * returns null.
     *
     * @param aRef - a canonical xpath expression e.g. '/parent/child'
     * @return - returns the last step of a complex xpath
     *         or null in case the passed nodeset does not contain steps.
     */
    public static String stripFirstStep(String aRef) {
        String ref = aRef;

        if (!(ref.equals("") && (ref != null))) {
            if (isAbsoluteReference(ref)) {
                ref = ref.substring(1);
            }

            int pos = ref.indexOf("/");

            if (pos != -1) {
                return (ref.substring(pos + 1));
            }
        }

        return null;
    }

    /**
     * returns the instance id for given locationPath.
     * <p/>
     * This code is duplicated in parts from BoundElement!
     *
     * @param path the locationPath to investigate
     * @return the index of the instance or if not specified in locatonPath the id of the default Instance
     */
    public static String getInstanceId(Model model, String path) {
        if (hasInstanceFunction(path)) {
            // get instance id from location path
            int pos = path.indexOf(INSTANCE_FUNCTION_NAME);
            int len = INSTANCE_FUNCTION_NAME.length();
            String result = path.substring(pos + len + 1, path.indexOf("')"));
            return result;
        } else {
            // lookup default instance
            Instance defaultInstance = model.getDefaultInstance();
            if (defaultInstance == null) {
                // set instance id empty
                return "";
            } else {
                // get instance id from default instance
                return defaultInstance.getId();
            }
        }
    }

    public static String stripInstanceFunction(String path) {
        String result;
        if (hasInstanceFunction(path)) {
            //simple solution cause instance function always stands at the start of a path
            result = path.substring(path.indexOf("')") + ("')").length());
        } else {
            result = path;
        }
        return result;
    }

    /**
     * Removes any predicates from the specified path.
     *
     * @param xpath an arbitrary xpath.
     * @return the xpath without any predicates.
     */
    public static String removePredicates(String xpath) {
        StringBuffer buffer = new StringBuffer(xpath);
        int opening = buffer.indexOf("[");
        int closing;

        while (opening > -1) {
            closing = buffer.indexOf("]", opening);
            buffer.replace(opening, closing + 1, "");
            opening = buffer.indexOf("[", opening);
        }

        return buffer.toString();
    }

}

//end of class

