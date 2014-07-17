/*
 * $Id: XMLStyleConstants.java,v 1.3 2009/01/22 22:14:59 edankert Exp $
 *
 * Copyright (c) 2002 - 2009, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.text.xml;

/**
 * The contants used for the XML editor.
 * 
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.3 $, $Date: 2009/01/22 22:14:59 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public interface XMLStyleConstants {
    /** The style constant for element name */
    public static final String ELEMENT_NAME     = "element-name";
    /** The style constant for element prefix */
    public static final String ELEMENT_PREFIX   = "element-prefix";
    /** The style constant for element value */
    public static final String ELEMENT_VALUE    = "element-value";

    /** The style constant for attribute name */
    public static final String ATTRIBUTE_NAME   = "attribute-name";
    /** The style constant for attribute prefix */
    public static final String ATTRIBUTE_PREFIX = "attribute-prefix";
    /** The style constant for attribute value */
    public static final String ATTRIBUTE_VALUE  = "attribute-value";

    /** The style constant for namespace name*/
    public static final String NAMESPACE_NAME   = "namespace-name";
    /** The style constant for namespace prefix */
    public static final String NAMESPACE_PREFIX = "namespace-prefix";
    /** The style constant for namespace value */
    public static final String NAMESPACE_VALUE  = "namespace-value";

    /** The style constant for entity */
    public static final String ENTITY           = "Entity";
    /** The style constant for comment */
    public static final String COMMENT          = "Comment";
    /** The style constant for cdata */
    public static final String CDATA          	= "CDATA";
    /** The style constant for declaration */
    public static final String DECLARATION      = "Declaration";

    /** The style constant for special */
    public static final String SPECIAL          = "Special";
    /** The style constant for string */
    public static final String STRING           = "String";

    public static final String WHITESPACE       = "Whitespace";

    /** The style constant for entity */
    public static final String ENTITY_REFERENCE = "EntityReference";
}
