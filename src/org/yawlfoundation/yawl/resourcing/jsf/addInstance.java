/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import javax.faces.FacesException;

/*
 * selectUser.java
 *
 * @author:  Michael Adams
 * Date: 08/01/2009
 */

public class addInstance extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }


    /****************************************************************************/

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


    private StaticText staticHeader = new StaticText();

    public StaticText getStaticHeader() { return staticHeader; }

    public void setStaticHeader(StaticText st) { staticHeader = st; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }
    

    private Button btnOK = new Button();

    public Button getBtnOK() { return btnOK; }

    public void setBtnOK(Button b) { btnOK = b; }


    private Button btnCancel = new Button();

    public Button getBtnCancel() { return btnCancel; }

    public void setBtnCancel(Button b) { btnCancel = b; }


    private TextArea txtParamVal = new TextArea();

    public TextArea getTxtParamVal() { return txtParamVal; }

    public void setTxtParamVal(TextArea ta) { txtParamVal = ta; }


    private Label lblParam = new Label();

    public Label getLblParam() { return lblParam; }

    public void setLblParam(Label l) { lblParam = l; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelLayout pnlAddInstance ;

    public PanelLayout getPnlAddInstance() { return pnlAddInstance; }

    public void setPnlAddInstance(PanelLayout pnl) { pnlAddInstance = pnl; }


    /****************************************************************************/


    public addInstance() { }

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


    public void destroy() { }


    /****************************************************************************/

    public void prerender() {
        getSessionBean().checkLogon();
        getSessionBean().setActivePage(ApplicationBean.PageRef.selectUser);
    }



    public String btnOK_action() {
        if (txtParamVal.getText() != null)  {
            getSessionBean().setAddInstanceParamVal((String) txtParamVal.getText());
            return "showUserQueues";
        }
        else {
            return null ;
        }
    }


    public String btnCancel_action() {
        getSessionBean().clearAddInstanceParam();
        return "showUserQueues";
    }
}