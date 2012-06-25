package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * @author Michael Adams
 * @date 21/06/12
 */
public class TestLayout {

    public static void main(String args[]) {
        String path = "/Users/adamsmj/Documents/Subversion/distributions/orderfulfilment20.yawl";
        String specXML = StringUtil.fileToString(path);
        try {
            YSpecification spec = YMarshal.unmarshalSpecifications(specXML).get(0);
            YLayout layout = new YLayout(spec);
            XNode node = new XNodeParser().parse(specXML);
            String layoutStr = node.getChild("layout").toString();
            layout.parse(layoutStr);
            p(layout.toXML());
        }
        catch (Exception yse) {
            yse.printStackTrace();
        }
    }

    private static void p(String s) { System.out.println(s); }
}
