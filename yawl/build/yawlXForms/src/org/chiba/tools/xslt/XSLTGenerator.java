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
package org.chiba.tools.xslt;

import org.apache.log4j.Category;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * @author joern turner (joernt@chiba.sourceforge.net)
 * @version $Id: XSLTGenerator.java,v 1.8 2004/12/06 23:24:14 joernt Exp $
 */
public class XSLTGenerator implements UIGenerator, URIResolver {
    private static final String STYLESHEET = "html-default";
    private static Category cat = Category.getInstance(XSLTGenerator.class);
    private HashMap userParameters = null;
    private Node inputNode = null;
    private StylesheetLoader sl = null;
    private Object output = null;

    /**
     * Creates a new XSLTGenerator object.
     *
     * @param aLoader a StylesheetLoader object
     * @throws XFormsException if config can't be accessed
     */
    public XSLTGenerator(StylesheetLoader aLoader) throws XFormsException {
        this.sl = aLoader;

        //setting modelprefix and -suffix which is used for parameter encoding
        setParameter("model-prefix", Config.getInstance().getProperty("chiba.model-prefix"));
        setParameter("model-suffix", Config.getInstance().getProperty("chiba.model-suffix"));
    }

    /**
     * sets the input node for the transformer.
     *
     * @param input DOM Node which is passed to transformer
     */
    public void setInputNode(Node input) {
        this.inputNode = input;

        if (cat.isDebugEnabled()) {
            cat.debug("received input node ...");
            DOMUtil.prettyPrintDOM(this.inputNode);
        }
    }

    /**
     * add a parameters to the set of user parameters.  these parameters
     * are passed to the stylesheet used for generating the UI.
     */
    public void setParameter(String key, Object value) {
        if ((key != null) && (value != null)) {
            if (userParameters == null) {
                userParameters = new HashMap();
            }

            userParameters.put(key, value);
        }
    }

    /**
     * Set the object to recive the output.
     *
     * The <var>output</var> object recives the content generated by
     * this UIGenerator. The XSLTGenerator supports output objects of
     * type <code>java.io.Writer</code> and
     * <code>org.w3c.dom.Document</code>.
     *
     * @param output the object to recive the output.
     */
    public void setOutput(Object output) {
        this.output = output;
    }

    /**
     * Transforms the whole XForms-UI of the current DocumentContainer into
     * the appropriate client-representation. 
     *
     * All UI elements belonging to the XForms-namespace along with
     * their associated instance-values will be processed. The
     * generated content will be stored in or written to the output
     * object.
     */
    public void generate() throws XFormsException {
        if (output instanceof Writer) {
            generate((Writer)output);
        } else if (output instanceof Document) {
            generate((Document)output);
        } else {
            throw new XFormsException("Unsupported output type"+
                                      (output!=null?output.getClass().getName():"null"));
        }
    }

    /**
     * transforms the input document and writes the result to stream.
     *
     * @param responseWriter the result stream
     * @throws XFormsException if transformer errors occur
     */
    public void generate(Writer responseWriter) throws XFormsException {
        try {
            Transformer transform = sl.createTransformer(STYLESHEET, inputNode, this);
            setTransformerParameters(transform);

            DOMSource inputDoc = new DOMSource(inputNode);
            StreamResult sr = new StreamResult(responseWriter);
            transform.transform(inputDoc, sr);
        } catch (TransformerException e) {
            throw new XFormsException(e);
        }
    }

    /**
     * transforms the input and writes the result to DOM.
     *
     * @param output the resulting DOM
     * @throws XFormsException if transformer errors occur
     */
    public void generate(Document output) throws XFormsException {
        try {
            Transformer transform = sl.createTransformer(STYLESHEET, inputNode, this);
            setTransformerParameters(transform);

            DOMSource inputDoc = new DOMSource(inputNode);
            transform.transform(inputDoc, new DOMResult(output));
        } catch (TransformerException e) {
            throw new XFormsException(e.getMessageAndLocation());
        }
    }

    /**
     * implements javax.xml.transform.URIResolver. This method is called by the Transformer
     * when it hits e.g. a 'document()' function or an 'import' statement to resolve the location
     * of files.
     *
     * @param href the local href used
     * @param base the base to resolve against
     * @return a Source object that can be used to load a resource
     * @throws TransformerException if transformation errors occur
     */
    public Source resolve(String href, String base) throws TransformerException {

        if (cat.isDebugEnabled()) {
            cat.debug("URIRESOLVER CALLED");
            cat.debug("URIRESOLVER href: " + href);
            cat.debug("URIRESOLVER base: " + base);
        }

        Document stylesheet = sl.loadDocument(href);

        //instance namespaces have to imported here
//		sl.importNamespaces(stylesheet);

        DOMSource ds = new DOMSource(stylesheet.getDocumentElement());

        return ds;
    }

    /**
     * transforms the whole XForms-UI of the current DocumentContainer into
     * the appropriate client-representation. All UI elements belonging to the
     * XForms-namespace along with their associated instance-values
     * will be processed.
     * todo:default-form processing
     */
    private void setTransformerParameters(Transformer transformer) {
        if (userParameters != null) {
            Iterator it = userParameters.keySet().iterator();

            cat.debug(">>> setting params...");
            while (it.hasNext()) {
                String key = (String) it.next();
                transformer.setParameter(key, userParameters.get(key));
                if (cat.isDebugEnabled()) {
                    cat.debug(key + "=" + userParameters.get(key));
                }
            }
            cat.debug(">>> setting params...end");
        }
    }
}

//end of class

