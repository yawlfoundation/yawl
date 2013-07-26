package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * @author Michael Adams
 * @date 20/06/13
 */
public class LayoutHandler {

    private YLayout _layout;


    protected LayoutHandler(YSpecification specification, String specXML) {
        _layout = new YLayout(specification);
        parse(specXML);
    }


    protected YLayout getLayout() { return _layout; }

    protected void setLayout(YLayout layout) { _layout = layout; }


    protected String appendLayoutXML(String specXML) {
        if (! (_layout == null || specXML == null)) {

            // -1 offset sets the indent to the correct level for insertion
            String layoutXML = _layout.toXNode().toPrettyString(-1, 2);

            // remove last \n from layoutXML, then insert at end of specXML
            layoutXML = layoutXML.substring(0, layoutXML.length() - 1);
            return StringUtil.insert(specXML, layoutXML, specXML.lastIndexOf("</"));
        }
        return specXML;  // return unchanged
    }


    private void parse(String xml) {
        XNode specNode = new XNodeParser().parse(xml);
        if (specNode != null) {
            try {
                _layout.parse(specNode.getChild("layout"));
            }
            catch (YLayoutParseException ylpe) {
                // report?
            }
        }
    }


}
