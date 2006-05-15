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

import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * A simple base class for convenient connector interface implementation.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public abstract class AbstractConnector implements Connector {
    /**
     * The URI the connector is associated to.
     */
    private String uri;

    /**
     * Use this encoding, if client does not indicate otherwise.
     */
    private String defaultEncoding = "UTF-8";

    /**
     * InstanceSerializerMap contains scheme+method+mediatype combination along
     * with proper instance serializer.
     */
    private InstanceSerializerMap instanceSerializerMap;

    /**
     * The context map.
     */
    private Map context;

    /**
     * Sets the URI the connector is associated to.
     *
     * @param uri the URI the connector is associated to.
     */
    public void setURI(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI the connector is associated to.
     *
     * @return the URI the connector is associated to.
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Register new instance serializer for given scheme, method and mediatype.
     * Method supports star convention, the scheme, method or mediatype set to
     * "*" acts as "for all" operator.
     *
     * @param scheme scheme part of the action uri
     * @param method method from xforms:method attribute
     * @param mediatype mediatype from xforms:mediatype attribute
     * @param serializer serializer that should be used for instance
     * serialization.
     */
    public void registerSerializer(String scheme, String method,
                                   String mediatype, InstanceSerializer serializer)
            throws XFormsException {

        if (instanceSerializerMap == null) {
            Config config = Config.getInstance();
            instanceSerializerMap = new InstanceSerializerMap(config.getInstanceSerializerMap());
        }
        instanceSerializerMap.registerSerializer(scheme, method, mediatype, serializer);
    }

    /**
     * Return serializer associated with given scheme, method and mediatype. The
     * lookup proceeds with following steps, returning first successfull
     * match:<br> <ol> <li> scheme, method, mediatype <li> scheme, method, "*"
     * <li> scheme, "*", mediatype <li> scheme, "*", "*" <li> "*", method,
     * mediatype <li> "*", method, "*" <li> "*", "*", mediatype <li> "*", "*",
     * "*" </ol>
     *
     * @param scheme scheme part of the action uri
     * @param method method from xforms:method attribute
     * @param mediatype mediatype from xforms:mediatype attribute
     * @return instance serializer or null
     */
    public InstanceSerializer getSerializer(String scheme, String method, String mediatype) throws XFormsException {
        if (instanceSerializerMap == null) {
            Config config = Config.getInstance();
            instanceSerializerMap = new InstanceSerializerMap(config.getInstanceSerializerMap());
        }
        return instanceSerializerMap.getSerializer(scheme, method, mediatype);
    }

    /**
     * Sets the encoding that will be used for serialization if client does not
     * provide it's own.
     *
     * @param defaultEncoding the encoding.
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Returns the default encoding for serialization.
     *
     * @return the default encoding as String.
     */
    public String getDefaultEncoding() {
        return this.defaultEncoding;
    }

    /**
     * Sets the context map.
     *
     * @param context the context map.
     */
    public void setContext(Map context) {
        this.context = context;
    }

    /**
     * Returns the context map.
     *
     * @return the context map.
     */
    public Map getContext() {
        return this.context;
    }

    /**
     * strips the fragment part  (the part that starts with '#') from the URI
     *
     * @return URI string with fragment cut off
     */
    protected String getURIWithoutFragment() {
        if (this.uri == null) {
            return null;
        }

        int fragmentIndex = this.uri.indexOf('#');

        if (fragmentIndex == -1) {
            return this.uri;
        }

        return this.uri.substring(0, fragmentIndex);
    }

    protected final void serialize(Submission submission, Node instance, OutputStream stream) throws Exception {

        String method = submission.getMethod();
        if (method == null) {
            // oops ...            
            throw new XFormsException("Submission method not defined.");
        }

        URI uri = new URI(getURI());
        String scheme = uri.getScheme();

        String mediatype = submission.getMediatype();
        if (mediatype == null) {
            mediatype = "application/xml";
        }

        InstanceSerializer serializer = getSerializer(scheme, method, mediatype);
        if (serializer == null) {
            // is exception the right way to go ?
            throw new XFormsException("No instance serializer defined for scheme '"
                    + scheme + "', method '" + method + "' and mediatype '"
                    + mediatype + "'");
        }

        serializer.serialize(submission, instance, stream, getDefaultEncoding());
    }

    /**
     * validate the instance according to its XMLSchema, if it is specified on
     * the model return true if the instance is valid, or if there is no
     * XMLSchema
     */
    public boolean validateSchema(Submission submission, Node instance) throws XFormsException {
        //launch schema validation
        Model model = submission.getModel();
        SchemaValidator validator = new SchemaValidator();
        return validator.validateSchema(model, instance);
    }

}

// end of class
