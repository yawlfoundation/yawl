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
package org.chiba.xml.util;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * simple wrapper class for transforming source DOM-trees into
 * target DOM-trees.
 * <p/>
 * the stylesheet, that's used for the transformation
 *
 * @author joern turner
 * @version $Id: DOMTransformer.java,v 1.9 2004/08/15 14:14:09 joernt Exp $
 * @deprecated - not in use any more
 */
public class DOMTransformer {
    //logging

    /**
     * Logger
     */
    protected static Category cat = Category.getInstance(DOMTransformer.class);

    //holds the parameters which are passed to the transformer
    private HashMap parameters = null;
    private URIResolver uriresolver = null;

    /**
     * create a DOMTransformer using aStyleSheet
     */
    public DOMTransformer() {
        parameters = new HashMap();
    }

    /**
     * set a parameter for the transformer which will be used
     */
    public void setParameter(String name, String param) {
        parameters.put(name, param);
    }

    /**
     * sets a Map of parameters for the transformer
     *
     * @param map __UNDOCUMENTED__
     */
    public void setParameters(Map map) {
        if (map != null) {
            parameters.putAll(map);
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param res __UNDOCUMENTED__
     */
    public void setURIResolver(URIResolver res) {
        uriresolver = res;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param tf  __UNDOCUMENTED__
     * @param map __UNDOCUMENTED__
     */
    public static void applyParameters(Transformer tf, Map map) {
        if (map != null) {
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                tf.setParameter(key, map.get(key));
            }
        }
    }

    /**
     * transforms a source-document into a target-document and
     * uses the params as input parameter(s) for the transformation.
     * <p/>
     * Parameters may be passed to the transformer by using the setParameter-method.
     *
     * @param in - a Node and its children to be transformed. Here the root-node
     *           of a document is expected.
     * @return - a Document-Node containing the result of the transform
     */
    public Node transformDOM(Document in, Document styleSheet) {
        return transformDOM(in, styleSheet, null);
    }

    /**
     * transforms a source-document into a target-document and
     * uses the params as input parameter(s) for the transformation.
     * <p/>
     * Parameters may be passed to the transformer by using the setParameter-method.
     *
     * @param in       - a Node and its children to be transformed. Here the root-node
     *                 of a document is expected.
     * @param resultTo append the transform output under this node.  if
     *                 null the result could only be fetched from the returnvalue.
     * @return - a Document-Node containing the result of the transform; if
     *         a resultTo is set the return value is identical to the set node
     *         (resp. element), if resultTo was null the return value will be
     *         DocumentNode
     */
    public Node transformDOM(Document in, Document styleSheet, Element resultTo) {
        DOMSource styleSource = new DOMSource(styleSheet);

        TransformerFactory tFactory = TransformerFactory.newInstance();

        if (uriresolver != null) {
            tFactory.setURIResolver(uriresolver);
        }

        try {
            //            Transformer transformer=tFactory.newTransformer(new StreamSource(getClass().getResourceAsStream(DEFAULTSTYLESHEET)));
            Transformer transformer = tFactory.newTransformer(styleSource);

            //set all params for the transformer
            String key = null;
            Iterator it = parameters.keySet().iterator();

            while (it.hasNext()) {
                key = (String) it.next();
                transformer.setParameter(key, parameters.get(key));
            }

            //doit
            DOMSource input = new DOMSource(in);
            DOMResult result = new DOMResult();

            if (resultTo != null) {
                result.setNode(resultTo);
            }

            transformer.transform(input, result);

            //reset params, in case this instance will be re-used
            clearParams();

            //return result DOM-tree
            return result.getNode();
        } catch (TransformerConfigurationException tc) {
            cat.error("Transformer Configuration Exception : " + tc.getMessage());
        } catch (TransformerException te) {
            cat.error("Transformer Exception : " + te.getMessage());
        }

        return null;
    }

    /**
     * if this DOMTransformer is used for several transformations...:
     * wipes out all parameters which may have been set by previous
     * uses of an DOMTransformer-instance.
     */
    private void clearParams() {
        parameters.clear();
    }
}


/*
   $Log: DOMTransformer.java,v $
   Revision 1.9  2004/08/15 14:14:09  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.8  2004/07/28 21:43:28  joernt
   optimized imports

   Revision 1.7  2004/01/27 10:00:13  joernt
   imports

   Revision 1.6  2003/11/07 00:19:22  joernt
   optimized imports

   Revision 1.5  2003/10/13 10:11:23  joernt
   javadoc;
   log message deleted.

   Revision 1.4  2003/10/02 15:15:51  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.3  2003/09/04 23:07:54  joernt
   changed setup to init;
   optimized imports
   Revision 1.2  2003/08/18 23:02:13  joernt
   javadoc fixes + optimize imports
   Revision 1.1.1.1  2003/05/23 14:54:06  unl
   no message
   Revision 1.15  2002/12/16 12:44:50  joernt
   minor cleanups
   Revision 1.14  2002/12/09 01:27:10  joernt
   -cleanup;
   -reorganised imports
   Revision 1.13  2002/06/14 22:33:24  joernt
   unused constant removed
   Revision 1.12  2002/01/12 20:54:07  eyestep
   added applyParameters func
   Revision 1.11  2001/12/14 14:12:16  joernt
   formatting
   Revision 1.10  2001/11/06 10:34:33  joern
   unneeded cast removed
   Revision 1.9  2001/10/30 14:18:54  gregor
   log4jing
   Revision 1.8  2001/09/20 18:21:03  gregor
   don't set null to parameters.putAll().
   Revision 1.7  2001/09/13 21:37:20  gregor
   changed to use HashMap now (make it possible to putAll()); add setParameters()
   Revision 1.6  2001/09/12 09:05:19  gregor
   added a new transformDOM method taking a target node
   Revision 1.5  2001/09/10 14:10:27  gregor
   added debug output
   Revision 1.4  2001/09/04 09:27:04  joern
   system.outs deleted
   Revision 1.3  2001/08/29 10:04:52  gregor
   support for URIResolver interface
   Revision 1.2  2001/08/15 10:18:28  joern
   constructor parameters changed; transformDOM parameters changed
   Revision 1.1  2001/08/14 14:44:50  joern
   initial
 */
