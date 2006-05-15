/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a
 *    Package may be copied, such that the Copyright Holder maintains some
 *    semblance of artistic control over the development of the package,
 *    while giving the users of the package the right to use and distribute
 *    the Package in a more-or-less customary fashion, plus the right to make
 *    reasonable modifications.
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
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of
 *    media cost, duplication charges, time of people involved, and so
 *    on. (You will not be required to justify it to the Copyright Holder,
 *    but only to the computing community at large as a market that must bear
 *    the fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself,
 *    though there may be fees involved in handling the item. It also means
 *    that recipients of the item may redistribute it under the same
 *    conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the
 *    Standard Version of this Package without restriction, provided that you
 *    duplicate all of the original copyright notices and associated
 *    disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications
 *    derived from the Public Domain or from the Copyright Holder. A Package
 *    modified in such a way shall still be considered the Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way,
 *    provided that you insert a prominent notice in each changed file
 *    stating how and when you changed that file, and provided that you do at
 *    least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make
 *        them Freely Available, such as by posting said modifications to
 *        Usenet or an equivalent medium, or placing the modifications on a
 *        major archive site such as ftp.uu.net, or by allowing the Copyright
 *        Holder to include your modifications in the Standard Version of the
 *        Package.
 *
 *        b) use the modified Package only within your corporation or
 *        organization.
 *
 *        c) rename any non-standard executables so the names do not conflict
 *        with standard executables, which must also be provided, and provide
 *        a separate manual page for each non-standard executable that
 *        clearly documents how it differs from the Standard Version.
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
 *        c) accompany any non-standard executables with their corresponding
 *        Standard Version executables, giving the non-standard executables
 *        non-standard names, and clearly documenting the differences in
 *        manual pages (or equivalent), together with instructions on where
 *        to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this
 *    Package. You may charge any fee you choose for support of this
 *    Package. You may not charge a fee for this Package itself.  However,
 *    you may distribute this Package in aggregate with other (possibly
 *    commercial) programs as part of a larger (possibly commercial) software
 *    distribution provided that you do not advertise this Package as a
 *    product of your own.
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
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.xpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOMCompiler - compiles an
 *
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id$
 */
public class DOMCompiler implements Compiler {
    private Document document = null;

    //    private JXPathContext context = null;
    public DOMCompiler(Document document) {
        this.document = document;

        //        this.context = JXPathContext.newContext(document);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param arguments __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object and(Object[] arguments) {
        Element element = createElement("and");
        addChildren(element, "arguments", arguments);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object divide(Object left, Object right) {
        Element element = createElement("divide");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object equal(Object left, Object right) {
        Element element = createElement("equal");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param expression __UNDOCUMENTED__
     * @param predicates __UNDOCUMENTED__
     * @param steps      __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object expressionPath(Object expression, Object[] predicates, Object[] steps) {
        Element element = createElement("expressionPath");
        addChild(element, "expression", expression);
        addChildren(element, "predicates", predicates);
        addChildren(element, "steps", steps);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param code __UNDOCUMENTED__
     * @param args __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object function(int code, Object[] args) {
        Element element = createElement("function");
        addAttribute(element, "name", getFunctionName(code));
        addChildren(element, "arguments", args);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param name __UNDOCUMENTED__
     * @param args __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object function(Object name, Object[] args) {
        Element element = createElement("function");
        addChild(element, "name", name);
        addChildren(element, "arguments", args);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object greaterThan(Object left, Object right) {
        Element element = createElement("greaterThan");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object greaterThanOrEqual(Object left, Object right) {
        Element element = createElement("greaterThanOrEqual");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object lessThan(Object left, Object right) {
        Element element = createElement("lessThan");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object lessThanOrEqual(Object left, Object right) {
        Element element = createElement("lessThanOrEqual");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param value __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object literal(String value) {
        Element element = createElement("literal");
        addAttribute(element, "value", value);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param absolute __UNDOCUMENTED__
     * @param steps    __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object locationPath(boolean absolute, Object[] steps) {
        Element element = createElement("locationPath");
        addAttribute(element, "absolute", String.valueOf(absolute));
        addChildren(element, "steps", steps);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object minus(Object left, Object right) {
        Element element = createElement("minus");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param argument __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object minus(Object argument) {
        Element element = createElement("minus");
        addChild(element, "argument", argument);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object mod(Object left, Object right) {
        Element element = createElement("mod");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object multiply(Object left, Object right) {
        Element element = createElement("multiply");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param qname __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object nodeNameTest(Object qname) {
        Element element = createElement("nodeNameTest");
        addChild(element, "nodeName", qname);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param nodeType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object nodeTypeTest(int nodeType) {
        Element element = createElement("nodeTypeTest");
        addAttribute(element, "nodeType", getNodeType(nodeType));

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param left  __UNDOCUMENTED__
     * @param right __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object notEqual(Object left, Object right) {
        Element element = createElement("notEqual");
        addChild(element, "left", left);
        addChild(element, "right", right);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param value __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object number(String value) {
        Element element = createElement("number");
        addAttribute(element, "value", value);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param arguments __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object or(Object[] arguments) {
        Element element = createElement("or");
        addChildren(element, "arguments", arguments);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param instruction __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object processingInstructionTest(String instruction) {
        Element element = createElement("processingInstructionTest");
        addAttribute(element, "processingInstruction", instruction);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param prefix __UNDOCUMENTED__
     * @param name   __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object qname(String prefix, String name) {
        Element element = createElement("qName");
        addAttribute(element, "prefix", prefix);
        addAttribute(element, "name", name);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param axis       __UNDOCUMENTED__
     * @param nodeTest   __UNDOCUMENTED__
     * @param predicates __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object step(int axis, Object nodeTest, Object[] predicates) {
        Element element = createElement("step");
        addAttribute(element, "axis", getAxisName(axis));
        addChild(element, "nodeTest", nodeTest);
        addChildren(element, "predicates", predicates);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param arguments __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object sum(Object[] arguments) {
        Element element = createElement("sum");
        addChildren(element, "arguments", arguments);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param arguments __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object union(Object[] arguments) {
        Element element = createElement("union");
        addChildren(element, "arguments", arguments);

        return element;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param qName __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Object variableReference(Object qName) {
        Element element = createElement("variableReference");
        addChild(element, "qName", qName);

        return element;
    }

    private String getAxisName(int axis) {
        switch (axis) {
            case AXIS_SELF:
                return "self";

            case AXIS_CHILD:
                return "child";

            case AXIS_PARENT:
                return "parent";

            case AXIS_ANCESTOR:
                return "ancestor";

            case AXIS_ATTRIBUTE:
                return "attribute";

            case AXIS_NAMESPACE:
                return "namespace";

            case AXIS_PRECEDING:
                return "preceding";

            case AXIS_FOLLOWING:
                return "following";

            case AXIS_DESCENDANT:
                return "descendant";

            case AXIS_ANCESTOR_OR_SELF:
                return "ancestor-or-self";

            case AXIS_FOLLOWING_SIBLING:
                return "following-sibling";

            case AXIS_PRECEDING_SIBLING:
                return "preceding-sibling";

            case AXIS_DESCENDANT_OR_SELF:
                return "descendant-or-self";
        }

        return null;
    }

    private String getFunctionName(int function) {
        switch (function) {
            case FUNCTION_LAST:
                return "last";

            case FUNCTION_POSITION:
                return "position";

            case FUNCTION_COUNT:
                return "count";

            case FUNCTION_ID:
                return "id";

            case FUNCTION_LOCAL_NAME:
                return "local-name";

            case FUNCTION_NAMESPACE_URI:
                return "namespace-uri";

            case FUNCTION_NAME:
                return "name";

            case FUNCTION_STRING:
                return "string";

            case FUNCTION_CONCAT:
                return "concat";

            case FUNCTION_STARTS_WITH:
                return "starts-with";

            case FUNCTION_CONTAINS:
                return "contains";

            case FUNCTION_SUBSTRING_BEFORE:
                return "substring-before";

            case FUNCTION_SUBSTRING_AFTER:
                return "substring-after";

            case FUNCTION_SUBSTRING:
                return "substring";

            case FUNCTION_STRING_LENGTH:
                return "string-length";

            case FUNCTION_NORMALIZE_SPACE:
                return "normalize-space";

            case FUNCTION_TRANSLATE:
                return "translate";

            case FUNCTION_BOOLEAN:
                return "boolean";

            case FUNCTION_NOT:
                return "not";

            case FUNCTION_TRUE:
                return "true";

            case FUNCTION_FALSE:
                return "false";

            case FUNCTION_LANG:
                return "lang";

            case FUNCTION_NUMBER:
                return "number";

            case FUNCTION_SUM:
                return "sum";

            case FUNCTION_FLOOR:
                return "floor";

            case FUNCTION_CEILING:
                return "ceiling";

            case FUNCTION_ROUND:
                return "round";

            case FUNCTION_NULL:
                return "null";

            case FUNCTION_KEY:
                return "key";

            case FUNCTION_FORMAT_NUMBER:
                return "format-number";
        }

        return null;
    }

    private String getNodeType(int nodeType) {
        switch (nodeType) {
            case NODE_TYPE_NODE:
                return "node";

            case NODE_TYPE_TEXT:
                return "text";

            case NODE_TYPE_COMMENT:
                return "comment";

            case NODE_TYPE_PI:
                return "processing-instruction";
        }

        return null;
    }

    private void addAttribute(Element element, String name, String value) {
        element.setAttribute(name, value);
    }

    private void addChild(Element element, String name, Object child) {
        Element wrapper = createElement(name);
        wrapper.appendChild((Node) child);
        element.appendChild(wrapper);
    }

    private void addChildren(Element element, String name, Object[] children) {
        Element wrapper = createElement(name);

        for (int i = 0; i < children.length; i++) {
            wrapper.appendChild((Node) children[i]);
        }

        element.appendChild(wrapper);
    }

    private Element createElement(String name) {
        return this.document.createElement(name);
    }
}


/*
   $Log$
   Revision 1.1  2006-02-27 17:28:02  maod
   *** empty log message ***

   Revision 1.9  2004/08/15 14:14:16  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.8  2003/11/07 00:25:45  joernt
   optimized imports

   Revision 1.7  2003/10/13 01:02:22  joernt
   javadoc;
   log message deleted.

   Revision 1.6  2003/10/02 15:15:50  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.5  2003/09/04 23:07:54  joernt
   changed setup to init;
   optimized imports
   Revision 1.4  2003/08/28 22:40:39  joernt
   commented out unused code
   Revision 1.3  2003/08/05 23:25:30  unl
   - cleanup
   Revision 1.2  2003/07/31 02:09:35  joernt
   optimized imports
   Revision 1.1  2003/07/01 22:10:02  unl
   - initial implementation
 */
