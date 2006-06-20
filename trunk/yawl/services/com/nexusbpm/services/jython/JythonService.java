package com.nexusbpm.services.jython;

/**
 * Interface for XFire service. It is not strictly necessary to give XFire the interface,
 * but the service class does have to implement <i>some</i> interface.
 */
public interface JythonService {
	public String execute(String data);
}
