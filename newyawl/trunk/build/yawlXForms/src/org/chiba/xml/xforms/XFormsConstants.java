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
 * XForms Constants used throughout the Chiba Framework.
 *
 * @version $Id: XFormsConstants.java,v 1.9 2004/12/01 13:14:34 unl Exp $
 */
public interface XFormsConstants {
    String ACCESSKEY_ATTRIBUTE = "accesskey";

    // actions
    String ACTION = "action";

    // submission attributes
    String ACTION_ATTRIBUTE = "action";
    String ALERT = "alert";
    String APPEARANCE_ATTRIBUTE = "appearance";
    String AT_ATTRIBUTE = "at";
    String BIND = "bind";
    String BIND_ATTRIBUTE = "bind";
    String BUBBLES_ATTRIBUTE = "bubbles";
    String CALCULATE_ATTRIBUTE = "calculate";
    String CANCELABLE_ATTRIBUTE = "cancelable";
    String CASE = "case";
    String CASE_ATTRIBUTE = "case";
    String CDATA_SECTION_ELEMENTS_ATTRIBUTE = "cdata-section-elements";

    // common selection elements
    String CHOICES = "choices";
    String CONSTRAINT_ATTRIBUTE = "constraint";
    String CONTROL_ATTRIBUTE = "control";
    String COPY = "copy";
    String DELETE = "delete";
    String DISPATCH = "dispatch";
    String ENCODING_ATTRIBUTE = "encoding";
    String END_ATTRIBUTE = "end";

    // extension
    String EXTENSION = "extension";

    // additional elements
    String FILENAME = "filename";

    // ui
    String GROUP = "group";
    String HELP = "help";
    String HINT = "hint";
    String INCLUDENAMESPACEPREFIXES_ATTRIBUTE = "includenamespaceprefixes";
    String INCREMENTAL_ATTRIBUTE = "incremental";
    String INDENT_ATTRIBUTE = "indent";
    String INDEX_ATTRIBUTE = "index";

    // form controls
    String INPUT = "input";
    String INSERT = "insert";
    String INSTANCE = "instance";
    String ITEM = "item";
    String ITEMSET = "itemset";
    String LABEL = "label";
    String LEVEL_ATTRIBUTE = "level";
    String LOAD = "load";
    String MAXOCCURS_ATTRIBUTE = "maxoccurs";
    String MEDIATYPE = "mediatype";
    String MEDIATYPE_ATTRIBUTE = "mediatype";
    String MESSAGE = "message";
    String METHOD_ATTRIBUTE = "method";
    String MINOCCURS_ATTRIBUTE = "minoccurs";

    // core elements
    String MODEL = "model";
    String MODEL_ATTRIBUTE = "model";
    String FUNCTIONS="functions";

    // action attributes
    String NAME_ATTRIBUTE = "name";

    // ui attributes
    String NAVINDEX_ATTRIBUTE = "navindex";
    String NODESET_ATTRIBUTE = "nodeset";
    String NUMBER_ATTRIBUTE = "number";
    String OMIT_XML_DECLARATION_ATTRIBUTE = "omit-xml-declaration";
    String OUTPUT = "output";
    String P3PTYPE_ATTRIBUTE = "p3ptype";
    String POSITION_ATTRIBUTE = "position";
    String RANGE = "range";
    String READONLY_ATTRIBUTE = "readonly";
    String REBUILD = "rebuild";
    String RECALCULATE = "recalculate";
    String REFRESH = "refresh";
    String REF_ATTRIBUTE = "ref";
    String RELEVANT_ATTRIBUTE = "relevant";
    String REPEAT = "repeat";
    String REPEAT_ATTRIBUTE = "repeat";
    String REPEAT_BIND_ATTRIBUTE = "repeat-bind";
    String REPEAT_MODEL_ATTRIBUTE = "repeat-model";
    String REPEAT_NODESET_ATTRIBUTE = "repeat-nodeset";
    String REPEAT_NUMBER_ATTRIBUTE = "repeat-number";
    String REPEAT_STARTINDEX_ATTRIBUTE = "repeat-startindex";
    String REPLACE_ATTRIBUTE = "replace";
    String REQUIRED_ATTRIBUTE = "required";
    String RESET = "reset";
    String RESOURCE_ATTRIBUTE = "resource";
    String REVALIDATE = "revalidate";
    String SECRET = "secret";
    String SELECT = "select";
    String SELECT1 = "select1";
    String SELECTED_ATTRIBUTE = "selected";
    String SELECTION_ATTRIBUTE = "selection";
    String SEND = "send";
    String SEPARATOR_ATTRIBUTE = "separator";
    String SETFOCUS = "setfocus";
    String SETINDEX = "setindex";
    String SETVALUE = "setvalue";
    String SHOW_ATTRIBUTE = "show";

    // common attributes
    String SRC_ATTRIBUTE = "src";
    String STANDALONE_ATTRIBUTE = "standalone";
    String STARTINDEX_ATTRIBUTE = "startindex";
    String START_ATTRIBUTE = "start";
    String STEP_ATTRIBUTE = "step";
    String SUBMISSION = "submission";
    String SUBMISSION_ATTRIBUTE = "submission";
    String SUBMIT = "submit";
    String SWITCH = "switch";
    String TARGET_ATTRIBUTE = "target";
    String TEXTAREA = "textarea";
    String TOGGLE = "toggle";
    String TRIGGER = "trigger";

    // bind attributes
    String TYPE_ATTRIBUTE = "type";
    String UPLOAD = "upload";
    String VALUE = "value";
    String VALUE_ATTRIBUTE = "value";
    String VERSION_ATTRIBUTE = "version";
}

//end of interface

