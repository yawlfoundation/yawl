package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.List;

/**
 * @author Michael Adams
 * @date 11/07/13
 */
public interface DynamicForm {

    /**
     * Build and show a form to capture the work item's output data values.
     *
     * @param title  The form's title
     * @param header A header text for the form top
     * @param schema An XSD schema of the data types and attributes to display
     * @param wir    The work item record to build the form for
     * @return true if form creation is successful
     */
    public boolean makeForm(String title, String header, String schema, WorkItemRecord wir);



    /**
     * Build and show a form to capture the input data values on a case start.
     *
     * @param title      The form's title
     * @param header     A header text for the form top
     * @param schema     An XSD schema of the data types and attributes to display
     * @param parameters a list of the root net's input parameters
     * @return true if form creation is successful
     */
    public boolean makeForm(String title, String header, String schema,
                            List<YParameter> parameters);


    /**
     * Gets the form's data list on completion of the form. The data list must be
     * a well-formed XML string representing the expected data structure for the work
     * item or case start. The opening and closing tag must be the name of task of which
     * the work item is an instance, or of the root net name of the case instance.
     *
     * @return A well-formed XML String of the work item's output data values
     */
    public String getDataList();


    /**
     * Gets a list of component identifiers that represent documents managed by the
     * doc store service. Only for case-level forms.
     *
     * When a document is uploaded to the Document Store at case start, the upload occurs
     * before the case has been launched and thus there is not yet a case id allocated.
     * Once the case launch is successful, this method should be called to associate the
     * case id with the already uploaded document.
     *
     * @return a List of Long identifiers
     */
    public List<Long> getDocComponentIDs();

}
