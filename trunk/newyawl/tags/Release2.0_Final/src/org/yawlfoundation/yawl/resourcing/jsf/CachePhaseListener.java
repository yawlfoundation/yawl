package org.yawlfoundation.yawl.resourcing.jsf;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: Michael Adams
 * Creation Date: 23/01/2008
 */

public class CachePhaseListener implements PhaseListener {

    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    public void afterPhase(PhaseEvent phaseEvent) { }

    
    public void beforePhase(PhaseEvent phaseEvent) {
        FacesContext facesContext = phaseEvent.getFacesContext();
        HttpServletResponse response =
                (HttpServletResponse) facesContext.getExternalContext().getResponse();

        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Expires", "Wed, 1 Jan 2008 05:00:00 GMT");     //in the past
        response.setContentType("text/html; charset=UTF-8");       
    }
}


