package com.nexusbpm.services.r;

import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSession;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import com.nexusbpm.services.data.Variable;

/**
 * R component for execution of R scripts.
 * 
 * <b>Use case:</b><br>
 * <p>
 * First draft of a R component that allows the execution of arbitrary R code.
 * 
 * @author Simon Urbanek
 * @author Matthew Sandoz (updates and changes, 2006-2007)
 * @created March 14, 2005
 * @hibernate.subclass discriminator-value="37"
 * @javabean.class name="RComponent" displayName="R Component"
 */
public class RComponent {

	private RServiceData data;
	
	public RComponent(RServiceData data) {
		this.data = data;
	}

	/**
	 * quotes a given string such that it can be passed to R in R code
	 * 
	 * @param s the string to quote
	 * @return quoted string
	 */
	public static String quoteString(String s) {
		int i = s.indexOf('\\');
		while (i >= 0) {
			s = s.substring(0, i + 1) + s.substring(i, s.length());
			i = s.indexOf('\\', i + 2);
		}
		i = s.indexOf('\"');
		while (i >= 0) {
			s = s.substring(0, i) + "\\" + s.substring(i, s.length());
			i = s.indexOf('\"', i + 2);
		}
		i = s.indexOf('\n');
		while (i >= 0) {
			if (i >= s.length() - 1) {
				s = s.substring(0, i) + "\\n";
				break;
			}
			s = s.substring(0, i) + "\\n" + s.substring(i + 1, s.length());
			i = s.indexOf('\n', i + 2);
		}
		return "\"" + s + "\"";
	}

	//wrap the code to run
	public String wrapCode(String code) {
		return "{ .output<-capture.output(.result<-try({ "
		+ data.getCode() 
		+ " },silent=TRUE)); if (any(class(.result)=='try-error')) .result else paste(.output, collapse='\n') }";
	}
	
	
	/**
	 * @see com.nexusbpm.services.component.Component#run()
	 */
	public RServiceData run() throws Exception {
		StringBuilder result = new StringBuilder("R service call results:\n");
		RSession session = null;
		Rconnection c = null;
		try {
			data.setOutput("");

			result.append("Session Attachment: \n");
			session = data.getSessionIn();
			if (session != null) {
				result.append("attaching to " + session + "\n");
				c = session.attach();
			}
			// connect to Rserve if we didn't attach to a session yet
			if (c == null)
			c = new Rconnection(data.getServer());

			// assign any necessary data from incoming attributes
			result.append("Input Parameters: \n");
			for (String attributeName: data.getVariableNames()) {
				if (!shouldPassToServer(attributeName)) continue;
				result.append(" " + data.getType(attributeName) + " " + attributeName + "=" + data.getPlain(attributeName) + "\n");
				Object val = data.get(attributeName);
				if (val instanceof Integer) {
					int i[] = { ((Integer) val).intValue() };
					c.assign(attributeName, i);
				} else if (val instanceof Number) {
					double d[] = { ((Number) val).doubleValue() };
					c.assign(attributeName, d);
				} else if (val != null) {
					c.assign(attributeName, val.toString());
				} else {
					c.assign(attributeName, (String) "");
				}
			}

			REXP x = c.eval(wrapCode(data.getCode()));

			result.append("Execution results:\n" + x.asString() + "\n");
			if (x.getAttribute() != null) { // only error has an attribute (the class)
				data.setError(x.asString() + "\n" + result);// what should we do after an error?
				throw new Exception("R error: " + x.asString());
			}

			result.append("Output Parameters:\n");
			// process dynamic attributes: (storing attributes)
			for (String attributeName: data.getVariableNames()) {
				if (!shouldPassToServer(attributeName)) continue;
				String ac = c.eval(
						"if (exists(\"" + attributeName + "\")) class(" + attributeName
								+ ")[1] else '..'").asString();
				if (!(ac == null || ac.equals(".."))) {
					if (data.getType(attributeName).equals(Variable.TYPE_INT)) {
						result.append(" " + ac + " " + attributeName + "=" + c.eval(attributeName).asInt() + "\n");
						data.setInteger(attributeName, c.eval(attributeName).asInt());
					} else if (data.getType(attributeName).equals(Variable.TYPE_DOUBLE)) {
						result.append(" " + ac + " " + attributeName + "=" + c.eval(attributeName).asDouble() + "\n");
						data.setDouble(attributeName, c.eval(attributeName).asDouble());
					} else {
						result.append(" " + ac + " " + attributeName + "=" + c.eval(attributeName).asString() + "\n");
						data.setPlain(attributeName, c.eval(attributeName).asString());
					}
				} else {
					result.append("Missing Output Variable " + attributeName + "\n");
				}
			}
		} catch (RSrvException re) {
			data.setError(re.getRequestErrorDescription());
		} catch (Exception e) {
			data.setError(e.getStackTrace().toString());
			e.printStackTrace();
		}
		finally {
			result.append("Session Detachment:\n");
			if (data.getKeepSession()) {
				RSession outSession = c.detach();
				data.setSessionOut(outSession);
				result.append("Session: " + session + "\n");
				result.append("suspended session for later use\n");
			} else { 
				c.close();
				data.setSessionOut(null);
			}
		}
		data.setOutput(result.toString());
		return data;
	}// run()

	public boolean shouldPassToServer(String attributeName) {
		boolean retval = true;
		if (
				attributeName.equals(RServiceData.Column.CODE.getName())
				|| 	attributeName.equals(RServiceData.Column.KEEP_SESSION.getName())
				|| 	attributeName.equals(RServiceData.Column.SESSION_IN.getName())
				|| 	attributeName.equals(RServiceData.Column.SESSION_OUT.getName())
		) retval = false;
		return retval;
	}
}
