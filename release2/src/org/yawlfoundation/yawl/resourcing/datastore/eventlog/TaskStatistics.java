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

    private List _taskEvents;
    private String _taskName;

    public TaskStatistics(List events, String name) {
        _taskEvents = events;
        _taskName = name;
    }


    public String generate() {
        return outputXML(generateSummaries(), initTallies());
    }


    private Map<String, TaskSummary> generateSummaries() { 
        Map<String, TaskSummary> table = new Hashtable<String, TaskSummary>();
        TaskSummary stat = null;
        String key = null;
        for (Object o : _taskEvents) {
            ResourceEvent event = (ResourceEvent) o;
            if (event.get_event().equals("offer")) {
                key = event.get_itemID();
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                if (stat.getOfferCount() == 0) stat.setOfferTime(event.get_timeStamp());
                stat.addOffer(event.get_participantID());
            }
            else if (event.get_event().equals("unoffer")) {
                key = event.get_itemID();
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                stat.setUnofferTime(event.get_timeStamp());
            }
            else if (event.get_event().equals("allocate")) {
                key = event.get_itemID();
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                stat.setAllocTime(event.get_timeStamp());
                stat.setAllocateID(event.get_participantID());
            }
            else if (event.get_event().equals("start")) {
                key = getRootItemID(event.get_itemID());
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                stat.setStartTime(event.get_timeStamp());
                stat.setStartID(event.get_participantID());
            }
            else if (event.get_event().equals("complete")) {
                key = getRootItemID(event.get_itemID());
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                stat.setCompleteTime(event.get_timeStamp());
            }
            else if (event.get_event().startsWith("cancelled")) {
                key = getRootItemID(event.get_itemID());
                stat = table.containsKey(key) ? table.get(key) : new TaskSummary(key);
                stat.setCancelTime(event.get_timeStamp());
            }
            if (key != null) table.put(key, stat);
        }
        return table;
    }


    private String outputXML(Map<String, TaskSummary> table, List<TaskSubTally> tallies) {
        Map<String, Integer> offers = new Hashtable<String, Integer>();
        Map<String, Integer> allocs = new Hashtable<String, Integer>();
        Map<String, Integer> starts = new Hashtable<String, Integer>();
        for (TaskSummary ts : table.values()) {
            int i = 0;
            tallies.get(i++).add(ts.getUnofferToOffer());
            tallies.get(i++).add(ts.getOfferToAlloc());
            tallies.get(i++).add(ts.getOfferToStart());
            tallies.get(i++).add(ts.getUnofferToAlloc());
            tallies.get(i++).add(ts.getUnofferToStart());
            tallies.get(i++).add(ts.getAllocToStart());
            tallies.get(i++).add(ts.getStartToComplete());
            tallies.get(i++).add(ts.getOfferToCancel());
            tallies.get(i++).add(ts.getAllocToCancel());
            tallies.get(i++).add(ts.getStartToCancel());
            tallies.get(i).add(ts.getTotalTime());

            for (String offerTo : ts.getOfferSet()) {
                if (offerTo != null) {
                    int oCount = offers.containsKey(offerTo) ? offers.get(offerTo) : 0;
                    offers.put(offerTo, ++oCount);
                }
            }
            String allocBy = ts.getAllocateID();
            if (allocBy != null) {
                int aCount = allocs.containsKey(allocBy) ? allocs.get(allocBy) : 0;
                allocs.put(allocBy, ++aCount);
            }
            String startBy = ts.getStartID();
            if (startBy != null) {
                int sCount = starts.containsKey(startBy) ? starts.get(startBy) : 0;
                starts.put(startBy, ++sCount);
            }
        }

        XNode node = new XNode("taskStatistics");
        node.addAttribute("task", _taskName);
        node.addAttribute("count", table.size());
        for (TaskSubTally tally : tallies) {
            if (tally.count > 0) node.addChild(tally.getNode());
        }
        XNode nPO = node.addChild("offers");
        for (String pid : offers.keySet()) {
            XNode offer = nPO.addChild("participant");
            offer.addAttribute("name", getParticipantName(pid));
            offer.addAttribute("instances", offers.get(pid));
        }
        XNode nPA = node.addChild("allocations");
        for (String pid : allocs.keySet()) {
            XNode alloc = nPA.addChild("participant");
            alloc.addAttribute("name", getParticipantName(pid));
            alloc.addAttribute("instances", allocs.get(pid));
        }
        XNode nPS = node.addChild("starts");
        for (String pid : starts.keySet()) {
            XNode start = nPS.addChild("participant");
            start.addAttribute("name", getParticipantName(pid));
            start.addAttribute("instances", starts.get(pid));
        }

        return node.toString();
    }


    private List<TaskSubTally> initTallies() {
        List<TaskSubTally> list = new ArrayList<TaskSubTally>();
        list.add(new TaskSubTally("unofferToOffer"));
        list.add(new TaskSubTally("offerToAllocate"));
        list.add(new TaskSubTally("offerToStart"));
        list.add(new TaskSubTally("unofferToAllocate"));
        list.add(new TaskSubTally("unofferToStart"));
        list.add(new TaskSubTally("allocateToStart"));
        list.add(new TaskSubTally("startToComplete"));
        list.add(new TaskSubTally("offerToCancel"));
        list.add(new TaskSubTally("allocateToCancel"));
        list.add(new TaskSubTally("startToCancel"));
        list.add(new TaskSubTally("totalDuration"));
        return list;
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


    private String getRootItemID(String id) {
        String[] parts = id.split(":");
        parts[0] = parts[0].substring(0, parts[0].lastIndexOf('.'));
        return parts[0] + ":" + parts[1];
    }


    /*********************************************************************/

    class TaskSummary {
        long offerTime = 0;
        long unofferTime = 0;
        long allocTime = 0;
        long startTime = 0;
        long completeTime = 0;
        long cancelTime = 0;
        String id;
        String allocateID;
        String startID;
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

        public Set<String> getOfferSet() { return offerSet; }

        public void setOfferSet(Set<String> set) { offerSet = set; }

        public int getOfferCount() { return offerSet.size(); }

        public void addOffer(String id) { offerSet.add(id); }

        public String getAllocateID() { return allocateID; }

        public void setAllocateID(String aID) { allocateID = aID; }

        public String getStartID() { return startID; }

        public void setStartID(String sID) { startID = sID; }

        public long getOfferToAlloc() {
            return (offerTime == 0) || (allocTime == 0) ? 0 : allocTime - offerTime;
        }

        public long getAllocToStart() {
            return (allocTime == 0) || (startTime == 0) ? 0 : startTime - allocTime;
        }

        public long getOfferToStart() {
            if (getAllocToStart() > 0) return 0;
            return (offerTime == 0) || (startTime == 0) ? 0 : startTime - offerTime;
        }

        public long getStartToComplete() {
            return (startTime == 0) || (completeTime == 0) ? 0 : completeTime - startTime;
        }

        public long getStartToCancel() {
            return (startTime == 0) || (cancelTime == 0) ? 0 : cancelTime - startTime;
        }

        public long getAllocToCancel() {
            if (getStartToCancel() > 0) return 0;
            return (allocTime == 0) || (cancelTime == 0) ? 0 : cancelTime - allocTime;
        }

        public long getOfferToCancel() {
            if (getAllocToCancel() > 0) return 0;
            return (offerTime == 0) || (cancelTime == 0) ? 0 : offerTime - startTime;
        }

        public long getUnofferToOffer() {
            return (unofferTime == 0) || (offerTime == 0) ? 0 : offerTime - unofferTime;
        }

        public long getUnofferToAlloc() {
            if (getUnofferToOffer() > 0) return 0;
            return (unofferTime == 0) || (allocTime == 0) ? 0 : allocTime - unofferTime;
        }

        public long getUnofferToStart() {
            if (getUnofferToAlloc() > 0) return 0;
            return (unofferTime == 0) || (startTime == 0) ? 0 : startTime - unofferTime;
        }

        public long getTotalTime() {
            long start = unofferTime;
            long end = completeTime;
            if (start == 0) start = offerTime;
            if (start == 0) start = allocTime;
            if (start == 0) start = startTime;
            if (end == 0) end = cancelTime;
            return (start == 0) || (end == 0) ? 0 : end - start;
        }
    }


    /***************************************************************************/

    class TaskSubTally {
        long max = 0, min = Long.MAX_VALUE, total = 0;
        int count = 0;
        String name;

        public TaskSubTally(String name) { this.name = name; }

        public void add(long period) {
            if (period > 0) {
                total += period;
                max = Math.max(max, period);
                min = Math.min(min, period);
                count++;
            }
        }

        public XNode getNode() {
            XNode node = new XNode(name);
            node.addChild("instances", count);
            node.addChild("min", StringUtil.formatTime(min));
            node.addChild("max", StringUtil.formatTime(max));
            node.addChild("average", StringUtil.formatTime(total / count));
            return node;
        }
    }
        
}
