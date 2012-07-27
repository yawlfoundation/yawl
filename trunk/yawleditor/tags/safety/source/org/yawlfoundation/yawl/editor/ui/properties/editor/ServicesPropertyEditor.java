package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import java.io.IOException;
import java.util.*;


public class ServicesPropertyEditor extends ComboPropertyEditor {

    private static Map<String, YAWLServiceReference> services;

    public static final String DEFAULT_WORKLIST = "Default Worklist";

    public ServicesPropertyEditor() {
        super();
        buildServicesMap();
        setAvailableValues(getValues());
    }


    public static YAWLServiceReference getService(String label) {
        return services.get(label);
    }


    private void buildServicesMap() {
        services = new Hashtable<String, YAWLServiceReference>();
        String label = "";

        try {
            for (YAWLServiceReference service : YConnector.getServices()) {
                if (! service.canBeAssignedToTask()) {
                    continue;          // ignore services that are not for tasks.
                }

                if (service.getUserName().equals("DefaultWorklist")) {
                    label = DEFAULT_WORKLIST;
                }
                else if (service.getDocumentation() != null) {
                    label = service.getDocumentation();
                }
                else label = service.getUserName();

                services.put(label, service);
            }
        }
        catch (IOException ioe) {
            // fall through to empty map
        }
    }


    private Object[] getValues() {
        List<String> labels = new ArrayList<String>(services.keySet());
        Collections.sort(labels);

        // move default worklist to first choice
        labels.remove(DEFAULT_WORKLIST);
        labels.add(0, DEFAULT_WORKLIST);
        return labels.toArray();
    }

}
