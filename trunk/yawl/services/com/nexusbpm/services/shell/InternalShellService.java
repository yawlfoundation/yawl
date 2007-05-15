package com.nexusbpm.services.shell;

import com.nexusbpm.services.data.NexusServiceData;
import com.nexusbpm.services.invoker.InternalNexusService;

public class InternalShellService extends InternalNexusService {

	@Override
	public NexusServiceData execute(NexusServiceData data) {
		ShellComponent component = new ShellComponent(data);
		try {
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
