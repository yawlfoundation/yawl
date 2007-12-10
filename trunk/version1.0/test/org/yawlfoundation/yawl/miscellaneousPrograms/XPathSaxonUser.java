/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryProcessor;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.xpath.XPathException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * 
 * @author Lachlan Aldred
 * Date: 11/02/2004
 * Time: 15:34:41
 * 
 */
public class XPathSaxonUser {
    public static void main(String[] args) {

        net.sf.saxon.Configuration config = new Configuration();
        net.sf.saxon.query.StaticQueryContext context = new net.sf.saxon.query.StaticQueryContext();
        QueryProcessor qp = new QueryProcessor(config, context);
        try {
            XQueryExpression exp = qp.compileQuery("generate-id(/bye_mum/hi_there)");

            DocumentInfo doc = qp.buildDocument(new StreamSource(new StringReader(
                    "<bye_mum inf='h'><hi_there/></bye_mum>")));
            DynamicQueryContext dynamicQueryContext = new DynamicQueryContext();
            dynamicQueryContext.setContextNode(doc);
            Object o = exp.evaluateSingle(dynamicQueryContext);
System.out.println("o = " + o);
        } catch (XPathException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
}
