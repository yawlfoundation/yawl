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

package org.chiba.adapter;

import org.chiba.xml.xforms.exception.XFormsException;

/**
 * This is the basic interface for integrating the Chiba processor into
 * arbitrary java environments.
 * <p/>
 * ChibaAdapter implementations are responsible to handle the complete lifecycle
 * of the processor: initialization, interaction processing (by delegating to a
 * InteractionHandler instance) and shutdown of the processor. Adpaters are also
 * responsible for instanciating an appropriate InteractionHandler for
 * processing the users input and pass it to the processor.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public interface ChibaAdapter {

    /**
     * Defines the key under which the submission response map may be accessed.
     * When a submission <code>replace="all"</code> happened, this map contains
     * the response of the submission target. There will be at least the
     * <code>SUBMISSION_RESPONSE_STREAM</code> property set.
     * <p/>
     * Additionally, there may be an arbitrary number of other properties set in
     * this map. In HTTP environments this could be the response headers.
     */
    String SUBMISSION_RESPONSE = "chiba.submission.response";

    /**
     * Defines the key under which the submission reponse stream may be accessed
     * as a property of the <code>SUBMISSION_RESPONSE</code> map.
     * <p/>
     * In HTTP environments the ChibaAdapter implementation has to forward this
     * response to the user agent. Simply routing this response to the user
     * agent works for now, but is not completely right, since the user agent
     * stays connected to Chiba instead of the submission target. Maybe a smart
     * redirect/proxy combination could help to achieve this.
     */
    String SUBMISSION_RESPONSE_STREAM = "chiba.submission.response.stream";

    /**
     * Defines the key for an URI to be loaded. ChibaAdapter implementations are
     * free to choose any strategy to load the specified resource.
     * <p/>
     * In HTTP environments this would normally be a redirect.
     */
    String LOAD_URI = "chiba.load.URI";

    /**
     * Defines the key for the target presentation context into which the URI
     * specified under <code>LOAD_URI</code> should be loaded. Possible values
     * are defined by the <code>load</code> action (currently
     * <code>replace</code> and <code>new</code>).
     * <p/>
     * In HTTP environments handling of <code>new</code> from the server side
     * might be achieved with some scripting only. For <code>replace</code> a
     * redirect should fit.
     */
    String LOAD_TARGET = "chiba.load.target";

    /**
     * Defines the key for accessing (HTTP) session ids.
     */
    String SESSION_ID = "chiba.session.id";

    /**
     * initialize the Adapter. This is necessary cause often the using
     * application will need to configure the Adapter before actually setting it
     * up.
     *
     * @throws XFormsException
     */
    void init() throws XFormsException;

    /**
     * executes the InteractionHandler
     *
     * @throws XFormsException
     */
    void executeHandler() throws XFormsException;

    /**
     * terminates the XForms processing. right place to do cleanup of
     * resources.
     *
     * @throws XFormsException
     */
    void shutdown() throws XFormsException;

}
