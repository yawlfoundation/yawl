package com.nexusbpm.services.r;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.invoker.InternalNexusService;

public class InternalRService extends InternalNexusService {

	@Override
	public NexusServiceData execute(NexusServiceData data) {
		try {
			RComponent component = new RComponent(new RServiceData(data));
			return component.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDocumentation() {
		return "Runs R code against an R Server";
	}

	@Override
	public String getServiceName() {
		return "R";
	}

}
