package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Persistence transport for a worklet specification
 *
 * @author Michael Adams
 * @date 10/09/15
 */
public class WorkletSpecification {

    private String _key;                        // for hibernate only
    private YSpecificationID _specID;
    private String _xml;


    public WorkletSpecification() { }

    public WorkletSpecification(String xml) {
        if (xml != null) {
            _specID = extractSpecID(xml);
            _xml = xml;
        }
    }


    public WorkletSpecification(YSpecificationID specID, String xml) {
        _specID = specID;
        _xml = xml;
    }


    public YSpecificationID getSpecID() { return _specID; }

    public void setSpecID(YSpecificationID specID) { _specID = specID; }

    public String getXML() { return _xml; }

    public void setXML(String xml) { _xml = xml; }

    public String getName() { return _specID != null ? _specID.getUri() : null; }


    // the key is the spec UID for post 2.0 specs, and the uri for pre 2.0 specs
    // the assumption is that the latest spec version should always be used, so
    // only the latest is actually persisted
    public String getKey() { return _specID != null ? _specID.getKey() : null; }


    private void setKey(String key) { /* no action needed, for hibernate only*/ }


    protected YSpecificationID extractSpecID(String xml) {
        YSpecificationID specID = null;
        Document doc = JDOMUtil.stringToDocument(xml);
        if (doc != null) {
            Element root = doc.getRootElement();
            if (root != null) {
                YSchemaVersion schemaVersion = YSchemaVersion.fromString(
                        root.getAttributeValue("version"));
                Namespace ns = root.getNamespace();
                Element spec = root.getChild("specification", ns);
                if (spec != null) {
                    String uri = spec.getAttributeValue("uri");
                    String version = "0.1";
                    String identifier = null;
                    if (! (schemaVersion == null || schemaVersion.isBetaVersion())) {
                        Element metadata = spec.getChild("metaData", ns);
                        if (metadata != null) {
                            version = metadata.getChildText("version", ns);
                            identifier = metadata.getChildText("identifier", ns);
                        }
                    }
                    specID = new YSpecificationID(identifier, version, uri);
                }
            }
        }
        return specID;
    }


}
