/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.component.Upload;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 29/11/11
 */
public class DynFormFileUpload {

    private DocComponent owner;
    private PanelLayout uploadPanel;
    private PanelLayout blockoutPanel;
    private Upload upload;


    public DynFormFileUpload(DocComponent docComponent) {
        owner = docComponent;
    }


    public DocComponent getOwner() { return owner; }



    public List<PanelLayout> getPanels() {
        List<PanelLayout> panels = new ArrayList<PanelLayout>();
        panels.add(getBlockoutPanel());
        panels.add(getUploadPanel());
        return panels;
    }


    public Upload getUploader() {
        if (upload == null) {
            upload = new Upload();
            upload.setId("fileUpload");
            upload.setColumns(50);
            upload.setStyleClass("fileUpload");
            upload.setStyle("left: 12px; top: 40px; position: absolute");
            upload.setImmediate(true);
        }
        return upload;
    }


    private PanelLayout getBlockoutPanel() {
        if (blockoutPanel == null) {
            blockoutPanel = new PanelLayout();
            blockoutPanel.setId("uploadBlockoutPanel");
            blockoutPanel.setStyleClass("transPanel");
            blockoutPanel.setStyle("border:0;");
        }
        return blockoutPanel;
    }


    private PanelLayout getUploadPanel() {
        if (uploadPanel == null) {
            uploadPanel = new PanelLayout();
            uploadPanel.setId("pnlUpload");
            uploadPanel.setStyleClass("orgDataUploadPanel");
            uploadPanel.setStyle("left:" + calcUploadPanelLeft() + "px");
            uploadPanel.getChildren().add(makeHeaderText());
            uploadPanel.getChildren().add(getUploader());
            uploadPanel.getChildren().add(makeOKButton());
            uploadPanel.getChildren().add(makeCancelButton());
            uploadPanel.getAttributes().put("docComponent", owner);   // save ref
        }
        return uploadPanel;
    }


    private StaticText makeHeaderText() {
        StaticText header = new StaticText();
        header.setId("pnlUploadHeader");
        header.setStyleClass("pageSubheading");
        header.setStyle("position:absolute; left: 12px; top: 12px");
        header.setText("Upload file");
        return header;
    }

    
    private Button makeOKButton() {
        Button button = new Button();
        button.setId("btnUpload");
        button.setStyleClass("caseMgtButton");
        button.setStyle("left: 12px; top: 81px");
        button.setText("Upload");
        button.setActionListener(bindListener("#{dynForm.btnUploadAction}"));
        return button;
    }


    private Button makeCancelButton() {
        Button button = new Button();
        button.setId("btnCancelUpload");
        button.setStyleClass("caseMgtButton");
        button.setStyle("left: 125px; top: 81px");
        button.setText("Cancel");
        button.setActionListener(bindListener("#{dynForm.btnCancelUploadAction}"));
        return button;
    }


    private MethodBinding bindListener(String binding) {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding(binding, new Class[]{ActionEvent.class});
    }
    
    
    private int calcUploadPanelLeft() {
        int formCentre = owner.getFormWidth() / 2;
        int uploadPanelCentre = 574 / 2;                // 574 = set width of panel
        return formCentre - uploadPanelCentre;          // neg. offset
    }

}
