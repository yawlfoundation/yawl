/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.util;

import net.sf.saxon.jdom.DocumentWrapper;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.s9api.*;
import org.jdom.Document;
import org.jdom.Element;

import java.io.StringWriter;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 10/07/2008
 */

public class SaxonUtil {

    private static Processor _processor = new Processor(false);


    public static String evaluateQuery(String query, Document dataDoc)
            throws SaxonApiException {

        // initialise, compile & load the evaluator
        XQueryEvaluator evaluator = initEvaluator(query, dataDoc);

        // create a StringWriter to receive the output of the evaluation
        Serializer output = new Serializer();
        StringWriter writer = new StringWriter();
        output.setOutputWriter(writer);

        // evaluate the query & return the result as a string
        evaluator.run(output);
        return removeHeader(writer.toString());
    }


    public static Element evaluateTreeQuery(String query, Document dataDoc)
            throws SaxonApiException {

        return JDOMUtil.stringToElement(evaluateQuery(query, dataDoc));
    }


    public static List evaluateListQuery(String query, Element data)
            throws SaxonApiException {

        // put the element in a jdom document
        Document dataDoc = new Document((Element) data.clone());
        String result = evaluateQuery(query, dataDoc);

        // use the string result to create a doc to get it expressed as an element list
        Document resultDoc = JDOMUtil.stringToDocument(StringUtil.wrap(result, "root"));
        return resultDoc.getRootElement().cloneContent();
    }


    public static XQueryExecutable compileXQuery(String query)
            throws SaxonApiException {
        return _processor.newXQueryCompiler().compile(query);
    }


    private static XQueryEvaluator initEvaluator(String query, Document dataDoc)
            throws SaxonApiException {

        // wrap the jdom doc into something saxon understands
        DocumentInfo saxonDoc = new DocumentWrapper(dataDoc, "",
                                                _processor.getUnderlyingConfiguration());
        XdmNode xdmDoc = _processor.newDocumentBuilder().wrap(saxonDoc);

        // compile & load query
        XQueryExecutable executable = compileXQuery(query);
        XQueryEvaluator evaluator = executable.load();

        // set the context to the jdom (data) document
        evaluator.setContextItem(xdmDoc);
        return evaluator ;
    }


    private static String removeHeader(String xml) {
        if ((xml != null) && xml.trim().startsWith("<?xml")) {
            int closingPos = xml.indexOf("?>");
            if (closingPos > -1) {
                xml = xml.substring(closingPos + 2);
            }
        }
        return xml;
    }
}

