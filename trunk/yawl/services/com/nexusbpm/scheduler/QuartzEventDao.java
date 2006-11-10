/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.util.Date;
import java.util.List;

public interface QuartzEventDao {

    public QuartzEvent getRecord(Long id);
    public List getRecords(Date startDate, Date endDate);
    public void saveRecord(QuartzEvent record);
    public void removeRecord(Long id);
}
