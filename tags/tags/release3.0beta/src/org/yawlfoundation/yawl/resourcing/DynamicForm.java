package org.yawlfoundation.yawl.resourcing;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 11/07/13
 */
public interface DynamicForm {

    public static final int CASE_START = 0;
    public static final int WORK_ITEM = 1;

    /**
     * Build and show a form to capture the work item's output data values.
     *
     * @param formType              one of CASE_START (to capture values for net-level input
     *                              parameters on case start) or WORK_ITEM (to display input
     *                              parameters and capture output parameters for a work item)
     * @param title                 The title to show in the browser window or tab
     * @param data                  An XML String containing the data input values with which to
     *                              populate the form
     * @param schema                An XSD schema of the data types and attributes to display
     * @param userDefinedAttributes a Map of key-value pairs representing user-
     *                              defined attributes which may be used to
     *                              configure the form's layout and display
     * @return true if creating and showing the form is successful
     */
    public boolean showForm(int formType, String title, String data, String schema,
                            Map<String, String> userDefinedAttributes);


    /**
     * Gets the work item data list on completion of the form. The data list must be
     * a well-formed XML string representing the expected data structure for the work
     * item. The opening and closing tag must be the name of task of which the work
     * item is an instance.
     *
     * @return A well-formed XML String of the work item's output data values
     */
    public String getDataList();

}
