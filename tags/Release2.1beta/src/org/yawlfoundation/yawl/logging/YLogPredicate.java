package org.yawlfoundation.yawl.logging;

import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 17/02/2010
 */
public class YLogPredicate {

    private String _startPredicate ;
    private String _completionPredicate ;

    public YLogPredicate() {}

    public YLogPredicate(Element xml) {
        fromXML(xml);
    }

    public YLogPredicate(Element xml, Namespace ns) {
        fromXML(xml, ns);
    }

    public String getStartPredicate() { return _startPredicate; }

    public String getCompletionPredicate() { return _completionPredicate; }

    public void setStartPredicate(String predicate) {
        _startPredicate = predicate ;
    }

    public void setCompletionPredicate(String predicate) {
        _completionPredicate = predicate ;
    }

    public String getParsedStartPredicate(YWorkItem workItem) {
        if (_startPredicate == null) return null;
        return new YLogPredicateWorkItemParser(workItem).parse(_startPredicate);
    }

    public String getParsedStartPredicate(YDecomposition decomp) {
        if (_startPredicate == null) return null;
        return new YLogPredicateDecompositionParser(decomp).parse(_startPredicate);
    }

    public String getParsedStartPredicate(YParameter param) {
        if (_startPredicate == null) return null;
        return new YLogPredicateParameterParser(param).parse(_startPredicate);
    }

    public String getParsedCompletionPredicate(YWorkItem workItem) {
        if (_completionPredicate == null) return null;
        return new YLogPredicateWorkItemParser(workItem).parse(_completionPredicate);
    }

    public String getParsedCompletionPredicate(YDecomposition decomp) {
        if (_completionPredicate == null) return null;
        return new YLogPredicateDecompositionParser(decomp).parse(_completionPredicate);
    }

    public String getParsedCompletionPredicate(YParameter param) {
        if (_completionPredicate == null) return null;
        return new YLogPredicateParameterParser(param).parse(_completionPredicate);
    }


    public void fromXML(Element xml) {
        _startPredicate = xml.getChildText("start");
        _completionPredicate = xml.getChildText("completion");
    }

    public void fromXML(Element xml, Namespace ns) {
        _startPredicate = xml.getChildText("start", ns);
        _completionPredicate = xml.getChildText("completion", ns);
    }

    public String toXML() {
        if ((_startPredicate == null) && (_completionPredicate == null)) return "";

        StringBuilder xml = new StringBuilder("<logPredicate>");
        if (_startPredicate != null)
            xml.append(StringUtil.wrapEscaped(_startPredicate, "start"));
        if (_completionPredicate != null)
            xml.append(StringUtil.wrapEscaped(_completionPredicate, "completion"));
        xml.append("</logPredicate>");
        return xml.toString();
    }

}
