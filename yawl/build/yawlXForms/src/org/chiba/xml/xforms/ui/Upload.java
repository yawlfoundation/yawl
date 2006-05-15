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
package org.chiba.xml.xforms.ui;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Category;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.xforms.InstanceData;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.ModelItem;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.UnsupportedEncodingException;

/**
 * Implementation of XForms Upload Element.
 *
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id$
 */
public class Upload extends AbstractFormControl {
    private static final Category LOGGER = Category.getInstance(Upload.class);

    /**
     * Creates a new Upload object.
     *
     * @param element the DOM Element
     * @param model   the context Model
     */
    public Upload(Element element, Model model) {
        super(element, model);
    }

    /**
     * Sets the value of this form control.
     * <p/>
     * The bound instance data is updated and the event sequence for this control
     * is executed. Event sequences are described in Chapter 4.6 of XForms 1.0 Recommendation.
     *
     * @param value the value to be set.
     */
    public void setValue(String value) throws XFormsException {
        LOGGER.warn("Update control cannot be set with this method.");
    }

    /**
     * Sets the value of this form control.
     * <p/>
     * The bound instance data is updated and the event sequence for this control
     * is executed. Event sequences are described in Chapter 4.6 of XForms 1.0 Recommendation.
     *
     * @param contentType the content-type of the uploaded data
     * @param filename    the filename of the uploaded data
     * @param data        the actual data as byte array
     */
    public void setValue(String contentType, String filename, byte[] data)
            throws XFormsException {

        InstanceData instance = this.model.getInstance(getInstanceId());

        if (!hasModelBinding()) {
            throw new XFormsException("Single node binding not supported yet");
        } else {

            String dataValue = null;

            if (data != null && data.length > 0) {

                // default mediatype
                if (contentType == null || contentType.equals("")) {
                    contentType = "application/octet-stream";
                }

                // check data binding ...
                String datatype = getModelBinding().getDatatype();
                if (datatype.equalsIgnoreCase("base64Binary")) {
                    dataValue = new String(Base64.encodeBase64(data, true));
                } else if (datatype.equalsIgnoreCase("hexBinary")) {
                    dataValue = new String(Hex.encodeHex(data));
                } else if (datatype.equalsIgnoreCase("anyURI")) {
                    // should be URI in this case
                    try {
                        dataValue = new String(data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // ignore ...
                    }
                } else {
                    throw new XFormsException("Unsupported datatype for Upload: " + datatype);
                }

                instance.setNodeValue(getLocationPath(), dataValue);
                ModelItem item = instance.getModelItem(getLocationPath());
                if (!item.isReadonly()) {
                    item.setFileName(filename);
                    item.setMediatype(contentType);
                }
            } else {
                // reset the values, can this be done more intelligent way ?
                instance.setNodeValue(getLocationPath(), "");
                ModelItem item = instance.getModelItem(getLocationPath());
                if (!item.isReadonly()) {
                    item.setFileName("");
                    item.setMediatype("");
                }
                filename = "";
                contentType = "";
            }
            
            // update children ...
            NodeList nodes = getElement().getChildNodes();
            if (nodes != null && nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Object userData = ((ElementImpl) node).getUserData();
                    if (userData != null) {
                        if (userData instanceof Mediatype) {
                            ((Mediatype) userData).setValue(contentType);
                        } else if (userData instanceof Filename) {
                            ((Filename) userData).setValue(filename);
                        }
                    }
                }
            }

            dispatchValueChangeSequence();
        }
    }

    /**
     * Accessor method for Filename element
     */
    void setFilename(String filename) throws XFormsException {
        InstanceData instance = this.model.getInstance(getInstanceId());
        if (hasModelBinding()) {
            ModelItem item = instance.getModelItem(getLocationPath());
            if (!item.isReadonly()) {
                item.setFileName(filename);
            }
        }
    }

    /**
     * Accessor method for Mediatype element
     */
    void setMediatype(String mediatype) throws XFormsException {
        InstanceData instance = this.model.getInstance(getInstanceId());
        if (hasModelBinding()) {
            ModelItem item = instance.getModelItem(getLocationPath());
            if (!item.isReadonly()) {
                item.setMediatype(mediatype);
            }
        }
    }

    /**
     * Return true, if the bound data-item has a datatype of 'xsd:anyUri'.
     *
     * @return true, if the data-type of the binding is anyUri, false otherwise
     */
    public boolean hasAnyUriType() {
        String datatype = getModelBinding().getDatatype();
        return "anyURI".equalsIgnoreCase(datatype);
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }
}

//end of class
