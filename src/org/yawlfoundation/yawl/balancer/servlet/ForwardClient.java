package org.yawlfoundation.yawl.balancer.servlet;

import org.yawlfoundation.yawl.balancer.instance.EngineInstance;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 20/7/17
 */
public class ForwardClient extends Interface_Client {

    private final Map<String, String> _connectionParams;


    public ForwardClient(String user, String pw) {
        super();
        _connectionParams = buildConnectionParams(user, pw);
    }


    @Override
    public String executeGet(String urlStr, Map<String, String> paramsMap) throws IOException {
        return super.executeGet(urlStr, paramsMap);
    }


    @Override
    public String executePost(String urlStr, Map<String, String> paramsMap) throws IOException {
        return super.executePost(urlStr, paramsMap);
    }


    public String executePost(EngineInstance instance, Map<String, String> params,
                                 String path) throws IOException {
        return forward(instance, params, path, "POST");
    }


    protected String forward(EngineInstance instance, Map<String, String> params,
                           String path, String method) throws IOException {
        String handle = checkOrConnect(instance);
        if (! handle.equals("FAIL")) {
            params.put("sessionHandle", handle);
            try {
                return method.equals("GET") ? executeGet(instance.getURL(path), params) :
                        executePost(instance.getURL(path), params);
            }
            catch (Exception e) {
                return StringUtil.wrap(e.getMessage(), "failure");
            }
        }
        return StringUtil.wrap("Failed to connect to " + instance.getName(),
                "failure");
    }


    private String checkOrConnect(EngineInstance instance) throws IOException {
        String handle = instance.getSessionHandle();
        if (handle != null) {
            _connectionParams.put("sessionHandle", handle);
            _connectionParams.put("action", "checkConnection");
            String result = executePost(instance.getURL("/ib"), _connectionParams);
            if (successful(result)) {
                return handle;
            }
        }
        _connectionParams.put("action", "connect");
        String result = executePost(instance.getURL("/ib"), _connectionParams);
        if (successful(result)) {
            handle = StringUtil.unwrap(result);
            instance.setSessionHandle(handle);
            return handle;
        }
        return "FAIL";
    }


    private Map<String, String> buildConnectionParams(String user, String pw) {
        Map<String, String> connectionParams = new HashMap<String, String>();
        connectionParams.put("userid", user);
        connectionParams.put("password", pw);
        return connectionParams;
    }

}
