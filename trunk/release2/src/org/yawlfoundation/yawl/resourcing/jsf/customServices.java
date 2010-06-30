/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.util.HttpURLValidator;

import javax.faces.FacesException;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import java.util.List;

/**
 *  Backing bean for the case mgt page.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  v0.1, 23/01/2008
 *
 *
 *  Last Date: 09/04/2008
 */

public class customServices extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }


    public customServices() { }


    public void init() {
        super.init();

        try {
            _init();
        } catch (Exception e) {
            log("customServices Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void destroy() { }


    // Return references to scoped data beans

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }

    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /********************************************************************************/

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

    public void setForm1(Form f) { form1 = f;}


    private PanelLayout pnlAddService = new PanelLayout();

    public PanelLayout getPnlAddService() { return pnlAddService; }

    public void setPnlAddService(PanelLayout pl) { pnlAddService = pl; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private Label lblName = new Label();

    public Label getLblName() { return lblName; }

    public void setLblName(Label l) { lblName = l; }


    private Label lblPassword = new Label();

    public Label getLblPassword() { return lblPassword; }

    public void setLblPassword(Label l) { lblPassword = l; }


    private Label lblConfirmPassword = new Label();

    public Label getLblConfirmPassword() { return lblConfirmPassword; }

    public void setLblConfirmPassword(Label l) { lblConfirmPassword = l; }


    private Label lblURL = new Label();

    public Label getLblURL() { return lblURL; }

    public void setLblURL(Label l) { lblURL = l; }


    private Label lblDesc = new Label();

    public Label getLblDesc() { return lblDesc; }

    public void setLblDesc(Label l) { lblDesc = l; }


    private TextField txtName = new TextField();

    public TextField getTxtName() { return txtName; }

    public void setTxtName(TextField tf) { txtName = tf; }


    private PasswordField txtPassword ;

    public PasswordField getTxtPassword() { return txtPassword; }

    public void setTxtPassword(PasswordField pw) { txtPassword = pw; }


    private PasswordField txtConfirmPassword ;

    public PasswordField getTxtConfirmPassword() { return txtConfirmPassword; }

    public void setTxtConfirmPassword(PasswordField pw) { txtConfirmPassword = pw; }


    private TextField txtURL = new TextField();

    public TextField getTxtURL() { return txtURL; }

    public void setTxtURL(TextField tf) { txtURL = tf; }


    private TextArea txtDescription = new TextArea();

    public TextArea getTxtDescription() { return txtDescription; }

    public void setTxtDescription(TextArea ta) { txtDescription = ta; }


    private PanelLayout pnlServices = new PanelLayout();

    public PanelLayout getPnlServices() { return pnlServices; }

    public void setPnlServices(PanelLayout pl) { pnlServices = pl; }


    private StaticText staticText2 = new StaticText();

    public StaticText getStaticText2() { return staticText2; }

    public void setStaticText2(StaticText st) { staticText2 = st; }


    private HtmlDataTable dataTable1 = new HtmlDataTable();

    public HtmlDataTable getDataTable1() { return dataTable1; }

    public void setDataTable1(HtmlDataTable hdt) { dataTable1 = hdt; }


    private UIColumn colName = new UIColumn();

    public UIColumn getColName() { return colName; }

    public void setColName(UIColumn uic) { colName = uic; }


    private HtmlOutputText colNameRows = new HtmlOutputText();

    public HtmlOutputText getColNameRows() { return colNameRows; }

    public void setColNameRows(HtmlOutputText hot) { colNameRows = hot; }


    private HtmlOutputText colNameHeader = new HtmlOutputText();

    public HtmlOutputText getColNameHeader() { return colNameHeader; }

    public void setColNameHeader(HtmlOutputText hot) { colNameHeader = hot; }


    private UIColumn colURI = new UIColumn();

    public UIColumn getColURI() { return colURI; }

    public void setColURI(UIColumn uic) { colURI = uic; }


    private HtmlOutputText colURIRows = new HtmlOutputText();

    public HtmlOutputText getColURIRows() { return colURIRows; }

    public void setColURIRows(HtmlOutputText hot) { colURIRows = hot; }


    private HtmlOutputText colURIHeader = new HtmlOutputText();

    public HtmlOutputText getColURIHeader() { return colURIHeader; }

    public void setColURIHeader(HtmlOutputText hot) { colURIHeader = hot; }


    private UIColumn colDescription = new UIColumn();

    public UIColumn getColDescription() { return colDescription; }

    public void setColDescription(UIColumn uic) { colDescription = uic; }


    private HtmlOutputText colDescriptionRows = new HtmlOutputText();

    public HtmlOutputText getColDescriptionRows() { return colDescriptionRows; }

    public void setColDescriptionRows(HtmlOutputText hot) { colDescriptionRows = hot; }


    private HtmlOutputText colDescriptionHeader = new HtmlOutputText();

    public HtmlOutputText getColDescriptionHeader() { return colDescriptionHeader; }

    public void setColDescriptionHeader(HtmlOutputText hot) { colDescriptionHeader = hot; }


    private UIColumn colSBar = new UIColumn();

    public UIColumn getColSBar() { return colSBar; }

    public void setColSBar(UIColumn uic) { colSBar = uic; }


    private Button btnRemove = new Button();

    public Button getBtnRemove() { return btnRemove; }

    public void setBtnRemove(Button b) { btnRemove = b; }


    private Button btnAdd = new Button();

    public Button getBtnAdd() { return btnAdd; }

    public void setBtnAdd(Button b) { btnAdd = b; }


    private Button btnClear = new Button();

    public Button getBtnClear() { return btnClear; }

    public void setBtnClear(Button b) { btnClear = b; }


    private HiddenField hdnRowIndex = new HiddenField();

    public HiddenField getHdnRowIndex() { return hdnRowIndex; }

    public void setHdnRowIndex(HiddenField hf) { hdnRowIndex = hf; }


    private List<YAWLServiceReference> dataList ;

    public List<YAWLServiceReference> getDataList() { return dataList; }

    public void setDataList(List<YAWLServiceReference> d) { dataList = d; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private PanelGroup pnlGroup ;

    public PanelGroup getPnlGroup() { return pnlGroup; }

    public void setPnlGroup(PanelGroup group) { pnlGroup = group; }


    /********************************************************************************/

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = getSessionBean().getMessagePanel();

    /**
     * Overridden method that is called immediately before the page is rendered
     */
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.customServices);
        _sb.showMessagePanel();
    }


    // remove the selected service from the engine
    public String btnRemove_action() {
        try {
            Integer selectedRowIndex = new Integer((String) hdnRowIndex.getValue());
            _sb.removeRegisteredService(selectedRowIndex);
            msgPanel.success("Service successfully removed.");
        }
        catch (NumberFormatException nfe) {
            msgPanel.error("No service selected to remove.");
        }
        catch (Exception e) {
            msgPanel.error("Could not remove service. See logs for details.");
            e.printStackTrace();
        }
        return null;
    }


    // add a new service to the engine
    public String btnAdd_action() {
        String name = (String) txtName.getText() ;
        String password = (String) txtPassword.getText();
        String uri = (String) txtURL.getText();
        String doco = (String) txtDescription.getText();
        if (! (isNullOrEmpty(name) || isNullOrEmpty(password) ||
                isNullOrEmpty(uri) || isNullOrEmpty(doco))) {
            if (! password.equals(txtConfirmPassword.getText())) {
                msgPanel.error("Password and Confirm Password are different.");
            }
            else {
                String validMsg = HttpURLValidator.validate(uri);
                if (validMsg.startsWith("<success")) {
                    _sb.addRegisteredService(name, password, uri, doco);
                    clearInputs();
                    msgPanel.success("Service successfully added.");
                }
                else {
                    msgPanel.error("Invalid URL: " + msgPanel.format(validMsg));
                }
            }    
        }
        else
            msgPanel.warn("Add Service: Please enter values in all fields.");

        return null;
    }


    public boolean isNullOrEmpty(String text) {
        return (text == null) || (text.length() == 0);
    }

    
    public String btnClear_action() {
        clearInputs();
        return null;
    }

    
    private void clearInputs() {
        txtName.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtURL.setText("");
        txtDescription.setText("");
    }
    
}

