/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a set of secondary resources for a single task (via a ResourceMap)
 *
 * @author Michael Adams
 * @date 11/05/2011
 */
public class SecondaryResources {

    private final ResourceManager _rm;
    private final SecResDataSet _defaultDataSet;
    private final Logger _log;

    // a map of runtime-changed resources (by an administrator)
    private Map<String, SecResDataSet> _itemToDataSetMap;

    public SecondaryResources() {
        _rm = ResourceManager.getInstance();
        _defaultDataSet = new SecResDataSet();
        _log = LogManager.getLogger(this.getClass());
    }


    /***********************************************************************/

    public String toXML() {
        return _defaultDataSet.toXML();
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        _defaultDataSet.parse(e, nsYawl);
    }


    /***********************************************************************/

    public void engage(WorkItemRecord wir) {
        String wirID = getReferenceItemID(wir);
        getDataSet(wirID).engage(wir);
    }


    public void disengage(WorkItemRecord wir) {
        String wirID = getReferenceItemID(wir);
        getDataSet(wirID).disengage(wir);
        removeDataSet(wirID);
    }


    private String getReferenceItemID(WorkItemRecord wir) {
        String parentID = wir.getParentID();
        return (parentID != null) ? parentID : wir.getID();
    }


    public boolean available(WorkItemRecord wir) {
        return getDataSet(wir).available(wir);
    }

    
    public List<String> checkAvailability(WorkItemRecord wir) {
        return getDataSet(wir).checkAvailability();
    }


    /*************************************************************************/

    public SecResDataSet newDataSet() { return new SecResDataSet(); }


    public SecResDataSet getDefaultDataSet() {
        return _defaultDataSet;
    }


    public SecResDataSet getDataSet(WorkItemRecord wir) {
        return getDataSet(wir.getID());
    }


    public SecResDataSet addDataSet(WorkItemRecord wir, SecResDataSet dataSet) {
        if (_itemToDataSetMap == null) {
            _itemToDataSetMap = new ConcurrentHashMap<String, SecResDataSet>();
        }
        return _itemToDataSetMap.put(wir.getID(), dataSet);
    }


    public SecResDataSet removeDataSet(WorkItemRecord wir) {
        return removeDataSet(wir.getID());
    }


    public void removeDataSetsForCase(String caseID) {
        if (_itemToDataSetMap != null) {
           Set<String> toRemove = new HashSet<String>();

           for (String id : _itemToDataSetMap.keySet()) {
                if (id.startsWith(caseID) && (":.".indexOf(id.charAt(caseID.length())) > -1)) {
                    toRemove.add(id);
                }
            }
            for (String id : toRemove) _itemToDataSetMap.remove(id);
        }    
    }


    /**************************************************************************/

    private boolean isAvailable(AbstractResource resource) {
        return resource.isAvailable() && isDisengaged(resource.getID());
    }


    private void announceUnavailable(AbstractResource resource, WorkItemRecord wir) {
        ResourceManager.getInstance().getClients().announceResourceUnavailable(
                resource, wir, false);
        _log.warn("Secondary Resource '" + resource.getName() +
                "' unavailable for work item: " + wir.getID());
    }


    private void announceUnavailable(String name, WorkItemRecord wir) {
        ResourceManager.getInstance().getClients().announceResourceUnavailable(
                null, wir, false);
        _log.warn("There are no available members of " + name +
                "' to allocate as a secondary resource for work item: " + wir.getID());
    }


    private AbstractResource getRandomAvailableResource(List<AbstractResource> resourceList) {
        if ((resourceList == null) || resourceList.isEmpty()) {
            return null;     // none found
        }
        
        // while the list has resources, remove a random selection and check if a
        // reservation for it would succeed
        while (resourceList.size() > 0) {
            AbstractResource actual = resourceList.remove(
                    (int) Math.floor(Math.random() * resourceList.size()));
            if (isAvailable(actual)) {
                return actual;                                      // found a candidate
            }
        }
        return null;
    }


    private SecResDataSet getDataSet(String id) {
        SecResDataSet dataSet = null;
        if (_itemToDataSetMap != null) {
            dataSet = _itemToDataSetMap.get(id);
        }
        return (dataSet != null) ? dataSet : _defaultDataSet;
    }


    private SecResDataSet removeDataSet(String id) {

        if (_itemToDataSetMap != null) {
            return _itemToDataSetMap.remove(id);
        }
        return null;
    }    


    private boolean isDisengaged(String resourceID) {
        List result = LogMiner.getInstance().getLastBusyOrReleaseEvents(resourceID);
        if (result.size() > 0) {
            long busyStamp = -1, releasedStamp = -1;
            for (Object o : result) {
                Object[] content = (Object[]) o;
                if (content[0].equals("busy")) {
                    busyStamp = (Long) content[1];
                }
                else releasedStamp = (Long) content[1];
            }

            // if two events, it's available if released comes after busy
            return releasedStamp >= busyStamp;
        }
        return true;                                       // no result = available
    }


    /*****************************************************************************/

    public class SecResDataSet {

        private final Set<Participant> participants;                // unique resource
        private final Set<NonHumanResource> nonHumanResources;      // unique resource
        private final List<Role> roles;              // allows duplicates

        // [id::(optional)subcat, category]
        private final Map<String, CategoryStack> nonHumanCategories;


        public SecResDataSet() {
            participants = new HashSet<Participant>();
            roles = new ArrayList<Role>();
            nonHumanResources = new HashSet<NonHumanResource>();
            nonHumanCategories = new HashMap<String, CategoryStack>();
        }


        public void addParticipant(String id) {
            Participant p = _rm.getOrgDataSet().getParticipant(id);
            if (p != null) participants.add(p);
            else _log.warn("Unknown Participant ID in secondary resources spec: " + id);
        }


        public void addParticipant(Participant p) {
            if (_rm.getOrgDataSet().isKnownParticipant(p))
                participants.add(p);
            else
                _log.warn("Could not add unknown Participant to secondary resources: " +
                        p.getID());
        }


        public void addParticipantUnchecked(String id) {
            participants.add(new Participant(id));
        }



        public void addRole(String id) {
            Role role = _rm.getOrgDataSet().getRole(id);
            if (role != null) roles.add(role);
            else _log.warn("Unknown Role ID in secondary resources spec: " + id);
        }


        public void addRole(Role role) {
            if (_rm.getOrgDataSet().isKnownRole(role))
                roles.add(role);
            else
                _log.warn("Could not add unknown Role to secondary resources: " +
                        role.getID());
        }


        public void addRoleUnchecked(String id) {
            Role role = new Role();
            role.setID(id);
            roles.add(role);
        }



        public void addNonHumanResource(String id) {
            NonHumanResource resource = _rm.getOrgDataSet().getNonHumanResource(id);
            if (resource != null) nonHumanResources.add(resource);
            else _log.warn("Unknown nonhuman resource ID in secondary resources spec: " + id);
        }


        public void addNonHumanResource(NonHumanResource resource) {
            if (_rm.getOrgDataSet().isKnownNonHumanResource(resource))
                nonHumanResources.add(resource);
            else
                _log.warn("Could not add unknown nonhuman resource to secondary resources: " +
                        resource.getID());
        }


        public void addNonHumanResourceUnchecked(String id) {
            nonHumanResources.add(new NonHumanResource(id));
        }



        public boolean addNonHumanCategory(String id) {
            NonHumanCategory category = getCategoryByID(id);
            if (category != null) {
                putNonHumanCategory(id, category);
                return true;
            }
            _log.warn("Unknown nonhuman category ID in secondary resources spec: " + id);
            return false;
        }


        public boolean addNonHumanCategory(NonHumanCategory category) {
            return addNonHumanCategory(category.getID(), category);
        }


        public boolean addNonHumanCategoryUnchecked(String id) {
            NonHumanCategory category = new NonHumanCategory();
            category.setID(id);
            putNonHumanCategory(id, category);
            return true;
        }


        public boolean addNonHumanCategory(String id, String subcat) {
            if (subcat != null) id += "::" + subcat;
            return addNonHumanCategory(id);
        }


        public boolean addNonHumanCategory(NonHumanCategory category, String subcat) {
            String id = category.getID();
            if (subcat != null) id += "::" + subcat;
            return addNonHumanCategory(id, category);
        }


        public boolean addNonHumanCategoryUnchecked(String id, String subcat) {
            NonHumanCategory category = new NonHumanCategory();
            category.setID(id);
            String mapID = (subcat != null) ? id += "::" + subcat : id;
            putNonHumanCategory(mapID, category);
            return true;
        }


        public boolean remove(String id) {

            // try participant or nonhuman resource first
            AbstractResource resource = _rm.getOrgDataSet().getResource(id);
            if (resource != null) {
                if (resource instanceof Participant) return participants.remove(resource);
                else return nonHumanResources.remove(resource);
            }

            // ... then role
            Role role = _rm.getOrgDataSet().getRole(id);
            if (role != null) return roles.remove(role);

            // ... and finally categories
            CategoryStack stack = nonHumanCategories.get(id);
            if (stack != null) {
                stack.removeOne();
                if (stack.isEmpty()) nonHumanCategories.remove(id);
                return true;
            }
            return false;
        }


        public Set<Participant> getParticipants() { return participants; }

        public List<Role> getRoles() { return roles; }

        public Set<NonHumanResource> getNonHumanResources() { return nonHumanResources; }

        public Map<String, CategoryStack> getNonHumanCategories() {
            return nonHumanCategories;
        }


        public SecResDataSet copy() {
            SecResDataSet copied = newDataSet();
            copied.getParticipants().addAll(participants);
            copied.getRoles().addAll(roles);
            copied.getNonHumanResources().addAll(nonHumanResources);
            copied.getNonHumanCategories().putAll(copyNonHumanCategories());
            return copied;
        }


        public boolean hasResources() {
            return getResourcesCount() > 0;
        }


        public int getResourcesCount() {
            return participants.size() + roles.size() +
                    nonHumanResources.size() + getCategoryCount();
        }


        public boolean available(WorkItemRecord wir) {
            return getResourcesCount() == selectResources(wir).size();
        }


        public List<String> checkAvailability() {
            List<String> problems = new ArrayList<String>();
            if (hasResources()) {
                for (Participant p : participants) {
                    if (! isAvailable(p)) problems.add(problemMsg(p.getName()));
                }
                List<AbstractResource> taken = new ArrayList<AbstractResource>(participants);
                for (Role r : roles) {
                    AbstractResource selection = selectOneFromRole(r, taken);
                    if (selection == null) problems.add(problemMsg("Role ", r.getName(),
                            isMultiSelection(r, roles)));
                    else taken.add(selection);
                }
                for (NonHumanResource r : nonHumanResources) {
                    if (! isAvailable(r)) problems.add(problemMsg(r.getName()));
                }
                taken.clear();
                taken.addAll(nonHumanResources);
                for (String id : nonHumanCategories.keySet()) {
                    CategoryStack c = nonHumanCategories.get(id);
                    String subcat = getSubcatFromID(id);
                    AbstractResource selection = selectOneFromCategory(c.get(), subcat, taken);
                    if (selection == null) {
                        String label = c.get().getName();
                        if (subcat != null) {
                            label += " (subcategory " + subcat + ") ";
                        }
                        problems.add(problemMsg("Category ", label, c.getCounter() > 1));
                    }
                }
            }
            return problems;
        }


        public String getNonHumanCategoryDisplayLabel(String id) {
            CategoryStack stack = nonHumanCategories.get(id);
            if (stack != null) {
                String label = stack.get().getName();
                String subcat = getSubcatFromID(id);
                if (subcat != null) label += " -> " + subcat;
                return label;
            }
            else return "[unknown]";
        }


        public List<String> getCategoryLabelList(String id) {
            List<String> labelList = new ArrayList<String>();
            CategoryStack stack = nonHumanCategories.get(id);
            if (stack != null) {
                String label = getNonHumanCategoryDisplayLabel(id);
                for (int i=0; i < stack.getCounter(); i++) {
                    labelList.add(label);
                }
            }
            return labelList;
        }


        public void clear() {
            participants.clear();
            roles.clear();
            nonHumanResources.clear();
            nonHumanCategories.clear();
        }


        /***********************************************************************/

        protected String toXML() {
            if (! hasResources()) return "";

            XNode node = new XNode("secondary");
            for (Participant p : participants) {
                node.addChild("participant", p.getID());
            }
            for (Role r : roles) {
                node.addChild("role", r.getID());
            }
            for (NonHumanResource r : nonHumanResources) {
                node.addChild("nonHumanResource", r.getID());
            }
            for (String id : nonHumanCategories.keySet()) {
                CategoryStack c = nonHumanCategories.get(id);
                String subcat = getSubcatFromID(id);
                for (int i=0; i < c.getCounter(); i++) {
                    XNode child = node.addChild("nonHumanCategory", c.get().getID());
                    if (subcat != null) {
                        child.addAttribute("subcategory", subcat);
                    }
                }
            }
            return node.toPrettyString();
        }


        protected void parse(Element e, Namespace nsYawl) throws ResourceParseException {
            if (e == null) return;
            for (Element ePart : e.getChildren("participant", nsYawl)) {
                addParticipant(ePart.getText());
            }
            for (Element eRole : e.getChildren("role", nsYawl)) {
                addRole(eRole.getText());
            }
            for (Element eNHR : e.getChildren("nonHumanResource", nsYawl)) {
                addNonHumanResource(eNHR.getText());
            }
            for (Element eCat : e.getChildren("nonHumanCategory", nsYawl)) {
                addNonHumanCategory(eCat.getText(), eCat.getAttributeValue("subcategory"));
            }
        }


        protected void engage(WorkItemRecord wir) {
            if (hasResources()) {
                for (AbstractResource resource : selectResources(wir)) {
                    EventLogger.log(wir, resource.getID(), EventLogger.event.busy);
                }
            }
        }


        protected void disengage(WorkItemRecord wir) {
            if (hasResources()) {
                for (Object o : LogMiner.getInstance().getBusyResources(wir.getID())) {
                    ResourceEvent event = (ResourceEvent) o;
                    EventLogger.log(wir, event.get_resourceID(), EventLogger.event.released);
                }
            }
        }



        protected List<AbstractResource> selectResources(WorkItemRecord wir) {
            List<AbstractResource> selected = new ArrayList<AbstractResource>();
            if (hasResources()) {
                for (Participant p : participants) {
                    if (isAvailable(p)) selected.add(p);
                    else announceUnavailable(p, wir);
                }
                for (Role r : roles) {
                    AbstractResource selection = selectOneFromRole(r, selected);
                    if (selection != null) selected.add(selection);
                    else announceUnavailable("role '" + r.getName(), wir);
                }
                for (NonHumanResource r : nonHumanResources) {
                    if (isAvailable(r)) selected.add(r);
                    else announceUnavailable(r, wir);
                }
                for (String id : nonHumanCategories.keySet()) {
                    CategoryStack c = nonHumanCategories.get(id);
                    String subcat = getSubcatFromID(id);
                    AbstractResource selection = selectOneFromCategory(c.get(), subcat, selected);
                    if (selection != null) selected.add(selection);
                    else announceUnavailable("nonhuman resource category '" +
                            c.get().getName(), wir);
                }
            }
            return selected;
        }


        private NonHumanCategory getCategoryByID(String id) {
            int pos = id.indexOf("::");
            String cleanID = (pos > -1) ? id.substring(0, pos) : id;
            return _rm.getOrgDataSet().getNonHumanCategory(cleanID);
        }


        private boolean addNonHumanCategory(String id, NonHumanCategory category) {
            if (_rm.getOrgDataSet().isKnownNonHumanCategory(category)) {
                putNonHumanCategory(id, category);
                return true;
            }
            _log.warn("Could not add unknown nonhuman category to secondary resources: " +
                    id);
            return false;
        }


        private void putNonHumanCategory(String id, NonHumanCategory category) {
            CategoryStack stack = nonHumanCategories.get(id);
            if (stack == null) {
                nonHumanCategories.put(id, new CategoryStack(category));
            }
            else stack.add(category);
        }


        private Map<String, CategoryStack> copyNonHumanCategories() {
            Map<String, CategoryStack> copied = new HashMap<String, CategoryStack>();
            for (String id : nonHumanCategories.keySet()) {
                copied.put(id, nonHumanCategories.get(id).copy());
            }
            return copied;
        }


        private int getCategoryCount() {
            int counter = 0;
            for (CategoryStack stack : nonHumanCategories.values()) {
                counter += stack.getCounter();
            }
            return counter;
        }


        private AbstractResource selectOneFromRole(Role r, List<AbstractResource> selected) {
            List<AbstractResource> roleList = new ArrayList<AbstractResource>(r.getResources());
            roleList.removeAll(selected);
            return getRandomAvailableResource(roleList);
        }


        private AbstractResource selectOneFromCategory(NonHumanCategory c, String subcat,
                 List<AbstractResource> selected) {
            List<AbstractResource> categoryList =
                    new ArrayList<AbstractResource>(c.getSubCategoryResources(subcat));
            categoryList.removeAll(selected);
            return getRandomAvailableResource(categoryList);
        }


        private String getSubcatFromID(String id) {
            int pos = id.indexOf("::");
            return (pos > -1) ? id.substring(pos + 2) : null;
        }


        private <T> boolean isMultiSelection(T selection, List<T> list) {
            int hits = 0;
            for (T listed : list) {
                if (listed.equals(selection)) hits++;
                if (hits > 1) break;
            }
            return hits > 1;
        }
                

        private String problemMsg(String name) {
            return name + " is not currently available.";
        }
       
        private String problemMsg(String type, String name, boolean multi) {
            String constraint = (multi ? "insufficient" : "no");
            return String.format("%s %s has %s resources currently available.",
                    type, name, constraint);
        }

    }


    class CategoryStack {
        private NonHumanCategory category;
        private int counter = 0;

        CategoryStack() { }

        CategoryStack(NonHumanCategory category) { add(category); }

        public void add(NonHumanCategory o) {
            if (category == null) category = o;
            counter++;
        }

        public void removeOne() {
            counter--;
            if (counter == 0) category = null;
        }

        public boolean isEmpty() { return counter == 0; }

        public int getCounter() { return counter; }

        public NonHumanCategory get() { return category; }

        public CategoryStack copy() {
            CategoryStack copied = new CategoryStack();
            copied.add(this.category);
            copied.counter = this.counter;
            return copied;
        }
        
    }

}
