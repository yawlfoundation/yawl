/*
 * InstanceSerializerMap.java
 *
 * Created on August 25, 2004, 11:04 AM
 */

package org.chiba.xml.xforms.connector;

import java.util.HashMap;
import java.util.Map;
import org.chiba.xml.xforms.connector.serializer.FormDataSerializer;
import org.chiba.xml.xforms.connector.serializer.MultipartRelatedSerializer;
import org.chiba.xml.xforms.connector.serializer.UrlEncodedSerializer;
import org.chiba.xml.xforms.connector.serializer.XMLSerializer;

/** InstanceSerializerMap
 *
 * @author  Peter Mikula <peter.mikula@digital-artefacts.fi>
 */
public class InstanceSerializerMap {

    private Map serializerMap = new HashMap();

    /** Default constructor. This constructor initializes the map with default
     * serializers as specified in http://www.w3.org/TR/xforms/slice11.html#submit-options.
     * Note: MultipartPostSerializer is not supported yet.
     */
    public InstanceSerializerMap() {
        
        /**
         * Default constructor is responsible for registering default instance serializers according to specs:
         * http://www.w3.org/TR/xforms/slice11.html#submit-options
         */
        registerSerializer("http", "post", "*", new XMLSerializer());
        registerSerializer("https", "post", "*", new XMLSerializer());
        registerSerializer("mailto", "post", "*", new XMLSerializer());

        registerSerializer("file", "get", "*", new UrlEncodedSerializer());
        registerSerializer("http", "get", "*", new UrlEncodedSerializer());
        registerSerializer("https", "get", "*", new UrlEncodedSerializer());

        registerSerializer("file", "put", "*", new XMLSerializer());
        registerSerializer("http", "put", "*", new XMLSerializer());
        registerSerializer("https", "put", "*", new XMLSerializer());

        registerSerializer("http", "multipart-post", "*", new MultipartRelatedSerializer());
        registerSerializer("https", "multipart-post", "*", new MultipartRelatedSerializer());
        registerSerializer("mailto", "multipart-post", "*", new MultipartRelatedSerializer());
        
        registerSerializer("http", "form-data-post", "*", new FormDataSerializer());
        registerSerializer("https", "form-data-post", "*", new FormDataSerializer());
        registerSerializer("mailto", "form-data-post", "*", new FormDataSerializer());

        registerSerializer("http", "url-encoded-post", "*", new UrlEncodedSerializer());
        registerSerializer("https", "url-encoded-post", "*", new UrlEncodedSerializer());
        registerSerializer("mailto", "url-encoded-post", "*", new UrlEncodedSerializer());
    }
    
    /** Create and initialize serializer map with existing InstanceSerializerMap.
     */
    public InstanceSerializerMap(InstanceSerializerMap map) {
        this.serializerMap.putAll(map.serializerMap);
    }
    
    /**
     * Register new instance serializer for given scheme, method and mediatype. Method supports star convention, the
     * scheme, method or mediatype set to "*" acts as "for all" operator.
     *
     * @param scheme     scheme part of the action uri
     * @param method     method from xforms:method attribute
     * @param mediatype  mediatype from xforms:mediatype attribute
     * @param serializer serializer that should be used for instance serialization.
     */
    public void registerSerializer(String scheme, String method,
                                   String mediatype, InstanceSerializer serializer) {
                                       
        serializerMap.put(scheme+":"+method+":"+mediatype, serializer);
    }

    /**
     * Return serializer associated with given scheme, method and mediatype. The lookup proceeds with following steps,
     * returning first successfull match:<br> <ol> <li> scheme, method, mediatype <li> scheme, method, "*" <li> scheme,
     * "*", mediatype <li> scheme, "*", "*" <li> "*", method, mediatype <li> "*", method, "*" <li> "*", "*", mediatype
     * <li> "*", "*", "*" </ol>
     *
     * @param scheme    scheme part of the action uri
     * @param method    method from xforms:method attribute
     * @param mediatype mediatype from xforms:mediatype attribute
     * @return instance serializer or null
     */
    public InstanceSerializer getSerializer(String scheme, String method, String mediatype) {
        InstanceSerializer serializer;
        serializer = (InstanceSerializer) this.serializerMap.get(scheme + ":" + method + ":" + mediatype);
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get(scheme + ":" + method + ":*");
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get(scheme + ":*:" + mediatype);
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get(scheme + ":*:*");
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get("*:" + method + ":" + mediatype);
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get("*:" + method + ":*");
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get("*:*:" + mediatype);
        }
        if (serializer == null) {
            serializer = (InstanceSerializer) this.serializerMap.get("*:*:*");
        }

        return serializer;
    }
}
