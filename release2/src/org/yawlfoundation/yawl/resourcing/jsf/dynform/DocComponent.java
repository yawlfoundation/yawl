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
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.model.UploadedFile;
import org.yawlfoundation.yawl.documentStore.YDocument;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.client.DocStoreClient;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Michael Adams
 * @date 26/11/11
 */
public class DocComponent extends PanelLayout {
    
    private long docID;
    private String docName;
    private String caseID;

    private Label label;
    private TextField textField;
    private Button btnUp;
    private Button btnDown;
    private DynFormFileUpload uploader;
    private int formWidth;
    
    private static final String UP_TEXT = "⋀";
    private static final String DOWN_TEXT = "⋁";

    
    public DocComponent() { }
    
    public DocComponent(String id, String name, String panelID, TextField field) {
        docID = StringUtil.isNullOrEmpty(id) ? -1 : Long.parseLong(id);
        docName = name;
        textField = field;
        buildComponent(panelID);
    }
    
    public void setName(String name) { docName = name; }
    
    public String getName() { return docName; }

    public void setID(long id) { docID = id; }

    public long getID() { return docID; }

    public void setCaseID(String id) { caseID = id; }

    public String getCaseID() { return caseID; }
    
    public Label getLabel() { return label; }
    
    public void setLabel(Label l) {label = l; }
    
    public int getFormWidth() { return formWidth; }
    
    public void setFormWidth(int width) { formWidth = width; }
    
    
    public String getOutputXML() {
        StringBuilder sb = new StringBuilder();
        if (docID > -1) sb.append(StringUtil.wrap(String.valueOf(docID), "id"));
        sb.append(StringUtil.wrap(docName, "name"));
        return sb.toString();
    }
    
    
    public void setSubComponentStyles(int width) {
        textField.setStyle(String.format("left:0; width: %dpx", width));
        textField.setStyleClass("dynformInputReadOnly");
        setButtonStyle(btnUp, width + 6);                             // 20 = btn width
        setButtonStyle(btnDown, width + 26);
    }

    
    public String processButtonAction(Button btn) {
        if (btn.getText().equals(UP_TEXT)) {
            return prepareUpload();
        }
        else {
            return doDownload();
        }
    }
    
    
    public String doUpload() {
        String errorMsg = null;
        UploadedFile uploadedFile = uploader.getUploader().getUploadedFile();
        byte[] fileBytes = uploadedFile.getBytes();
        if (fileBytes.length > 0) {
            YDocument document = new YDocument(caseID, docID, fileBytes);
            DocStoreClient client = ResourceManager.getInstance().getDocStoreClient();
            try {
                String id = client.putDocument(document, client.getHandle());
                if (! ((id == null) || id.startsWith("<fail"))) {
                    docID = Long.parseLong(StringUtil.unwrap(id));
                    docName = uploadedFile.getOriginalName();
                    textField.setText(docName);
                    btnDown.setDisabled(false);
                    setDownloadButtonToolTip(btnDown);
                }
                else errorMsg = StringUtil.unwrap(id);
            }
            catch (IOException ioe) {
                errorMsg = ioe.getMessage();
            }
        }
        else errorMsg = "Error uploading file. File length is zero";

        completeUpload();
        return errorMsg;
    }


    public void completeUpload() {
        PanelLayout outermostPanel = getOutermostPanel();
        outermostPanel.getChildren().removeAll(uploader.getPanels());
        uploader = null;
    }


    private void buildComponent(String uniqueID) {
        textField.setDisabled(true);                         // can't be edited directly
        getChildren().add(textField);
        btnUp = makeButton("upload", UP_TEXT, uniqueID);
        btnDown = makeButton("download", DOWN_TEXT, uniqueID);
        getChildren().add(btnUp);
        getChildren().add(btnDown);
    }


    private Button makeButton(String name, String text, String uniqueID) {
        Button button = new Button();
        button.setId(uniqueID + "btn" + name);
        button.setNoTextPadding(true);
        button.setMini(true);
        button.setEscape(false);
        button.setStyleClass("dynformDocButton");
        button.setImmediate(true);
        button.setActionListener(bindButtonListener());
        button.setText(text);
        if (name.startsWith("up")) {
            setUploadButtonToolTip(button, docID > -1);       // can always upload
        }
        else {
            button.setDisabled(docID < 0);                   // no id means no doc
            setDownloadButtonToolTip(button);
        }
        return button ;
    }
    
    
    private void setDownloadButtonToolTip(Button btn) {
        String tip = btn.isDisabled() ? "No document to download" : "Download the document";
        btn.setToolTip(tip);
    }


    private void setUploadButtonToolTip(Button btn, boolean hasDoc) {
        String tip = hasDoc ? "Update the document" : "Upload a document";
        btn.setToolTip(tip);
    }


    private MethodBinding bindButtonListener() {
        Application app = FacesContext.getCurrentInstance().getApplication();
        return app.createMethodBinding("#{dynForm.btnDocumentAction}",
                                                  new Class[]{ActionEvent.class});
    }
    
    
    private void setButtonStyle(Button btn, int width) {
        btn.setStyle(String.format("left: %dpx", width));  // 20 = btn width
    }


    private String doDownload() {
        String errorMsg = null;
        DocStoreClient client = ResourceManager.getInstance().getDocStoreClient();
        try {
            YDocument doc = client.getDocument(docID, client.getHandle());
            if (doc.getDocument() != null) {
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletResponse response =
                        ( HttpServletResponse ) context.getExternalContext().getResponse();
                response.setContentType("multipart/form-data");
                response.setBufferSize(doc.getDocumentSize());
                response.setHeader("Content-Disposition",
                        "attachment;filename=\"" + docName + "\"");
                OutputStream os = response.getOutputStream();
                os.write(doc.getDocument());
                os.flush();
                os.close();
                FacesContext.getCurrentInstance().responseComplete();
            }
            else errorMsg = "Unable to create export file: malformed xml.";
        }
        catch (IOException ioe) {
            errorMsg = "Unable to create export file. Please see the log for details.";
        }
        return errorMsg;
    }
    
    private String prepareUpload() {
        PanelLayout outermostPanel = getOutermostPanel();
        uploader = new DynFormFileUpload(this);
        outermostPanel.getChildren().addAll(uploader.getPanels());
        return null;
    }



    private PanelLayout getOutermostPanel() {
        UIComponent parent = getParent();
        while (parent != null) {
            UIComponent grandparent = parent.getParent();
            if (grandparent instanceof PanelLayout) {
                parent = grandparent;
            }
            else break;
        }
        return (PanelLayout) parent;
    }

}
