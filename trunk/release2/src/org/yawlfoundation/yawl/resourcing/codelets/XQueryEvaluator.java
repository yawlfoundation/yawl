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
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.util.YSaxonOutPutter;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 30/04/2008
 */
public class XQueryEvaluator {

    private Element _inputs ;
    private Element _outData ;
    private List<YParameter> _outputs ;


    public XQueryEvaluator() { }

    public XQueryEvaluator(Element inputs, List<YParameter> outputs, Element outData) {
        _inputs = inputs ;
        _outputs = outputs ;
        _outData = outData ;
    }

    public void setInputData(Element inputs) { _inputs = inputs ; }

    public void setOutputParams(List<YParameter> outputs) { _outputs = outputs ; }

    public void setOutData(Element out) { _outData = out ; }


    public Element evaluate() throws CodeletExecutionException,
                                     YStateException, YQueryException {
        if (_outputs == null) {
           String msg = "Cannot perform evaluation: Missing or empty output parameter list.";
           throw new CodeletExecutionException(msg) ;
        }

        // convert input vars to a Doc
        Document dataDoc = new Document((Element) _inputs.clone());
        
        // for each wir.output params, do evalQuery & add it to the result
        for (YParameter param : _outputs) {
            String name = param.getName();
            String query = param.getInitialValue() ;
            Element output = evaluateQuery(query, dataDoc);
            if (output != null)
                _outData.addContent((Element) output.clone()) ;
        }

        return _outData ;
    }

    
    // based on YTask.evaluateTreeQuery //
    private Element evaluateQuery(String query, Document dataDoc)
               throws CodeletExecutionException, YStateException, YQueryException {

        Element resultingData;

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
                String result = saxonOutputter.getString();
                SAXBuilder builder = new SAXBuilder();
                Document doclet = builder.build(new StringReader(result));
                resultingData = doclet.detachRootElement();
            }
            else {
                throw new CodeletExecutionException("No data produced.");
            }
        }
        catch (XPathException e) {
            YQueryException yqe = new YQueryException(
                    "Invalid query: " + query + ".\n" +
                    "Message from parser: [" + e.getMessage() + "]");
            yqe.setStackTrace(e.getStackTrace());
            throw yqe;
        }
        catch (Exception e) {
            if (e instanceof CodeletExecutionException)
                throw (CodeletExecutionException) e;
            
            YStateException se = new YStateException(e.getMessage());
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
        return resultingData;
    }
}
