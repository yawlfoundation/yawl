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
package org.chiba.xml.xforms;

import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.ElementNSImpl;
import org.chiba.xml.xforms.action.*;
import org.chiba.xml.xforms.ui.*;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * XFormsElementFactory creates objects for all DOM Nodes in the input Document that are
 * in the XForms namespace or are bound to some instance node by XForms binding attributes.
 * <p/>
 * These objects holds the XForms semantics and are attached to their original DOM equivalent
 * via the Xerces-specific getUserData/setUserData methods in ElementNSImpl.
 *
 * @author <a href="mailto:joernt@users.sourceforge.net">Joern Turner</a>
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id: XFormsElementFactory.java,v 1.21 2004/12/13 00:22:32 unl Exp $
 * @see ElementNSImpl
 */
public class XFormsElementFactory implements XFormsConstants {
    private static final String[] ACTION_ELEMENTS = {
        ACTION, DISPATCH, REBUILD, RECALCULATE, REVALIDATE,
        REFRESH, SETFOCUS, LOAD, SETVALUE, SEND, RESET,
        MESSAGE, TOGGLE, INSERT, DELETE, SETINDEX
    };

    //not used currently - just for completeness
//    private static final String[] CORE_ELEMENTS = {MODEL, INSTANCE, BIND, SUBMISSION};
    private static final String[] FORM_CONTROLS = {
        GROUP, INPUT, SECRET, TEXTAREA, OUTPUT, UPLOAD, RANGE,
        TRIGGER, SUBMIT, SELECT, SELECT1
    };
    private static final String[] UI_ELEMENTS = {
        EXTENSION, CHOICES, ITEM, VALUE, FILENAME, MEDIATYPE,
        LABEL, HELP, HINT, ALERT, SWITCH, CASE, REPEAT, ITEMSET,
        COPY
    };
    private static final List ACTION_ELEMENT_LIST = Arrays.asList(ACTION_ELEMENTS);
//    private static final List CORE_ELEMENT_LIST = Arrays.asList(CORE_ELEMENTS); //not used currently - just for completeness
    private static final List FORM_CONTROL_LIST = Arrays.asList(FORM_CONTROLS);
    private static final List UI_ELEMENT_LIST = Arrays.asList(UI_ELEMENTS);

    /**
     * Creates a new XFormsElementFactory object.
     */
    public XFormsElementFactory() {
    }

    /**
     * returns true, if the given DOM Element is a XForms action Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms action Element
     */
    public static boolean isActionElement(Element element) {
        String name = element.getLocalName();
        String uri = element.getNamespaceURI();

        return NamespaceCtx.XFORMS_NS.equals(uri) && ACTION_ELEMENT_LIST.contains(name);
    }

    /**
     * returns true, if the given DOM Element is a XForms bind Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms bind Element
     */
    public static boolean isBindElement(Element element) {
        String name = element.getLocalName();
        String uri = element.getNamespaceURI();

        return NamespaceCtx.XFORMS_NS.equals(uri) && BIND.equals(name);
    }

    /**
     * returns true, if the given DOM Element is a XForms submission Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms submission Element
     */
    public static boolean isSubmissionElement(Element element) {
        String name = element.getLocalName();
        String uri = element.getNamespaceURI();

        return NamespaceCtx.XFORMS_NS.equals(uri) && SUBMISSION.equals(name);
    }

    /**
     * returns true, if the given DOM Element is a XForms UI Element.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element is a XForms UI Element
     */
    public static boolean isUIElement(Element element) {
        if (hasRepeatAttributes(element)) {
            return true;
        }

        String name = element.getLocalName();
        String uri = element.getNamespaceURI();

        return NamespaceCtx.XFORMS_NS.equals(uri) &&
                (UI_ELEMENT_LIST.contains(name) || FORM_CONTROL_LIST.contains(name));
    }

    /**
     * factory method for XFormsElement objects.
     *
     * @param element - the DOM Element which will be annotated
     * @param model   the owning model
     * @return the created object
     */
    public XFormsElement createXFormsElement(Element element, Model model) /*throws XFormsException*/ {
        String localName = element.getLocalName();

        XFormsElement xformsElement;

        // 3.3 The XForms Core Module
        if (localName.equals(MODEL)) {
            xformsElement = new Model(element);
        } else if (localName.equals(INSTANCE)) {
            xformsElement = new Instance(element, model);
        } else if (localName.equals(BIND)) {
            xformsElement = new Bind(element, model);
        } else if (localName.equals(SUBMISSION)) {
            xformsElement = new Submission(element, model);
        } else if (localName.equals(EXTENSION)) {
            // 3.5 The XForms Extension module
            xformsElement = new Extension(element, model);
        } else if (localName.equals(INPUT) || localName.equals(SECRET) || localName.equals(TEXTAREA)) {
            // 8.1 The XForms Form Controls Module
            xformsElement = new Text(element, model);
        } else if (localName.equals(OUTPUT)) {
            xformsElement = new Output(element, model);
        } else if (localName.equals(UPLOAD)) {
            xformsElement = new Upload(element, model);
        } else if (localName.equals(RANGE)) {
            xformsElement = new Range(element, model);
        } else if (localName.equals(TRIGGER)) {
            xformsElement = new Trigger(element, model);
        } else if (localName.equals(SUBMIT)) {
            xformsElement = new Submit(element, model);
        } else if (localName.equals(SELECT)) {
            Selector selector = new Selector(element, model);
            selector.setMultiple(true);
            xformsElement = selector;
        } else if (localName.equals(SELECT1)) {
            Selector selector = new Selector(element, model);
            selector.setMultiple(false);
            xformsElement = selector;
        } else if (localName.equals(CHOICES)) {
//			8.2 Common Markup for Selection Controls
            xformsElement = new Choices(element, model);
        } else if (localName.equals(ITEM)) {
            xformsElement = new Item(element, model);
        } else if (localName.equals(VALUE)) {
            xformsElement = new Value(element, model);
        } else if (localName.equals(FILENAME)) {
//			8.3 Additional Elements
            xformsElement = new Filename(element, model);
        } else if (localName.equals(MEDIATYPE)) {
            xformsElement = new Mediatype(element, model);
        } else if (localName.equals(LABEL) || localName.equals(HELP) || localName.equals(HINT) ||
                localName.equals(ALERT)) {
            xformsElement = new Common(element, model);
        } else if (localName.equals(GROUP)) {
//			9.1 The XForms Group Module
            if (element.hasAttributeNS(NamespaceCtx.CHIBA_NS, "transient")) {
                xformsElement = new RepeatItem(element, model);
            } else {
                xformsElement = new Group(element, model);
            }
        } else if (localName.equals(SWITCH)) {
//			9.2 The XForms Switch Module (w/o actions)
            xformsElement = new Switch(element, model);
        } else if (localName.equals(CASE)) {
            xformsElement = new Case(element, model);
        } else if (localName.equals(REPEAT) || hasRepeatAttributes(element)) {
//			9.3 The XForms Repeat Module (w/o actions)
            xformsElement = new Repeat(element, model);
        } else if (localName.equals(ITEMSET)) {
            xformsElement = new org.chiba.xml.xforms.ui.Itemset(element, model);
        } else if (localName.equals(COPY)) {
            xformsElement = new Copy(element, model);
        } else if (localName.equals(ACTION)) {
//			10.1 The XForms Action Module
            xformsElement = new ActionAction(element, model);
        } else if (localName.equals(DISPATCH)) {
            xformsElement = new DispatchAction(element, model);
        } else if (localName.equals(REBUILD)) {
            xformsElement = new RebuildAction(element, model);
        } else if (localName.equals(RECALCULATE)) {
            xformsElement = new RecalculateAction(element, model);
        } else if (localName.equals(REVALIDATE)) {
            xformsElement = new RevalidateAction(element, model);
        } else if (localName.equals(REFRESH)) {
            xformsElement = new RefreshAction(element, model);
        } else if (localName.equals(SETFOCUS)) {
            xformsElement = new SetFocusAction(element, model);
        } else if (localName.equals(LOAD)) {
            xformsElement = new LoadAction(element, model);
        } else if (localName.equals(SETVALUE)) {
            xformsElement = new SetValueAction(element, model);
        } else if (localName.equals(SEND)) {
            xformsElement = new SendAction(element, model);
        } else if (localName.equals(RESET)) {
            xformsElement = new ResetAction(element, model);
        } else if (localName.equals(MESSAGE)) {
            xformsElement = new MessageAction(element, model);
        } else if (localName.equals(TOGGLE)) {
            xformsElement = new ToggleAction(element, model);
        } else if (localName.equals(INSERT)) {
            xformsElement = new InsertAction(element, model);
        } else if (localName.equals(DELETE)) {
            xformsElement = new DeleteAction(element, model);
        } else if (localName.equals(SETINDEX)) {
            xformsElement = new SetIndexAction(element, model);
        } else {
//todo: try to find custom action
            //todo: throw exception
            return null;
        }

        // attach XFormsElement to DOM Element by using Xerces specific method
        ((ElementImpl) element).setUserData(xformsElement);

        return xformsElement;
    }

    /**
     * returns true, if the given DOM Element has XForms repeat binding attributes.
     *
     * @param element the DOM Element to investigate
     * @return true, if the given DOM Element has XForms repeat binding attributes
     */
    public static boolean hasRepeatAttributes(Element element) {
        return element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_MODEL_ATTRIBUTE) ||
                element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_BIND_ATTRIBUTE) ||
                element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_NODESET_ATTRIBUTE);
    }
}

//end of class


