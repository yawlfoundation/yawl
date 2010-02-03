package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * The unique identifier of a specification.
 *
 * NOTE: For schema versions prior to 2.0, the spec's uri was used as its identifier, but
 *       since a user-defined uri cannot guarantee uniqueness, the identifier field was
 *       introduced for v2.0 (which will theoretically always be unique). Specification
 *       versioning was also introduced in v2.0. Therefore, to handle specifications of
 *       different schema versions:
 *       - all pre-2.0 schema based specifications are given a default version of '0.1'
 *       - all pre-2.0 schema based specifications will have a null 'identifier' field
 *         and the 'uri' field will be used to 'uniquely' identify the specification
 *       - all 2.0 and later schema based specifications will use the 'identifier' field
 *         to uniquely identify a specification-version family (all versions of a
 *         specification have the same identifier).
 *
 *      The getKey method is used to determine which of 'identifier' or 'uri' is used as
 *      the unique identifier.
 *
 * @author Mike Fowler
 *         Date: 05-Sep-2006
 *
 * @author Michael Adams 08-09 heavily modified for versions 2.0 - 2.1
 */

public class YSpecificationID implements Comparable<YSpecificationID> {

    private String identifier;                         // a system generated UUID string
    private YSpecVersion version;
    private String uri;                                // the user-defined name


    public YSpecificationID() { }                      // for persistence

    public YSpecificationID(String identifier, YSpecVersion version, String uri) {
        this.identifier = identifier;
        this.version = (version != null) ? version : new YSpecVersion("0.1");
        this.uri = uri;
    }

    public YSpecificationID(String identifier, String version, String uri) {
        this(identifier, new YSpecVersion(version), uri);
    }

    public YSpecificationID(WorkItemRecord wir) {
        this(wir.getSpecIdentifier(), wir.getSpecVersion(), wir.getSpecURI());
    }

    // default constructor for pre-2.0 specs
    public YSpecificationID(String uri) {
        this(null, new YSpecVersion("0.1"), uri) ;
    }



    public String getIdentifier() { return identifier; }

    public String getVersionAsString() { return version.toString(); }

    public YSpecVersion getVersion() { return version; }

    public String getUri() { return uri; }

    public String getKey() { return (identifier != null) ? identifier : uri ; }


    public void setIdentifier(String identifier) { this.identifier = identifier ; }

    public void setVersion(String ver) { version.setVersion(ver) ; }

    public void setVersion(YSpecVersion ver) { version = ver ; }

    public void setUri(String n) { uri = n; }
    

    @Override public boolean equals(Object obj) {
        if (obj instanceof YSpecificationID) {
            YSpecificationID id = (YSpecificationID) obj;

            return id.getIdentifier().equals(identifier) &&
                   id.getVersion().equals(version) &&
                   id.getUri().equals(uri) ;
        }
        else {
            return false;
        }
    }


    @Override public String toString() {
        return uri + " - version " + version.toString();
    }


    public int compareTo(YSpecificationID o) {
        String key = getKey();
        if (key.equals(o.getKey())) {
            return version.compareTo(o.getVersion());
        }
        else return o.getKey().compareTo(key);
    }

    
    // utility method for bundling up specIDs for passing across the interfaces
    public Map<String,String> toMap() {
        Map<String,String> result = new HashMap<String, String>();
        result.put("specidentifier", identifier);
        result.put("specversion", version.getVersion());
        result.put("specuri", uri);
        return result;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<specificationid>");
        xml.append(StringUtil.wrap(identifier, "identifier"))
           .append(StringUtil.wrap(version.getVersion(), "version"))
           .append(StringUtil.wrap(uri, "uri"))
           .append("</specificationid>");
        return xml.toString();           
    }

}
