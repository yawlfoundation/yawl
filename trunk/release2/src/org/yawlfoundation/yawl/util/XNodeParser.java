package org.yawlfoundation.yawl.util;

/**
 * Author: Michael Adams
 * Creation Date: 22/03/2010
 */
public class XNodeParser {

    private boolean _check;

    public XNodeParser() {
        _check = false;
    }

    public XNodeParser(boolean check) {
        _check = check;
    }


    /**
     * Parses a simple xml string into an XNode structure
     * @param s the string to parse
     * @return the root XNode, with contents
     */
    public XNode parse(String s) {
        return parse(s, 0);
    }


    private XNode parse(String s, int depth) {

        // remove any header
        if (s.startsWith("<?xml")) s = s.substring(s.indexOf("?>") + 1);
        
        // if well-formedness check required use JDOM to check it
        if (_check && (JDOMUtil.stringToElement(s) == null)) return null;

        // get the text inside the opening tag and use it to create a new XNode
        String tagDef = s.trim().substring(1, s.indexOf('>'));
        XNode node = newNode(tagDef);
        node.setDepth(depth);

        // if this element is not fully enclosed in a single tag
        if (! tagDef.endsWith("/")) {                     // '>' is already removed
            s = s.substring(s.indexOf(">") + 1).trim();   // all after opening tag

            // if contents starts with a tag
            if (s.startsWith("<")) {

                // get entire inner string to the matching closing tag (exclusive)
                String childStr = s.substring(0, s.indexOf("</" + node.getName()));
                node.addChild(parse(childStr, depth + 1));      // recurse
            }
            else {
                node.setText(s.substring(0, s.indexOf("<")));
            }
        }

        return node;
    }


    /**
     * Creates a new XNode from the text provided
     * @param s the inner contents of an opening tag (ie. name + attributes)
     * @return the created node
     */
    private XNode newNode(String s) {
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);  // lop off '/', if any

        String[] parts = s.split("\\s+");                         // split on whitespace
        XNode node = new XNode(parts[0]);                  // parts[0] is the tag's name

        // if there are any attributes defined, add them to the node
        for (int i=1; i<parts.length; i++) {
            String[] attribute = parts[i].split("=");
            node.addAttribute(attribute[0], dequote(attribute[1]));
        }
        return node;
    }

    
    private String dequote(String s) {
        return s.substring(1, s.lastIndexOf('"'));          // remove surrounding quotes
    }
}
