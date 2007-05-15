package com.nexusbpm.services.sas;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.invoker.InternalNexusService;

public class InternalSasService extends InternalNexusService {

	@Override
	public NexusServiceData execute(NexusServiceData data) {
		try {
			SasComponent component = new SasComponent(new SasServiceData(data));
			component.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDocumentation() {
		return "executes a shell script";
	}

	@Override
	public String getServiceName() {
		return "Shell";
	}

}
