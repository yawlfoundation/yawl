package com.nexusbpm.scheduler;

import java.util.Date;

public interface HistoryPersister {
	void logCaseStarted(
			String triggerName,
			Date scheduledFireTime,
			Date actualFireTime,
			String caseID );
	
	void logCaseStartFailure(
			String triggerName,
			Date scheduledFireTime,
			Date actualFireTime,
			String failureMessage );
}
