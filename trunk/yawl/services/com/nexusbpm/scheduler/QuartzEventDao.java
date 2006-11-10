package com.nexusbpm.scheduler;

import java.util.List;

public interface QuartzEventDao {

    public QuartzEvent getRecord(Long id);
    public List getRecords();
    public void saveRecord(QuartzEvent record);

public void removeRecord(Long id);
}
