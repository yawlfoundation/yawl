/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes;

import com.sun.rave.web.ui.component.PanelLayout;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.List;

/**
 * The base class for all DynAttribute classes.
 *
 * Create Date: 18/05/2009
 *
 *  @author Michael Adams
 *  @version 2.0
 */

public abstract class AbstractDynAttribute {


    /*******************************************************************************/

    // ABSTRACT METHODS - to be implemented by extending classes //

    /**
     * Applies modifications affecting the display of fields
     * @param fieldList the list of DynFormField objects for the form. Note that
     * a DynFormField can have DynFormField children of its own, hierarchically.
     * @param wir the workitem being displayed on the form
     * @param p the participant generating the form
     */
    public abstract void adjustFields(List<DynFormField> fieldList, WorkItemRecord wir, Participant p) ;

    /**
     * Applies modifications to the display of dynamic form fields, as required
     * @param parentPanel - the top level container of UIComponent objects for the form.
     * Note that panel may contain other panels, hierarchically
     * @param wir the workitem being displayed on the form
     * @param p the participant generating the form
     */
    public abstract void applyAttributes(PanelLayout parentPanel, WorkItemRecord wir, Participant p) ;

    /*******************************************************************************/

}