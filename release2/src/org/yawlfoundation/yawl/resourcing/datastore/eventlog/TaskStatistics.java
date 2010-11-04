/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.*;

/**
 * @author Michael Adams
 * @date 29/09/2010
 */
public class TaskStatistics {

    private enum Counter { unofferToCancel, unofferToOffer, offerToAllocate, offerToStart,
            unofferToAllocate, unofferToStart, allocateToStart, startToComplete,
            startToReallocate, offerToCancel, allocateToCancel, startToCancel,
            totalDuration, unofferToTimeout, offerToTimeout, allocateToTimeout,
            startToTimeout }

    private List _taskEvents;
    private String _taskName;

    public TaskStatistics(List events, String name) {
        _taskEvents = events;
        _taskName = name;
    }


    public String generate() {
        return generateXNode().toString();
    }
    

    public XNode generateXNode() {
        return outputXML(generateSummaries());
    }


    private Map<String, TaskSummary> generateSummaries() { 
        Map<String, TaskSummary> table = new Hashtable<String, TaskSummary>();
        TaskSummary stat = null;
        String key = null;
        for (Object o : _taskEvents) {
            ResourceEvent event = (ResourceEvent) o;
            if (event.get_event().equals("offer")) {
                key = event.get_itemID();
                stat = getTaskSummary(table, key);
                if (stat.getOfferCount() == 0) stat.setOfferTime(event.get_timeStamp());
                stat.addOffer(event.get_resourceID());
            }
            else if (event.get_event().equals("unoffer")) {
                key = event.get_itemID();
                stat = getTaskSummary(table, key);
                stat.setUnofferTime(event.get_timeStamp());
            }
            else if (event.get_event().equals("allocate")) {
                key = event.get_itemID();
                stat = getTaskSummary(table, key);
                stat.setAllocTime(event.get_timeStamp());
                stat.setAllocateID(event.get_resourceID());
            }
            else if (event.get_event().equals("start")) {
                key = getRootItemID(event.get_itemID());
                stat = getTaskSummary(table, key);
                stat.setStartTime(event.get_timeStamp());
                stat.setStartID(event.get_resourceID());
            }
            else if (event.get_event().equals("complete")) {
                key = getRootItemID(event.get_itemID());
                stat = getTaskSummary(table, key);
                stat.setCompleteTime(event.get_timeStamp());
                stat.setCompleteID(event.get_resourceID());
            }
            else if (event.get_event().startsWith("cancel")) {
                key = getReferenceItemID(table, event.get_itemID());
                stat = getTaskSummary(table, key);
                stat.setCancelTime(event.get_timeStamp());
            }
            else if (event.get_event().startsWith("timer")) {
                key = getReferenceItemID(table, event.get_itemID());
                stat = getTaskSummary(table, key);
                stat.setTimeout(event.get_timeStamp());
            }
            else if (event.get_event().startsWith("realloc")) {
                key = getRootItemID(event.get_itemID());
                stat = getTaskSummary(table, key);
                stat.setReallocTime(event.get_timeStamp());
            }
            if (key != null) table.put(key, stat);
        }
        return table;
    }


    private XNode outputXML(Map<String, TaskSummary> table) {
        Map<String, TaskSubTallyList> offers = new Hashtable<String, TaskSubTallyList>();
        Map<String, TaskSubTallyList> allocs = new Hashtable<String, TaskSubTallyList>();
        Map<String, TaskSubTallyList> starts = new Hashtable<String, TaskSubTallyList>();
        Map<String, TaskSubTallyList> completes = new Hashtable<String, TaskSubTallyList>();
        Map<String, TaskSubTallyList> totals = new Hashtable<String, TaskSubTallyList>();
        Map<String, TaskSubTallyList> timeouts = new Hashtable<String, TaskSubTallyList>();
        TaskSubTallyList cancels = new TaskSubTallyList();
        TaskSubTallyList overall = new TaskSubTallyList();
        for (TaskSummary ts : table.values()) {
            accumulateAll(ts, overall);

            for (String offerTo : ts.getOfferSet()) {
                accumulate(offers, ts, offerTo, getOfferCounters());
            }
            accumulate(allocs, ts, ts.getAllocateID(), getAllocCounters());
            accumulate(starts, ts, ts.getStartID(), getStartCounters());
            accumulate(completes, ts, ts.getCompleteID(), getCompleteCounters());    
            accumulate(totals, ts, ts.getCompleteID(), getTotalCounter());
            accumulate(timeouts, ts, ts.getTimeoutOwner(), getTimeoutCounters());
            accumulate(ts, cancels, getCancelCounters());
        }

        XNode node = new XNode("taskStatistics");
        node.addAttribute("task", _taskName);
        node.addAttribute("created", table.size());
        if (! table.isEmpty()) {
            if (overall.get(Counter.totalDuration) != null) {
                node.addAttribute("completed", overall.get(Counter.totalDuration).count);
            }
            if (! offers.isEmpty()) node.addChild(getStateNode(offers, "offers"));
            if (! allocs.isEmpty()) node.addChild(getStateNode(allocs, "allocations"));
            if (! starts.isEmpty()) node.addChild(getStateNode(starts, "starts"));
            if (! completes.isEmpty()) node.addChild(getStateNode(completes, "completions"));
            if (! cancels.isEmpty()) {
                node.addChild(getStateNode(cancels, "cancels", null, cancels.getSubCount()));
            }
            if (! timeouts.isEmpty()) node.addChild(getStateNode(timeouts, "timeouts"));
            if (! totals.isEmpty()) node.addChild(getStateNode(totals, "totalDurations"));

            XNode overallNode = node.addChild("overall");
            overall.sort();
            for (TaskSubTally tally : overall.values()) {
                if (tally.count > 0) overallNode.addChild(tally.getNode());
            }
        }
        return node;
    }


    private String getParticipantName(String pid) {
        String name = "Unavailable" ;
        if (pid != null) {
            if (! pid.equals("admin")) {
                Participant p = ResourceManager.getInstance().getOrgDataSet().getParticipant(pid);
                if (p != null) name = p.getFullName();
            }
            else name = "admin" ;
        }
        return name;
    }


    private void accumulateAll(TaskSummary ts, TaskSubTallyList list) {
        accumulate(ts, list, getOfferCounters());
        accumulate(ts, list, getAllocCounters());
        accumulate(ts, list, getStartCounters());
        accumulate(ts, list, getCompleteCounters());
        accumulate(ts, list, getTimeoutCounters());
        accumulate(ts, list, getTotalCounter());
    }


    private void accumulate(TaskSummary ts, TaskSubTally tally, Counter counter) {
        tally.add(ts.getPeriod(counter));
    }


    private void accumulate(TaskSummary ts, TaskSubTallyList list, List<Counter> counters) {
        for (Counter counter : counters) {
            list.add(counter, ts.getPeriod(counter));
        }
    }


    private void accumulate(Map<String, TaskSubTallyList> map, TaskSummary ts,
                            String pid, List<Counter> counters) {
        if (pid != null) {
            TaskSubTallyList list = map.get(pid);
            if (list == null) {
                list = new TaskSubTallyList();
                map.put(pid, list);
            }
            accumulate(ts, list, counters);
            list.count++;
        }
    }


    private XNode getStateNode(Map<String, TaskSubTallyList> tallyMap, String name) {
        XNode node = new XNode(name);
        for (String pid : tallyMap.keySet()) {
            TaskSubTallyList list = tallyMap.get(pid);
            node.addChild(getStateNode(list, "participant", getParticipantName(pid),
                    list.count));
        }
        return node;
    }


    private XNode getStateNode(TaskSubTallyList list, String tag, String name, int instances) {
        XNode node = new XNode(tag);
        list.sort();
        if (name != null) node.addAttribute("name", name);
        node.addAttribute("instances", instances);
        for (TaskSubTally tally : list.values()) {
            if (tally.count > 0) {
                node.addChild(tally.getNode());
            }
        }
        return node;
    }


    private List<Counter> getOfferCounters() {
        return Arrays.asList(Counter.unofferToOffer);
    }


    private List<Counter> getAllocCounters() {
        return Arrays.asList(Counter.offerToAllocate, Counter.unofferToAllocate);
    }


    private List<Counter> getStartCounters() {
        return Arrays.asList(Counter.offerToStart, Counter.unofferToStart,
                Counter.allocateToStart, Counter.startToReallocate);
    }


    private List<Counter> getCompleteCounters() {
        return Arrays.asList(Counter.startToComplete);
    }


    private List<Counter> getCancelCounters() {
        return Arrays.asList(Counter.unofferToCancel, Counter.offerToCancel, 
                Counter.allocateToCancel, Counter.startToCancel);
    }


    private List<Counter> getTimeoutCounters() {
        return Arrays.asList(Counter.unofferToTimeout, Counter.offerToTimeout,
                Counter.allocateToTimeout, Counter.startToTimeout);
    }


    private List<Counter> getTotalCounter() {
        return Arrays.asList(Counter.totalDuration);
    }


    private TaskSummary getTaskSummary(Map<String, TaskSummary> table, String key) {
        return table.containsKey(key) ? table.get(key) : new TaskSummary(key);
    }


    private String getReferenceItemID(Map<String, TaskSummary> table, String id) {
        if (table.containsKey(id)) {
            TaskSummary ts = table.get(id);
            if (ts.getStartTime() > 0) return getRootItemID(id);
        }
        return id;
    }

    private String getRootItemID(String id) {
        String[] parts = id.split(":");
        if (parts[0].contains(".")) {
            parts[0] = parts[0].substring(0, parts[0].lastIndexOf('.'));
        }    
        return parts[0] + ":" + parts[1];
    }


    /*********************************************************************/

    class TaskSummary {
        long offerTime = 0, unofferTime = 0, allocTime = 0, startTime = 0;
        long reallocTime = 0, completeTime = 0, cancelTime = 0, timeout = 0;
        String id;
        String allocateID, startID, completeID;
        Set<String> offerSet = new HashSet<String>();

        public TaskSummary(String id) { this.id = id; }

        public long getOfferTime() { return offerTime; }

        public void setOfferTime(long time) { offerTime = time; }

        public long getUnofferTime() { return unofferTime; }

        public void setUnofferTime(long time) { unofferTime = time; }

        public long getAllocTime() { return allocTime; }

        public void setAllocTime(long time) { allocTime = time; }

        public long getStartTime() { return startTime; }

        public void setStartTime(long time) { startTime = time; }

        public long getCompleteTime() { return completeTime; }

        public void setCompleteTime(long time) { completeTime = time; }

        public long getCancelTime() { return cancelTime; }

        public void setCancelTime(long time) { cancelTime = time ; }

        public long getReallocTime() { return reallocTime; }

        public void setReallocTime(long time) { reallocTime = time; }

        public Set<String> getOfferSet() { return offerSet; }

        public void setOfferSet(Set<String> set) { offerSet = set; }

        public int getOfferCount() { return offerSet.size(); }

        public void addOffer(String id) { offerSet.add(id); }

        public String getAllocateID() { return allocateID; }

        public void setAllocateID(String aID) { allocateID = aID; }

        public String getStartID() { return startID; }

        public void setStartID(String sID) { startID = sID; }

        public String getCompleteID() { return completeID; }

        public void setCompleteID(String cID) { completeID = cID; }

        public long getTimeout() { return timeout; }

        public void setTimeout(long time) { timeout = time; }

        public long getOfferToAlloc() { return getDiff(offerTime, allocTime); }

        public long getAllocToStart() { return getDiff(allocTime, startTime); }

        public long getOfferToStart() {
            return (getAllocToStart() > 0) ? 0 : getDiff(offerTime, startTime);
        }

        public long getStartToComplete() { return getDiff(startTime, completeTime); }

        public long getStartToCancel() { return getDiff(startTime, cancelTime); }

        public long getStartToRealloc() { return getDiff(startTime, reallocTime); }

        public long getAllocToCancel() {
            return (getStartToCancel() > 0) ? 0 : getDiff(allocTime, cancelTime);
        }

        public long getOfferToCancel() {
            return (getAllocToCancel() > 0) ? 0 : getDiff(offerTime, cancelTime);
        }

        public long getUnofferToCancel() {
            return (getOfferToCancel() > 0) ? 0 : getDiff(unofferTime, cancelTime);
        }


        public long getUnofferToOffer() { return getDiff(unofferTime, offerTime); }

        public long getUnofferToAlloc() {
            return (getUnofferToOffer() > 0) ? 0 : getDiff(unofferTime, allocTime);
        }

        public long getUnofferToStart() {
            return (getUnofferToAlloc() > 0) ? 0 : getDiff(unofferTime, startTime);
        }

        public long getTotalTime() {
            long start = unofferTime;
            long end = completeTime;
            if (start == 0) start = offerTime;
            if (start == 0) start = allocTime;
            if (start == 0) start = startTime;
            if (end == 0) end = cancelTime;
            if (end == 0) end = timeout;
            return (start == 0) || (end == 0) ? 0 : end - start;
        }

        public long getUnofferToTimeout() {
            return (getOfferToTimeout() > 0) ? 0 : getDiff(unofferTime, timeout);
        }

        public long getOfferToTimeout() {
            return (getAllocToTimeout() > 0) ? 0 : getDiff(offerTime, timeout);
        }

        public long getAllocToTimeout() {
            return (getStartToTimeout() > 0) ? 0 : getDiff(allocTime, timeout);
        }

        public long getStartToTimeout() { return getDiff(startTime, timeout); }

        public String getTimeoutOwner() {
            String owner = null;
            if (timeout > 0) {
                if (startID != null) owner = startID;
                else if (allocateID != null) owner = allocateID;
                else owner = "undefined";
            }
            return owner;
        }

        public long getPeriod(Counter counter) {
            long period = 0;
            switch (counter) {
                case unofferToCancel:   period = getUnofferToCancel(); break;
                case unofferToOffer:    period = getUnofferToOffer(); break;
                case offerToAllocate:   period = getOfferToAlloc(); break;
                case offerToStart:      period = getOfferToStart(); break;
                case unofferToAllocate: period = getUnofferToAlloc(); break;
                case unofferToStart:    period = getUnofferToStart(); break;
                case allocateToStart:   period = getAllocToStart(); break;
                case startToComplete:   period = getStartToComplete(); break;
                case startToReallocate: period = getStartToRealloc(); break;
                case offerToCancel:     period = getOfferToCancel(); break;
                case allocateToCancel:  period = getAllocToCancel(); break;
                case startToCancel:     period = getStartToCancel(); break;
                case totalDuration:     period = getTotalTime(); break;
                case unofferToTimeout:  period = getUnofferToTimeout(); break;
                case offerToTimeout:    period = getOfferToTimeout(); break;
                case allocateToTimeout: period = getAllocToTimeout(); break;
                case startToTimeout:    period = getStartToTimeout(); break;
            }
            return period;
        }

        private long getDiff(long from, long to) {
            return (from == 0) || (to == 0) ? 0 : to - from;            
        }
    }


    /***************************************************************************/

    class TaskSubTally {
        long max = 0, min = Long.MAX_VALUE, total = 0;
        int count = 0;
        Counter counter;

        public TaskSubTally(Counter counter) { this.counter = counter; }

        public void add(long period) {
            if (period > 0) {
                total += period;
                max = Math.max(max, period);
                min = Math.min(min, period);
                count++;
            }
        }

        public XNode getNode() {
            XNode node = new XNode(counter.name());
            node.addChild("instances", count);
            node.addChild("min", StringUtil.formatTime(min));
            node.addChild("max", StringUtil.formatTime(max));
            node.addChild("average", StringUtil.formatTime(total / count));
            return node;
        }
    }

    /***************************************************************************/

    class TaskSubTallyList extends LinkedHashMap<Counter, TaskSubTally> {

        private int count;

        public TaskSubTallyList() { }

        public void add(Counter counter, long period) {
            if (period > 0) {
                TaskSubTally tally = get(counter);
                if (tally == null) {
                    tally = new TaskSubTally(counter);
                    put(counter, tally);
                }
                tally.add(period);
            }     
        }

        public void sort() {
            TaskSubTallyList list = new TaskSubTallyList();
            for (Counter counter : Counter.values()) {                
                TaskSubTally tally = get(counter);
                if (tally != null) {
                    list.put(counter, tally);
                }
            }
            clear();
            putAll(list);
        }

        public int getSubCount() {
            int subCount = 0;
            for (TaskSubTally tally : values()) {
                 subCount += tally.count;
            }
            return subCount;
        }
    }
        
}
