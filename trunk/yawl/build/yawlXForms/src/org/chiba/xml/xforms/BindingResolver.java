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
package org.chiba.xml.xforms;

import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The binding resolver implements the scoped resolution of model binding
 * expressions as well as ui binding expressions.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: BindingResolver.java,v 1.27 2004/12/13 00:20:53 unl Exp $
 */
public class BindingResolver implements XFormsConstants {
    /**
     * xpath for outermost context
     */
    public static final String OUTERMOST_CONTEXT = "/*[1]";

    /**
     * Creates a new binding resolver.
     */
    public BindingResolver() {
    }

    /**
     * Checks wether the specified path expression is an absolute path.
     *
     * @param expression the path expression.
     * @return <code>true</code> if specified path expression is an absolute
     * path, otherwise <code>false</code>.
     */
    private static boolean isAbsolutePath(String expression) {
        return expression.startsWith("/") || expression.indexOf("instance(") != -1;
    }

    /**
     * Checks wether the specified path expression is a dot reference.
     *
     * @param expression the path expression.
     * @return <code>true</code> if specified path expression is a dot
     * reference, otherwise <code>false</code>.
     */
    private static boolean isDotReference(String expression) {
        return expression.equals(".");
    }

    /**
     * Returns the enclosing binding element of the specified xforms element.
     *
     * @param xFormsElement the xforms element.
     * @return the enclosing binding element of the specified xforms element or
     * <code>null</code> if there is no enclosing binding element.
     */
    public static Binding getEnclosingBinding(XFormsElement xFormsElement) {
        Binding enclosingBinding = null;
        Container container = xFormsElement.getContainerObject();
        Node currentNode = xFormsElement.getElement();
        String modelId = xFormsElement.getModel().getId();

        while (enclosingBinding == null) {
            Node parentNode = currentNode.getParentNode();

            if (parentNode == null) {
                break;
            }

            if (!(parentNode instanceof ElementImpl)) {
                break;
            }

            ElementImpl elementImpl = (ElementImpl) parentNode;
            Object o = elementImpl.getUserData();

            if (BindingResolver.hasModelBinding(elementImpl)) {
                Binding binding = (Binding) o;

                if (binding.getModelId().equals(modelId)) {
                    String bindId = binding.getBindingId();
                    enclosingBinding = (Binding) container.lookup(bindId);
                    break;
                }
            }

            if (BindingResolver.hasUIBinding(elementImpl)) {
                Binding binding = (Binding) o;

                if (binding.getModelId().equals(modelId)) {
                    enclosingBinding = binding;
                    break;
                }
            }

            currentNode = parentNode;
        }

        return enclosingBinding;
    }

    /**
     * Returns the fully resolved expression path of the specified xforms
     * element.
     *
     * @param xFormsElement the xforms element.
     * @param repeatEntryId the id of the repeat entry which contains the xforms
     * element (optional).
     * @return the fully resolved expression path of the specified xforms
     * element.
     */
    public static String getExpressionPath(XFormsElement xFormsElement, String repeatEntryId) {
        String expressionPath;
        Binding bindingElement;
        Container container = xFormsElement.getContainerObject();
        boolean modelBinding = BindingResolver.hasModelBinding(xFormsElement.getElement());

        if (modelBinding) {
            String bindId = ((Binding) xFormsElement).getBindingId();
            bindingElement = (Binding) container.lookup(bindId);
        }
        else {
            bindingElement = (Binding) xFormsElement;
        }

        // UI controls as well as actions can have a repeatItemId. This id
        // references either a RepeatItem (in Repeats) or an Item (in Itemsets).
        if (repeatEntryId != null) {
            // lookup repeat id for relative resolution
            Binding repeatItem = (Binding) container.lookup(repeatEntryId);
            String relativeId = repeatItem.getBindingId();

            // resolve relatively to enclosing repeat
            String relativePath = container.getBindingResolver().resolve(bindingElement, relativeId);

            if (BindingResolver.isAbsolutePath(relativePath)) {
                // resolved to absolute path
                expressionPath = relativePath;
            }
            else {
                if (relativePath.length() == 0) {
                    // resolve enclosing repeat
                    expressionPath = repeatItem.getLocationPath();
                }
                else {
                    // resolve enclosing repeat and attach relative path
                    expressionPath = repeatItem.getLocationPath() + "/" + relativePath;
                }
            }
        }
        else {
            // resolve expression path
            expressionPath = container.getBindingResolver().resolve(bindingElement);
        }

        // return expression path
        return expressionPath;
    }

    /**
     * Checks wether the specified binding element has a model binding.
     *
     * @param element the binding element.
     * @return <code>true</code> if the binding element has a model binding
     * reference, otherwise <code>false</code>.
     */
    public static boolean hasModelBinding(Element element) {
        if (element.hasAttributeNS(NamespaceCtx.XFORMS_NS, BIND_ATTRIBUTE)) {
            return true;
        }

        if (element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_BIND_ATTRIBUTE)) {
            return true;
        }

        return false;
    }

    // public helper methods

    /**
     * Checks wether the specified binding element has a model binding
     * expression.
     *
     * @param element the binding element.
     * @return <code>true</code> if the binding element has a model binding
     * expression, otherwise <code>false</code>.
     */
    public static boolean hasModelBindingExpression(Element element) {
        return NamespaceCtx.XFORMS_NS.equals(element.getNamespaceURI()) &&
                element.getLocalName().equals(BIND) &&
                element.hasAttributeNS(NamespaceCtx.XFORMS_NS, NODESET_ATTRIBUTE);
    }

    /**
     * Checks wether the specified binding element has a nodeset binding
     * expression.
     *
     * @param element the binding element.
     * @return <code>true</code> if the binding element has a nodeset binding
     * expression, otherwise <code>false</code>.
     */
    private static boolean hasNodesetBindingExpression(Element element) {
        return element.hasAttributeNS(NamespaceCtx.XFORMS_NS, NODESET_ATTRIBUTE) ||
                element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_NODESET_ATTRIBUTE);
    }

    /**
     * Checks wether the specified binding element has a single node binding
     * expression.
     *
     * @param element the binding element.
     * @return <code>true</code> if the binding element has a single node
     * binding expression, otherwise <code>false</code>.
     */
    private static boolean hasSingleNodeBindingExpression(Element element) {
        return element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REF_ATTRIBUTE);
    }

    /**
     * Checks wether the specified binding element has an ui binding.
     *
     * @param element the binding element.
     * @return <code>true</code> if the binding element has an ui binding
     * expression, otherwise <code>false</code>.
     */
    public static boolean hasUIBinding(Element element) {
        return (!BindingResolver.hasModelBinding(element)) &&
                (BindingResolver.hasSingleNodeBindingExpression(element) ||
                BindingResolver.hasNodesetBindingExpression(element));
    }

    // scoped resolution

    /**
     * Returns the fully resolved location path.
     * <p/>
     * This method implements Scoped Resolution as defined in the <code>7.3
     * Evaluation Context</code> chapter of the spec.
     *
     * @param binding the binding object to be resolved.
     * @return the fully resolved location path.
     */
    public String resolve(Binding binding) {
        return resolve(binding, null);
    }

    /**
     * Returns the relatively resolved location path.
     * <p/>
     * This method implements partial resolution up to the specified binding
     * object. If no id is given or is not passed during resolution this method
     * implements Scoped resolution.
     *
     * @param binding the binding object to be resolved.
     * @param relativeId the id of the binding object where resolution stops.
     * This parameter is optional and is used for the stepwise resolution of
     * repeated elements.
     * @return the fully resolved location path.
     */
    private String resolve(Binding binding, String relativeId) {
        // [1] get binding expression
        String expression = binding.getBindingExpression();

        // [2] check for null, empty, or dot expression
        if ((expression == null) || (expression.length() == 0) || BindingResolver.isDotReference(expression)) {
            // [2.1] check for enclosing element
            Binding enclosing = binding.getEnclosingBinding();

            if (enclosing == null) {
                // return outermost binding expression
                return OUTERMOST_CONTEXT;
            }

            // [2.2] check for relative id
            if (enclosing.getBindingId().equals(relativeId)) {
                // return empty expression
                return "";
            }

            // return enclosing element's expression
            return resolve(enclosing, relativeId);
        }

        // [3] strip eventual self reference
        expression = BindingResolver.stripSelfReference(expression);

        // [5] check for absolute reference
        if (BindingResolver.isAbsolutePath(expression)) {
            // return absolute expression
            return expression;
        }

        // [6] check for enclosing binding element
        Binding enclosing = binding.getEnclosingBinding();

        if (enclosing != null) {
            // [6.1] check for relative id.
            if (enclosing.getBindingId().equals(relativeId)) {
                // return relative expression
                return expression;
            }

            // [6.2] check for empty parent expression
            String parent = resolve(enclosing, relativeId);
            if (parent.length() == 0) {
                // return relative expression
                return expression;
            }

            // return composed binding expression
            return parent + "/" + expression;
        }

        // [7] compose outermost binding expression
        return OUTERMOST_CONTEXT + "/" + expression;
    }

    /**
     * Checks wether the specified path expression is a self reference and
     * strips the self referencing expression portion.
     *
     * @param expression the path expression.
     * @return the stripped path expression if the specified path expression is
     * a self reference, otherwise the unmodified path expression.
     */
    private static String stripSelfReference(String expression) {
        if (expression.startsWith("./")) {
            // strip self reference
            return expression.substring(2);
        }

        // leave unmodified
        return expression;
    }
}
