package org.yawlfoundation.yawl.editor.ui.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.allocators.GenericAllocator;
import org.yawlfoundation.yawl.resourcing.filters.GenericFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.schema.XSDType;

import java.io.IOException;
import java.util.*;

public class ResourceMapping {

  public static final int SYSTEM_INTERACTION_POINT = 0;
  public static final int USER_INTERACTION_POINT = 1;

  public static final int CAN_SUSPEND_PRIVILEGE              = 100;
  public static final int CAN_REALLOCATE_STATELESS_PRIVILEGE = 101;
  public static final int CAN_REALLOCATE_STATEFUL_PRIVILEGE  = 102;
  public static final int CAN_DEALLOCATE_PRIVILEGE           = 103;
  public static final int CAN_DELEGATE_PRIVILEGE             = 104;
  public static final int CAN_SKIP_PRIVILEGE                 = 105;
  public static final int CAN_PILE_PRIVILEGE                 = 106;

    private YAWLAtomicTask resourceRequiringTask;
    private int offerInteractionPoint;
    private int allocateInteractionPoint;
    private int startInteractionPoint;
    private YAWLAtomicTask retainFamiliarTask;
    private String retainFamiliarTaskID;
    private String separationOfDutiesTaskID;
    private YAWLAtomicTask separationOfDutiesTask;
    private List<Participant> baseUserDistributionList;
    private List<Object> secondaryResourcesList;
    private List<Role> baseRoleDistributionList;
    private List<DataVariableContent> baseVariableContentList;
    private List<AbstractSelector> resourcingFilters;
    private AbstractSelector allocationMechanism;
    private HashSet<Integer> enabledPrivileges;

  public ResourceMapping() {
    super();
    initialise();
  }

  private void initialise() {
    setOfferInteractionPoint(USER_INTERACTION_POINT);
    setAllocateInteractionPoint(USER_INTERACTION_POINT);
    setStartInteractionPoint(USER_INTERACTION_POINT);
    setEnabledPrivileges(new HashSet<Integer>());
  }
  

  public ResourceMapping(YAWLAtomicTask resourceRequiringTask, boolean setVariableList) {
    super();
    initialise();
    setResourceRequiringTask(resourceRequiringTask);
    if (setVariableList)
        setBaseVariableContentList(buildDefaultBaseVariableContentList());
  }
  
  public YAWLAtomicTask getResourceRequiringTask() {
    return resourceRequiringTask;
  }
  
  public void setResourceRequiringTask(YAWLAtomicTask task) {
    resourceRequiringTask = task;
  }

  /* ------ Offer Related Attributes ------ */
  
  public void setOfferInteractionPoint(int setting) {
    offerInteractionPoint = setting;
  }
  
  public int getOfferInteractionPoint() {
    return offerInteractionPoint;
  }
  
  public YAWLAtomicTask getRetainFamiliarTask() {
    return retainFamiliarTask;
  }

  public void setRetainFamiliarTask(YAWLAtomicTask task) {
    retainFamiliarTask = task;
  }

    public void setRetainFamiliarTaskID(String taskid) {
      retainFamiliarTaskID = taskid;
    }

    public String getRetainFamiliarTaskID() {
      return retainFamiliarTaskID;
    }

    public String getSeparationOfDutiesTaskID() {
      return separationOfDutiesTaskID;
    }

    public void setSeparationOfDutiesTaskID(String taskid) {
      separationOfDutiesTaskID = taskid;
    }

  public YAWLAtomicTask getSeparationOfDutiesTask() {
    return separationOfDutiesTask;
  }

  public void setSeparationOfDutiesTask(YAWLAtomicTask task) {
    separationOfDutiesTask = task;
  }
  
  private List<DataVariableContent> buildDefaultBaseVariableContentList() {
    LinkedList<DataVariableContent> list = new LinkedList<DataVariableContent>();

    List<YVariable> validPossibleVariables = getNetVariablesValidForResourcing();
    for(YVariable variable : validPossibleVariables) {
      
      list.add(new DataVariableContent(variable));
    }

    return list;
  }


  private List<YVariable> getNetVariablesValidForResourcing() {
      YNet net = YAWLEditorDesktop.getInstance().getSelectedYNet();
      List<YVariable> variables = new ArrayList<YVariable>();
      for (YVariable variable : net.getLocalVariables().values()) {
          if (variable.getDataTypeName().equals(XSDType.getString(XSDType.STRING))) {
              variables.add(variable);
          }
      }
      for (YVariable variable : net.getInputParameters().values()) {
          if (variable.getDataTypeName().equals(XSDType.getString(XSDType.STRING))) {
              variables.add(variable);
          }
      }

      return variables;
  }


  public void setBaseUserDistributionList(List<Participant> userList) {
    baseUserDistributionList = userList;
  }
  
  public List<Participant> getBaseUserDistributionList() {
    return baseUserDistributionList;
  }

    public void setSecondaryResourcesList(List<Object> list) {
      secondaryResourcesList = list;
    }

    public List<Object> getSecondaryResourcesList() {
      return secondaryResourcesList != null ? secondaryResourcesList :
              Collections.emptyList();
    }

  public void setBaseRoleDistributionList(List<Role> roles) {
    baseRoleDistributionList = roles;
  }
  
  public List<Role> getBaseRoleDistributionList() {
    return baseRoleDistributionList;
  }
  
  public void setBaseVariableContentList(List<DataVariableContent> list) {
    baseVariableContentList = list;
  }
  
  public List<DataVariableContent> getBaseVariableContentList() {
    return baseVariableContentList;
  }

  // remove null members
  public void cleanBaseUserDistributionList() {
      List<Participant> pList = getBaseUserDistributionList();
      if ((pList != null) && pList.contains(null)) {
          List<Participant> cleanList = new LinkedList<Participant>();
          for (Participant p : pList) if (p != null) cleanList.add(p);
          setBaseUserDistributionList(cleanList);
      }
  }

  public void cleanBaseRoleDistributionList() {    
      List<Role> rList = getBaseRoleDistributionList();
      if ((rList != null) && rList.contains(null)) {
          List<Role> cleanRoleList = new LinkedList<Role>();
          for (Role r : rList) if (r != null) cleanRoleList.add(r);
          setBaseRoleDistributionList(cleanRoleList);
      }
  }

  public void cleanDistributionLists() {
      cleanBaseUserDistributionList();
      cleanBaseRoleDistributionList();
  }



  /**
   * Resynchronises the variable content map with changes that may have been
   * applied to the task's data perspective definitions. All new variables that
   * could be used to store resourcing data default to storing plain data initially.
   */
  
  public void syncWithDataPerspective() {
    if (getBaseVariableContentList() == null) {
      return;  // nothing to do if none have been specified.
    }
    
    LinkedList<DataVariableContent> variablesToRemove = new LinkedList<DataVariableContent>();
    for(DataVariableContent variableContent : getBaseVariableContentList()) {
      if (!variableContent.isValidForResourceContainment()) {
        variablesToRemove.add(variableContent);
      }
    }
    
    for(DataVariableContent variableContent : variablesToRemove) {
      getBaseVariableContentList().remove(variableContent);
    }
    
    List<YVariable> variablesToAdd = new ArrayList<YVariable>();
    for(YVariable variable : getNetVariablesValidForResourcing()) {
      boolean variableFound = false;
      for(DataVariableContent variableContent : getBaseVariableContentList()) {
        if (variableContent.getVariable() == variable) {
          variableFound = true;
          break;
        }
      }
      if (!variableFound && DataVariableContent.isValidForResourceContainment(variable)) {
        variablesToAdd.add(variable);
      }
    }
    
    for(YVariable variable: variablesToAdd) {
      getBaseVariableContentList().add(
          new DataVariableContent(variable)
      );
    }
  }

  public void setResourcingFilters(List<AbstractSelector> filters) {
    resourcingFilters = filters;
  }
  
  public List<AbstractSelector>  getResourcingFilters() {
    return resourcingFilters;
  }
  
  /* ------ Allocation Related Attributes ------ */
  
  public void setAllocateInteractionPoint(int setting) {
    allocateInteractionPoint = setting;
  }
  
  public int getAllocateInteractionPoint() {
    return allocateInteractionPoint;
  }

  public AbstractSelector getAllocationMechanism() {
    return getAllocateInteractionPoint() == SYSTEM_INTERACTION_POINT ?
       allocationMechanism : null;
  }
  
  public void setAllocationMechanism(AbstractSelector allocator) {
    allocationMechanism = allocator;
  }

  /* ------ Start Related Attributes ------ */
  
  public void setStartInteractionPoint(int setting) {
    startInteractionPoint = setting;
  }

  
  public int getStartInteractionPoint() {
    return startInteractionPoint;
  }

  
  /* ------ Privilege Related Attributes ------ */

  public void setEnabledPrivileges(HashSet<Integer> privileges) {
    enabledPrivileges = privileges;
  }
  
  public HashSet<Integer> getEnabledPrivileges() {
    return enabledPrivileges;
  }
  
  public void enablePrivilege(int privilege, boolean enabled) {
    if (enabled) {
      getEnabledPrivileges().add(privilege);
    } else {
      getEnabledPrivileges().remove(new Integer(privilege));
    }
  }
  
  public boolean isPrivilegeEnabled(int  privilege) {
      return getEnabledPrivileges().contains(new Integer(privilege));
  }

  /*********************************************************************************/

  // These parse methods are called from SpecificationImporter when importing //
  // a specification from its xml representation                                    //
  
  public boolean parse(Element resourceSpec, NetGraphModel containingNet) {
      boolean badRef = false;
      if (resourceSpec != null) {
          Namespace nsYawl = resourceSpec.getNamespace() ;
          badRef = parseOffer(resourceSpec.getChild("offer", nsYawl), nsYawl, containingNet) ;
          parseAllocate(resourceSpec.getChild("allocate", nsYawl), nsYawl) ;
          parseStart(resourceSpec.getChild("start", nsYawl), nsYawl) ;
          badRef = badRef || parseSecondary(resourceSpec.getChild("secondary", nsYawl), nsYawl);
          parsePrivileges(resourceSpec.getChild("privileges", nsYawl), nsYawl) ;
      }
      return badRef;
  }

    
  public boolean parseOffer(Element offerElement, Namespace nsYawl, NetGraphModel containingNet) {
      boolean badRef = false;
      setOfferInteractionPoint(parseInitiator(offerElement));

      // if offer is not system-initiated, there's no more to do
      if (getOfferInteractionPoint() == SYSTEM_INTERACTION_POINT) {
          badRef = parseDistributionSet(offerElement, nsYawl, containingNet) ;
          parseFamiliarTask(offerElement, nsYawl, containingNet) ;
      }
      return badRef;
  }


  private boolean parseDistributionSet(Element e,
                                   Namespace nsYawl, NetGraphModel containingNet) {
      boolean badRef = false;

      Element eDistSet = e.getChild("distributionSet", nsYawl);
      if (eDistSet != null) {
          badRef = parseInitialSet(eDistSet, nsYawl, containingNet) ;
          parseFilters(eDistSet, nsYawl) ;
          parseConstraints(eDistSet, nsYawl, containingNet) ;
      }
      return badRef;
  }


  private boolean parseInitialSet(Element e, Namespace nsYawl, NetGraphModel containingNet) {
      boolean badRef = false;

      Element eInitialSet = e.getChild("initialSet", nsYawl);
      if (eInitialSet != null) {
          badRef = parseParticipants(eInitialSet, nsYawl);
          badRef = badRef || parseRoles(eInitialSet, nsYawl);
          parseDynParams(eInitialSet, nsYawl, containingNet);
      }
      return badRef;
  }


  private boolean parseParticipants(Element e, Namespace nsYawl) {
      boolean badRef = false;
      Map<String, Participant> liveMap = getUserMap();
      List<Participant> result = new LinkedList<Participant>();

      for (Element eParticipant : e.getChildren("participant", nsYawl)) {
          String pid = eParticipant.getText();
          if (pid != null) {
              Participant p = liveMap.get(pid);
              if (p != null) {
                  result.add(p);
              }
              else {
                  badRef = true;
              }
          }
      }
      setBaseUserDistributionList(result);
      return badRef;
  }


  private boolean parseRoles(Element e, Namespace nsYawl) {
      boolean badRef = false;
      Map<String, Role> liveMap = getRoleMap();
      List<Role> result = new LinkedList<Role>();

      for (Element eRole : e.getChildren("role", nsYawl)) {
          String rid = eRole.getText();
          if (rid != null) {
              Role r = liveMap.get(rid);
              if (r != null) {
                  result.add(r);
              }
              else {
                  badRef = true;
              }
          }
      }
      setBaseRoleDistributionList(result);
      return badRef;
  }


  private void parseDynParams(Element e, Namespace nsYawl, NetGraphModel containingNet) {
      List<DataVariableContent> result = new LinkedList<DataVariableContent>();
      YNet yNet = (YNet) containingNet.getDecomposition();
      for (Element eParam : e.getChildren("param", nsYawl)) {
          String name = eParam.getChildText("name", nsYawl);
          YVariable var = yNet.getLocalOrInputVariable(name);
          if (var != null) {
              String refers = eParam.getChildText("refers", nsYawl);
              int contentType = DataVariableContent.DATA_CONTENT_TYPE;
              if (refers != null) {
                  if (refers.equals("participant")) {
                      contentType = DataVariableContent.PARTICIPANT_CONTENT_TYPE;
                  }
                  else if (refers.equals("role")) {
                      contentType = DataVariableContent.ROLE_CONTENT_TYPE;
                  }
              }
              result.add(new DataVariableContent(var, contentType));
          }
      }
      setBaseVariableContentList(result);
  }


    private void parseFilters(Element e, Namespace nsYawl) {
        List<AbstractSelector> result = new LinkedList<AbstractSelector>();
        Element eFilters = e.getChild("filters", nsYawl);
        if (eFilters != null) {
            for (Element eFilter : eFilters.getChildren("filter", nsYawl)) {
                String filterName = eFilter.getChildText("name", nsYawl);
                if (filterName != null) {
                    String simpleName = filterName.substring(filterName.lastIndexOf('.') + 1);
                    AbstractSelector filter = new GenericFilter(simpleName);
                    filter.setCanonicalName(filterName);
                    filter.setParams(parseParams(eFilter, nsYawl));
                    result.add(filter);
                }
            }
        }

        setResourcingFilters(result);
    }


  private void parseConstraints(Element e, Namespace nsYawl, NetGraphModel containingNet) {
      Element eConstraints = e.getChild("constraints", nsYawl);
      if (eConstraints != null) {
              for (Element eConstraint : eConstraints.getChildren("constraint", nsYawl)) {
                  String constraintName = eConstraint.getChildText("name", nsYawl);
                  if ((constraintName != null) &&
                          (constraintName.equals("SeparationOfDuties"))) {
                      Map<String, String> params = parseParams(eConstraint, nsYawl);
                      String famTaskName = params.get("familiarTask");
                        if (famTaskName != null) {
                            setSeparationOfDutiesTaskID(famTaskName);
                      }
                  }
              }
          }
  }


  private void parseFamiliarTask(Element e, Namespace nsYawl, NetGraphModel containingNet) {
      Element eFamTask = e.getChild("familiarParticipant", nsYawl);
      if (eFamTask != null) {
          String famTaskName = eFamTask.getAttributeValue("taskID");
          if (famTaskName != null) {
              this.setRetainFamiliarTaskID(famTaskName);
          }
      }
  }

  // this must be called only after all the net's tasks are loaded
  public void finaliseRetainFamiliarTasks(Set<YAWLAtomicTask> taskSet) {

      // first do the retain familiar tasks
      String famTaskName = this.getRetainFamiliarTaskID();
      if (famTaskName != null) {
          YAWLAtomicTask famTask = getTaskWithName(famTaskName, taskSet);
          if (famTask != null) {
              this.setRetainFamiliarTask(famTask);
          }
      }

      // now do the separation of duties constraint
      famTaskName = this.getSeparationOfDutiesTaskID();
      if (famTaskName != null) {
          YAWLAtomicTask famTask = getTaskWithName(famTaskName, taskSet);
          if (famTask != null) {
              this.setSeparationOfDutiesTask(famTask);
          }
      }
  }


  public void parseAllocate(Element allocateElement, Namespace nsYawl) {
      setAllocateInteractionPoint(parseInitiator(allocateElement));
      if (allocateElement != null) {
          Element allocator = allocateElement.getChild("allocator", nsYawl) ;
          if (allocator != null) {
              String name = allocator.getChildText("name", nsYawl);
              if (name != null) {
                  String simpleName = name.substring(name.lastIndexOf('.') + 1);
                  GenericAllocator selector = new GenericAllocator(simpleName);
                  selector.setCanonicalName(name);
                  setAllocationMechanism(selector);
              }
          }
      }
  }


  public void parseStart(Element startElement, Namespace nsYawl) {
      setStartInteractionPoint(parseInitiator(startElement));
  }


  private Map<String, Participant> getUserMap() {
      return YConnector.getParticipantMap();
  }


    private Map<String, Role> getRoleMap() {
        return YConnector.getRoleMap();
    }


    private Map<String, NonHumanResource> getAssetMap() {
        return YConnector.getNonHumanResourceMap();
    }


    private Map<String, ResourcingCategory> getCategoryMap() {
        Map<String, ResourcingCategory> liveMap =  new HashMap<String, ResourcingCategory>();
        try {
            for (ResourcingCategory category :
                     ResourcingCategory.convertCategories(
                             YConnector.getNonHumanCategories())) {
                 liveMap.put(category.getKey(), category);
            }
        }
        catch (IOException ioe) {
            // fall through to empty map
        }
         return liveMap;
     }


    private boolean parseSecondary(Element e, Namespace nsYawl) {
        if (e == null) return false;                   // no secondary resources defined
        boolean badRef = false;
        List<Object> result = new LinkedList<Object>();
        Map<String, Participant> userMap = getUserMap();
        Map<String, Role> roleMap = getRoleMap();
        Map<String, NonHumanResource> assetMap = getAssetMap();
        Map<String, ResourcingCategory> categoryMap = getCategoryMap();

        List users = e.getChildren("participant", nsYawl);
        for (Object o : users) {
            String id = ((Element) o).getText();
            if (id != null) {
                Participant p = userMap.get(id);
                if (p != null) {
                    result.add(p);
                }
                else badRef = true;
            }
        }
        List roles = e.getChildren("role", nsYawl);
        for (Object o : roles) {
            String id = ((Element) o).getText();
            if (id != null) {
                Role r = roleMap.get(id);
                if (r != null) {
                    result.add(r);
                }
                else badRef = true;
            }
        }
        List assets = e.getChildren("nonHumanResource", nsYawl);
        for (Object o : assets) {
            String id = ((Element) o).getText();
            if (id != null) {
                NonHumanResource r = assetMap.get(id);
                if (r != null) {
                    result.add(r);
                }
                else badRef = true;
            }
        }
        List categories = e.getChildren("nonHumanCategory", nsYawl);
        for (Object o : categories) {
            String id = ((Element) o).getText();
            if (id != null) {
                String subcat = ((Element) o).getAttributeValue("subcategory");
                if (subcat != null) id += "<>" + subcat;
                ResourcingCategory r = categoryMap.get(id);
                if (r != null) {
                    result.add(r);
                }
                else badRef = true;
            }
        }
        setSecondaryResourcesList(result);
        return badRef;
    }




  public void parsePrivileges(Element privilegesElement, Namespace nsYawl) {
      if (privilegesElement != null) {
          List ePrivileges = privilegesElement.getChildren("privilege", nsYawl);

          // if no privileges element to deal with, we're done
          if (ePrivileges == null) return;

          Iterator itr = ePrivileges.iterator() ;
          while (itr.hasNext()) {
              Element ePrivilege = (Element) itr.next();

              // get the privilege we're referring to
              String privName = ePrivilege.getChildText("name", nsYawl) ;
              if (privName != null) {
                  String allowall = ePrivilege.getChildText("allowall", nsYawl);
                  if ((allowall != null) && (allowall.equalsIgnoreCase("true"))) {
                      enablePrivilege(getPrivilege(privName), true);
                  }
              }
          }
      }
  }


  public int parseInitiator(Element e) {
      int initiator = USER_INTERACTION_POINT ;                            // default
      if (e != null) {
          String initiatorValue = e.getAttributeValue("initiator") ;
          if ((initiatorValue != null) && (initiatorValue.equals("system")))
              initiator = SYSTEM_INTERACTION_POINT;
      }
      return initiator;
  }

  public Map<String, String> parseParams(Element e, Namespace nsYawl) {
      HashMap<String, String> result = new HashMap<String, String>() ;
      Element eParams = e.getChild("params", nsYawl);
      if (eParams != null) {
          List params = eParams.getChildren("param", nsYawl) ;
          for (Object o : params) {
              Element eParam = (Element) o ;
              result.put(eParam.getChildText("key", nsYawl),
                         eParam.getChildText("value", nsYawl));
           }
      }
      return result ;
  }


  private int getPrivilege(String name) {
      if (name.equals("canSuspend")) return CAN_SUSPEND_PRIVILEGE;
      if (name.equals("canReallocateStateless")) return CAN_REALLOCATE_STATELESS_PRIVILEGE;
      if (name.equals("canReallocateStateful")) return CAN_REALLOCATE_STATEFUL_PRIVILEGE;
      if (name.equals("canDeallocate")) return CAN_DEALLOCATE_PRIVILEGE;
      if (name.equals("canDelegate")) return CAN_DELEGATE_PRIVILEGE;
      if (name.equals("canSkip")) return CAN_SKIP_PRIVILEGE;
      if (name.equals("canPile")) return CAN_PILE_PRIVILEGE;
      return -1;
  }


  private YAWLAtomicTask getTaskWithName(String name, Set<YAWLAtomicTask> taskSet) {
      YAWLAtomicTask result = null;
      if (name != null) {
          for (YAWLAtomicTask task : taskSet) {
              if (((YAWLVertex) task).getID().equals(name)) {
                  result = task ;
                  break;
              }
          }
      }
      return result;
  }


  /*********************************************************************************/
  
  public String toString() {
    
    StringBuffer systemAllocationMechanism = new StringBuffer("");
    
    if (getAllocateInteractionPoint() == SYSTEM_INTERACTION_POINT) {
      if (getAllocationMechanism() != null) {
        systemAllocationMechanism.append(
            "System Allocation Mechanism\n" + 
            "---------------------------\n" + 
            "  " + getAllocationMechanism().getDisplayName() + "\n"
        );
      }
    }
    
    StringBuffer baseUserDistribitionListString = new StringBuffer("");
    if (getBaseUserDistributionList() != null && getBaseUserDistributionList().size() > 0) {
      baseUserDistribitionListString.append(
          "Base User Distribution List:\n" + 
          "---------------------------\n"
      );
      for(Participant user : getBaseUserDistributionList()) {
        baseUserDistribitionListString.append("  " + user.getName() + "\n");
      }
    }

    StringBuffer baseRoleDistribitionListString = new StringBuffer("");
    if (getBaseRoleDistributionList() != null && getBaseRoleDistributionList().size() > 0) {
      baseRoleDistribitionListString.append(
          "Base RoleDistribution List:\n" + 
          "---------------------------\n"
      );
      for(Role role : getBaseRoleDistributionList()) {
        baseRoleDistribitionListString.append("  " + role.getName() + "\n");
      }
    }

    StringBuffer baseVariableContentListString = new StringBuffer("");
    if (getBaseVariableContentList() != null && getBaseVariableContentList().size()> 0) {
      baseVariableContentListString.append(
          "Variable Content List:\n" + 
          "----------------------\n"
      );
      for(DataVariableContent content: getBaseVariableContentList()) {
        baseVariableContentListString.append(
            "  " + content.getVariable().getName() + 
            " contains " + 
            content.getContentTypeAsString() + "\n");
      }
    }

    StringBuffer resourceFiltersString = new StringBuffer("");
    if (getResourcingFilters() != null && getResourcingFilters().size()> 0) {
      resourceFiltersString.append(
          "Resource Filters:\n" + 
          "----------------------\n"
      );
      for(AbstractSelector filter : getResourcingFilters()) {
        resourceFiltersString.append(
            "  " + filter.getDisplayName() +  "\n"
        );
      }
    }
    
    StringBuffer retainFamiliarTaskString = new StringBuffer("");
    if(getRetainFamiliarTask() != null) {
      retainFamiliarTaskString.append("Retain Familiar Task = (" 
          + ((YAWLTask) getRetainFamiliarTask()).getID()
          + ").\n"
      );
    }

    StringBuffer separationOfDutiesTaskString = new StringBuffer("");
    if(getSeparationOfDutiesTask() != null) {
      retainFamiliarTaskString.append("Separation of Duties Task = (" 
          + ((YAWLTask) getSeparationOfDutiesTask()).getID()
          + ").\n"
      );
    }

    StringBuffer enabledPrivilegesString = new StringBuffer("");
    
    if (getEnabledPrivileges().size() > 0) {
      enabledPrivilegesString.append(
          "Enabled Runtime Privileges:\n" + 
          "---------------------------\n"
      );
      for(Integer privilege: getEnabledPrivileges()) {
        enabledPrivilegesString.append("  " + privilege + "\n");
      }
    }
    
    return 
        "Interaction Points:\n-------------------\n  Offer: " + getOfferInteractionPoint() + 
        ", Allocate: " + getAllocateInteractionPoint() + 
        ", Start: " + getStartInteractionPoint() + "\n" +
        systemAllocationMechanism +
        retainFamiliarTaskString +
        baseUserDistribitionListString +
        baseRoleDistribitionListString +
        baseVariableContentListString +
        resourceFiltersString +
        separationOfDutiesTaskString +
        enabledPrivilegesString;
  }
}