package org.yawlfoundation.yawl.resourcing.util;

import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.ElementCreationInstruction;
import org.yawlfoundation.yawl.schema.ElementReuseInstruction;
import org.yawlfoundation.yawl.schema.Instruction;
import org.yawlfoundation.yawl.schema.XMLToolsForYAWL;
import org.yawlfoundation.yawl.forms.InstanceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds a populated schema for the display of data on dynamic forms.
 *
 * Based on code from WorkItemProcessor (b8.2)
 *
 * Author: Michael Adams
 * Creation Date: 14/02/2008
 */

public class DataSchemaProcessor {

    public DataSchemaProcessor() { }

    public String createSchema(SpecificationData specData)
                          throws IOException, JDOMException,
                                 YSchemaBuildingException, YSyntaxException {

        List<YParameter> inputParams = specData.getInputParams();
        List<Instruction> instructions = getInstructionList(inputParams) ;

        return buildSchema(instructions, specData, specData.getRootNetID()) ;
    }


    public String createSchema(SpecificationData specData, TaskInformation taskInfo,
                               WorkItemRecord wir) throws IOException, JDOMException,
                                          YSchemaBuildingException, YSyntaxException {

        // get the parameters signature for the task
        YParametersSchema paramsSignature = taskInfo.getParamSchema();

        // for each input param build an instruction
        List<YParameter> inputParams = paramsSignature.getInputParams();
        List<Instruction> inputInstructions = getInstructionList(inputParams);

        // for each output param build an instruction
        List<YParameter> outputParams = paramsSignature.getOutputParams();
        List<Instruction> outputInstructions = getInstructionList(outputParams);

        // remove duplicate instructions (i.e. I/O params)
        List<Instruction> instructions = mergeInstructions(inputInstructions,
                                                           outputInstructions);

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
 //       Collections.sort(instructions);
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


    private List<Instruction> mergeInstructions(List<Instruction> inputs,
                                                List<Instruction> outputs) {

        // for any inst. in both lists, remove it from inputs
        for (Instruction instruction : outputs) {
            Instruction duplicate = getDuplicate(instruction, inputs);
            if (duplicate != null) inputs.remove(duplicate);
        }

        // merge the lists
        outputs.addAll(inputs);
        return outputs;
    }


    // returns the instruction if it is in the list
    private Instruction getDuplicate(Instruction instruction,
                                     List<Instruction> list) {
        Instruction result = null ;
        for (Instruction i : list)
            if (i.getElementName().equals(instruction.getElementName())) {
                result = i;
                break ;
            }

        return result ;
    }

}
