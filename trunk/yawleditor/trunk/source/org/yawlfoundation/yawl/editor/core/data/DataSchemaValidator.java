package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Element;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

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
        if (StringUtil.isNullOrEmpty(schema)) return Collections.emptyList();
        SchemaHandler validator = new SchemaHandler(schema);
        validator.compileSchema();
        List<String> messages = validator.getMessages();
        if (messages.isEmpty()) messages.addAll(checkReservedTypeNames(validator));
        return messages;
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
    public List<String> validate(String data) {
        if (! hasValidDataTypeDefinition()) {
            return Arrays.asList("Error: Schema is malformed or null");
        }
        if (! schemaHandler.validate(data)) {
            return schemaHandler.getErrorMessages();
        }
        return Collections.emptyList();
    }

    // pre: valid data schema already set
    public List<String> validate(String variableName, String complexTypeData) {
        return validate(StringUtil.wrap(complexTypeData, variableName));
    }


    private List<String> checkReservedTypeNames(SchemaHandler validator) {
        List<String> messages = new ArrayList<String>();
        for (String typeName : validator.getPrimaryTypeNames()) {
            if (YInternalType.isType(typeName)) {
                StringBuilder s = new StringBuilder();
                s.append("Error ")
                 .append(getNamePosition(typeName, validator.getSchema()))
                 .append(": [").append(typeName).append("] is a reserved type name.");
                messages.add(s.toString());
            }
        }
        return messages;
    }


    private String getNamePosition(String typeName, String schema) {
        int pos = schema.indexOf(typeName);
        if (pos < 0) return "";

        // count lines
        int lines=1;
        int lastLineAt = 0;
        for (int i=0; i<pos; i++) {
           if (schema.charAt(i) == '\n') {
               lines++;
               lastLineAt = i;
           }
        }
        return lines + ":" + (pos - lastLineAt - 1);
    }

}
