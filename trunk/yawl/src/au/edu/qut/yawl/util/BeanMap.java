package au.edu.qut.yawl.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanMap {

	public static Map<String, Object> getBeanMap(Object bean) {
		Map p = new HashMap<String,	Object>();
		Method[] methods = bean.getClass().getMethods();
		for (Method m: methods) {
			String name = m.getName();
			if (name.startsWith("get") || name.startsWith("is")) {
				if (m.getParameterTypes().length == 0 && m.getReturnType().getName() != "void") {
				int index = name.startsWith("get") ? 3 : 2;
				String propName = name.substring(index, index + 1).toLowerCase() + name.substring(index + 1);
				Object propValue = null;
					try {
						propValue = m.invoke(bean, new Object[0]);
					} catch (Exception e) {}
					if (propValue != null) {
						p.put(propName, propValue);
					}
				}
			}
		}
		return p;
	}
	
}
