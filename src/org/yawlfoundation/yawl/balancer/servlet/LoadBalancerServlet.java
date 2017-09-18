package org.yawlfoundation.yawl.balancer.servlet;

import org.yawlfoundation.yawl.balancer.EngineInstance;
import org.yawlfoundation.yawl.balancer.OperatingMode;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.config.ConfigChangeListener;
import org.yawlfoundation.yawl.balancer.polling.PollingService;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 22/6/17
 */
public class LoadBalancerServlet extends YHttpServlet implements ConfigChangeListener {

    private final ForwardClient _forwardClient = new ForwardClient();
    private final XNodeParser _parser = new XNodeParser();

    private Set<EngineInstance> _engineSet;
    private EngineInstance _authenticator;
    private Map<String, String> _connectionParams;
    private Map<YSpecificationID, String> _specMap;
    private boolean _allActive = false;
    private Random _random = new Random();
    private int _lastCaseNbr = 0;

    private static final List<String> SESSION_ACTIONS = Arrays.asList(
            "checkConnection", "connect", "checkIsAdmin", "disconnect");

    private static final List<String> ITEM_ACTIONS = Arrays.asList(
            "checkout", "checkin", "getChildren", "getWorkItem", "getStartingDataSnapshot",
            "checkAddInstanceEligible", "createInstance", "rejectAnnouncedEnabledTask",
            "suspend", "rollback", "unsuspend", "skip");

    private static final List<String> CASE_ACTIONS = Arrays.asList(
            "cancelCase", "getSpecificationForCase", "getSpecificationIDForCase",
            "getCaseState", "getCaseData", "getWorkItemInstanceSummary");

    private static final List<String> SPEC_ACTIONS = Arrays.asList(
            "upload", "unload");


    public LoadBalancerServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        _engineSet = new HashSet<EngineInstance>();
        _parser.suppressMessages(true);
        Config.load(getServletContext());
        Config.addChangeListener(this);

        List<String> locations = Config.getLocations();
        for (String location : locations) {
            String[] parts = location.split(":");
            String host = parts[0];
            int port = StringUtil.strToInt(parts[1], -1);
            _engineSet.add(new EngineInstance(host, port));
        }

//        _authenticator = getRandomInstance();
//        _authenticator.setAuthenticator(true);
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("HEAD".equals(req.getMethod())) return;
        doPost(req, resp);
    }

    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!_allActive) {
            waitUntilAllActive();
            _authenticator = getRandomInstance();
            _authenticator.setAuthenticator(true);
        }
//        _log.warn("\n" + RequestDumpUtil.dump(req) + "\n");
        String requri = req.getRequestURI();
        if (requri != null && !req.getRequestURI().startsWith("/bal")) {
            _log.error("Balancer received invalid request URI: {}", requri);
            processResponse(resp, "<error/>");
        }

        String action = req.getParameter("action");
        if (action != null) {
            String result;
            if (isSessionAction(action)) {
                result = handleSessionAction(req);
            }
            else if (isItemAction(action)) {
                result = handleItemAction(req);
            }
            else if (isCaseAction(action)) {
                result = handleCaseAction(req);
            }
            else if (isLaunchAction(action)) {
                result = handleLaunchAction(req);
            }
            else if (isSpecAction(action)) {
                result = handleSpecAction(req);
            }
            else {
                result = handleAction(req);
            }
            processResponse(resp, result);
        }
        else {
            _log.error("Null action in call");
        }
    }


    @Override
    public void destroy() {
        for (EngineInstance instance : _engineSet) {
            instance.close();
        }
        PollingService.shutdown();
        Config.stopConfigMonitoring();
        super.destroy();
    }


    public void configChanged(Map<String, String> changedValues) {
        if (changedValues.containsKey("poll_interval")) {
            PollingService.reschedule();
        }
    }

    
    private void processResponse(HttpServletResponse resp, String result)
            throws IOException {
        ServletUtils.sendResponse(resp, response(result));
    }


    private String handleAction(HttpServletRequest req) throws IOException {
        return forward(req);
    }


    private boolean isSessionAction(String action) {
        return SESSION_ACTIONS.contains(action);
    }


    private boolean isItemAction(String action) {
        return ITEM_ACTIONS.contains(action);
    }


    private boolean isLaunchAction(String action) {
        return action.equals("launchCase");
    }

    private boolean isCaseAction(String action) {
        return CASE_ACTIONS.contains(action);
    }


    private boolean isSpecAction(String action) {
        return SPEC_ACTIONS.contains(action);
    }


    private String handleSessionAction(HttpServletRequest req)
            throws IOException {
        if (! _authenticator.isRestored() && "connect".equals(req.getParameter("action"))) {
            restoreCases();
            restoreSpecifications();
            PollingService.schedule();
        }
        return forwardPost(_authenticator.getURL(req.getPathInfo()), buildParamMap(req));
    }


    private String handleLaunchAction(HttpServletRequest req) throws IOException {
        EngineInstance instance = getIdlestEngine();
        Map<String, String> params = buildParamMap(req);
        if (checkOrAddSpec(instance, params)) {
            params.put("caseid", getNextCaseNbr());
            String result = forwardPost(instance, params, req.getPathInfo());
            if (successful(result)) {
                instance.addCase(result);
                System.out.println("Case " + result + " launched on engine " +
                        instance.getName());
            }
            return result;
        }
        return "Failed to launch case: unknown specification";
    }

    
    private String handleItemAction(HttpServletRequest req) throws IOException {
        String caseID = getRootCaseID(req.getParameter("workItemID"));
        return handleCaseSpecificAction(req, caseID);
    }


    private String handleCaseAction(HttpServletRequest req) throws IOException {
        return handleCaseSpecificAction(req, req.getParameter("caseid"));
    }


    private String handleCaseSpecificAction(HttpServletRequest req, String caseID)
            throws IOException {
        if (caseID == null) {
            caseID = req.getParameter("caseID");
        }
        if (caseID != null) {
            EngineInstance instance = getEngineForCase(caseID);
            if (instance != null) {
                return forwardPost(instance, buildParamMap(req), "/ib");
            }
        }
        return "<failure>Unknown Case Identifier</failure>";
    }


    private String handleSpecAction(HttpServletRequest req) throws IOException {
        String action = req.getParameter("action");
        if (action.equals("upload")) {
            String result = forwardPost(getRandomInstance(), buildParamMap(req), "/ia");
            if (successful(result)) {
                String specXML = req.getParameter("specXML");
                _specMap.put(getSpecIDFromXML(specXML), specXML);
            }
            return result;
        }
        else {           // unload
            String specIdentifier = req.getParameter("specidentifier");
            String specVersion = req.getParameter("specversion");
            String specURI = req.getParameter("specuri");
            YSpecificationID specID = new YSpecificationID(specIdentifier, specVersion, specURI);
            for (EngineInstance instance : _engineSet) {
                if (isLoaded(instance, specID)) {
                    forwardPost(instance, buildParamMap(req), "/ia");
                }
            }
            _specMap.remove(specID);
            return "<success/>";
        }
    }


    private void forward(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher("/yawl/ib");
        rd.forward(req, resp);
    }


    private String forward(HttpServletRequest req) throws IOException {
        Set<String> resultSet = new HashSet<String>();
        Map<String, String> params = buildParamMap(req);
        for (EngineInstance instance : _engineSet) {
            resultSet.add(forward(instance, params, req.getPathInfo(), req.getMethod()));
        }
        return coalesceResults(resultSet);
    }


    private String forwardPost(String url, Map<String, String> params) throws IOException {
        return _forwardClient.executePost(url, params);
    }


    private String forwardGet(String url, Map<String, String> params) throws IOException {
        return _forwardClient.executeGet(url, params);
    }


    private EngineInstance getIdlestEngine() {
        if (Config.getOperatingMode() == OperatingMode.RANDOM) {
            return getRandomInstance();
        }
        EngineInstance idlest = null;
        double lowestScore = Double.MAX_VALUE;
        for (EngineInstance engine : _engineSet) {
            try {
                double busyness = engine.getBusyness(false);
                System.out.printf("Busyness: %s %.3f\n", engine.getName(), busyness);
                if (idlest == null || busyness < lowestScore) {
                    idlest = engine;
                    lowestScore = busyness;
                }
            }
            catch (Exception e) {
                _log.warn("Unable to gather statistics from engine {} - {}",
                        engine.getName(), e.getMessage());
            }
        }
        return idlest;
    }


    private String coalesceResults(Set<String> resultSet) {
        if (resultSet == null) {
            return null;
        }
        int count = resultSet.size();
        if (count == 1) {
            return resultSet.iterator().next();
        }

        XNode resNode = null;
        String failMsg = null;
        for (String result : resultSet) {
            if (StringUtil.unwrap(result).isEmpty()) {
                continue;
            }
            if (failMsg == null && result.startsWith("<fail")) {
                failMsg = result;
                continue;
            }

            XNode node = _parser.parse(result);
            if (! hasParentTag(node)) {
                result = StringUtil.wrap(result, "temp");
                node = _parser.parse(result);
            }
            if (node != null) {
                if (resNode == null) {
                    resNode = node;
                }
                else {
                    resNode.addChildren(node.getChildren());
                }
            }
        }
        if (resNode != null) {
            resNode.removeDuplicateChildren();
            String out =  resNode.toString();
            if (out.startsWith("<temp>")) {
                out = StringUtil.unwrap(out);
            }
            return out;
        }
        else if (failMsg != null) {
            return failMsg;
        }
        else return "";
    }


    private boolean hasParentTag(XNode node) {
        if (node == null) return false;               // no parent tag, multi-elements
        if (! node.hasChildren()) return true;        // no child, single element
        if (node.getChildCount() == 1) return true;   // one child, thus parent tag

        // if children have different names, is a single element
        String name = null;
        for (XNode child : node.getChildren()) {
            if (name == null) name = child.getName();
            else if (! name.equals(child.getName())) return false;
        }
        return true;    // all children same name
    }


    private EngineInstance getRandomInstance() {
        int count = _random.nextInt(_engineSet.size());
        Iterator<EngineInstance> iter = _engineSet.iterator();
        for (int i = 0; i < count; i++) {
            iter.next();
        }
        return iter.next();
    }


    private String checkOrConnect(EngineInstance instance) throws IOException {
        String handle = instance.getSessionHandle();
        if (handle != null) {
            getConnectionParams().put("sessionHandle", handle);
            getConnectionParams().put("action", "checkConnection");
            String result = forwardPost(instance.getURL("/ib"), getConnectionParams());
            if (successful(result)) {
                return handle;
            }
        }
        getConnectionParams().put("action", "connect");
        String result = forwardPost(instance.getURL("/ib"), getConnectionParams());
        if (successful(result)) {
            handle = StringUtil.unwrap(result);
            instance.setSessionHandle(handle);
            return handle;
        }
        return "FAIL";
    }


    private Map<String, String> getConnectionParams() {
        if (_connectionParams == null) {
            _connectionParams = new HashMap<String, String>();
            _connectionParams.put("userid", "loadBalancer");
            _connectionParams.put("password", "yBalance");
        }
        return _connectionParams;
    }


    private String getRootCaseID(String itemID) {
        if (itemID != null) {
            String caseID = itemID.substring(0, itemID.indexOf(':'));
            int firstDot = caseID.indexOf('.');
            return (firstDot > -1) ? caseID.substring(0, firstDot) : caseID;
        }
        return null;
    }


    private EngineInstance getEngineForCase(String caseID) {
        for (EngineInstance instance : _engineSet) {
            if (instance.hasCase(caseID)) {
                return instance;
            }
        }
        return null;
    }


    private Map<String, String> buildParamMap(HttpServletRequest req) {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration e = req.getParameterNames();
        while(e.hasMoreElements()) {
            String param = (String) e.nextElement();
            String value = req.getParameter(param);
            params.put(param, value);
        }
        return params;
    }


    private String getNextCaseNbr() {
        if (_lastCaseNbr == 0) {
            for (EngineInstance instance : _engineSet) {
                 _lastCaseNbr = Math.max(_lastCaseNbr, instance.getLastCaseNbr());
            }
        }
        return String.valueOf(++_lastCaseNbr);
    }


    private void restoreCases() throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getAllRunningCases");
        for (EngineInstance instance : _engineSet) {
            String result = forwardPost(instance, params, "/ib");
            if (successful(result)) {
                XNode node = new XNodeParser().parse(result);
                if (node != null) {
                    List<String> caseList = new ArrayList<String>();
                    for (XNode specNode : node.getChildren()) {
                        for (XNode caseNode : specNode.getChildren()) {
                            caseList.add(caseNode.getText());
                        }
                    }
                    instance.addCases(caseList);
                    instance.setRestored(true);
                }
            }
        }
    }


    private void restoreSpecifications() throws IOException {
        _specMap = new HashMap<YSpecificationID, String>();
        for (EngineInstance instance : _engineSet) {
            for (SpecificationData specData : getSpecList(instance)) {
                String specXML = getSpecification(instance, specData);
                if (specXML != null) {
                    _specMap.put(specData.getID(), specXML);
                }
            }
        }
    }


    // xml has already been validated via upload call
    private YSpecificationID getSpecIDFromXML(String specXML) throws IOException {
        try {
            List<YSpecification> specList = YMarshal.unmarshalSpecifications(specXML, false);
            if (specList.isEmpty()) {
                throw new IOException("Failed to parse specification xml");
            }
            return specList.get(0).getSpecificationID();
        }
        catch (YSyntaxException yse) {
             throw new IOException(yse.getMessage());
        }
    }

    
    private List<SpecificationData> getSpecList(EngineInstance instance) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getSpecificationPrototypesList");
        String result = forwardPost(instance, params, "/ib");
        return successful(result) && ! StringUtil.unwrap(result).isEmpty() ?
                Marshaller.unmarshalSpecificationSummary(response(result)) :
                Collections.<SpecificationData>emptyList();
    }

    
    private String getSpecification(EngineInstance instance, SpecificationData specData)
            throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getSpecification");
        params.put("specidentifier", specData.getSpecIdentifier());
        params.put("specversion", specData.getSpecVersion());
        params.put("specuri", specData.getSpecURI());

        String result = forwardPost(instance, params, "/ib");
        return successful(result) ? result : null;
    }


    private boolean checkOrAddSpec(EngineInstance instance, Map<String, String> params)
            throws IOException {
        YSpecificationID specID = new YSpecificationID(params.get("specidentifier"),
                params.get("specversion"), params.get("specuri"));
        if (! isLoaded(instance, specID)) {
            String specXML = _specMap.get(specID);
            if (specXML != null) {
                return uploadSpec(instance, specXML);
            }
        }
        return true;
    }


    private boolean uploadSpec(EngineInstance instance, String specXML) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "upload");
        params.put("specXML", specXML);
        return successful(forwardPost(instance, params, "/ia"));
    }


    private String forwardPost(EngineInstance instance, Map<String, String> params,
                               String path) throws IOException {
        return forward(instance, params, path, "POST");
    }


    private String forward(EngineInstance instance, Map<String, String> params,
                               String path, String method) throws IOException {
        String handle = checkOrConnect(instance);
        if (! handle.equals("FAIL")) {
            params.put("sessionHandle", handle);
            try {
                return method.equals("GET") ? forwardGet(instance.getURL(path), params) :
                        forwardPost(instance.getURL(path), params);
            }
            catch (Exception e) {
                return StringUtil.wrap(e.getMessage(), "failure");
            }
        }
        return StringUtil.wrap("Failed to connect to " + instance.getName(),
                "failure");
    }

    
    private boolean isLoaded(EngineInstance instance, YSpecificationID specID)
            throws IOException {
        for (SpecificationData specData : getSpecList(instance)) {
            if (specData.getID().equals(specID)) {
                return true;
            }
        }
        return false;
    }

    
    private boolean successful(String xml) { return _forwardClient.successful(xml); }


    private void waitUntilAllActive() {
        int countdown = getInitialisationWaitLimit();
        do {
            _allActive = true;
            for (EngineInstance instance : _engineSet) {
                _allActive &= instance.isActive();
                 if (! _allActive) break;
            }
            if (_allActive) break;
            try {
                Thread.sleep(5000);
                countdown -= 5000;
            }
            catch (InterruptedException ie) {
                //
            }
        } while (! _allActive && countdown > 0);

        if (! _allActive) {
            removeInactiveEngines();
        }
    }


    private int getInitialisationWaitLimit() {
       int limit = Config.getEngineInitWait();
       return limit > 0 ? limit * 1000 : 60000;
    }

    private void removeInactiveEngines() {
       Set<EngineInstance> inactive = new HashSet<EngineInstance>();
        for (EngineInstance instance : _engineSet) {
            if (!instance.isActive()) {
                inactive.add(instance);
                _log.warn("Engine at port {} has not initialised, " +
                        "and so has been removed from the pool.", instance.getPort());
            }
        }
        _engineSet.removeAll(inactive);
    }

}
