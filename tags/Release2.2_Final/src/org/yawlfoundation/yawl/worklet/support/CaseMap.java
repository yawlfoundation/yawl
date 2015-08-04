/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.support;

import java.util.*;

/** The CaseMap class maintains a two way mapping between case ids and worklet names
 *  for worklet instances that have been launched by a particular checked out item or
 *  handler runner and are currently executing.
 *
 * It is used exclusively by the WorkletRecord class.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class CaseMap {

    private Map<String, String> _caseIdToWorkletName = new Hashtable<String, String>();
    private Map<String, String> _workletNameToCaseId = new Hashtable<String, String>();


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

    public Set<String> getAllCaseIDs() {
        return _caseIdToWorkletName.keySet();
    }

    public Set<String> getAllWorkletNames() {
        return _workletNameToCaseId.keySet();
    }

    public List<String> getCaseIdList() {
        return new ArrayList<String>(_workletNameToCaseId.values());
    }

    public List<String> getWorkletList() {
        return new ArrayList<String>(_caseIdToWorkletName.values());
    }

    public String getWorkletCSVList() {
        return getCaseMapAsCSVLists().get("workletNames");
    }


    public String getCaseIdCSVList() {
        return getCaseMapAsCSVLists().get("caseIDs");
    }


    public Map<String, String> getCaseMapAsCSVLists() {
        Map<String, String> result = new Hashtable<String, String>();
        List<String> caseIDs = getCaseIdList();
        String idcsv = RdrConversionTools.StringListToString(caseIDs);
        if (idcsv != null) result.put("caseIDs", idcsv);
        String namecsv = makeCSVNameList(caseIDs);
        if (namecsv != null) result.put("workletNames", namecsv);
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
    private String makeCSVNameList(List<String> caseIDs) {
        String result = "";
        for (String caseID : caseIDs) {
            if (result.length() > 0) result += ",";
            result += getWorkletName(caseID);
        }
        if (result.length() == 0) result = null ;
        return result ;
    }

}
