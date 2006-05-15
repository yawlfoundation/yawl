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

import org.apache.log4j.Category;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.constraints.RelevanceSelector;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.exception.XFormsLinkException;
import org.chiba.xml.xforms.exception.XFormsSubmitError;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.chiba.adapter.ChibaAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Implementation of XForms Submission Element.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class Submission extends XFormsElement {
    private static Category LOGGER = Category.getInstance(Submission.class);
    private String action = null;
    private String method = null;
    private String version = null;
    private Boolean indent = null;
    private String mediatype = null;
    private String encoding = null;
    private Boolean omitxmldeclaration = null;
    private Boolean standalone = null;
    private String cdatasectionelements = null;
    private String replace = null;
    private String separator = null;
    private List includenamespaceprefixes = null;
    private String instanceId = null;
    private String locationPath = null;

    /**
     * Creates a new Submission object.
     *
     * @param element DOM Element of this submission
     * @param model   the parent Model
     */
    public Submission(Element element, Model model) {
        super(element, model);
    }

    // todo: refactor submission driver to have setters for these (IOC)
    // submission options

    /**
     * Returns the <code>action</code> submission option.
     *
     * @return the <code>action</code> submission option.
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Returns the <code>cdata-section-elements</code> submission option.
     *
     * @return the <code>cdata-section-elements</code> submission option.
     */
    public String getCDATASectionElements() {
        return this.cdatasectionelements;
    }

    /**
     * Returns the <code>encoding</code> submission option.
     *
     * @return the <code>encoding</code> submission option.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Returns the <code>includenamespaceprefixes</code> submission option.
     *
     * @return the <code>includenamespaceprefixes</code> submission option.
     */
    public List getIncludeNamespacePrefixes() {
        return this.includenamespaceprefixes;
    }

    /**
     * Returns the <code>indent</code> submission option.
     *
     * @return the <code>indent</code> submission option.
     */
    public Boolean getIndent() {
        return this.indent;
    }

    /**
     * Returns the <code>mediatype</code> submission option.
     *
     * @return the <code>mediatype</code> submission option.
     */
    public String getMediatype() {
        return this.mediatype;
    }

    /**
     * Returns the <code>method</code> submission option.
     *
     * @return the <code>method</code> submission option.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns the <code>omit-xml-declaration</code> submission option.
     *
     * @return the <code>omit-xml-declaration</code> submission option.
     */
    public Boolean getOmitXMLDeclaration() {
        return this.omitxmldeclaration;
    }

    /**
     * Returns the submission <code>replace</code> mode.
     *
     * @return the submission <code>replace</code> mode.
     */
    public String getReplace() {
        return this.replace;
    }

    /**
     * Returns the <code>separator</code> submission option.
     *
     * @return the <code>separator</code> submission option.
     */
    public String getSeparator() {
        return this.separator;
    }

    /**
     * Returns the <code>standalone</code> submission option.
     *
     * @return the <code>standalone</code> submission option.
     */
    public Boolean getStandalone() {
        return this.standalone;
    }

    /**
     * Returns the <code>version</code> submission option.
     *
     * @return the <code>version</code> submission option.
     */
    public String getVersion() {
        return this.version;
    }

    // lifecycle methods

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init");
        }

        initializeSubmission();
        Initializer.initializeActionElements(this.model, this.element, null);
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }

    // event handling methods

    /**
     * Performs the default action for the given event.
     *
     * @param event the event for which default action is requested.
     */
    protected void performDefault(Event event) {
        try {
            if (isCancelled(event)) {
                return;
            }

            if (event.getType().equals(EventFactory.SUBMIT)) {
                submit();
                return;
            }
        } catch (Exception e) {
            // handle exception and stop event propagation
            handleException(e);
            event.stopPropagation();
        }
    }

    /**
     * Implements <code>xforms-submit</code> default action.
     */
    protected void submit() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " submit");
        }

        // prepare submission
        Instance instance = this.model.getInstance(getInstanceId());
        String path = getLocationPath();

        // validate instance items
        boolean valid = this.model.getValidator().validate(instance, path);

        if (!valid) {
            // refresh to reflect validation result if failed
            this.container.dispatch(this.model.getTarget(), EventFactory.REFRESH, null);
            throw new XFormsSubmitError("instance validation failed", this.model.getTarget(), this.action);
        }

        Node instanceNode;
        try {
            if (this.includenamespaceprefixes != null) {
                getLogger().warn(this + " submit: the 'includenamespaceprefixes' attribute is not supported yet");
            }

            // select relevant instance items
            instanceNode = RelevanceSelector.selectRelevant(instance, path);
        } catch (Exception e) {
            throw new XFormsSubmitError("instance relevance selection failed", e, this.model.getTarget(), this.action);
        }

        Map responseMap;
        try {

            // serialize and transmit instance items
            responseMap = this.container.getConnectorFactory()
                    .createSubmissionHandler(this.action, this.element)
                    .submit(this, instanceNode);
        } catch (Exception e) {
            throw new XFormsSubmitError("instance submission failed", e, this.model.getTarget(), this.action);
        }

        // post-process submission
        if (this.replace.equals("all")) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " submit: replacing all");
            }

            this.container.dispatch(this.target, EventFactory.SUBMIT_DONE, null);
            this.forward(responseMap);
            //exit without dispatch of submit-done
            return;
        } else if (this.replace.equals("instance")) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " submit: replacing instance");
            }

            Document responseInstance;

            try {
                InputStream responseStream = (InputStream) responseMap.get(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);
                responseInstance = DOMUtil.parseInputStream(responseStream, true, false);
                responseStream.close();
            } catch (Exception e) {
                throw new XFormsSubmitError("instance parsing failed", e, this.model.getTarget(), this.action);
            }

            this.model.getInstance(getInstanceId()).setInstanceDocument(responseInstance);

            // NOTE: we don not dispatch xforms-model-construct here as proposed by the spec,
            // since it would reset all other instances to their initial state, which is not
            // not what we want !
            this.container.dispatch(this.model.getTarget(), EventFactory.REBUILD, null);
            this.container.dispatch(this.model.getTarget(), EventFactory.RECALCULATE, null);
            this.container.dispatch(this.model.getTarget(), EventFactory.REVALIDATE, null);
            this.container.dispatch(this.model.getTarget(), EventFactory.REFRESH, null);
        } else if (this.replace.equals("none")) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " submit: replacing none");
            }
        } else {
            throw new XFormsSubmitError("unknown replace mode " + this.replace, this.model.getTarget(), this.action);
        }
        this.container.dispatch(this.target, EventFactory.SUBMIT_DONE, null);
    }

    protected void initializeSubmission() throws XFormsException {
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, ACTION_ATTRIBUTE)) {
            // get required action attribute
            this.action = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, ACTION_ATTRIBUTE);
        } else {
            // complain
            throw new XFormsLinkException("no action specified for submission", this.target, null);
        }

        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, METHOD_ATTRIBUTE)) {
            // get required method attribute
            this.method = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, METHOD_ATTRIBUTE);
        } else {
            // complain
            throw new XFormsLinkException("no method specified for submission", this.target, null);
        }

        // get optional version attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, VERSION_ATTRIBUTE)) {
            this.version = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    VERSION_ATTRIBUTE);
        }

        // get optional indent attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, INDENT_ATTRIBUTE)) {
            this.indent = Boolean.valueOf(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    INDENT_ATTRIBUTE));
        }

        // get optional mediatype attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, MEDIATYPE_ATTRIBUTE)) {
            this.mediatype = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    MEDIATYPE_ATTRIBUTE);
        }

        // get optional encoding attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, ENCODING_ATTRIBUTE)) {
            this.encoding = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    ENCODING_ATTRIBUTE);
        }

        // get optional omit-xml-declaration attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, OMIT_XML_DECLARATION_ATTRIBUTE)) {
            this.omitxmldeclaration = Boolean.valueOf(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    OMIT_XML_DECLARATION_ATTRIBUTE));
        }

        // get optional standalone attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, STANDALONE_ATTRIBUTE)) {
            this.standalone = Boolean.valueOf(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    STANDALONE_ATTRIBUTE));
        }

        // get optional cdata-section-elements attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, CDATA_SECTION_ELEMENTS_ATTRIBUTE)) {
            this.cdatasectionelements = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    CDATA_SECTION_ELEMENTS_ATTRIBUTE);
        }

        // get optional replace attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REPLACE_ATTRIBUTE)) {
            this.replace = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    REPLACE_ATTRIBUTE);
        } else {
            // default per schema
            this.replace = "all";
        }

        // get optional action attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, SEPARATOR_ATTRIBUTE)) {
            this.separator = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    SEPARATOR_ATTRIBUTE);
        } else {
            // default per schema
            this.separator = ";";
        }

        // get optional includenamespaceprefixes attribute
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, INCLUDENAMESPACEPREFIXES_ATTRIBUTE)) {
            StringTokenizer tokenizer = new StringTokenizer(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    INCLUDENAMESPACEPREFIXES_ATTRIBUTE));
            this.includenamespaceprefixes = new ArrayList(tokenizer.countTokens());

            while (tokenizer.hasMoreTokens()) {
                this.includenamespaceprefixes.add(tokenizer.nextToken());
            }
        }
    }

    public void forward(Map responseMap) {
        this.container.getProcessor().getContext().put(ChibaAdapter.SUBMISSION_RESPONSE, responseMap);
    }

    public void redirect(String uri) {
        this.container.getProcessor().getContext().put(ChibaAdapter.LOAD_URI, uri);
    }

    private String getInstanceId() {
        if (this.instanceId != null) {
            return this.instanceId;
        }

        String path = getLocationPath();

        if (PathUtil.hasInstanceFunction(path)) {
            this.instanceId = PathUtil.getInstanceId(this.model, path);
        } else {
            this.instanceId = this.model.getDefaultInstance().getId();
        }

        return this.instanceId;
    }

    // todo: implement binding interface ?
    private String getLocationPath() {
        if (this.locationPath != null) {
            return this.locationPath;
        }

        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, BIND_ATTRIBUTE)) {
            Bind bind = (Bind) this.container.lookup(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,
                    BIND_ATTRIBUTE));
            this.locationPath = bind.getLocationPath();

            return this.locationPath;
        }

        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REF_ATTRIBUTE)) {
            this.locationPath = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, REF_ATTRIBUTE);

            return this.locationPath;
        }

        this.locationPath = "/";

        return this.locationPath;
    }


    public Map getSubmissionMap() {
        return (Map) container.getProcessor().getContext().get(ChibaAdapter.SUBMISSION_RESPONSE);
    }

}
