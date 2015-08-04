/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.beanutils.BeanComparator;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Vector;

/**
 *
 * This class provides helper methods to perform common DOM related tasks.
 *
 * @author Mike Fowler
 *         Date: Oct 25, 2005
 */
public class DOMUtil
{
    private static final String EMPTY_ELEMENT_REMOVAL_XSLT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                             "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n" +
                                                             "    <xsl:template match=\"@*|*\">\n" +
                                                             "        <xsl:copy>\n" +
                                                             "            <xsl:apply-templates select=\"@*|*|text()\"/>\n" +
                                                             "        </xsl:copy>\n" +
                                                             "    </xsl:template>\n" +
                                                             "\n" +
                                                             "    <xsl:template match=\"*[not(.//text())]\"/>\n" +
                                                             "</xsl:stylesheet> ";
    private static Transformer emptyElementXSLT = null;


    /**
     * Converts a xml String to a DOM Document
     *
     * @param xml to be parsed
     * @return DOM Document representation of XML.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document getDocumentFromString(String xml) throws ParserConfigurationException, SAXException,
                                                                    IOException
    {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /**
     * Creates a new, namespace aware, Document node
     *
     * @return the created DOM Document, otherwise return null
     */
    public static Document createDocumentInstance() throws ParserConfigurationException
    {
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        builder.setNamespaceAware(true);
        return builder.newDocumentBuilder().newDocument();
    }

    /**
     * Creates a new, namespace aware, Document node
     *
     * @return the created DOM Document, otherwise return null
     */
    public static Document createNamespacelessDocumentInstance() throws ParserConfigurationException
    {
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        builder.setNamespaceAware(true);
        return builder.newDocumentBuilder().newDocument();
    }

    public static Document getNamespacelessDocumentFromDocument(Document dom) throws TransformerException, IOException, ParserConfigurationException, SAXException
    {
        return getNamespacelessDocumentFromString(getXMLStringFragmentFromNode(dom));
    }

    public static Document getNamespacelessDocumentFromString(String xml) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /**
     * Extracts to text from the supplied node and it's children.
     *
     * @param node to extract text from.
     * @return String representation of the node text.
     */
    public static String getNodeText(Node node)
    {

        if (node == null || !node.hasChildNodes()) return "";
        StringBuilder result = new StringBuilder();

        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
        {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE)
            {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() ==
                     Node.CDATA_SECTION_NODE)
            {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() ==
                     Node.ENTITY_REFERENCE_NODE)
            {
                // Recurse into the subtree for text
                // (and ignore comments)
                result.append(getNodeText(subnode));
            }
        }
        return result.toString();
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     * The xml header is omitted.
     *
     * @param node to convert to a String
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node) throws TransformerException
    {
        return getXMLStringFragmentFromNode(node, true);
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     * The xml header is omitted.
     *
     * @param node to convert to a String
     * @param encoding Target encoding of output XML
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node, String encoding) throws TransformerException
    {
        return getXMLStringFragmentFromNode(node, false, encoding);
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     *
     * @param node to convert to a String
     * @param omitDeclaration set to false to include the &lt;? xml ?&gt; declaration
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node, boolean omitDeclaration) throws TransformerException
    {
        return getXMLStringFragmentFromNode(node, omitDeclaration, "UTF-8");
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     *
     * @param node to convert to a String
     * @param omitDeclaration set to false to include the &lt;? xml ?&gt; declaration
     * @param encoding Target encoding of output XML
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node, boolean omitDeclaration, String encoding) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(node);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.setOutputProperty("indent", "yes");
        if (omitDeclaration) transformer.setOutputProperty("omit-xml-declaration", "yes");
        else transformer.setOutputProperty("omit-xml-declaration", "no");
        transformer.transform(source, new StreamResult(baos));

        try
        {
            return baos.toString(encoding);
        }
        catch (UnsupportedEncodingException e) //I would prefer to propagate the exception, but it breaks to many contracts
        {
            return baos.toString();
        }
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     *
     * @param node to convert to a String
     * @param omitDeclaration set to false to include the &lt;? xml ?&gt; declaration
     * @param collapseEmptyTags set to false to use the long form of xml tags (ie. &lt;a&gt;&lt;/a&gt;)
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node, boolean omitDeclaration, boolean collapseEmptyTags) throws TransformerException
    {
        return getXMLStringFragmentFromNode(node, omitDeclaration, "UTF-8", collapseEmptyTags);
    }

    /**
     * Takes the supplied node as the root of an xml document and converts it to a String representation.
     *
     * @param node to convert to a String
     * @param omitDeclaration set to false to include the &lt;? xml ?&gt; declaration
     * @param encoding Target encoding of output XML
     * @param collapseEmptyTags set to false to use the long form of xml tags (ie. &lt;a&gt;&lt;/a&gt;)
     * @return String XML fragment.
     * @throws TransformerException
     */
    public static String getXMLStringFragmentFromNode(Node node, boolean omitDeclaration, String encoding, boolean collapseEmptyTags) throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(node);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(!collapseEmptyTags) transformer.setOutputProperty("method", "xhtml");
        transformer.setOutputProperty("indent", "yes");
        if (omitDeclaration) transformer.setOutputProperty("omit-xml-declaration", "yes");
        else transformer.setOutputProperty("omit-xml-declaration", "no");
        transformer.transform(source, new StreamResult(baos));

        try
        {
            return baos.toString(encoding);
        }
        catch (UnsupportedEncodingException e) //I would prefer to propagate the exception, but it breaks to many contracts
        {
            return baos.toString();
        }
    }

    public static Document removeEmptyNodes(Node node) throws IOException, TransformerException, SAXException, ParserConfigurationException {

            if(emptyElementXSLT == null)
            {
                ByteArrayInputStream bais = new ByteArrayInputStream(EMPTY_ELEMENT_REMOVAL_XSLT.getBytes("UTF-8"));
                StreamSource stream = new StreamSource(bais);
                emptyElementXSLT = TransformerFactoryImpl.newInstance().newTransformer(stream);

            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DOMSource source = new DOMSource(node);



            emptyElementXSLT.transform(source, new StreamResult(baos));


            Document dom = getDocumentFromString(baos.toString("UTF-8"));


            return dom;


    }

    /**
     * Takes the supplied node and recursively removes empty (no content/child nodes) elements
     *
     * @param node to remove all empty elements from
     * @return Trimmed node
     * @deprecated
     * @throws XPathExpressionException
     */
    public static Node removeEmptyElements(Node node) throws XPathExpressionException
    {



        NodeList list = selectNodeList(node, "*[string-length(normalize-space(.)) = 0]");

        for (int i = 0; i < list.getLength(); i++)
        {
            node.removeChild(list.item(i));
        }



        if(node.hasChildNodes())
        {
            NodeList childs = node.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++)
            {
                if(childs.item(i) instanceof Element)
                {
                    removeEmptyElements(childs.item(i));
                }
            }
        }


        return node;
    }

    public static Node selectSingleNode(Node node, String expression) throws XPathExpressionException
    {
        XPathFactory factory = XPathFactory.newInstance();
        XPath path = factory.newXPath();
        Object result = path.evaluate(expression, node, XPathConstants.NODE);
        return (Node) result;
    }

    public static String selectNodeText(Node node, String expression) throws XPathExpressionException
    {
        XPathFactory factory = XPathFactory.newInstance();
        XPath path = factory.newXPath();
        Object result = path.evaluate(expression, node, XPathConstants.NODE);
        return getNodeText((Node)result);
    }

    public static NodeList selectNodeList(Node node, String expression) throws XPathExpressionException
    {
        XPathFactory factory = XPathFactory.newInstance();
        XPath path = factory.newXPath();
        Object result = path.evaluate(expression, node, XPathConstants.NODESET);
        return (NodeList) result;
    }

    /**
     * Formats a string of XML suitable for standard display (ie. no xml directive and idented). If the
     * formatting fails, the original XML string is returned unaltered.
     *
     * @param xml to format for display
     * @return formatted xml if no exceptions occured, otherwise original string.
     */
    public static String formatXMLStringForDisplay(String xml)
    {
        try
        {
            return getXMLStringFragmentFromNode(getDocumentFromString(xml));
        }
        catch (Exception e)
        {
            return xml;
        }
    }

    /**
     * Formats a string of XML suitable for standard display (ie. no xml directive and idented). If the
     * formatting fails, the original XML string is returned unaltered.
     *
     * @param xml to format for display
     * @return formatted xml if no exceptions occured, otherwise original string.
     */
    public static String formatXMLStringForDisplay(String xml, boolean omitDeclaration)
    {
        try
        {
            return getXMLStringFragmentFromNode(getDocumentFromString(xml), omitDeclaration);
        }
        catch (Exception e)
        {
            return xml;
        }
    }

    /**
     * Removes all child nodes from the context Node node.
     * @param node to remove all children from
     */
    public static void removeAllChildNodes(Node node)
    {
        NodeList children = node.getChildNodes();
        for (int j = children.getLength() - 1; j >= 0; node.removeChild(children.item(j)), j--) ;
    }

    public static void removeAllAttributes(Element element)
    {
        Vector<String> names = new Vector<String>();

        int length = element.getAttributes().getLength();
        NamedNodeMap atts = element.getAttributes();

        for(int i = 0; i < length; names.add(atts.item(i).getLocalName()), i++);

        for(String name : names) element.removeAttribute(name);
    }

    /**
     * Converts a XML String into an Input source with UTF-8 encoding
     * @param xml
     * @return an instantiated InputSource
     * @throws UnsupportedEncodingException
     */
    public static InputSource createUTF8InputSource(String xml) throws UnsupportedEncodingException
    {
        return new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /**
     * Converts a Document dom into an Input source with UTF-8 encoding
     * @param node
     * @return an instantiated InputSource
     * @throws UnsupportedEncodingException
     * @throws TransformerException
     */
    public static InputSource createUTF8InputSource(Node node) throws UnsupportedEncodingException, TransformerException
    {
        return createUTF8InputSource(DOMUtil.getXMLStringFragmentFromNode(node));
    }

    /**
     * Alphabetises the top level children of the node root.
     * @param root
     * @return The alphabetised root node.
     */
    public static Node alphabetiseChildNodes(Node root) throws XPathExpressionException
    {
        root = removeEmptyElements(root);
        NodeList list = selectNodeList(root, "child::*");
        removeAllChildNodes(root);

        if(list.getLength() > 0)
        {
            Vector<Node> nodes = new Vector<Node>(list.getLength());

            for(int i = 0; i < list.getLength(); nodes.add(list.item(i)), i++);

            Collections.sort(nodes, new BeanComparator("localName"));

            for(Node node : nodes)
            {
                root.appendChild(node);
            }
        }

        return root;
    }
}
