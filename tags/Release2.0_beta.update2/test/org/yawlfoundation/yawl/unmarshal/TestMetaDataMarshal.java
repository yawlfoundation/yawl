/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.unmarshal;

import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.textui.TestRunner;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * 
 * @author Lachlan Aldred
 * Date: 4/08/2005
 * Time: 08:27:49
 * 
 */
public class TestMetaDataMarshal extends TestCase{
    private YMetaData metaData;

    public void setUp(){
        metaData = new YMetaData();
        metaData.setContributor("Lachlan Aldred");
        metaData.setContributor("Arthur ter Hofstede");
        metaData.setContributor("Lindsay Bradford");
        metaData.setContributor("Guy Redding");
        metaData.setCoverage("covers this example test");
        metaData.setCreated(new Date());
        metaData.setCreator("Peter Pan");
        metaData.setDescription("This tests the metadata class");
        metaData.setStatus("This is not production class meta data");
        metaData.setSubject("testing");
        metaData.setSubject("and more testing");
        metaData.setTitle("Meta Data Test");
        metaData.setValidFrom(new Date());
        metaData.setValidUntil(new Date(new Date().getTime() + 86400000));
        metaData.setVersion(1.1);
    }

    public void testToXML(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("<metaData>" +
                "<title>Meta Data Test</title>" +
                "<creator>Peter Pan</creator>" +
                "<subject>testing</subject>" +
                "<subject>and more testing</subject>" +
                "<description>This tests the metadata class</description>" +
                "<contributor>Guy Redding</contributor>" +
                "<contributor>Lindsay Bradford</contributor>" +
                "<contributor>Arthur ter Hofstede</contributor>" +
                "<contributor>Lachlan Aldred</contributor>" +
                "<coverage>covers this example test</coverage>" +
                "<validFrom>"+df.format(new Date())+"</validFrom>" +
                "<validUntil>"+df.format(new Date(new Date().getTime() + 86400000))+"</validUntil>" +
                "<created>"+df.format(new Date())+"</created>" +
                "<version>1.1.1</version>" +
                "<status>This is not production class meta data</status>" +
                "</metaData>",
                metaData.toXML());
    }

    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestMetaDataMarshal.class);
        return suite;
    }
}
