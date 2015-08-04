/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;

import java.io.StringWriter;
import java.util.List;

/**
 * @author Michael Adams
 * @date 10/07/2008
 */

public class SaxonUtil {

    private static final Processor _processor = new Processor(false);
    private static final Serializer _output = new Serializer();
    private static final XQueryCompiler _compiler = _processor.newXQueryCompiler();
    private static final DOMOutputter _domOutputter = new DOMOutputter();

    private static final Logger _log = Logger.getLogger(SaxonUtil.class);

    static {
        _compiler.setErrorListener(new SaxonErrorListener());
    }


    /**
     * Evaluates an XQuery against a data document
     * @param query the XQuery to evaluate
     * @param dataDoc a JDOM Document containing the data tree
     * @return an XML String representing the result of the evaluation
     * @throws SaxonApiException if there's a problem with the XQuery or Document
     */
    public static String evaluateQuery(String query, Document dataDoc)
            throws SaxonApiException {
        log(query, dataDoc);

        // initialise, compile & load the evaluator
        XQueryEvaluator evaluator = initEvaluator(query, dataDoc);

        // create a StringWriter to receive the output of the evaluation
        StringWriter writer = new StringWriter();
        _output.setOutputWriter(writer);

        // evaluate the query & return the result as a string
        evaluator.run(_output);
        String result = writer.toString();
        log(result, null);
        return removeHeader(result);
    }


    /**
     * Evaluates an XQuery against a data document
     * @param query the XQuery to evaluate
     * @param dataDoc a JDOM Document containing the data tree
     * @return a JDOM Element representing the result of the evaluation
     * @throws SaxonApiException if there's a problem with the XQuery or Document
     */
    public static Element evaluateTreeQuery(String query, Document dataDoc)
            throws SaxonApiException {
        return JDOMUtil.stringToElement(evaluateQuery(query, dataDoc));
    }


    /**
     * Evaluates an XQuery against a data document
     * @param query the XQuery to evaluate
     * @param dataElem a JDOM Element containing the data tree
     * @return a List containing the Element(s) resulting from the evaluation
     * @throws SaxonApiException if there's a problem with the XQuery or Element
     */
    public static List<Content> evaluateListQuery(String query, Element dataElem)
            throws SaxonApiException {

        // put the element in a jdom document
        Document dataDoc = new Document(dataElem.clone());
        String result = evaluateQuery(query, dataDoc);

        // use the string result to create a doc to get it expressed as an element list
        Document resultDoc = JDOMUtil.stringToDocument(StringUtil.wrap(result, "root"));
        return resultDoc.getRootElement().cloneContent();
    }


    /**
     * Compiles an XQuery so that it can be executed
     * @param query the XQuery to compile
     * @return the executable query
     * @throws SaxonApiException if there's a problem with the XQuery
     */
    public static XQueryExecutable compileXQuery(String query)
            throws SaxonApiException {
        ((SaxonErrorListener) _compiler.getErrorListener()).reset();
        return _compiler.compile(query);
    }

    public static List<String> getCompilerMessages() {
        return ((SaxonErrorListener) _compiler.getErrorListener()).getAllMessages();
    }


    /******************************************************************************/

    private static XQueryEvaluator initEvaluator(String query, Document dataDoc)
            throws SaxonApiException {

        // wrap the jdom doc into something saxon understands
        org.w3c.dom.Document domDoc = toDOM(dataDoc);

        // compile & load query
        XQueryExecutable executable = compileXQuery(query);
        XQueryEvaluator evaluator = executable.load();

        // set the context to the data document
        evaluator.setContextItem(_processor.newDocumentBuilder().wrap(domDoc));
        return evaluator ;
    }


    private static org.w3c.dom.Document toDOM(Document doc) throws SaxonApiException {
        try {
            return _domOutputter.output(doc);
        }
        catch (JDOMException jde) {
            throw new SaxonApiException("Failed to translate JDOM Document for processing.");
        }
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


    private static void log(String query, Document doc) {
        if (_log.isInfoEnabled()) {
            if (doc != null)
                _log.info("Evaluating query: '" + query + "' ...using data document:" +
                        JDOMUtil.documentToStringDump(doc));
            else _log.info("Query result: " + query);
        }
    }
}

