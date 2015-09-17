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

package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.engine.YSpecificationID;

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

    private Map<String, YSpecificationID> _caseIdToSpecId =
            new HashMap<String, YSpecificationID>();


    public CaseMap() {}

    public void addCase(String caseID, YSpecificationID specID) {
        _caseIdToSpecId.put(caseID, specID);
    }


    public YSpecificationID getSpecID(String caseID) {
        return _caseIdToSpecId.get(caseID);
    }


    public String getWorkletName(String caseID) {
        YSpecificationID specID = getSpecID(caseID);
        return specID != null ? specID.getUri() : null;
    }

    public String getCaseID(String workletName) {
        for (String caseID : _caseIdToSpecId.keySet()) {
            if (workletName.equals(getWorkletName(caseID))) {
                return caseID;
            }
        }
        return null;
    }

    public void removeCase(String caseID) {
        if (caseID != null) {
            _caseIdToSpecId.remove(caseID);
        }
    }

    public void removeWorklet(String workletName) {
        removeCase(getCaseID(workletName));
    }

    public void removeAllCases() {
        _caseIdToSpecId.clear();
    }

    public Set<String> getAllCaseIDs() {
        return _caseIdToSpecId.keySet();
    }

    public Set<String> getAllWorkletNames() {
        Set<String> names = new HashSet<String>();
        for (YSpecificationID specID : _caseIdToSpecId.values()) {
            names.add(specID.getUri());
        }
        return names;
    }

    public List<String> getCaseIdList() {
        return new ArrayList<String>(_caseIdToSpecId.keySet());
    }


    public String getWorkletCSVList() {
        return getCaseMapAsCSVLists().get("workletNames");
    }


    public String getCaseIdCSVList() {
        return getCaseMapAsCSVLists().get("caseIDs");
    }


    public Map<String, String> getCaseMapAsCSVLists() {
        Map<String, String> result = new HashMap<String, String>();
        List<String> caseIDs = getCaseIdList();
        String idcsv = RdrConversionTools.StringListToString(caseIDs);
        if (idcsv != null) result.put("caseIDs", idcsv);
        String namecsv = makeCSVNameList(caseIDs);
        if (namecsv != null) result.put("workletNames", namecsv);
        return result;
    }


   public boolean hasRunningWorklets() {
       return ! _caseIdToSpecId.isEmpty() ;
    }


    public void restore(String caseIdList, String wNameList) {
        List caseids = RdrConversionTools.StringToStringList(caseIdList);
        List wList = RdrConversionTools.StringToStringList(wNameList);
        if ((caseids != null ) && (wList != null)) {
            for (int i=0; i < caseids.size(); i++) {
             //todo   addCase((String) caseids.get(i), (String) wList.get(i));
            }    
        }
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
