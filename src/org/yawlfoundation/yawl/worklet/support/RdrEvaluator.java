package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.Rdr;
import org.yawlfoundation.yawl.worklet.rdr.RdrPair;
import org.yawlfoundation.yawl.worklet.rdr.RdrTree;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 7/01/2016
 */
public class RdrEvaluator {

    private final Rdr _rdr = new Rdr();                   // rule set interface
    private final EngineClient _client;

    public RdrEvaluator(EngineClient client) {
        _client = client;
    }

    public Rdr getRdrInterface() { return _rdr; }


    public RdrPair evaluate(WorkItemRecord wir) {
        try {
            Element data = wir.getDataList();
            if (data == null) {                               // wir not yet started
                data = _client.getStartingData(wir.getID());
            }
            if (data != null) {
                Element searchData = getSearchData(wir, data);
                return search(new YSpecificationID(wir), wir.getTaskID(), searchData);
            }
        }
        catch (IOException fallthrough) {

        }
        return null;
    }


    public RdrPair evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType) {
        return _rdr.evaluate(specID, taskID, data, rType);
    }


    private RdrPair search(YSpecificationID specID, String taskID, Element data) {
        if (data != null) {
            RdrTree tree = getTree(specID, taskID, RuleType.ItemSelection);
            if (tree != null) return tree.search(data);
        }
        return null;
    }


    private Element getSearchData(WorkItemRecord wir, Element data) {
        Element processData = appendDataTypes(wir, data.clone());

        //convert the wir contents to an Element
        Element wirElement = JDOMUtil.stringToElement(wir.toXML()).detach();

        Element eInfo = new Element("process_info");     // new Element for process data
        eInfo.addContent(wirElement);
        processData.addContent(eInfo);                     // add element to case data
        return processData;
    }


    private Element appendDataTypes(WorkItemRecord wir, Element data) {
        List<YParameter> inputParams = _client.getTaskInputParams(wir);
        for (Element varElement : data.getChildren()) {
            String varName = varElement.getName();
            for (YParameter param : inputParams) {
                if (param.getName().equals(varName)) {
                    varElement.setAttribute("type", param.getDataTypeNameUnprefixed());
                    break;
                }
            }
        }
        return data;
    }

    /**
     * returns the rule tree (if any) for the parameters passed
     */
    public RdrTree getTree(YSpecificationID specID, String taskID, RuleType treeType) {
        return _rdr.getRdrTree(specID, taskID, treeType);
    }


    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified workitem
     * @param itemID - the id of the item to get the triggers for
     * @return the (String) list of triggers
     */
    public List<String> getExternalTriggersForItem(String itemID) {
        if (itemID != null) {
            try {
                WorkItemRecord wir = _client.getEngineStoredWorkItem(itemID);
                if (wir != null) {
                    RdrTree tree = getTree(new YSpecificationID(wir), wir.getTaskID(),
                            RuleType.ItemExternalTrigger);
                    return getExternalTriggers(tree);
                }
            }
            catch (IOException fallthrough) {
                //
            }
        }
        return null;
    }

    //***************************************************************************//

    /** Traverse the extracted conditions from all nodes of the passed RdrTree
     *  and return the external exception triggers found within them
     * @param tree - the (external exception) RdrTree containing the triggers
     *  @return the (String) list of triggers
     */
    public List<String> getExternalTriggers(RdrTree tree) {
        List<String> list = new ArrayList<String>();
        if (tree != null) {
            for (String cond : tree.getAllConditions()) {
                String trigger = getConditionValue(cond, "trigger");
                if (trigger != null) {
                    trigger = trigger.replaceAll("\"","");         // de-quote
                    list.add(trigger);
                }
            }
        }
        return list ;
    }

    //***************************************************************************//

    /**
     * Gets the value for the specified variable in the condition string
     * @param cond - the condition containing the value
     * @param var - the variable to get the value of
     * @return the value of the variable passed
     */
    private String getConditionValue(String cond, String var){
        String[] parts = cond.split("=");

        for (int i = 0; i < parts.length; i+=2) {
            if (parts[i].trim().equalsIgnoreCase(var))
                return parts[i+1].trim() ;
        }
        return null ;
    }


}
