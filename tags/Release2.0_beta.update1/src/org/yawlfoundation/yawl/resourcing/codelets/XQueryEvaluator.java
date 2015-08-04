/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation. The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.codelets;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryProcessor;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.xpath.XPathException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.YSaxonOutPutter;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

/**
 * @author: Michael Adams
 * Creation Date: 30/04/2008
 */
public class XQueryEvaluator extends AbstractCodelet {

    public XQueryEvaluator() {
        super();
        setDescription("This codelet executes an XQuery. Required parameters:<br> " +
                       "Input: query (type String, required): the XQuery<br>" +
                       " &nbsp;&nbsp; plus other values used by the XQuery<br>" +
                       "Output: result (type String)");
    }


    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {

        if (outParams == null) {
           String msg = "Cannot perform evaluation: Missing or empty output parameter list.";
           throw new CodeletExecutionException(msg) ;
        }

        this.setInputs(inData, inParams, outParams);

        // convert input vars to a Doc
        Document dataDoc = new Document((Element) inData.clone());

        String query = (String) getParameterValue("query");
        String output = evaluateQuery(query, dataDoc);

        setParameterValue("result", output);
        return getOutputData() ;
    }

    
    // based on YTask.evaluateTreeQuery //
    private String evaluateQuery(String query, Document dataDoc)
               throws CodeletExecutionException {

        String resultingData;

        try {

            // create a processor and compile the query
            Configuration config = new Configuration();
            StaticQueryContext context = new StaticQueryContext();
            QueryProcessor qp = new QueryProcessor(config, context);
            XQueryExpression expression = qp.compileQuery(query);

            // format the data Document
            XMLOutputter outputter = new XMLOutputter();
            DocumentInfo docInfo = qp.buildDocument(new StreamSource(
                                   new StringReader(outputter.outputString(dataDoc))));
            DynamicQueryContext dynamicQueryContext = new DynamicQueryContext();
            dynamicQueryContext.setContextNode(docInfo);

            // evaluate the query
            Object resultObj = expression.evaluateSingle(dynamicQueryContext);
            NodeInfo nodeInfo = (NodeInfo) resultObj;

            // output the result 
            if (nodeInfo != null) {
                YSaxonOutPutter saxonOutputter = new YSaxonOutPutter(nodeInfo);
                resultingData = saxonOutputter.getString();
            }
            else {
                throw new CodeletExecutionException("No data produced for query:" +
                                                     query + ".");
            }
        }
        catch (XPathException e) {
            throw new CodeletExecutionException(
                    "Invalid query: " + query + ".\n" +
                    "Message from parser: [" + e.getMessage() + "]");
        }
        catch (Exception e) {
            throw new CodeletExecutionException(
                    "Exception in query evaluation: " + query + ".");
        }
        return resultingData;
    }
}
