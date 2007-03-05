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
import org.chiba.xml.xforms.action.AbstractAction;
import org.chiba.xml.xforms.exception.XFormsBindingException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.AbstractUIElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

/**
 * Initializer holds some static methods that help with recursive initialization and updating of XFormsElement
 * instances.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: Initializer.java,v 1.29 2004/08/15 14:14:18 joernt Exp $
 */
public class Initializer {
    /**
     * Avoids instantiation.
     */
    private Initializer() {
    }

    /**
     * Initializes all action children of the specified element.
     *
     * @param model   the current context model.
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeActionElements(Model model, Element element) throws XFormsException {
        Initializer.initializeActionElements(model, element, null);
    }

    /**
     * Initializes all action children of the specified element.
     *
     * @param model        the current context model.
     * @param element      the element to start with.
     * @param repeatItemId the id of the containing repeat item, if any.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeActionElements(Model model, Element element, String repeatItemId) throws XFormsException {
        XFormsElementFactory elementFactory = model.getContainer().getElementFactory();
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;

                if ((XFormsElementFactory.isActionElement(elementImpl))) {
                    //pass context model
                    Model contextModel = Initializer.getContextModel(model, elementImpl);
                    AbstractAction actionElement = (AbstractAction) elementFactory.createXFormsElement(elementImpl, contextModel);

                    if (repeatItemId != null) {
                        actionElement.setRepeatItemId(repeatItemId);
                        actionElement.setGeneratedId(model.getContainer().generateId());
                        actionElement.register();
                    }
                    actionElement.setRepeatItemId(repeatItemId);
                    actionElement.init();
                }
            }
        }
    }

    /**
     * Initializes all bind children of the specified element.
     *
     * @param model   the current context model.
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeBindElements(Model model, Element element) throws XFormsException {
        XFormsElementFactory elementFactory = model.getContainer().getElementFactory();
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;

                if ((XFormsElementFactory.isBindElement(elementImpl))) {
                    Bind bindElement = (Bind) elementFactory.createXFormsElement(elementImpl, model);
                    bindElement.init();
                }
            }
        }
    }

    /**
     * Initializes all submission children of the specified element.
     *
     * @param model   the current context model.
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeSubmissionElements(Model model, Element element) throws XFormsException {
        XFormsElementFactory elementFactory = model.getContainer().getElementFactory();
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;

                if ((XFormsElementFactory.isSubmissionElement(elementImpl))) {
                    Submission submissionElement = (Submission) elementFactory.createXFormsElement(elementImpl, model);
                    submissionElement.init();
                }
            }
        }
    }

    /**
     * Initializes all ui children of the specified element.
     *
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeUIElements(Element element) throws XFormsException {
        ElementImpl elementImpl = (ElementImpl) element.getOwnerDocument().getDocumentElement();
        Container container = (Container) elementImpl.getUserData();
        Initializer.initializeUIElements(container.getDefaultModel(), element, null);
    }

    /**
     * Initializes all ui children of the specified element.
     *
     * @param model        the current context model.
     * @param element      the element to start with.
     * @param repeatItemId the id of the containing repeat item, if any.
     * @throws XFormsException if any error occurred during init.
     */
    public static void initializeUIElements(Model model, Element element, String repeatItemId) throws XFormsException {
        XFormsElementFactory elementFactory = model.getContainer().getElementFactory();
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;

                if ((XFormsElementFactory.isUIElement(elementImpl))) {
                    Model contextModel = Initializer.getContextModel(model, elementImpl);
                    AbstractUIElement uiElement = (AbstractUIElement) elementFactory.createXFormsElement(elementImpl, contextModel);

                    if (repeatItemId != null) {
                        uiElement.setRepeatItemId(repeatItemId);
                        uiElement.setGeneratedId(model.getContainer().generateId());
                        uiElement.register();
                    }
                    uiElement.init();
                } else {
                    Initializer.initializeUIElements(model, elementImpl, repeatItemId);
                }
            }
        }
    }

    /**
     * Updates all ui children of the specified element. This is called during refresh.
     *
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during update.
     */
    public static void updateUIElements(Element element) throws XFormsException {
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;
                Object userData = elementImpl.getUserData();

                if ((userData != null) && userData instanceof AbstractUIElement) {
                    ((AbstractUIElement) userData).update();
                } else {
                    Initializer.updateUIElements(elementImpl);
                }
            }
        }
    }

    /**
     * Disposes all ui children of the specified element.
     *
     * @param element the element to start with.
     * @throws XFormsException if any error occurred during disposal.
     */
    public static void disposeUIElements(Element element) throws XFormsException {
        NodeList childNodes = element.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl elementImpl = (ElementImpl) node;
                Object userData = elementImpl.getUserData();

                if ((userData != null) && userData instanceof AbstractUIElement) {
                    ((AbstractUIElement) userData).dispose();
                } else {
                    Initializer.disposeUIElements(elementImpl);
                }
            }
        }
    }

    /**
     * Returns the context model of the specified element.
     * <p/>
     * The context model is determined as follows: <ol> <li>If the element has a model binding, the context model of the
     * bind element is returned.</li> <li>If the element has an ui binding including a model attribute, this model is
     * looked up and returned.</li> <li>The current context model is returned.</li> </ol>
     *
     * @param model   the current context model.
     * @param element the element in question.
     * @return the context model of the specified element.
     * @throws XFormsBindingException if the model binding or the ui binding is invalid.
     */
    private static Model getContextModel(Model model, Element element) throws XFormsBindingException {
        if (BindingResolver.hasModelBinding(element)) {
            String bindId;

            if (element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.BIND_ATTRIBUTE)) {
                bindId = element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.BIND_ATTRIBUTE);
            } else {
                bindId = element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.REPEAT_BIND_ATTRIBUTE);
            }

            XFormsElement xFormsElement = model.getContainer().lookup(bindId);

            if (xFormsElement == null) {
                throw new XFormsBindingException("bind '" + bindId + "' not found", (EventTarget) element, bindId);
            }
            if (!(xFormsElement instanceof Bind)) {
                throw new XFormsBindingException("element '" + bindId + "' is not a bind", (EventTarget) element, bindId);
            }

            return xFormsElement.getModel();
        }

        if (BindingResolver.hasUIBinding(element) &&
                element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.MODEL_ATTRIBUTE)) {

            String modelId = element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.MODEL_ATTRIBUTE);
            XFormsElement xFormsElement = model.getContainer().lookup(modelId);

            if (xFormsElement == null) {
                throw new XFormsBindingException("model '" + modelId + "' not found", (EventTarget) element, modelId);
            }
            if (!(xFormsElement instanceof Model)) {
                throw new XFormsBindingException("element '" + modelId + "' is not a model", (EventTarget) element, modelId);
            }

            return (Model) xFormsElement;
        }

        return model;
    }

}

//end of class
