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

/**
 * Helper class with loads internal XSLT-stylesheets.
 *
 * loads the Templates-object from disk or classpath.<br><br>
 *
 * If 'stylesheetPath' is defined, the Templates object is created
 * from the stylesheet-file on disk. This allows you to work with your own
 * version of the stylesheet.<br><br>
 *
 * Otherwise the stylesheet is loaded with getResourceAsStream from the chiba-classpath,
 * parsed into a DOM-tree and passed on to the stylesheet-processor for compilation.<br><br>
 *
 * In both cases the compiled Templates object is created only once and cached for further transforms.<br><br>
 *
 * If the file changes on disk, it is automatically reloaded.
 * <br><br>
 * @version $Id$
 */
package org.chiba.tools.xslt;

import org.apache.log4j.Category;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * loads stylesheet from disk or classpath.
 */
public class StylesheetLoader {
    //    private boolean compiled=false;

    /**
     * holds several Templates-objects of a stylesheet. This is the part which is immutable
     * in an Transformation, while the Transformer-object is only valid
     * for one run. Users of this class are expected to call createTransformer(...)
     * directly.
     */
    private static Hashtable STYLESHEETS = new Hashtable();
    private static Category cat = Category.getInstance(StylesheetLoader.class);
    private String stylesheetPath = null;
    private String stylesheetFile = null;
    private URIResolver resolver;


    /**
     * The transformer factory.
     */
    private TransformerFactory transformerFactory = null;
//    private TransformerFactory factory;

    /**
     * allows to set the stylesheet file to be used by the Transformer
     *
     * @param stylesheetFile - the filename of the stylesheet file (stylesheet must be present
     *                       in the stylesheetPath
     */
    public void setStylesheetFile(String stylesheetFile) {
        this.stylesheetFile = stylesheetFile;
    }

    /**
     * default constructor
     */
    public StylesheetLoader() {
    }

    /**
     * uses this to load Stylesheets from directory.
     */
    public StylesheetLoader(String stylesheetPath) {
        this.stylesheetPath = stylesheetPath;
    }

    /**
     * factory-meth for creating a Transformer for the desired Templates-object (Stylesheet).
     * This method also takes care that the Instance namespaces are imported into
     * the stylesheet.
     *
     * @param styleId  - the string id of the stylesheet used for transform
     * @param input    - the input XML Document for the transform
     * @param resolver - the URIResolver which allows loading of imported stylesheets
     * @return - a Transformer that can be used for a single pass
     */
    public Transformer createTransformer(String styleId, Node input, URIResolver resolver)
            throws TransformerException, TransformerConfigurationException {
        //use styleheet from chiba:stylesheet Attribute on root-element if present
        String fileName = getStyleheetName(input, styleId);

        this.resolver = resolver;
        Transformer transformer;
        TransformerFactory factory = createTransformerFactory(resolver);

        if (cat.isDebugEnabled()) {
            cat.debug("fileName: " + fileName);
            cat.debug("URIResolver: " + resolver.toString());
        }

        if (!getCompiled()) {
            cat.info("Stylesheets will not be compiled");

            Document styles = loadDocument(fileName);
            transformer = factory.newTransformer(new DOMSource(styles));
        } else {
            //compiling stylesheet
            Templates t = (Templates) STYLESHEETS.get(fileName);

            if (t == null) {
                Document styles = loadDocument(fileName);
//				importNamespaces(styles);
                t = compileAndCache(fileName, styles);
            } else {
                cat.info("cachehit: file: " + fileName);
            }

            transformer = t.newTransformer();
        }

        return transformer;
    }


    /**
     * this adds namespace declarations that are exitent in the input
     * XML document to the stylesheet. This is important for handling
     * instance namespaces that are not known until runtime.
     *
     *
     * @param stylesheet - the XSLT stylesheet where namespace-declarations are added to the root
     * element.
     */
//	public void importNamespaces(Document stylesheet) {
//		Element root = stylesheet.getDocumentElement();
//
//		//create namespace-declarations
//		//create your namespace context
//		//        ns=new NamespaceCtx(input);
//		HashMap namespaces = ns.getAllNamespaces();
//		Iterator i = namespaces.keySet().iterator();
//		String prefix;
//		String qname = null;
//
//		while (i.hasNext()) {
//			prefix = (String)i.next();
//			qname = "xmlns:" + prefix;
//
//			if (!(root.hasAttribute(qname))) {
//				root.setAttribute(qname, (String)namespaces.get(prefix));
//
//				if (cat.isDebugEnabled()) {
//					cat.debug("setting namespace: " + prefix + ":" + (String)namespaces.get(prefix));
//				}
//			}
//		}
//	}

//    public Document loadDocument(String fileName, URIResolver resolver) throws TransformerException {
//        this.resolver=resolver;
//        return loadDocument(fileName);
//    }

    /**
     * loads stylesheets from the local package path or from a
     * eventually configured stylesheetPath.
     *
     * @param fileName - the local filename of the XSLT stylesheet
     */
    public Document loadDocument(String fileName) throws TransformerException {
        Document stylesheet;
        String filePath;

        //       synchronized (instance) {
        if (stylesheetPath == null) {
            //take as it is
            stylesheet = loadFromClasspath(fileName);
        } else {
            if (stylesheetPath.endsWith(File.separator)) {
                filePath = stylesheetPath + fileName;
            } else {
                filePath = stylesheetPath + File.separator + fileName;
            }

            stylesheet = loadFromDisk(filePath);
        }

        //       }
        cat.info("stylesheet filename to load: " + fileName);
        if (STYLESHEETS.get(fileName) != null) {
//compile imported or include stylesheet.
            Templates t = compileAndCache(fileName, stylesheet);

//            cat.info("Stylesheet '" + fileName + "' will be compiled");
//            Templates t = factory.newTemplates(new DOMSource(stylesheet));
//            cat.info("adding " + fileName + " to cache");
            STYLESHEETS.put(fileName, t);
        }
        return stylesheet;
    }

    private boolean getCompiled() {
        try {
            if (Config.getInstance().getProperty("chiba.stylesheets.compiled").equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (XFormsConfigException e) {
            return false; // 'false' is the default if value is not present
        }
    }

    /**
     * this returns the name of the stylesheet file (e.g. 'mystyles.xsl'). Uses the following order
     * of precedence:<br>
     * [1] if stylesheetFile is not null, this value is used<br>
     * [2] if the input XML has a chiba:stylesheet Attribute on the root Element this is used<br>
     * [3] finally, the stylesheet filename is grabbed from the config-file. Here the symbolic name
     * (the value of the 'name' attribute) must be used to map to a configured stylesheet.<br>
     * <br>
     * If all fails a TransformerException is thrown.
     * <p/>
     * If the
     * input Document container has a chiba:stylesheet Attribute this is used. Otherwise
     * the stylesheet-name is read from the config-file.
     *
     * @return - the filename of the stylesheet to use for transformation
     */
    private String getStyleheetName(Node input, String styleId)
            throws TransformerException {
        Element root;

        if (input.getNodeType() == Node.DOCUMENT_NODE) {
            root = ((Document) input).getDocumentElement();
        } else {
            root = (Element) input;
        }

        if (this.stylesheetFile != null) {
            return this.stylesheetFile;
        }

        if (root.hasAttributeNS(NamespaceCtx.CHIBA_NS, "stylesheet")) {
            return root.getAttributeNS(NamespaceCtx.CHIBA_NS, "stylesheet");
        }

        try {
            return Config.getInstance().getStylesheet(styleId);
        } catch (XFormsConfigException e) {
            throw new TransformerException(e);
        }
    }

    private TransformerFactory createTransformerFactory(URIResolver resolver) {
        if (transformerFactory == null) {
            // Lazy initialization.
            //            String key = "javax.xml.transform.TransformerFactory";
            //            String value = "org.apache.xalan.xsltc.trax.SmartTransformerFactoryImpl";
            //            Properties props = System.getProperties();
            //            props.put(key, value);
            //            System.setProperties(props);
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(resolver);
            //cat.debug(transformerFactory.toString());
        }

        return transformerFactory;
    }

    private Document loadFromClasspath(String fileName)
            throws TransformerException {
        //loading relative to this class
        InputStream in = getClass().getResourceAsStream(fileName);
        Document stylesheet;

        if (cat.isDebugEnabled()) {
            cat.debug("loadFromClasspath: " + fileName);
        }

        try {
            stylesheet = DOMUtil.parseInputStream(in, true, false);
        } catch (ParserConfigurationException e) {
            throw new TransformerException(e);
        } catch (SAXException e) {
            throw new TransformerException(e);
        } catch (IOException e) {
            throw new TransformerException(e);
        }

        return stylesheet;
    }

    private Document loadFromDisk(String fileName) throws TransformerException {
        if (cat.isDebugEnabled()) {
            cat.debug("loadFromDisk: " + fileName);
        }

        try {
            return DOMUtil.parseXmlFile(fileName, true, false);
        } catch (ParserConfigurationException e) {
            throw new TransformerException(e);
        } catch (SAXException e) {
            throw new TransformerException(e);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
    }

    private Templates compileAndCache(String fileName, Document styles) throws TransformerConfigurationException {
        Templates t;
        cat.info("Stylesheet '" + fileName + "' will be compiled");
        TransformerFactory factory = createTransformerFactory(this.resolver);
        t = factory.newTemplates(new DOMSource(styles));
        STYLESHEETS.put(fileName, t);
        cat.info("adding " + fileName + " to cache");
        return t;
    }

}

//end of class




