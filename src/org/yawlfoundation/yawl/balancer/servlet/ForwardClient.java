package org.yawlfoundation.yawl.balancer.servlet;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 20/7/17
 */
public class ForwardClient extends Interface_Client {

    @Override
    public String executeGet(String urlStr, Map<String, String> paramsMap) throws IOException {
        return super.executeGet(urlStr, paramsMap);
    }


    @Override
    public String executePost(String urlStr, Map<String, String> paramsMap) throws IOException {
        return super.executePost(urlStr, paramsMap);
    }
}
