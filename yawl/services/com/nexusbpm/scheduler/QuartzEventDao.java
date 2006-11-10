package com.nexusbpm.scheduler;

import java.util.Date;
import java.util.List;

public interface QuartzEventDao {

    public QuartzEvent getRecord(Long id);
    public List getRecords(Date startDate, Date endDate);
    public void saveRecord(QuartzEvent record);

public void removeRecord(Long id);
}
