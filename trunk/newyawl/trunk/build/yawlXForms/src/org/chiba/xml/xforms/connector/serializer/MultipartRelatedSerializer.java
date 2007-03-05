package org.chiba.xml.xforms.connector.serializer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.xforms.ModelItem;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.connector.InstanceSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Multipart/Related serializer
 * @author Peter Mikula <peter.mikula@digital-artefacts.fi>
 * @version $Id: MultipartRelatedSerializer.java,v 1.2 2004/09/22 07:49:49 mico Exp $
 */
public class MultipartRelatedSerializer implements InstanceSerializer {
    
    public void serialize(Submission submission, Node node,
        OutputStream outputStream, String encoding) 
        throws Exception 
    {        
	Map cache = new HashMap();
        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart part = new MimeBodyPart();
        multipart.addBodyPart(part);
        
        part.setText(new String(serializeXML(multipart, cache, 
            submission, node, encoding), encoding));
        part.setContentID("<instance.xml@start>");
        part.addHeader("Content-Type", "application/xml");
        part.addHeader("Content-Transfer-Encoding", "base64");
        part.setDisposition("attachment");
        part.setFileName("instance.xml");

        multipart.setSubType("related; start=\"instance.xml@start\"");        
        outputStream.write(new String("Content-Type: " +
            multipart.getContentType() + 
            "\n\nThis is a MIME message.\n").getBytes(encoding));
        multipart.writeTo(outputStream);            
    }
    
    protected byte[] serializeXML(MimeMultipart multipart, Map cache, 
        Submission submission, Node instance, String encoding) throws Exception
    {
        // traverse the tree and replace fileupload
        if (instance instanceof Document) {
            visitNode(cache, ((Document)instance).getDocumentElement(), multipart);
        } else {
            visitNode(cache, instance, multipart);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();        
        XMLSerializer serializer = new XMLSerializer();
        serializer.serialize(submission, instance, stream, encoding);
        return stream.toByteArray();
    }
    
    protected void visitNode(Map cache, Node node, MimeMultipart multipart) throws Exception {
        
        ModelItem item = (ModelItem) ((ElementImpl) node).getUserData();
        if (item != null && item.getDatatype() != null && item.getDatatype().equalsIgnoreCase("anyURI")) {
            String name = item.getFilename();
            if (name == null || item.getValue() == null || item.getValue().equals("")) {
              return;
            }
            
            String cid = (String) cache.get(name);
            if (cid == null) {
                int count = multipart.getCount();
                cid = name+"@part"+(count+1);
                
                MimeBodyPart part = new MimeBodyPart();
                part.setContentID("<"+cid+">");
                
                DataHandler dh = new DataHandler(new ModelItemDataSource(item));
                part.setDataHandler(dh);
                
                part.addHeader("Content-Type", item.getMediatype());
                part.addHeader("Content-Transfer-Encoding", "base64");
                part.setDisposition("attachment");
                part.setFileName(name);
                multipart.addBodyPart(part);
                cache.put(name, cid);                
            }
            
            Element element = (Element) node;
            // remove text node
            NodeList list = node.getChildNodes();
            for(int i=0; i<list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() != Node.TEXT_NODE)
                    continue;
                n.setNodeValue("cid:"+cid);
                break;
            }
        } else {
            NodeList list = node.getChildNodes();
            for(int i=0; i<list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    visitNode(cache, n, multipart);
                }
            }
        }
    }
    
    private class ModelItemDataSource implements DataSource {
        
        private ModelItem item;
        public ModelItemDataSource(ModelItem item) {
            this.item = item;
        }
        
        public String getContentType() {
            String type = item.getMediatype();
            if (type == null) {
                type = "application/octet-stream";
            }
            return type;
        }
        
        public String getName() {
            return item.getFilename();
        }
        
        public InputStream getInputStream() throws IOException {
                        
            byte [] data = null;
            try {
                if (item.getDatatype().equals("anyURI")) {
                    URL url = new URL(item.getValue());
                    return url.openStream();
                } else if (item.getDatatype().equals("base64Binary")) {
                    data = Base64.decodeBase64(item.getValue().getBytes());
                } else if (item.getDatatype().equals("hexBinary")) {
                    data = Hex.decodeHex(item.getValue().toCharArray());
                } else {
                    throw new IOException("unsupported data type");
                }            
            } catch(Exception e) {
                throw new IOException(e.getMessage());
            }
            return new ByteArrayInputStream(data);
        }
        
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("unsupported operation.");
        }        
    }
}

//end of class
