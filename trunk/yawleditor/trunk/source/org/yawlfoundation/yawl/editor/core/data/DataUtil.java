package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.ui.data.DataSchemaValidator;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class DataUtil {

    private String _schema;
    private Document _schemaDoc;
    private DataSchemaValidator _validator;

    private static final String DEFAULT_SCHEMA =
       "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";


    public DataUtil(String schema) {
        _validator = new DataSchemaValidator();
        setSchema(schema);
    }


    public void setSchema(String schema) {
        _schema = schema != null ? schema : DEFAULT_SCHEMA;
        _schemaDoc = JDOMUtil.stringToDocument(schema);
        _validator.setDataTypeSchema(_schema);
    }


    public List<String> getBuiltInTypeNames() {
        return XSDType.getInstance().getBuiltInTypeList();
    }


    public List<String> getInternalTypeNames() {
        List<String> typeList = new ArrayList<String>();
        for (YInternalType internalType : YInternalType.values()) {
            typeList.add(internalType.name());
        }
        return typeList;
    }


    // todo: replace this with a more complete data validation solution
    public List<String> getUserDefinedTypeNames() {
        List<String> typeList = new ArrayList<String>();
            if (_schema != null) {
                Element dataSchema = JDOMUtil.stringToElement(_schema);
                for (Element child : dataSchema.getChildren()) {
                    String name = child.getAttributeValue("name");
                    if (name != null) {
                        typeList.add(name);
                    }
                }
            }
        return typeList;
    }


    public String getMultiInstanceItemType(String dataType) {
        if (_schemaDoc != null) {
            Element root = _schemaDoc.getRootElement();
            Namespace ns = root.getNamespace();
            for (Element child : root.getChildren()) {
                String name = child.getAttributeValue("name");
                if (name != null && name.equals(dataType)) {
                    Element sequence = child.getChild("sequence", ns);
                    if (sequence != null) {
                        Element element = sequence.getChild("element", ns);
                        if (element != null) {
                            String maxOccurs = element.getAttributeValue("maxOccurs");
                            if (maxOccurs != null && (maxOccurs.equals("unbounded") ||
                                    ! maxOccurs.equals("1"))) {
                                return element.getAttributeValue("type");
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public List<String> getDataTypeNames() {
        List<String> typeList = getBuiltInTypeNames();
        typeList.addAll(getInternalTypeNames());
        typeList.addAll(getUserDefinedTypeNames());
        Collections.sort(typeList);
        return typeList;
    }


    public String getXQuerySuffix(String dataType) {
        if (XSDType.getInstance().isBuiltInType(dataType)) {
            return "text()";
        }
        if (YInternalType.isName(dataType)) {
            return "*";
        }

        switch(_validator.getDataTypeComplexity(dataType)) {
            case Complex: return "*";
            case Simple:
            case Unknown:
            default: return "text()";
        }
    }

}
