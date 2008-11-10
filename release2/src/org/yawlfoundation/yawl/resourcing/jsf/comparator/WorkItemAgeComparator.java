package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.Comparator;

/**
 * Allows Workitems to be sorted on enablement time for display in age order in workqueues
 *
 * Author: Michael Adams
 * Creation Date: 15/01/2008
 */
public class WorkItemAgeComparator implements Comparator<WorkItemRecord> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(WorkItemRecord wir1, WorkItemRecord wir2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (wir1 == null) return -1;
        if (wir2 == null) return 1;


        // try to compare enablement times
        try {
            long wir1EnableTime = Long.parseLong(wir1.getEnablementTimeMs());
            long wir2EnableTime = Long.parseLong(wir2.getEnablementTimeMs());

            // return +ve means first wir is younger
            if ((wir1EnableTime==0) && (wir2EnableTime==0))
                return compareCaseIDs(wir1, wir2);
            else {
                int result = (int) (wir1EnableTime - wir2EnableTime);

                // if identical enable times, sort on caseid
                if (result == 0)
                    return compareCaseIDs(wir1, wir2);
                else
                    return result;
            }
        }
        catch (NumberFormatException nfe) {
            return compareCaseIDs(wir1, wir2);
        }
	}


    /** if there's a problem comparing enablement times, as a secondary choice
     *  compare on caseids (assumption: caseids get allocated in chronological
     *  order - which is not always the case)
     */
    private static int compareCaseIDs(WorkItemRecord wir1, WorkItemRecord wir2) {
        String caseID1 = wir1.getCaseID();
        String caseID2 = wir2.getCaseID();
        return caseID1.compareTo(caseID2);
    }

}