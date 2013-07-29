package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Element;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 7/05/12
 */
public class DataSchemaValidator {

    private SchemaHandler schemaHandler;

    public enum DataTypeComplexity { Unknown, Simple, Complex }


    public DataSchemaValidator() { }

    public DataSchemaValidator(String schema) {  setDataTypeSchema(schema); }


    public List<String> getSchemaValidationResults(String schema) {
        SchemaHandler validator = new SchemaHandler(schema);
        validator.compileSchema();
        return validator.getMessages();
    }

    public void setDataTypeSchema(String schema) {
        schemaHandler = new SchemaHandler(schema);
    }

    public String getDataTypeSchema() {
        if (hasValidDataTypeDefinition()) {
            return schemaHandler.getSchema();
        }
        return null;
    }

    public boolean hasValidDataTypeDefinition() {
        return (schemaHandler != null) && schemaHandler.compileSchema();
    }

    public Set<String> getPrimarySchemaTypeNames() {
        return (schemaHandler != null) ? schemaHandler.getPrimaryTypeNames() :
                Collections.<String>emptySet();
    }

    public boolean isDefinedTypeName(String name) {
        return getPrimarySchemaTypeNames().contains(name);
    }

    public String createSchemaForVariable(String variableName, String dataType) {
        if (schemaHandler != null) {
            DataSchemaBuilder dsb = new DataSchemaBuilder(schemaHandler.getTypeMap());
            return dsb.buildSchema("data", variableName, dataType);
        }
        else throw new IllegalStateException("No schema has been set for this specification.");
    }

    public DataTypeComplexity getDataTypeComplexity(String dataType) {
        if (schemaHandler == null) {
            throw new IllegalStateException("No schema has been set for this specification.");
        }
        Element definition = schemaHandler.getDataTypeDefinition(dataType);
        if (definition.getName().endsWith("complexType")) {
            return DataTypeComplexity.Complex;
        }
        else if (definition.getName().endsWith("simpleType")) {
            return DataTypeComplexity.Simple;
        }
        else return DataTypeComplexity.Unknown;
    }

    // pre: valid data schema already set
    public String validate(String data) {
        if (! hasValidDataTypeDefinition()) {
            return "Error: Schema is malformed or null";
        }
        if (! schemaHandler.validate(data)) {
            return schemaHandler.getConcatenatedMessage();
        }
        return "";
    }

    // pre: valid data schema already set
    public String validate(String variableName, String complexTypeData) {
        return validate(StringUtil.wrap(complexTypeData, variableName));
    }
}
