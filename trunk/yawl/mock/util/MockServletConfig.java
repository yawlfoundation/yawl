package util;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.jasper.servlet.JspCServletContext;

public class MockServletConfig implements ServletConfig {

	public String getInitParameter(String arg0) {
		return "";
	}
	public Enumeration getInitParameterNames() {
		return null;
	}
	public ServletContext getServletContext() {
		try {
			return new JspCServletContext(new PrintWriter(System.out), new URL("http://localhost:8080/yawl"));
		} catch (MalformedURLException e) {
			return null;
		}
	}
	public String getServletName() {
		return "test";
	}
}
