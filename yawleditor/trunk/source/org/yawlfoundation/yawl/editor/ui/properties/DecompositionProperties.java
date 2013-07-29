package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ServicesPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Michael Adams
 * @date 16/07/12
 */
public class DecompositionProperties extends CellProperties {

    private YDecomposition _decomposition;
    private UserDefinedAttributes _udAttributes;

    public DecompositionProperties() {
        super();
        _udAttributes = UserDefinedAttributes.getInstance();
    }

    protected void setVertex(YAWLVertex vertex) {
        super.setVertex(vertex);
        _decomposition = ((YAWLTask) vertex).getDecomposition();
        _udAttributes.setDecomposition(_decomposition);
        setReadOnly("CustomService", _decomposition instanceof YNet);
    }


    public String getStartLogPredicate() {
        if (_decomposition != null) {
            YLogPredicate predicate = _decomposition.getLogPredicate();
            return predicate != null ? predicate.getStartPredicate() : "";
        }
        return "";
    }

    public void setStartLogPredicate(String predicate) {
        if (_decomposition != null) {
            YLogPredicate logPredicate = _decomposition.getLogPredicate();
            if (logPredicate == null) {
                logPredicate = new YLogPredicate();
                _decomposition.setLogPredicate(logPredicate);
            }
            logPredicate.setStartPredicate(predicate);
            setDirty();
        }
    }


    public String getCompletionLogPredicate() {
        if (_decomposition != null) {
            YLogPredicate predicate = _decomposition.getLogPredicate();
            return predicate != null ? predicate.getCompletionPredicate() : "";
        }
        return "";
    }

    public void setCompletionLogPredicate(String predicate) {
        if (_decomposition != null) {
            YLogPredicate logPredicate = _decomposition.getLogPredicate();
            if (logPredicate == null) {
                logPredicate = new YLogPredicate();
                _decomposition.setLogPredicate(logPredicate);
            }
            logPredicate.setCompletionPredicate(predicate);
            setDirty();
        }
    }


    public String getCustomService() {
        String serviceName = null;
        if (_decomposition != null) {
            if (_decomposition instanceof YNet) return "";

            YAWLServiceReference service =
                    ((YAWLServiceGateway) _decomposition).getYawlService();
            serviceName = service != null ? service.getServiceName() :
                    ServicesPropertyEditor.DEFAULT_WORKLIST;
        }

        // update for old specs
        if (serviceName == null || serviceName.equals("Default Engine Worklist")) {
            serviceName = ServicesPropertyEditor.DEFAULT_WORKLIST;
        }
        enableServiceProperties(serviceName);
        return serviceName;
    }

    public void setCustomService(String serviceName) {
        if (_decomposition != null) {
            ((YAWLServiceGateway) _decomposition).setYawlService(
                    ServicesPropertyEditor.getService(serviceName));
            setDirty();
            enableServiceProperties(serviceName);
        }
    }


    public boolean isAutomated() {
        boolean auto = _decomposition != null &&
                ! _decomposition.requiresResourcingDecisions();
        setReadOnly("Codelet", ! auto);
        setReadOnly("Resourcing", auto);
        setReadOnly("CustomForm", auto);
        return auto;
    }

    public void setAutomated(boolean auto) {
        if (_decomposition != null) {
            _decomposition.setExternalInteraction(!auto);
            setReadOnly("Codelet", ! auto);
            setReadOnly("Resourcing", auto);
            setReadOnly("CustomForm", auto);
            setDirty();
            if (! auto) firePropertyChange("Codelet", null);
        }
    }


    public String getCodelet() {
        if (_decomposition != null) {
            String codelet = _decomposition.getCodelet();
            if (codelet != null) return codelet;
        }
        return null;
    }

    public void setCodelet(String codelet) {
        if (_decomposition != null) {
            if (codelet != null && codelet.equals("None")) codelet = null;
            _decomposition.setCodelet(codelet);
            setDirty();
        }
    }


    public NetTaskPair getTaskDataVariables() {
        return new NetTaskPair(specificationHandler.getControlFlowHandler().getRootNet(),
                _decomposition, (YAWLTask) vertex);
    }

    public void setTaskDataVariables(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }



    /**************************************************************************/

    public Color getExBackgroundColour() {
        String colourStr = _decomposition.getAttribute("background-color");
        return colourStr != null ? hexToColor(colourStr) : Color.WHITE;
    }

    public void setExBackgroundColour(Color colour) {
        _decomposition.setAttribute("background-color", colorToHex(colour));
    }


    public Color getExBackgroundAltColour() {
        String colourStr = _decomposition.getAttribute("background-alt-color");
        return colourStr != null ? hexToColor(colourStr) : Color.WHITE;
    }

    public void setExBackgroundAltColour(Color colour) {
        _decomposition.setAttribute("background-alt-color", colorToHex(colour));
    }


    public FontColor getExFont() {
        return getFontColorFromAttributes("font");
    }

    public void setExFont(FontColor font) {
        setAttributesFromFontColor(font, "font");
    }


    public FontColor getExHeaderFont() {
        return getFontColorFromAttributes("header-font");
    }

    public void setExHeaderFont(FontColor font) {
        setAttributesFromFontColor(font, "header-font");
    }


    public boolean isExHideBanner() {
        String hideBanner = _decomposition.getAttribute("hideBanner");
        return hideBanner != null && hideBanner.equalsIgnoreCase("true");
    }

    public void setExHideBanner(boolean hideBanner) {
        if (hideBanner) _decomposition.setAttribute("hideBanner", "true");
    }


    public String getExJustify() {
        String justify = _decomposition.getAttribute("justify");
        return justify != null ? justify : "left";
    }


    public void setExJustify(String justify) {
        if (! justify.equals("left")) _decomposition.setAttribute("justify", justify);
    }


    public String getExLabel() {
        return _decomposition.getAttribute("label");
    }

    public void setExLabel(String label) {
        if (! StringUtil.isNullOrEmpty(label)) _decomposition.setAttribute("label", label);
    }


    public Color getExPageBackgroundColour() {
        String colourStr = _decomposition.getAttribute("page-background-color");
        return colourStr != null ? hexToColor(colourStr) : Color.WHITE;
    }

    public void setExPageBackgroundColour(Color colour) {
        _decomposition.setAttribute("page-background-color", colorToHex(colour));
    }


    public File getExPageBackgroundImage() {
        String path = _decomposition.getAttribute("page-background-image");
        return path != null ? new File(path) : null;
    }

    public void setExPageBackgroundImage(File path) {
        if (path != null) {
            _decomposition.setAttribute("page-background-image", path.getAbsolutePath());
        }
    }


    public boolean getExReadOnly() {
        String readOnly = _decomposition.getAttribute("readOnly");
        return readOnly != null && readOnly.equalsIgnoreCase("true");
    }

    public void setExReadOnly(boolean readOnly) {
        if (readOnly) _decomposition.setAttribute("readOnly", String.valueOf(readOnly));
    }


    public String getExTitle() {
        return _decomposition.getAttribute("title");
    }

    public void setExTitle(String title) {
        if (! StringUtil.isNullOrEmpty(title)) _decomposition.setAttribute("title", title);
    }


    public Object getUdAttributeValue() {
        return _udAttributes.getValue();
    }

    public void setUdAttributeValue(Object value) {
        _udAttributes.setValue(value);
    }


    /****************************************************************************/

    private void enableServiceProperties(String service) {
        boolean isDefaultWorklist = service.equals(ServicesPropertyEditor.DEFAULT_WORKLIST);
        setReadOnly("Automated", ! isDefaultWorklist);
        if (! isDefaultWorklist) {
            firePropertyChange("Automated", false);
        }
    }


    private void setAttributesFromFontColor(FontColor fontColor, String prefix) {
        Font font = fontColor.getFont();
        String family = font.getFamily();
        if (! UIManager.getDefaults().getFont("Label.font").getFamily().equals(family)) {
            _decomposition.setAttribute(prefix + "-family", family);
        }
        int size = font.getSize();
        if (size != SpecificationModel.getInstance().getFontSize()) {
            _decomposition.setAttribute(prefix + "-size", String.valueOf(size));
        }
        String style = intToFontStyle(font.getStyle());
        if (style != null) {
            _decomposition.setAttribute(prefix + "-style", style);
        }
        Color colour = fontColor.getColour();
        if (! colour.equals(Color.BLACK)) {
            _decomposition.setAttribute(prefix + "-color", colorToHex(colour));
        }
    }


    private FontColor getFontColorFromAttributes(String prefix) {
        String family = _decomposition.getAttribute(prefix + "-family");
        if (family == null) family = UIManager.getDefaults().getFont("Label.font").getFamily();
        int size = StringUtil.strToInt(_decomposition.getAttribute(prefix + "-size"), 0);
        if (size == 0) size = SpecificationModel.getInstance().getFontSize();
        int style = fontStyleToInt(_decomposition.getAttribute(prefix + "-style"));
        String colourStr = _decomposition.getAttribute(prefix + "-color");
        Color colour = StringUtil.isNullOrEmpty(colourStr) ? Color.BLACK : hexToColor(colourStr);
        return new FontColor(new Font(family, style, size), colour);
    }


    private int fontStyleToInt(String style) {
        if (style == null) return Font.PLAIN;
        if (style.equals("bold")) return Font.BOLD;
        if (style.equals("italic")) return Font.ITALIC;
        if (style.equals("bold,italic")) return Font.BOLD | Font.ITALIC;
        return Font.PLAIN;
    }


    private String intToFontStyle(int style) {
        switch (style) {
            case Font.BOLD : return "bold";
            case Font.ITALIC : return "italic";
            case Font.BOLD | Font.ITALIC : return "bold,italic";
            default : return null;
        }
    }

}
