package org.yawlfoundation.yawl.balancer.output;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/8/17
 */
public class RequestStatOutputter extends AbstractLoadOutputter {


    public RequestStatOutputter(String engineName) {
        super("requests", engineName);
    }


    protected void writeValues(Map<String, String> values) throws IOException { }


    protected String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("time,timestamp,name,min,max,mean,count,perSec,");
        sb.append("prevMin,prevMax,prevMean,prevCount,prevPerSec,");
        sb.append("allMin,allMax,allMean,allCount,allPerSec");
        return sb.toString();
    }

}
