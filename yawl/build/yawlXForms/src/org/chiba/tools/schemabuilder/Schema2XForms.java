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
package org.chiba.tools.schemabuilder;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author joern turner <joernt@chiba.sourceforge.net>
 * @version $Id: Schema2XForms.java,v 1.11 2004/08/15 16:27:22 joernt Exp $
 */
public class Schema2XForms extends Task {

    public static void main(String args[]) {
        Schema2XForms builder = new Schema2XForms();

        int length = args.length;
        for (int i = 0; i < length; i++) {
            String argi = args[i];
            if (argi != null && argi.indexOf('=') > 0) {
                int equal = argi.indexOf('=');
                String name = argi.substring(0, equal);
                String value = argi.substring(equal + 1);
                builder.setProperty(name, value);
            }
        }

        try {
            builder.execute();
        } catch (BuildException ex) {
            ex.printStackTrace();
        }

    }

    private File outputFile = null;
    private String inputURI = null;
    private StreamSource instance = null;
    private String action = null;
    private String instanceHref = null;
    private String rootTagName = null;
    private String stylesheet = null;
    private String submitMethod = null;
    private String base = null;
    private boolean useSchemaTypes = false;
    private WrapperElementsBuilder wrapper = new BaseWrapperElementsBuilder();

    /**
     * method to set a property
     *
     * @param name  name of the property to set
     * @param value value of the property to set
     */
    public void setProperty(String name, String value) {
        if (name != null && value != null) {
            if (name.equals("outputFile")) {
                this.setOutputFile(new File(value));
            } else if (name.equals("inputURI")) {
                try {
                    this.setInputURI(value);
                } catch (IOException ex) {
                    System.err.println("IOException while setting inputURI: " + value);
                }
            } else if (name.equals("instanceFile")) {
                try {
                    this.setInstanceFile(value);
                } catch (IOException ex) {
                    System.err.println("IOException while setting instanceFile: " + value);
                }
            } else if (name.equals("instanceHref")) {
                this.setInstanceHref(value);
            } else if (name.equals("rootTagName")) {
                this.setRootTagName(value);
            } else if (name.equals("action")) {
                this.setAction(value);
            } else if (name.equals("submitMethod")) {
                this.setSubmitMethod(value);
            } else if (name.equals("wrapperType")) {
                this.setWrapperType(value);
            } else if (name.equals("stylesheet")) {
                this.setStylesheet(value);
            } else if (name.equals("base")) {
                this.setBase(value);
            } else if (name.equals("useSchemaTypes")) {
                this.setUseSchemaTypes(value);
            }
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param action __UNDOCUMENTED__
     */
    public void setAction(String action) {
        if ((action != null) && !action.equals("")) {
            this.action = action;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param inputURI __UNDOCUMENTED__
     * @throws IOException __UNDOCUMENTED__
     */
    public void setInputURI(String inputURI) throws IOException {
        this.inputURI = inputURI;

        /*URL url=getClass().getResource(inputFile);
        System.out.println("InputFile: "+url.toString()+", path="+url.getPath());
        this.inputURI=url.getPath();*/
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param instanceFile __UNDOCUMENTED__
     * @throws IOException __UNDOCUMENTED__
     */
    public void setInstanceFile(String instanceFile) throws IOException {
        if ((instanceFile != null) && !instanceFile.equals("")) {
            File file = new File(instanceFile);

            if ((file != null) && file.exists()) {
                this.instance = new StreamSource(file);
            } else {
                System.out.println("warning: the instance file does not exist");
            }
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param instanceHref __UNDOCUMENTED__
     */
    public void setInstanceHref(String instanceHref) {
        if ((instanceHref != null) && !instanceHref.equals("")) {
            this.instanceHref = instanceHref;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param out __UNDOCUMENTED__
     */
    public void setOutputFile(File out) {
        this.outputFile = out;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param rootTagName __UNDOCUMENTED__
     */
    public void setRootTagName(String rootTagName) {
        this.rootTagName = rootTagName;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param stylesheet __UNDOCUMENTED__
     */
    public void setStylesheet(String stylesheet) {
        if ((stylesheet != null) && !stylesheet.equals("")) {
            this.stylesheet = stylesheet;
        }
    }

    /**
     * If set to 'true' activates type checking.
     *
     * @param schemaTypes if set to 'true' activates type checking
     */
    public void setUseSchemaTypes(String schemaTypes) {
        if (schemaTypes != null && schemaTypes.equals("true")) {
            this.useSchemaTypes = true;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param submitMethod __UNDOCUMENTED__
     */
    public void setSubmitMethod(String submitMethod) {
        if ((submitMethod != null) && !submitMethod.equals("")) {
            this.submitMethod = submitMethod;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param wrapperType __UNDOCUMENTED__
     */
    public void setWrapperType(String wrapperType) {
        if ((wrapperType != null)
                && (wrapperType.equalsIgnoreCase("xhtml")
                || wrapperType.equalsIgnoreCase("html"))) {
            wrapper = new XHTMLWrapperElementsBuilder();
        }
    }

    /**
     * sets the base
     *
     * @param base new value for base
     */
    public void setBase(String base) {
        if (base != null && !base.equals("")) {
            this.base = base;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @throws BuildException __UNDOCUMENTED__
     */
    public void execute() throws BuildException {
        //System.out.println("DEBUG: stylesheet=" + stylesheet);

        BaseSchemaFormBuilder builder;

        if (instance != null) {
            builder =
                    new BaseSchemaFormBuilder(rootTagName,
                            instance,
                            action,
                            submitMethod,
                            wrapper,
                            stylesheet,
                            base,
                            useSchemaTypes);
        } else {
            builder =
                    new BaseSchemaFormBuilder(rootTagName,
                            instanceHref,
                            action,
                            submitMethod,
                            wrapper,
                            stylesheet,
                            base,
                            useSchemaTypes);
        }

        Document out;
        //System.out.println(rootTagName);

        try {
            out = builder.buildForm(inputURI);
        } catch (FormBuilderException e) {
            throw new BuildException(e);
        }

        boolean writeToScreen = (outputFile == null);

        if (!writeToScreen) {
            try {
                FileOutputStream fout = new FileOutputStream(outputFile);
                DOMUtil.prettyPrintDOM(out.getDocumentElement(), fout);
                //System.out.println("file written in " + outputFile.getAbsolutePath());
            } catch (java.io.FileNotFoundException ex) {
                ex.printStackTrace();
                writeToScreen = true;
            } catch (javax.xml.transform.TransformerException ex) {
                ex.printStackTrace();
                writeToScreen = true;
            }
        }

        if (writeToScreen) {
            System.out.println("Warning: no output file");
            DOMUtil.prettyPrintDOM(out.getDocumentElement());
        }
    }
}
