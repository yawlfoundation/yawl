/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.support;

import java.util.*;

/** The CaseMap class maintains a two way mapping between case ids and worklet names
 *  for worklet instances that have been launched by a particular checked out item or
 *  handler runner and are currently executing.
 *
 * It is used exclusively by the WorkletRecord class.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04-09/2006
 */

public class CaseMap {

    private HashMap<String,String> _caseIdToWorkletName = new HashMap<String,String>();
    private HashMap<String,String> _workletNameToCaseId = new HashMap<String,String>();

    
    public CaseMap() {}

    public void addCase(String caseID, String workletName) {
        _caseIdToWorkletName.put(caseID, workletName);
        _workletNameToCaseId.put(workletName, caseID);
    }

    public String getWorkletName(String caseID) {
        return _caseIdToWorkletName.get(caseID);
    }

    public String getCaseID(String workletName) {
        return _workletNameToCaseId.get(workletName);
    }

    public void removeCase(String caseID) {
        String workletName = getWorkletName(caseID);
        removeFromMaps(caseID, workletName) ;
    }

    public void removeWorklet(String workletName) {
        String caseID = getCaseID(workletName);
        removeFromMaps(caseID, workletName) ;
    }

    public void removeAllCases() {
        _caseIdToWorkletName.clear();
        _workletNameToCaseId.clear();
    }

    public Set getAllCaseIDs() {
        return _caseIdToWorkletName.keySet();
    }

    public Set getAllWorkletNames() {
        return _workletNameToCaseId.keySet();
    }

    public ArrayList getCaseIdList() {
        return new ArrayList<String>(_workletNameToCaseId.values());
    }

    public ArrayList getWorkletList() {
        return new ArrayList<String>(_caseIdToWorkletName.values());
    }

    public String getWorkletCSVList() {
        return (String) getCaseMapAsCSVLists().get("workletNames");
    }


    public String getCaseIdCSVList() {
        return (String) getCaseMapAsCSVLists().get("caseIDs");
    }


    public HashMap getCaseMapAsCSVLists() {
        HashMap result = new HashMap();
        ArrayList caseIDs = getCaseIdList();
        result.put("caseIDs", RdrConversionTools.StringListToString(caseIDs));
        result.put("workletNames", makeCSVNameList(caseIDs));
        return result;
    }


   public boolean hasRunningWorklets() {
       return ! _workletNameToCaseId.isEmpty() ;
    }


    public void restore(String caseIdList, String wNameList) {
        List caseids = RdrConversionTools.StringToStringList(caseIdList);
        List wList = RdrConversionTools.StringToStringList(wNameList);
        if ((caseids != null ) && (wList != null)) {
            for (int i=0; i < caseids.size(); i++) {
                addCase((String) caseids.get(i), (String) wList.get(i));
            }    
        }
    }


    private void removeFromMaps(String caseID, String workletName) {
        _caseIdToWorkletName.remove(caseID);
        _workletNameToCaseId.remove(workletName);
    }


    /** creates an ordered csv list of worklet names from case ids */
    private String makeCSVNameList(List caseIDs) {
        String result = "";
        for (Object caseID : caseIDs) {
            if (result.length() > 0) result += ",";
            result += getWorkletName((String) caseID);
        }
        if (result.length() == 0) result = null ;
        return result ;
    }

}
