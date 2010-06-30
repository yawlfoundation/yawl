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

    private HashMap _caseIdToWorkletName = new HashMap();
    private HashMap _workletNameToCaseId = new HashMap();
    private String _caseIdsAsCSVList ;


    public CaseMap() {}

    public void addCase(String caseID, String workletName) {
        _caseIdToWorkletName.put(caseID, workletName);
        _workletNameToCaseId.put(workletName, caseID);
    }

    public String getWorkletName(String caseID) {
        return (String) _caseIdToWorkletName.get(caseID);
    }

    public String getCaseID(String workletName) {
        return (String) _workletNameToCaseId.get(workletName);
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
        return new ArrayList(_workletNameToCaseId.values());
    }

    public ArrayList getWorkletList() {
        return new ArrayList(_caseIdToWorkletName.values());
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
        Iterator itr = caseIDs.iterator();
        while (itr.hasNext()) {
            if (result.length() > 0) result += ",";
            result += getWorkletName((String) itr.next());
        }
        if (result.length() == 0) result = null ;
        return result ;
    }

}
