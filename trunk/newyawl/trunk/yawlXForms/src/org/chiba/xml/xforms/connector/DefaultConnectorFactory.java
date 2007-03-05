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
package org.chiba.xml.xforms.connector;

import org.apache.log4j.Category;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;

import java.net.URI;

/**
 * Creates connector instances according to the URI schema passed in.
 * <p/>
 * Only absolute URIs are supported. This means they must have a scheme that
 * identifies the protocol to be used.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: DefaultConnectorFactory.java,v 1.6 2004/11/18 16:05:57 unl Exp $
 */
public class DefaultConnectorFactory extends ConnectorFactory {

    private static Category LOGGER = Category.getInstance(ConnectorFactory.class);

    /**
     * Creates a new connector factory.
     */
    public DefaultConnectorFactory() {
    }

    /**
     * Creates a new modelitem calculator for the specified URI.
     *
     * @param uri the relative or absolute URI string.
     * @param element the element to start with XML Base resolution for relative
     * URIs.
     * @return a new modelitem calculator for the specified URI.
     * @throws XFormsException if a relative URI could not be resolved, no
     * modelitem calculator is registered for the specified URI or any error
     * occurred during modelitem calculator creation.
     * @deprecated use custom extension functions instead
     */
    public ModelItemCalculator createModelItemCalculator(final String uri, final Element element) throws XFormsException {
        URI uriObj = getAbsoluteURI(uri, element);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("creating modelitem calculator for '" + uriObj + "'");
        }

        String className = Config.getInstance().getModelItemCalculator(uriObj.getScheme());
        if (className == null) {
            throw new XFormsException("no modelitem calculator registered for '" + uri + "'");
        }

        Object instance = createInstance(className);
        if (!(instance instanceof ModelItemCalculator)) {
            throw new XFormsException("object instance of '" + className + "' is no modelitem calculator");
        }

        ModelItemCalculator modelItemCalculator = (ModelItemCalculator) instance;
        modelItemCalculator.setURI(uriObj.toString());
        modelItemCalculator.setContext(getContext());

        return modelItemCalculator;
    }

    /**
     * Creates a new modelitem validator for the specified URI.
     *
     * @param uri the relative or absolute URI string.
     * @param element the element to start with XML Base resolution for relative
     * URIs.
     * @return a new modelitem validator for the specified URI.
     * @throws XFormsException if a relative URI could not be resolved, no
     * modelitem validator is registered for the specified URI or any error
     * occurred during modelitem validator creation.
     * @deprecated use custom extension functions instead
     */
    public ModelItemValidator createModelItemValidator(final String uri, final Element element) throws XFormsException {
        URI uriObj = getAbsoluteURI(uri, element);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("creating modelitem validator for '" + uriObj + "'");
        }

        String className = Config.getInstance().getModelItemValidator(uriObj.getScheme());
        if (className == null) {
            throw new XFormsException("no modelitem validator registered for '" + uri + "'");
        }

        Object instance = createInstance(className);
        if (!(instance instanceof ModelItemValidator)) {
            throw new XFormsException("object instance of '" + className + "' is no modelitem validator");
        }

        ModelItemValidator modelItemValidator = (ModelItemValidator) createInstance(className);
        modelItemValidator.setURI(uriObj.toString());
        modelItemValidator.setContext(getContext());

        return modelItemValidator;
    }

    /**
     * Creates a new submission handler for the specified URI.
     *
     * @param uri the relative or absolute URI string.
     * @param element the element to start with XML Base resolution for relative
     * URIs.
     * @return a new submission handler for the specified URI.
     * @throws XFormsException if a relative URI could not be resolved, no
     * submission handler is registered for the specified URI or any error
     * occurred during submission handler creation.
     */
    public SubmissionHandler createSubmissionHandler(final String uri, final Element element) throws XFormsException {
        URI uriObj = getAbsoluteURI(uri, element);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("creating submission handler for '" + uriObj + "'");
        }

        String className = Config.getInstance().getSubmissionHandler(uriObj.getScheme());
        if (className == null) {
            throw new XFormsException("no submission handler registered for '" + uri + "'");
        }

        Object instance = createInstance(className);
        if (!(instance instanceof SubmissionHandler)) {
            throw new XFormsException("object instance of '" + className + "' is no submission handler");
        }

        SubmissionHandler submissionHandler = (SubmissionHandler) instance;
        submissionHandler.setURI(uriObj.toString());
        submissionHandler.setContext(getContext());

        return submissionHandler;
    }

    /**
     * Creates a new URI resolver for the specified URI.
     *
     * @param uri the relative or absolute URI string.
     * @param element the element to start with XML Base resolution for relative
     * URIs.
     * @return a new URI resolver for the specified URI.
     * @throws XFormsException if a relative URI could not be resolved, if no
     * URI resolver is registered for the specified URI or any error occurred
     * during URI resolver creation.
     */
    public URIResolver createURIResolver(final String uri, final Element element) throws XFormsException {
        URI uriObj = getAbsoluteURI(uri, element);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("creating uri resolver for '" + uriObj + "'");
        }

        String className = Config.getInstance().getURIResolver(uriObj.getScheme());
        if (className == null) {
            throw new XFormsException("no uri resolver registered for '" + uri + "'");
        }

        Object instance = createInstance(className);
        if (!(instance instanceof URIResolver)) {
            throw new XFormsException("object instance of '" + className + "' is no uri resolver");
        }

        URIResolver uriResolver = (URIResolver) instance;
        uriResolver.setURI(uriObj.toString());
        uriResolver.setContext(getContext());

        return uriResolver;
    }

    /**
     * Creates a new object of the specifed class.
     *
     * @param className the class name.
     * @return a new object of the specifed class.
     * @throws XFormsException if any error occurred during object creation.
     */
    private Object createInstance(String className) throws XFormsException {
        try {
            Class clazz = Class.forName(className);
            return clazz.newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            throw new XFormsException(cnfe);
        }
        catch (InstantiationException ie) {
            throw new XFormsException(ie);
        }
        catch (IllegalAccessException iae) {
            throw new XFormsException(iae);
        }
    }

}

// end of class
