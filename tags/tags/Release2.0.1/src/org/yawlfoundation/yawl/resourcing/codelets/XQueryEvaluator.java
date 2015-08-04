/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation. The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.codelets;

import net.sf.saxon.s9api.SaxonApiException;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.SaxonUtil;

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


    private String evaluateQuery(String query, Document dataDoc)
               throws CodeletExecutionException {

        try {
            String result = SaxonUtil.evaluateQuery(query, dataDoc);
            if (result != null)
                return result;
            else
                throw new CodeletExecutionException("No data produced for query: '" +
                                                     query + "'.");
        }
        catch (SaxonApiException sapie) {
            throw new CodeletExecutionException(
                    "Invalid query: '" + query + "'.\n" +
                    "Message from parser: [" + sapie.getMessage() + "]");
        }
        catch (Exception e) {
            throw new CodeletExecutionException(
                    "Exception in query evaluation: '" + query + "'.");
        }
    }
}
