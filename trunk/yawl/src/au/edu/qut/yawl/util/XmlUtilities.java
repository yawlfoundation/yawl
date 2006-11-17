package au.edu.qut.yawl.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import au.edu.qut.yawl.engine.interfce.Interface_Client;

public class XmlUtilities {
	public static Exception getError(String xml) {
		Exception retval = null;
		if (!Interface_Client.successful(xml)) {
			try {
				String error = URLDecoder.decode(Interface_Client.stripOuterElement(
						Interface_Client.stripOuterElement(xml)), "UTF-8");
				InputStream is = new ByteArrayInputStream(((String) error).getBytes());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s = br.readLine();
				StringTokenizer st = new StringTokenizer(s, ":");
				String name = st.nextElement().toString();
				String message = st.nextElement().toString();
				List<StackTraceElement> list = new ArrayList<StackTraceElement>();
				while ((s = br.readLine()) != null) {
					st = new StringTokenizer(s, "(:) ");
					st.nextElement();
					String fqName = st.nextElement().toString();
					int whereislastdot = fqName.lastIndexOf(".");
					String method = fqName.substring(whereislastdot + 1);
					String clazz = fqName.substring(0, whereislastdot);
					String sourceFile = st.nextElement().toString();
					int lineNumber = -1;
					try {
						lineNumber = Integer.parseInt(st.nextElement().toString());
					} catch (Exception nfe) {
					}
					StackTraceElement e = new StackTraceElement(clazz, method, sourceFile, lineNumber);
					list.add(e);
				}
				Constructor c = Class.forName(name).getConstructor(new Class[] {String.class});
				retval = (Exception) c.newInstance(new Object[] {message});
				retval.setStackTrace(list.toArray(new StackTraceElement[] {}));
			} catch (Exception e) {
				retval = e;
			}
		}
		return retval;
	}
	

}
