/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.DefaultOptionsList;

import javax.faces.FacesException;

/*
 * selectUser.java
 *
 * @author:  Michael Adams
 * Date: 08/01/2008
 */

public class selectUser extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

        public selectUser() { }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    public void init() {
       super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("selectUser Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }


    /***************************************************************************/

    private Page page1 = new Page();
    
    public Page getPage1() { return page1; }
    
    public void setPage1(Page p) { page1 = p; }
    

    private Html html1 = new Html();
    
    public Html getHtml1() { return html1; }
    
    public void setHtml1(Html h) { html1 = h; }


    private Head head1 = new Head();
    
    public Head getHead1() { return head1; }
    
    public void setHead1(Head h) { head1 = h; }


    private Link link1 = new Link();
    
    public Link getLink1() { return link1; }
    
    public void setLink1(Link l) { link1 = l; }


    private Body body1 = new Body();
    
    public Body getBody1() { return body1; }
    
    public void setBody1(Body b) { body1 = b; }


    private Form form1 = new Form();
    
    public Form getForm1() { return form1; }
    
    public void setForm1(Form f) { form1 = f; }


    private DefaultOptionsList lbxUserListDefaultOptions = new DefaultOptionsList();

    public DefaultOptionsList getLbxUserListDefaultOptions() {
        return lbxUserListDefaultOptions;
    }

    public void setLbxUserListDefaultOptions(DefaultOptionsList dol) {
        lbxUserListDefaultOptions = dol;
    }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private Button btnOK = new Button();

    public Button getBtnOK() { return btnOK; }

    public void setBtnOK(Button b) { btnOK = b; }


    private Button btnCancel = new Button();

    public Button getBtnCancel() { return btnCancel; }

    public void setBtnCancel(Button b) { btnCancel = b; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }    
    

    /***************************************************************************/

    private SessionBean _sb = getSessionBean();

    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.selectUser);
    }

    public void destroy() { }

    public String btnOK_action() {
        if (_sb.getLbxUserList().getSelected() != null)  {
            String header = _sb.getUserListFormHeaderText();
            if (header.startsWith("Delegate"))
                _sb.setDelegating(true);
            else if (header.startsWith("Reallocate workitem"))
                _sb.setReallocating(true);
            return _sb.getNavigateTo();
        }
        else {
            return null ;
        }
    }


    public String btnCancel_action() {
        _sb.setAdminQueueAction(null) ;
        return _sb.getNavigateTo();
    }
}

