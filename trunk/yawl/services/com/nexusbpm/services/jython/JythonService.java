package com.nexusbpm.services.jython;

/**
 * Interface for XFire service.
 */
public interface JythonService {
	public String execute(String code, String output, String error, String vars);
}
