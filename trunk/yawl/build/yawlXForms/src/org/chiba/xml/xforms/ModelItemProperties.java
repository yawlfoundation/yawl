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
package org.chiba.xml.xforms;

/**
 * Instance data item objects are annotations to the DOM nodes which represent
 * the instance data. State information resulting from such operations as
 * <code>recalculate</code> and <code>revalidate</code> will be stored here.
 * UI classes use the computed properties to fetch the current status of the
 * bound item.
 * <p/>
 * ModelItemProperties are contributions to the infoset of a instance data node.
 * Each of this contributions like readonly, relevant, required, constraint is a
 * ModelItemProperty.
 *
 * @version $Id: ModelItemProperties.java,v 1.7 2004/08/15 14:14:18 joernt Exp $
 */
public interface ModelItemProperties {

    // lexical properties

    /**
     * Returns the <code>type</code> model item property.
     *
     * @return the <code>type</code> model item property.
     */
    String getDatatype();

    /**
     * Returns the <code>readonly</code> model item property.
     *
     * @return the <code>readonly</code> model item property.
     */
    String getReadonly();

    /**
     * Returns the <code>required</code> model item property.
     *
     * @return the <code>required</code> model item property.
     */
    String getRequired();

    /**
     * Returns the <code>relevant</code> model item property.
     *
     * @return the <code>relevant</code> model item property.
     */
    String getRelevant();

    /**
     * Returns the <code>calculate</code> model item property.
     *
     * @return the <code>calculate</code> model item property.
     */
    String getCalculate();

    /**
     * Returns the <code>constraint</code> model item property.
     *
     * @return the <code>constraint</code> model item property.
     */
    String getConstraint();

    /**
     * Returns the <code>p3ptype</code> model item property.
     *
     * @return the <code>p3ptype</code> model item property.
     */
    String getP3PType();


    // computed properties

    /**
     * Computes the <code>enabled</code> state of the associated model item.
     *
     * @return <code>true</code> if the associated model item is currently enabled,
     *         otherwise <code>false</code>.
     */
    boolean isEnabled();

    /**
     * Computes the <code>readonly</code> state of the associated model item.
     *
     * @return <code>true</code> if the associated model item is currently readonly,
     *         otherwise <code>false</code>.
     */
    boolean isReadonly();

    /**
     * Computes the <code>required</code> state of the associated model item.
     *
     * @return <code>true</code> if the associated model item is currently required,
     *         otherwise <code>false</code>.
     */
    boolean isRequired();

    /**
     * Computes the <code>valid</code> state of the associated model item.
     *
     * @return <code>true</code> if the associated model item is currently valid,
     *         otherwise <code>false</code>.
     */
    boolean isValid();


    // model item access

    /**
     * Sets the value of the associated model item.
     *
     * @param value the value of the associated model item.
     */
    void setValue(String value);

    /**
     * Returns the value of the associated model item.
     *
     * @return the value of the associated model item.
     */
    String getValue();

    /**
     * Returns the associated model item node.
     *
     * @return the associated model item node.
     */
    Object getNode();


    // change listeners

    /**
     * Registers the specified listener with the associated model item.
     *
     * @param listener the model item listener.
     */
    void register(ModelItemListener listener);

    /**
     * Deregisters the specified listener with the associated model item.
     *
     * @param listener the model item listener.
     */
    void deregister(ModelItemListener listener);

    /**
     * Notifies all registered model item listeners about any computed
     * model item property changes.
     */
    void notifyListeners();

    // file upload fixes
    void setFileName(String filename);

    String getFilename();

    void setMediatype(String mediatype);

    String getMediatype();

}

// end of interface
