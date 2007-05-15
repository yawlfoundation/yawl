package com.nexusbpm.services.ftp;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.invoker.InternalNexusService;

public class InternalFtpService extends InternalNexusService {

	@Override
	public NexusServiceData execute(NexusServiceData data) {
		try {
//			SqlComponent component = new SqlComponent(new FtpServiceData(data));
//			component.run();
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
		return "SQL";
	}

}
