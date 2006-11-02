package com.nexusbpm.editor.editors.schedule;

import java.util.Date;
import java.util.List;

public interface TriggerEx {
	public List<Date> getFireTimesBetween( Date start, Date end );
	public Date getFireTimeAfterEx( Date afterTime );
}
