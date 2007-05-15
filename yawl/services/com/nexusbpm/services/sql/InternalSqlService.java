package com.nexusbpm.services.sql;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.invoker.InternalNexusService;

public class InternalSqlService extends InternalNexusService {

	@Override
	public NexusServiceData execute(NexusServiceData data) {
		try {
			SqlComponent component = new SqlComponent(new SqlServiceData(data));
			return component.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDocumentation() {
		return "Runs SQL code against a database and sends the results to a URL";
	}

	@Override
	public String getServiceName() {
		return "Sql";
	}

}
