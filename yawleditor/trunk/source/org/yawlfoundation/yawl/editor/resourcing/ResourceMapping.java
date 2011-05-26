package org.yawlfoundation.yawl.editor.resourcing;

import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.DataVariableUtilities;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;
import org.yawlfoundation.yawl.schema.XSDType;

import java.io.Serializable;
import java.util.*;

public class ResourceMapping implements Serializable, Cloneable  {
  
  private static final long serialVersionUID = 1L;
  
  public static final int SYSTEM_INTERACTION_POINT = 0;
  public static final int USER_INTERACTION_POINT = 1;

  public static final int CAN_SUSPEND_PRIVILEGE              = 100;
  public static final int CAN_REALLOCATE_STATELESS_PRIVILEGE = 101;
  public static final int CAN_REALLOCATE_STATEFUL_PRIVILEGE  = 102;
  public static final int CAN_DEALLOCATE_PRIVILEGE           = 103;
  public static final int CAN_DELEGATE_PRIVILEGE             = 104;
  public static final int CAN_SKIP_PRIVILEGE                 = 105;
  public static final int CAN_PILE_PRIVILEGE                 = 106;

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();

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
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  public ResourceMapping(YAWLAtomicTask resourceRequiringTask, boolean setVariableList) {
    super();
    initialise();
    setResourceRequiringTask(resourceRequiringTask);
    if (setVariableList)
        setBaseVariableContentList(buildDefaultBaseVariableContentList());
  }
  
  public YAWLAtomicTask getResourceRequiringTask() {
    return (YAWLAtomicTask) serializationProofAttributeMap.get("resourceRequiringTask");
  }
  
  public void setResourceRequiringTask(YAWLAtomicTask resourceRequiringTask) {
    serializationProofAttributeMap.put("resourceRequiringTask",resourceRequiringTask);
  }

  /* ------ Offer Related Attributes ------ */
  
  public void setOfferInteractionPoint(int setting) {
    serializationProofAttributeMap.put("offerInteractionPoint",new Integer(setting));
  }
  
  public int getOfferInteractionPoint() {
    return ((Integer) serializationProofAttributeMap.get("offerInteractionPoint")).intValue();
  }
  
  public YAWLAtomicTask getRetainFamiliarTask() {
    return (YAWLAtomicTask) serializationProofAttributeMap.get("retainFamiliarTask");
  }

  public void setRetainFamiliarTask(YAWLAtomicTask task) {
    serializationProofAttributeMap.put("retainFamiliarTask", task);
  }

    public void setRetainFamiliarTaskID(String taskid) {
      serializationProofAttributeMap.put("retainFamiliarTaskID", taskid);
    }

    public String getRetainFamiliarTaskID() {
      return (String) serializationProofAttributeMap.get("retainFamiliarTaskID");
    }

    public String getSeparationOfDutiesTaskID() {
      return (String) serializationProofAttributeMap.get("separationOfDutiesTaskID");
    }

    public void setSeparationOfDutiesTaskID(String taskid) {
      serializationProofAttributeMap.put("separationOfDutiesTaskID", taskid);
    }


  public YAWLAtomicTask getSeparationOfDutiesTask() {
    return (YAWLAtomicTask) serializationProofAttributeMap.get("separationOfDutiesTask");
  }

  public void setSeparationOfDutiesTask(YAWLAtomicTask task) {
    serializationProofAttributeMap.put("separationOfDutiesTask", task);
  }
  
  private List<DataVariableContent> buildDefaultBaseVariableContentList() {
    LinkedList<DataVariableContent> list = new LinkedList<DataVariableContent>();

    List<DataVariable> validPossibleVariables = getNetVariablesValidForResourcing();
    for(DataVariable variable : validPossibleVariables) {
      
      list.add(new DataVariableContent(variable));
    }

    return list;
  }


  private List<DataVariable> getNetVariablesValidForResourcing() {
    NetGraph selectedGraph = YAWLEditorDesktop.getInstance().getSelectedGraph() ;
    Decomposition decomp = selectedGraph.getNetModel().getDecomposition();

    return  DataVariableUtilities.getVariablesOfType(decomp.getVariables(),
                    XSDType.getString(XSDType.STRING));
  }


  public void setBaseUserDistributionList(List<ResourcingParticipant> userList) {
    serializationProofAttributeMap.put("baseUserDistributionList", userList);
  }
  
  public List<ResourcingParticipant> getBaseUserDistributionList() {
    return (List<ResourcingParticipant>) serializationProofAttributeMap.get("baseUserDistributionList");
  }

    public void setSecondaryResourcesList(List<Object> list) {
      serializationProofAttributeMap.put("secondaryResourcesList", list);
    }

    public List<Object> getSecondaryResourcesList() {
        List<Object> list = (List<Object>) serializationProofAttributeMap.get("secondaryResourcesList");
      return list != null ? list : new ArrayList<Object>();
    }

  public void setBaseRoleDistributionList(List<ResourcingRole> roles) {
    serializationProofAttributeMap.put("baseRoleDistributionList", roles);
  }
  
  public List<ResourcingRole> getBaseRoleDistributionList() {
    return (List<ResourcingRole>) serializationProofAttributeMap.get("baseRoleDistributionList");
  }
  
  public void setBaseVariableContentList(List<DataVariableContent> list) {
    serializationProofAttributeMap.put("baseVariableContentList", list);
  }
  
  public List<DataVariableContent>  getBaseVariableContentList() {
    return (List<DataVariableContent>) serializationProofAttributeMap.get("baseVariableContentList");
  }

  // remove null members
  public void cleanBaseUserDistributionList() {
      List<ResourcingParticipant> pList = getBaseUserDistributionList();
      if ((pList != null) && pList.contains(null)) {
          List<ResourcingParticipant> cleanList = new LinkedList<ResourcingParticipant>();
          for (ResourcingParticipant p : pList) if (p != null) cleanList.add(p);
          setBaseUserDistributionList(cleanList);
      }
  }

  public void cleanBaseRoleDistributionList() {    
      List<ResourcingRole> rList = getBaseRoleDistributionList();
      if ((rList != null) && rList.contains(null)) {
          List<ResourcingRole> cleanRoleList = new LinkedList<ResourcingRole>();
          for (ResourcingRole p : rList) if (p != null) cleanRoleList.add(p);
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
    
    LinkedList<DataVariable> variablesToAdd = new LinkedList<DataVariable>();
    List<DataVariable> varList = getNetVariablesValidForResourcing();
    for(DataVariable variable : varList) {
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
    
    for(DataVariable variable: variablesToAdd) {
      getBaseVariableContentList().add(
          new DataVariableContent(variable)
      );
    }
  }

  public void setResourcingFilters(List<ResourcingFilter> filters) {
    serializationProofAttributeMap.put("resourcingFilters", filters);
  }
  
  public List<ResourcingFilter>  getResourcingFilters() {
    return (List<ResourcingFilter>) serializationProofAttributeMap.get("resourcingFilters");
  }
  
  /* ------ Allocation Related Attributes ------ */
  
  public void setAllocateInteractionPoint(int setting) {
    serializationProofAttributeMap.put("allocateInteractionPoint",new Integer(setting));
    
    if (getAllocationMechanism() == null) {
      setAllocationMechanism(AllocationMechanism.DEFAULT_MECHANISM);
    }
  }
  
  public int getAllocateInteractionPoint() {
    return ((Integer) serializationProofAttributeMap.get("allocateInteractionPoint")).intValue();
  }

  public AllocationMechanism getAllocationMechanism() {
    if (getAllocateInteractionPoint() == SYSTEM_INTERACTION_POINT) {
      return (AllocationMechanism) serializationProofAttributeMap.get("allocationMechanism");
    }
    return null;
  }
  
  public void setAllocationMechanism(AllocationMechanism allocationMechanism) {
    serializationProofAttributeMap.put("allocationMechanism", allocationMechanism);
  }

  /* ------ Start Related Attributes ------ */
  
  public void setStartInteractionPoint(int setting) {
    serializationProofAttributeMap.put("startInteractionPoint",new Integer(setting));
  }

  
  public int getStartInteractionPoint() {
    return ((Integer) serializationProofAttributeMap.get("startInteractionPoint")).intValue();
  }

  
  /* ------ Privilege Related Attributes ------ */

  public void setEnabledPrivileges(HashSet<Integer> privileges) {
    serializationProofAttributeMap.put("enabledPrivileges", privileges);
  }
  
  public HashSet<Integer> getEnabledPrivileges() {
    return (HashSet<Integer>) serializationProofAttributeMap.get("enabledPrivileges");
  }
  
  public void enablePrivilege(int privilege, boolean enabled) {
    if (enabled) {
      getEnabledPrivileges().add(new Integer(privilege));
    } else {
      getEnabledPrivileges().remove(new Integer(privilege));
    }
  }
  
  public boolean isPrivilegeEnabled(int  privilege) {
    if (getEnabledPrivileges().contains(new Integer(privilege))) {
      return true;
    }
    return false;
  }

  /*********************************************************************************/

  // These parse methods are called from EngineSpecificationImporter when importing //
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
      Map<String, ResourcingParticipant> liveMap = getUserMap();
      List<ResourcingParticipant> result = new LinkedList<ResourcingParticipant>();

      List participants = e.getChildren("participant", nsYawl);
      Iterator itr = participants.iterator();
      while (itr.hasNext()) {
          Element eParticipant = (Element) itr.next();
          String pid = eParticipant.getText();
          if (pid != null) {
              ResourcingParticipant p = liveMap.get(pid);
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
      Map<String, ResourcingRole> liveMap = getRoleMap();
      List<ResourcingRole> result = new LinkedList<ResourcingRole>();

      List roles = e.getChildren("role", nsYawl);
      Iterator itr = roles.iterator();
      while (itr.hasNext()) {
          Element eRole = (Element) itr.next();
          String rid = eRole.getText();
          if (rid != null) {
              ResourcingRole r = liveMap.get(rid);
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
      DataVariableSet netVars = containingNet.getVariableSet();
      List<DataVariableContent> result = new LinkedList<DataVariableContent>();

      List params = e.getChildren("param", nsYawl);
      Iterator itr = params.iterator();
      while (itr.hasNext()) {
          Element eParam = (Element) itr.next();
          String name = eParam.getChildText("name", nsYawl);
          DataVariable var = netVars.getVariableWithName(name);
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
      List<ResourcingFilter> result = new LinkedList<ResourcingFilter>();
      Element eFilters = e.getChild("filters", nsYawl);
      if (eFilters != null) {
          List filters = eFilters.getChildren("filter", nsYawl);
          if (filters != null) {
              Iterator itr = filters.iterator();
              while (itr.hasNext()) {
                  Element eFilter = (Element) itr.next();
                  String filterName = eFilter.getChildText("name", nsYawl);
                  if (filterName != null) {
                      String simpleName = filterName.substring(filterName.lastIndexOf('.') + 1);
                      result.add(new ResourcingFilter(simpleName, filterName, null,
                              (HashMap<String, String>) parseParams(eFilter, nsYawl)));
                  }
              }
          }
      }
      setResourcingFilters(result);
  }


  private void parseConstraints(Element e, Namespace nsYawl, NetGraphModel containingNet) {
      Element eConstraints = e.getChild("constraints", nsYawl);
      if (eConstraints != null) {
          List constraints = eConstraints.getChildren("constraint", nsYawl);
          if (constraints != null) {
              Iterator itr = constraints.iterator();
              while (itr.hasNext()) {
                  Element eConstraint = (Element) itr.next();
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
                  setAllocationMechanism(new AllocationMechanism(simpleName, name, "", ""));
                  // allocationmechanism.setParams(parseParams(allocator, nsYawl));
              }
          }
      }
  }


  public void parseStart(Element startElement, Namespace nsYawl) {
      setStartInteractionPoint(parseInitiator(startElement));
  }


    private Map<String, ResourcingParticipant> getUserMap() {
       List<ResourcingParticipant> liveList =
              ResourcingServiceProxy.getInstance().getAllParticipants();
      Map<String, ResourcingParticipant> liveMap = new HashMap<String, ResourcingParticipant>();
      if (liveList != null) {
          for (ResourcingParticipant resp : liveList) {
              liveMap.put(resp.getId(), resp);
          }
      }
      return liveMap;
    }


    private Map<String, ResourcingRole> getRoleMap() {
        Map<String, ResourcingRole> liveMap =  new HashMap<String, ResourcingRole>();
        List<ResourcingRole> liveList = ResourcingServiceProxy.getInstance().getAllRoles();
        if (liveList != null) {
            for (ResourcingRole role : liveList) {
                liveMap.put(role.getId(), role);
            }
        }
        return liveMap;
    }


    private Map<String, ResourcingAsset> getAssetMap() {
        Map<String, ResourcingAsset> liveMap =  new HashMap<String, ResourcingAsset>();
        List<ResourcingAsset> liveList =
                ResourcingServiceProxy.getInstance().getAllNonHumanResources();
        if (liveList != null) {
            for (ResourcingAsset asset : liveList) {
                liveMap.put(asset.getId(), asset);
            }
        }
        return liveMap;
    }


    private Map<String, ResourcingCategory> getCategoryMap() {
         Map<String, ResourcingCategory> liveMap =  new HashMap<String, ResourcingCategory>();
         List<ResourcingCategory> liveList =
                 ResourcingServiceProxy.getInstance().getAllNonHumanCategories();
         if (liveList != null) {
             for (ResourcingCategory category : liveList) {
                 liveMap.put(category.getKey(), category);
             }
         }
         return liveMap;
     }


    private boolean parseSecondary(Element e, Namespace nsYawl) {
        if (e == null) return false;                   // no secondary resources defined
        boolean badRef = false;
        List<Object> result = new LinkedList<Object>();
        Map<String, ResourcingParticipant> userMap = getUserMap();
        Map<String, ResourcingRole> roleMap = getRoleMap();
        Map<String, ResourcingAsset> assetMap = getAssetMap();
        Map<String, ResourcingCategory> categoryMap = getCategoryMap();

        List users = e.getChildren("participant", nsYawl);
        for (Object o : users) {
            String id = ((Element) o).getText();
            if (id != null) {
                ResourcingParticipant p = userMap.get(id);
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
                ResourcingRole r = roleMap.get(id);
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
                ResourcingAsset r = assetMap.get(id);
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
              if (((YAWLVertex) task).getEngineId().equals(name)) {
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
      for(ResourcingParticipant user : getBaseUserDistributionList()) {
        baseUserDistribitionListString.append("  " + user.getName() + "\n");
      }
    }

    StringBuffer baseRoleDistribitionListString = new StringBuffer("");
    if (getBaseRoleDistributionList() != null && getBaseRoleDistributionList().size() > 0) {
      baseRoleDistribitionListString.append(
          "Base RoleDistribution List:\n" + 
          "---------------------------\n"
      );
      for(ResourcingRole role : getBaseRoleDistributionList()) {
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
      for(ResourcingFilter filter : getResourcingFilters()) {
        resourceFiltersString.append(
            "  " + filter.getDisplayName() +  "\n"
        );
      }
    }
    
    StringBuffer retainFamiliarTaskString = new StringBuffer("");
    if(getRetainFamiliarTask() != null) {
      retainFamiliarTaskString.append("Retain Familiar Task = (" 
          + ((YAWLTask) getRetainFamiliarTask()).getEngineId()
          + ").\n"
      );
    }

    StringBuffer separationOfDutiesTaskString = new StringBuffer("");
    if(getSeparationOfDutiesTask() != null) {
      retainFamiliarTaskString.append("Separation of Duties Task = (" 
          + ((YAWLTask) getSeparationOfDutiesTask()).getEngineId()
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