package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 23/04/2009
 */
public class YLogService {

    private long serviceID ;
    private String name;
    private String url;

    public YLogService() {}

    public YLogService(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public long getServiceID() {
        return serviceID;
    }

    public void setServiceID(long serviceID) {
        this.serviceID = serviceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(100);
        xml.append(String.format("<service key=\"%d\">", serviceID));
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(url, "url"));
        xml.append("</service>");
        return xml.toString();
    }
}
