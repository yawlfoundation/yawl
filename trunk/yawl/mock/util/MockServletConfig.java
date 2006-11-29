/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class MockServletConfig implements ServletConfig {

	public String getInitParameter(String arg0) {
		return "";
	}

	public Enumeration getInitParameterNames() {
		return null;
	}

	public ServletContext getServletContext() {
			return new MockServletContext();
	}

	public String getServletName() {
		return "test";
	}

	public static class MockServletContext implements ServletContext {
		private Hashtable attributes = new Hashtable();

		private Hashtable parameters = new Hashtable();

		public void addInitParameter(String name, String value) {
			parameters.put(name, value);
		}

		public Object getAttribute(String name) {
			return (attributes.get(name));
		}

		public Enumeration getAttributeNames() {
			return (attributes.keys());
		}

		public ServletContext getContext(String uripath) {
			throw new UnsupportedOperationException();
		}

		public String getContextPath() {
			throw new UnsupportedOperationException();
		}

		public String getInitParameter(String name) {
			return ((String) parameters.get(name));
		}

		public Enumeration getInitParameterNames() {
			return (parameters.keys());
		}

		public int getMajorVersion() {
			return (2);
		}

		public String getMimeType(String path) {
			throw new UnsupportedOperationException();
		}

		public int getMinorVersion() {
			return (5);
		}

		public RequestDispatcher getNamedDispatcher(String name) {
			throw new UnsupportedOperationException();
		}

		public String getRealPath(String path) {
			throw new UnsupportedOperationException();
		}

		public RequestDispatcher getRequestDispatcher(String path) {
			throw new UnsupportedOperationException();
		}

		public URL getResource(String path) throws MalformedURLException {
			throw new UnsupportedOperationException();
		}

		public InputStream getResourceAsStream(String path) {
			throw new UnsupportedOperationException();
		}

		public Set getResourcePaths(String path) {
			throw new UnsupportedOperationException();
		}

		public Servlet getServlet(String name) throws ServletException {
			throw new UnsupportedOperationException();
		}

		public String getServletContextName() {
			return ("MockServletContext");
		}

		public String getServerInfo() {
			return ("MockServletContext");
		}

		public Enumeration getServlets() {
			throw new UnsupportedOperationException();
		}

		public Enumeration getServletNames() {
			throw new UnsupportedOperationException();
		}

		public void log(String message) {
		}

		public void log(Exception exception, String message) {
			throw new UnsupportedOperationException();
		}

		public void log(String message, Throwable exception) {
			throw new UnsupportedOperationException();
		}

		public void removeAttribute(String name) {
			attributes.remove(name);
		}

		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}
	}
}
