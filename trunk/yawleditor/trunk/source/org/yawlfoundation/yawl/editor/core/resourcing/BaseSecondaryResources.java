package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class BaseSecondaryResources {

    private Set<Participant> participants;                // unique resource
    private Set<NonHumanResource> nonHumanResources;      // unique resource
    private List<Role> roles;                             // allows duplicates

    // [id::(optional)subcat, category]
    private final Map<String, CategoryStack> nonHumanCategories;

    public BaseSecondaryResources() {
        participants = new HashSet<Participant>();
        roles = new ArrayList<Role>();
        nonHumanResources = new HashSet<NonHumanResource>();
        nonHumanCategories = new HashMap<String, CategoryStack>();
    }


    public void addParticipant(String id) {
         participants.add(new Participant(id));
    }

    public void addRole(String id) {
        Role role = new Role();
        role.setID(id);
        roles.add(role);
    }

    public void addNonHumanResource(String id) {
        nonHumanResources.add(new NonHumanResource(id));
    }


    public boolean addNonHumanCategory(String id) {
        NonHumanCategory category = new NonHumanCategory();
        category.setID(id);
        putNonHumanCategory(id, category);
        return true;
    }


    public boolean addNonHumanCategory(String id, String subcat) {
        if (subcat != null) id += "::" + subcat;
        return addNonHumanCategory(id);
    }


    public boolean hasResources() {
        return getResourcesCount() > 0;
    }


    public int getResourcesCount() {
        return participants.size() + roles.size() +
                nonHumanResources.size() + getCategoryCount();
    }

    private void putNonHumanCategory(String id, NonHumanCategory category) {
        CategoryStack stack = nonHumanCategories.get(id);
        if (stack == null) {
            nonHumanCategories.put(id, new CategoryStack(category));
        }
        else stack.add(category);
    }


    private int getCategoryCount() {
        int counter = 0;
        for (CategoryStack stack : nonHumanCategories.values()) {
            counter += stack.getCounter();
        }
        return counter;
    }


    private String getSubcatFromID(String id) {
        int pos = id.indexOf("::");
        return (pos > -1) ? id.substring(pos + 2) : null;
    }



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
