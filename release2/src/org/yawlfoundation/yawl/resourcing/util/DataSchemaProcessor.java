package org.yawlfoundation.yawl.resourcing.util;

import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.InstanceBuilder;
import org.yawlfoundation.yawl.schema.ElementCreationInstruction;
import org.yawlfoundation.yawl.schema.ElementReuseInstruction;
import org.yawlfoundation.yawl.schema.Instruction;
import org.yawlfoundation.yawl.schema.XMLToolsForYAWL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds a populated schema for the display of data on dynamic forms.
 *
 * Originally based on code from WorkItemProcessor (b8.2)
 *
 * Author: Michael Adams
 * Creation Date: 14/02/2008
 */

public class DataSchemaProcessor {

    public DataSchemaProcessor() { }

    // creates the required data schema at the net level (at case launch)
    public String createSchema(SpecificationData specData)
                          throws IOException, JDOMException,
                                 YSchemaBuildingException, YSyntaxException {

        List<YParameter> inputParams = specData.getInputParams();
        List<Instruction> instructions = getInstructionList(inputParams) ;

        return buildSchema(instructions, specData, specData.getRootNetID()) ;
    }


    // creates the required data schema at the task level
    public String createSchema(SpecificationData specData, TaskInformation taskInfo)
            throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException {

        // get the parameters signature for the task
        YParametersSchema paramsSignature = taskInfo.getParamSchema();

        // for each param build an instruction
        List<YParameter> params = paramsSignature.getCombinedParams();
        List<Instruction> instructions = getInstructionList(params);

        return buildSchema(instructions, specData, taskInfo.getDecompositionID()) ;
    }


    public String getInstanceData(String schema, String root, String data) {
        InstanceBuilder ib = new InstanceBuilder(schema, root, data);
        return ib.getInstance();
    }

    /********************************************************************************/

    private String buildSchema(List<Instruction> instructions, SpecificationData specData,
                               String rootElementName)
             throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException{

        XMLToolsForYAWL xmlToolsForYawl = new XMLToolsForYAWL();
        xmlToolsForYawl.setPrimarySchema(specData.getSchemaLibrary());
        return xmlToolsForYawl.createYAWLSchema(instructions, rootElementName);
    }

    
    private List<Instruction> getInstructionList(List<YParameter> params) {
        List<Instruction> instructions = Collections.synchronizedList(
                                                     new ArrayList<Instruction>());
        for (YParameter param : params) {
            if (null != param.getElementName())
                instructions.add(new ElementReuseInstruction(param.getElementName()));
            else
                instructions.add(new ElementCreationInstruction(
                                param.getName(),
                                param.isUntyped() ? "boolean" : param.getDataTypeName(),
                                ! param.isUserDefinedType()));
        }
        return instructions ;
    }

}
