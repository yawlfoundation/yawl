package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.resourcing.*;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SetSecondaryResourcesPanel extends ResourcingWizardPanel {

    private static final long serialVersionUID = 1L;

    private ResourcesPanel resourcesPanel;
    private ListPanel listPanel;

    public SetSecondaryResourcesPanel(ManageResourcingDialog dialog) {
        super(dialog);
    }

    protected void buildInterface() {
        setBorder(new EmptyBorder(5,5,11,5));

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        setLayout(gbl);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,0,15,0);

        JLabel discussion = new JLabel(
                "<html><body>Secondary resources are those additional resources that are " +
                        "required to carry out a task. They may include both human and " +
                        "non-human resources. Secondary human resources don't see the " +
                        "task on their worklist.<br><br>" +
                        "A resourced task must have one primary (human) resource, and " +
                        "may have zero or more secondary resources. Please select the " +
                        "secondary resources required for the task below.</body></html>"
        );

        add(discussion,gbc);

        gbc.gridy++;
        gbc.weightx = 0.666;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0,5,0,0);
        add(buildResourcesPanel(), gbc);

        gbc.gridx += 2;
        gbc.weightx = 0.333;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0,0,0,0);
        add(buildListPanel(), gbc);
    }

    public String getWizardStepTitle() {
        return "Secondary Resources";
    }


    private JPanel buildResourcesPanel() {
        resourcesPanel = new ResourcesPanel(this);
        return resourcesPanel;
    }

    private JPanel buildListPanel() {
        listPanel = new ListPanel(this);
        return listPanel;
    }


    protected void initialise() {
        // TODO: Initialise widgets
    }

    public void doBack() {
    }

    public boolean doNext() {
        return true;
    }

    public void refresh() {
        resourcesPanel.refresh();
        listPanel.initList();
        listPanel.refresh();        
    }

    public ManageResourcingDialog getResourcingDialog() {
        return (ManageResourcingDialog) getDialog();
    }

    public boolean shouldDoThisStep() {
        return getResourceMapping().getOfferInteractionPoint() ==
                ResourceMapping.SYSTEM_INTERACTION_POINT;
    }

    public void addSelection(Object selected) {
        if (selected != null) {
            listPanel.addSelected(selected);
        }
    }


    /********************************************************************************/
    /********************************************************************************/

    class ResourcesPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        private SetSecondaryResourcesPanel parent;
        private ParticipantsPanel participantsPanel;
        private RolePanel rolePanel;
        private AssetsPanel assetsPanel;
        private CategoryPanel categoryPanel;


        public ResourcesPanel(SetSecondaryResourcesPanel parent) {
            super();
            this.parent = parent;
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Available Resources"));

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();

            setLayout(gbl);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.insets = new Insets(0,2,0,2);
            gbc.fill = GridBagConstraints.BOTH;

            participantsPanel = new ParticipantsPanel(this);
            add(participantsPanel, gbc);

            gbc.gridx++;

            rolePanel = new RolePanel(this);
            add(rolePanel, gbc);

            gbc.gridx = 0;
            gbc.gridy++;

            assetsPanel = new AssetsPanel(this);
            add(assetsPanel, gbc);

            gbc.gridx++;

            categoryPanel = new CategoryPanel(this);
            add(categoryPanel, gbc);
        }

        protected void refresh() {
            participantsPanel.refresh();
            rolePanel.refresh();
            assetsPanel.refresh();
            categoryPanel.refresh();
        }

        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

        public void addSelection(Object selected) {
            parent.addSelection(selected);
        }
    }


    /********************************************************************************/

    class ParticipantsPanel extends JPanel implements ListSelectionListener {

        private static final long serialVersionUID = 1L;
        private UserList userList;
        private ResourcesPanel parent;

        public ParticipantsPanel(ResourcesPanel parent) {
            super();
            this.parent = parent;
            setLayout(new GridBagLayout());
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Participants"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(buildUserList(), gbc);
        }


        private JScrollPane buildUserList() {
            userList = new UserList();
            userList.getSelectionModel().addListSelectionListener(this);
            return new JScrollPane(userList);
        }

        public void setUserList(List<ResourcingParticipant> users) {
            userList.setUsers(users);
        }

        public void refresh() {
            setUserList(ResourcingServiceProxy.getInstance().getAllParticipants());
        }

        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

        public void valueChanged(ListSelectionEvent e) {
            if ((! e.getValueIsAdjusting()) && userList.isEnabled()) {
                parent.addSelection(userList.getSelected());
                userList.clearSelection();
            }
        }
    }

    /********************************************************************************/

    class UserList extends JList {

        private static final long serialVersionUID = 1L;
        private List<ResourcingParticipant> users;

        public UserList() {
            super();
        }

        public void setUsers(List<ResourcingParticipant> users) {
            setEnabled(false);
            this.users = users;
            String[] userNames = new String[users.size()];
            for(int i = 0; i < users.size(); i++) {
                userNames[i] = users.get(i).getName();
            }
            setListData(userNames);
            setEnabled(true);
        }


        public ResourcingParticipant getSelected() {
            int i = getSelectedIndex();
            return (i > -1) ? users.get(i) : null; 
        }
    }


    /********************************************************************************/
    /********************************************************************************/

    class RolePanel extends JPanel implements ListSelectionListener {

        private static final long serialVersionUID = 1L;
        private RoleList roleList;
        private ResourcesPanel parent;

        public RolePanel(ResourcesPanel parent) {
            super();
            setLayout(new GridBagLayout());
            this.parent = parent;
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Roles"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(buildRoleList(), gbc);
        }


        private JScrollPane buildRoleList() {
            roleList = new RoleList();
            roleList.getSelectionModel().addListSelectionListener(this);
            return new JScrollPane(roleList);
        }

        public void setRoleList(List<ResourcingRole> roles) {
            roleList.setRoles(roles);
        }

        public void refresh() {
            setRoleList(ResourcingServiceProxy.getInstance().getAllRoles());
        }

        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

        public void valueChanged(ListSelectionEvent e) {
            if ((! e.getValueIsAdjusting()) && roleList.isEnabled())  {
                parent.addSelection(roleList.getSelected());
                roleList.clearSelection();
            }
        }
    }

    /********************************************************************************/

    class RoleList extends JList {

        private static final long serialVersionUID = 1L;
        private List<ResourcingRole> roles;

        public RoleList() {
            super();
        }

        public void setRoles(List<ResourcingRole> roles) {
            setEnabled(false);
            this.roles = roles;
            String[] roleNames = new String[roles.size()];
            for(int i = 0; i < roles.size(); i++) {
                roleNames[i] = roles.get(i).getName();
            }
            setListData(roleNames);
            setEnabled(true);
        }

        public ResourcingRole getSelected() {
            int i = getSelectedIndex();
            return (i > -1) ? roles.get(i) : null; 
        }
    }


    /********************************************************************************/

    class AssetsPanel extends JPanel implements ListSelectionListener {

        private static final long serialVersionUID = 1L;
        private AssetsList assetsList;
        private ResourcesPanel parent;

        public AssetsPanel(ResourcesPanel parent) {
            super();
            setLayout(new GridBagLayout());
            this.parent = parent;
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Assets"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(buildUserList(), gbc);
        }


        private JScrollPane buildUserList() {
            assetsList = new AssetsList();
            assetsList.getSelectionModel().addListSelectionListener(this);
            return new JScrollPane(assetsList);
        }

        public void setUserList(List<ResourcingAsset> assets) {
            assetsList.setAssets(assets);
        }

        public void refresh() {
            setUserList(ResourcingServiceProxy.getInstance().getAllNonHumanResources());
        }

        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

        public void valueChanged(ListSelectionEvent e) {
            if ((! e.getValueIsAdjusting()) && assetsList.isEnabled()) {
                parent.addSelection(assetsList.getSelected());
                assetsList.clearSelection();
            }
        }
    }

    /********************************************************************************/

    class AssetsList extends JList {

        private static final long serialVersionUID = 1L;
        private List<ResourcingAsset> assets;

        public AssetsList() {
            super();
        }

        public void setAssets(List<ResourcingAsset> assets) {
            setEnabled(false);
            this.assets = assets;
            String[] assetNames = new String[assets.size()];
            for(int i = 0; i < assets.size(); i++) {
                assetNames[i] = assets.get(i).getName();
            }
            setListData(assetNames);
            setEnabled(true);
        }


        public ResourcingAsset getSelected() {
            int i = getSelectedIndex();
            return (i > -1) ? assets.get(i) : null; 
        }
    }


    /********************************************************************************/
    /********************************************************************************/

    class CategoryPanel extends JPanel implements ListSelectionListener {

        private static final long serialVersionUID = 1L;
        private CategoryList categoryList;
        private ResourcesPanel parent;

        public CategoryPanel(ResourcesPanel parent) {
            super();
            setLayout(new GridBagLayout());
            this.parent = parent;
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Categories"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(buildCategoryList(), gbc);
        }


        private JScrollPane buildCategoryList() {
            categoryList = new CategoryList();
            categoryList.getSelectionModel().addListSelectionListener(this);
            return new JScrollPane(categoryList);
        }

        public void setCategoryList(List<ResourcingCategory> categories) {
            categoryList.setCategories(categories);
        }

        public void refresh() {
            setCategoryList(ResourcingServiceProxy.getInstance().getAllNonHumanCategories());
        }

        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

        public void valueChanged(ListSelectionEvent e) {
            if ((! e.getValueIsAdjusting()) && categoryList.isEnabled())  {
                parent.addSelection(categoryList.getSelected());
                categoryList.clearSelection();
            }
        }
    }

    /********************************************************************************/

    class CategoryList extends JList {

        private static final long serialVersionUID = 1L;
        private List<ResourcingCategory> categories;

        public CategoryList() {
            super();
        }

        public void setCategories(List<ResourcingCategory> categories) {
            setEnabled(false);
            this.categories = categories;
            String[] categoryLabels = new String[categories.size()];
            for(int i = 0; i < categories.size(); i++) {
                categoryLabels[i] = categories.get(i).getListLabel();
            }
            setListData(categoryLabels);
            setEnabled(true);
        }

        public ResourcingCategory getSelected() {
            int i = getSelectedIndex();
            return (i > -1) ? categories.get(i) : null;
        }
    }


    /********************************************************************************/
    /********************************************************************************/
    // Selected List //

    class ListPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private SecondaryList list;

        private SetSecondaryResourcesPanel parent;

        public ListPanel(SetSecondaryResourcesPanel parent) {
            super();
            this.parent = parent;
            buildInterface();
        }

        private void buildInterface() {
            setBorder(new TitledBorder("Selected Resources"));

            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();

            setLayout(gbl);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 0.95;
            gbc.insets = new Insets(0,5,0,5);
            gbc.fill = GridBagConstraints.BOTH;

            add(buildList(), gbc);
            gbc.weighty = 0.05;
            addUnselectButton(gbc);
        }

        private void addUnselectButton(GridBagConstraints gbc) {
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0,0,0,0);
            JButton unselectButton = new JButton("Remove");
            unselectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = list.getSelectedIndex();
                    if (index > -1) {
                        list.removeSelectedResource(list.getSelectedIndex());
                    }    
                }
            });

            add(unselectButton, gbc);
        }

        private JScrollPane buildList() {
            list = new SecondaryList(this);
            return new JScrollPane(list);
        }


        public void initList() {
            list.getResourceList().clearAll();
            List<Object> resources = getResourceMapping().getSecondaryResourcesList();
            if (resources != null) {
                for (Object o : resources) {
                    addSelected(o);
                }
            }
        }

        
        public void addSelected(Object o) {
            SelectedResources resources = list.getResourceList();
            if (o instanceof ResourcingParticipant) resources.addParticipant((ResourcingParticipant) o);
            else if (o instanceof ResourcingAsset) resources.addResource((ResourcingAsset) o);
            else if (o instanceof ResourcingRole) resources.addRole((ResourcingRole) o);
            else if (o instanceof ResourcingCategory) resources.addCategory((ResourcingCategory) o);

            refresh();
        }

        public void refresh() { list.refresh(); }


        protected ResourceMapping getResourceMapping() {
            return parent.getResourceMapping();
        }

    }


    class SecondaryList extends JList {
        private static final long serialVersionUID = 1L;

        private SelectedResources resources;
        private ListPanel parent;

        public SecondaryList(ListPanel parent) {
            super();
            this.parent = parent;
            resources = new SelectedResources();
        }

        public void setResources(SelectedResources resources) {
            this.resources = resources;
            refresh();
        }


        public void removeSelectedResource(int index) {
            resources.remove(index);
            refresh();
        }

        public void refresh() {
            setEnabled(false);
            setListData(resources.getLabels());
            setEnabled(true);
            parent.getResourceMapping().setSecondaryResourcesList(resources.getAll());
        }

        public SelectedResources getResourceList() { return resources; }
    }


    class SelectedResources {

        List<ResourcingParticipant> participants = new ArrayList<ResourcingParticipant>();
        List<ResourcingAsset> resources = new ArrayList<ResourcingAsset>();
        List<ResourcingRole> roles = new ArrayList<ResourcingRole>();
        List<ResourcingCategory> categories = new ArrayList<ResourcingCategory>();

        boolean addParticipant(ResourcingParticipant participant) {
            return (! participants.contains(participant)) && participants.add(participant);
        }

        boolean addResource(ResourcingAsset resource) {
            return (! resources.contains(resource)) && resources.add(resource);
        }

        boolean addRole(ResourcingRole role) {
            return roles.add(role);
        }

        boolean addCategory(ResourcingCategory category) {
            return categories.add(category);
        }

        void remove(int index) {
            if (index < 0) return;
            if (index < participants.size()) {
                participants.remove(index);
                return;
            }
            index -= participants.size();
            if (index < resources.size()) {
                resources.remove(index);
                return;
            }
            index -= resources.size();
            if (index < roles.size()) {
                roles.remove(index);
                return;
            }
            index -= roles.size();
            if (index < categories.size()) categories.remove(index);
        }


        Object[] getLabels() {
            List<String> labels = new ArrayList<String>();
            for (ResourcingParticipant participant : participants) {
                labels.add(participant.getName());
            }
            for (ResourcingAsset resource : resources) {
                labels.add(resource.getName());
            }
            for (ResourcingRole role : roles) {
                labels.add(role.getName());
            }
            for (ResourcingCategory category : categories) {
                labels.add(category.getListLabel());
            }
            return labels.toArray();
        }

       List<Object> getAll() {
            List<Object> contents = new ArrayList<Object>();
            contents.addAll(participants);
            contents.addAll(resources);
            contents.addAll(roles);
            contents.addAll(categories);
            return contents;
        }

        void clearAll() {
            participants.clear();
            resources.clear();
            roles.clear();
            categories.clear();
        }
    }

}