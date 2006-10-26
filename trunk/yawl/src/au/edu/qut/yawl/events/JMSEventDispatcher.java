package au.edu.qut.yawl.events;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Properties;

import javax.jms.ObjectMessage;

import au.edu.qut.yawl.util.BeanMap;

/**
 * this dispatches yawl events to the configured embedded jms server 
 * defined in JmsProvider
 * 
 * @author matthew sandoz
 * @author nathan rose
 *
 */
public class JMSEventDispatcher implements YEventDispatcher {

	public JMSEventDispatcher() {
	}

	public void fireEvent(Serializable event) {
		try {
			ObjectMessage om = JmsProvider.getInstance()
					.getObjectMessage(event);
			Map<String, Object> p = BeanMap.getBeanMap(event);
			// just put all the bean properties into the message properties
			for (Map.Entry<String, Object> entry : p.entrySet()) {
				if (entry.getValue() instanceof Serializable) {
					 if (entry.getValue() instanceof Class) {
							om.setObjectProperty(entry.getKey().toString(), entry
									.getValue().toString());						 
					 }
					 else if (entry.getValue() instanceof byte[]) {
						 om.setObjectProperty(entry.getKey().toString(), new String((byte[]) entry.getValue()));
					 }
					 else {
							om.setObjectProperty(entry.getKey().toString(), entry
									.getValue());
					 }
				}
			}
			JmsProvider.getInstance().sendObjectMessage(om);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
