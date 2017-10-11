package org.yawlfoundation.yawl.simulation;

import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClientAdapter;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Michael Adams
 * @date 1/10/12
 */
public class YSimulator {

    private WorkQueueGatewayClientAdapter _wqAdapter;
    private ResourceGatewayClientAdapter _resAdapter;
    private SimulatorProperties _props;
    private String _handle;
    private Map<String, ParticipantSummary> _summaryMap;
    private Map<String, String> _userToPidMap;
    private Map<String, Set<String>> _roleToPidsMap;
    private Map<String, Long> _caseStartTimeMap;
    private long _startTime;
    private long _slowestCaseTime;
    private long _fastestCaseTime;
    private long _totalTime;

    protected static final String DEFAULT_URL = ":8080/resourceService/workqueuegateway";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "YAWL";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final Timer _timer = new Timer();

    protected enum SimulationType {Workitem, Resource, Process}


    public static void main(String[] args) {
        YSimulator sim = new YSimulator();
        String configFile = (args.length > 0) ? args[0] : "config.xml";
        sim.run(configFile);
    }


    private void run(String configFile) {
        init();
        try {
            _props.parse(configFile);
            checkSpecLoaded();
            start();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to run simulation: " + e.getMessage());
        }
    }


    private void init() {
        _props = new SimulatorProperties(this);
        _summaryMap = new HashMap<String, ParticipantSummary>();
        _userToPidMap = new HashMap<String, String>();
        _roleToPidsMap = new HashMap<String, Set<String>>();
        _caseStartTimeMap = new HashMap<String, Long>();
        _slowestCaseTime = 0;
        _fastestCaseTime = Long.MAX_VALUE;
        _totalTime = 0;
    }


    protected void connect(String url) {
        _wqAdapter = new WorkQueueGatewayClientAdapter(url);
        _handle = _wqAdapter.connect(DEFAULT_USER, DEFAULT_PASSWORD);
        if (!successful(_handle)) {
            fail("Failed to connect to a resource service using: " + url +
                    ", reason: " + _handle);
        }

        // can use same handle
        _resAdapter = new ResourceGatewayClientAdapter(url.replaceFirst("workqueue", ""));
    }


    private void checkSpecLoaded() throws IOException {
        for (SpecificationData specData : _wqAdapter.getLoadedSpecs(_handle)) {
            if (specData.getID().equals(_props.getSpecID())) return;
        }
        fail("Specification is not loaded in the Engine: " + _props.getSpecID().toString());
    }


    protected String getParticipantID(String userID) throws IOException, ResourceGatewayException {
        if (_userToPidMap.containsKey(userID)) {
            return _userToPidMap.get(userID);
        }

        Participant p = _wqAdapter.getParticipantFromUserID(userID, _handle);
        if (p == null) {
            fail("No participant found with userid: " + userID);
            return null;                             // to make the compiler happy
        }
        _userToPidMap.put(userID, p.getID());
        _summaryMap.put(p.getID(), new ParticipantSummary(p));
        return p.getID();
    }


    protected Set<String> getPIDsForRole(String roleName)
            throws IOException, ResourceGatewayException {
        if (_roleToPidsMap.containsKey(roleName)) {
            return _roleToPidsMap.get(roleName);
        }

        Set<String> pids = new HashSet<String>();
        Set<Participant> pSet = _resAdapter.getParticipantsWithRole(roleName, _handle);
        if (pSet != null) {
            for (Participant p : pSet) {
                pids.add(p.getID());
                _summaryMap.put(p.getID(), new ParticipantSummary(p));
            }
        } else fail("No role found with name '" + roleName + "' or role has no members.");

        _roleToPidsMap.put(roleName, pids);
        return pids;
    }


    private void start() throws IOException, ResourceGatewayException {
        _startTime = System.currentTimeMillis();
        startPoller();
        for (int i = 1; i <= _props.getCaseCount(); i++) {
            TimedCaseLauncher launcher = new TimedCaseLauncher(i);
            _timer.schedule(launcher, _props.getInterval() * i);
        }
    }


    private void startPoller() {
        TimedQueuePoller poller = new TimedQueuePoller();
        _timer.scheduleAtFixedRate(poller, _props.getInterval() * 2, _props.getInterval());
    }


    protected boolean hasRunningCases() throws IOException {
        String response = _wqAdapter.getRunningCases(_props.getSpecID(), _handle);
        return successful(response) && StringUtil.unwrap(response).length() > 0;
    }

    protected void cancelAllCases() {
        try {
            String response = _wqAdapter.getRunningCases(_props.getSpecID(), _handle);
            if (successful(response)) {
                XNode cases = new XNodeParser().parse(response);
                for (XNode caseNode : cases.getChildren()) {
                    if (successful(_wqAdapter.cancelCase(caseNode.getText(), _handle))) {
                        print(now() + " - Cancelled case " + caseNode.getText());
                    }
                }
            }
        } catch (IOException ioe) {
            // forget it - just trying to clean up anyway
        }
    }


    protected void allocateOffers() throws IOException, ResourceGatewayException {
        Map<String, List<String>> taskToOfferMap = new HashMap<String, List<String>>();
        for (String pid : _props.getResources()) {
             Set<WorkItemRecord> offered = getOffered(pid);
             for (WorkItemRecord wir : offered) {
                 List<String> offerees = taskToOfferMap.get(wir.getID());
                 if (offerees == null) {
                     offerees = new ArrayList<String>();
                     taskToOfferMap.put(wir.getID(), offerees);
                 }
                 offerees.add(pid);
             }
        }
        Random r = new Random();
        for (String itemID : taskToOfferMap.keySet()) {
            List<String> offerees = taskToOfferMap.get(itemID);
            String pid = offerees.get(r.nextInt(offerees.size()));
            _wqAdapter.acceptOffer(pid, itemID, _handle);
        }
    }


    protected Set<WorkItemRecord> getOffered(String pid)
            throws IOException, ResourceGatewayException {
        return _wqAdapter.getQueuedWorkItems(pid, WorkQueue.OFFERED, _handle);
    }


    protected Set<WorkItemRecord> getAllocated(String pid)
            throws IOException, ResourceGatewayException {    
        return _wqAdapter.getQueuedWorkItems(pid, WorkQueue.ALLOCATED, _handle);
    }


    private int getExecutingCount(String pid)
            throws IOException, ResourceGatewayException {
        return _wqAdapter.getQueuedWorkItems(pid, WorkQueue.STARTED, _handle).size();
    }


    // return true if no work items are currently executing
    private boolean deadlocked() throws IOException, ResourceGatewayException {
        for (String pid : _props.getResources()) {
            if (getExecutingCount(pid) > 0) return false;
        }
        return true;
    }


    protected boolean process(String pid, WorkItemRecord wir)
            throws IOException, ResourceGatewayException {

        ResourceLimit limit = _props.getLimit(pid);
        if (limit.hasBeenExceeded()) {
            _summaryMap.get(pid).reportLimitReached();
            _wqAdapter.deallocateItem(pid, wir.getID(), _handle);
            return false;
        }

        // ignore busy resources if required
        if (_props.getConcurrent(wir.getTaskID(), pid) <= getExecutingCount(pid)) {
            return false;
        }

        wir = _wqAdapter.startItem(pid, wir.getID(), _handle);
        int processingTime = _props.getProcessingTime(wir.getTaskID(), pid);
        _summaryMap.get(pid).addWork(wir.getTaskID(), processingTime);
        print(MessageFormat.format("{0} - Started workitem {1} and processing for {2} ms",
                now(), wir.getID(), processingTime));
        limit.increment(processingTime);
        TimedItemCompleter completer = new TimedItemCompleter(wir, pid);
        _timer.schedule(completer, processingTime);
        return true;
    }


    protected boolean successful(String s) {
        return !(s == null || s.startsWith("<fail"));
    }

    protected void fail(String msg) {
        print("Failed to run simulation: " + msg);
        System.exit(1);
    }


    protected void print(String msg) {
        System.out.println(msg);
    }

    private String now() {
        return SDF.format(new Date(System.currentTimeMillis()));
    }


    private String getCaseSummary() {
        long now = System.currentTimeMillis();
        StringBuilder s = new StringBuilder();
        s.append("Summary for all cases:\n")
                .append("\tSimulation Started: ")
                .append(SDF.format(new Date(_startTime)))
                .append("\n\tSimulation Completed: ")
                .append(SDF.format(new Date(now)))
                .append("\n\tTotal running time: ")
                .append(now - _startTime)
                .append(" ms")
                .append("\n\tNumber of cases completed: ")
                .append(_props.getCaseCount())
                .append("\n\tFastest case cycle time: ")
                .append(_fastestCaseTime)
                .append(" ms")
                .append("\n\tSlowest case cycle time: ")
                .append(_slowestCaseTime)
                .append(" ms")
                .append("\n\tAverage case cycle time: ")
                .append(_totalTime / _props.getCaseCount())
                .append(" ms");
        return s.toString();
    }


    /**
     * **********************************************************************
     */

    class TimedQueuePoller extends TimerTask {

        public void run() {
            try {
                if (hasRunningCases()) {
                    allocateOffers();
                    boolean startedOne = false;
                    for (String pid : _props.getResources()) {
                        for (WorkItemRecord wir : getAllocated(pid)) {
                            startedOne = process(pid, wir) || startedOne;
                        }
                    }
                    if (!startedOne && deadlocked()) {
                        summariseAndExit(false);
                    }
                } else {
                    summariseAndExit(true);
                }
            } catch (Exception e) {
                // break
            }
        }


        private void summariseAndExit(boolean successful) {
            if (successful) {
                print("Simulation completed successfully.\n");
            } else {
                _timer.cancel();
                cancelAllCases();
                print("Simulation completed prematurely - " +
                        "it appears all resources have reached their limits!\n");
            }
            for (ParticipantSummary summary : _summaryMap.values()) {
                print(summary.getSummary());
            }
            print(getCaseSummary());
            System.exit(0);
        }

    }


    /**
     * **********************************************************************
     */

    class TimedItemCompleter extends TimerTask {

        String pid;
        WorkItemRecord wir;


        TimedItemCompleter(WorkItemRecord wir, String pid) {
            this.wir = wir;
            this.pid = pid;
        }


        public void run() {
            try {
                _wqAdapter.completeItem(pid, wir.getID(), _handle);
                print(now() + " - Completed workitem " + wir.getID());

                // check if case has completed
                if (!successful(_wqAdapter.getCaseData(wir.getRootCaseID(), _handle))) {
                    long startTime = _caseStartTimeMap.remove(wir.getRootCaseID());
                    long caseLength = System.currentTimeMillis() - startTime;
                    print(MessageFormat.format("{0} - Completed case {1} in {2} ms",
                            now(), wir.getRootCaseID(), caseLength));
                    if (caseLength > _slowestCaseTime) _slowestCaseTime = caseLength;
                    if (caseLength < _fastestCaseTime) _fastestCaseTime = caseLength;
                    _totalTime += caseLength;
                }
            } catch (Exception e) {
                print("Failed to complete workitem " + wir.getID() + ": " + e.getMessage());
            }
        }

    }


    /**
     * **********************************************************************
     */

    class TimedCaseLauncher extends TimerTask {

        int count;

        TimedCaseLauncher(int count) { this.count = count; }


        public void run() {
            try {
                String caseID = _wqAdapter.launchCase(_props.getSpecID(), null, _handle);
                if (!successful(caseID)) fail("Failed to launch case: " + caseID);
                _caseStartTimeMap.put(caseID, System.currentTimeMillis());
                print(MessageFormat.format("{0} - Started case {1} ({2}/{3})",
                        now(), caseID, count, _props.getCaseCount()));
            } catch (Exception e) {
                fail("Failed to launch case: " + e.getMessage());
            }
        }

    }


    /**
     * ***************************************************************************
     */

    class ParticipantSummary {

        private Participant p;
        private Map<String, List<Integer>> taskMap;
        private boolean reported;


        ParticipantSummary(Participant p) {
            this.p = p;
            taskMap = new HashMap<String, List<Integer>>();
        }


        void addWork(String task, int time) {
            List<Integer> times = taskMap.get(task);
            if (times == null) {
                times = new ArrayList<Integer>();
                taskMap.put(task, times);
            }
            times.add(time);
        }


        void reportLimitReached() {
            if (!reported) {
                print(MessageFormat.format(
                        "{0} - The work limit for resource {1} (userid {2}) has " +
                                "been reached for this run.",
                        now(), p.getFullName(), p.getUserID()));
                reported = true;
            }
        }


        String getSummary() {
            int totalTasks = 0;
            long totalTime = 0;
            StringBuilder s = new StringBuilder();
            s.append("Summary for ").append(p.getFullName())
                    .append(" (user '").append(p.getUserID())
                    .append("'):\n");
            for (String task : taskMap.keySet()) {
                List<Integer> times = taskMap.get(task);
                long sumTime = sumTimes(times);
                totalTasks += times.size();
                totalTime += sumTime;
                s.append("\tTask '").append(task).append("': ")
                        .append(times.size()).append(" instances, ")
                        .append(sumTime).append(" msec total time.\n");
            }
            s.append("\tTotal: ").append(totalTasks).append(" instances, ")
                    .append(totalTime);
            if (totalTasks > 0) {
                s.append(" msec total time, ")
                        .append((int) totalTime / totalTasks).append(" average per instance.\n");
            } else s.append(" msec total time.\n");

            return s.toString();
        }


        private long sumTimes(List<Integer> times) {
            long total = 0;
            for (int time : times) total += time;
            return total;
        }

    }

}
