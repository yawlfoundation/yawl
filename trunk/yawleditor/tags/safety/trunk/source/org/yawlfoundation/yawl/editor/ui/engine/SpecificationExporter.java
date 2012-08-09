/*
 * Created on 09/02/2006
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.YEditorSpecification;
import org.yawlfoundation.yawl.editor.core.data.*;
import org.yawlfoundation.yawl.editor.core.identity.EngineIdentifier;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingCategory;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.allocators.GenericAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.PiledExecution;
import org.yawlfoundation.yawl.resourcing.constraints.SeparationOfDuties;
import org.yawlfoundation.yawl.resourcing.filters.GenericFilter;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.OfferInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.SecondaryResources;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SpecificationExporter extends EngineEditorInterpretor {

    private static YEditorSpecification _spec = SpecificationModel.getSpec();


    public static boolean checkAndExportEngineSpecToFile(SpecificationModel model,
                                                         String fullFileName) {  //todo save/saveas
        boolean success = false;
        try {
            if (checkUserDefinedDataTypes(model)) {
                YLayout layout = new LayoutExporter().parse(model);
                populateSpecification(model);
                analyseIfNeeded(model);
                if (fullFileName != null) {
                    _spec.saveAs(fullFileName, UserSettings.getFileSaveOptions(), layout);
                }
                else _spec.save(layout, UserSettings.getFileSaveOptions());
                success = true;
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The attempt to save this specification to file failed.\n " +
                            "Please see the log for details", "Save File Error",
                    JOptionPane.ERROR_MESSAGE);
            LogWriter.error("Error saving specification to file.", e);
        }
        reset();
        return success;
    }


    private static boolean checkUserDefinedDataTypes(SpecificationModel editorSpec) {
        List<String> results = new ArrayList<String>();
        results.addAll(EngineSpecificationValidator.checkUserDefinedDataTypes(editorSpec));
        if (! results.isEmpty()) {
            YAWLEditor.getInstance().showProblemList("Export Errors", results);
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Could not export Specification due to missing or invalid user-defined " +
                            "datatypes.\nPlease see the problem list below for details.",
                    "Export Datatype Error", JOptionPane.ERROR_MESSAGE);
        }
        return results.isEmpty();
    }


    private static void analyseIfNeeded(SpecificationModel model) {
        List<String> results = new ArrayList<String>();

        if (UserSettings.getVerifyOnSave()) {
            results.addAll(EngineSpecificationValidator.getValidationResults(
                    _spec.getSpecification()));
        }
        if (UserSettings.getAnalyseOnSave()) {
            results.addAll(model.analyse());
        }
        YAWLEditor.getInstance().showProblemList("Analysis Results", results);
    }


    public static String getEngineSpecificationXML(SpecificationModel editorSpec) {
        return getEngineSpecificationXML(populateSpecification(editorSpec));
    }


    public static String getEngineSpecificationXML(YSpecification engineSpec) {
        try {
            return YMarshal.marshal(engineSpec);
        } catch (Exception e) {
            LogWriter.error("Error marshalling specification to XML.", e);
            return null;
        }
    }


    public static YSpecification populateSpecification(SpecificationModel model) {
        YSpecification spec = _spec.getSpecification();
        initialise();
        generateEngineMetaData(model);

        //important:  Engine API expects nets to be pre-generated before composite tasks reference them.
        //            We need to build the nets first, and THEN populate the nets with elements.
        generateRootNet(model);
        generateSubNets(model);
        populateEngineNets(spec);
        generateEngineDataTypeDefinition(model);

        return spec;
    }

    private static void generateEngineDataTypeDefinition(SpecificationModel model) {
        String schema = adjustSchemaForInternalTypes(model.getDataTypeDefinition());

        // remove any header inadvertently inserted by user
        if (schema.startsWith("<?xml")) {
            schema = schema.substring(schema.indexOf('>') + 1);
        }
        try {
            _spec.setSchema(schema);
        }
        catch (Exception eActual) {
            try {
                schema = adjustSchemaForInternalTypes(SpecificationModel.DEFAULT_TYPE_DEFINITION);
                _spec.setSchema(schema);
            }
            catch (Exception eDefault) {}
        }
    }


    private static String adjustSchemaForInternalTypes(String specDataSchema) {
        for (YInternalType type : YInternalType.values()) {
            specDataSchema = type.adjustSchema(specDataSchema);
        }
        return specDataSchema;
    }


    private static void generateEngineMetaData(SpecificationModel model) {
        _spec.setVersion(model.getVersionNumber());
    }

    private static void generateRootNet(SpecificationModel model) {
        YNet rootEngineNet = generateEngineNet(model.getStartingNet());
        editorToEngineNetMap.put(model.getStartingNet(), rootEngineNet);
    }


    private static void generateSubNets(SpecificationModel model) {
        for (NetGraphModel editorNet : model.getSubNets()) {
            YNet engineSubNet = generateEngineNet(editorNet);
            editorToEngineNetMap.put(editorNet, engineSubNet);
        }
    }


    private static YNet generateEngineNet(NetGraphModel editorNet) {
        YNet engineNet = (YNet) editorNet.getDecomposition().getNet();
        engineNet.setID(XMLUtilities.toValidXMLName(editorNet.getName()));
        generateDecompositionParameters(engineNet, editorNet.getDecomposition());
        establishEngineLocalVariables(engineNet, editorNet);
        return engineNet;
    }


    private static void establishEngineLocalVariables(YNet engineNet, NetGraphModel editorNet) {
        DataVariableSet dataSet = editorNet.getDecomposition().getVariables();
        for (DataVariable editorNetVariable : dataSet.getLocalVariables()) {
            YVariable localVar = editorNetVariable.getLocalVariable();
            String dataType = localVar.getDataTypeName();
            if (dataType.equals("string")) {
                localVar.setInitialValue(XMLUtilities.quoteSpecialCharacters(localVar.getInitialValue()));
            }
            else flagInternalTypeUse(dataType);

            engineNet.setLocalVariable(localVar);
        }

        // There is a requirement in the engine that passing data to a net's output parameters needs
        // the net to have a local variable of the same name (and type, I assume) as the output parameter.
        // Lachlan assures me that the assignment is automatic so long as the name of the net local variable
        // and the net output parameter are the same. Highly redundant IMO, but it won't work in the engine
        // without this intermediate variable being used for state transport.
        for (DataVariable editorNetVariable : dataSet.getOutputVariables()) {
            if (editorNetVariable.getUsage() != DataVariable.USAGE_OUTPUT_ONLY) {
                continue;
            }

            YVariable engineNetVariable = new YVariable(engineNet);
            engineNetVariable.setDataTypeAndName(
                    editorNetVariable.getDataType(),
                    editorNetVariable.getName(),
                    XML_SCHEMA_URI
            );

            engineNet.setLocalVariable(engineNetVariable);
        }
    }


    private static void populateEngineNets(YSpecification spec) {
        for (NetGraphModel netModel : editorToEngineNetMap.keySet()) {
            populateEngineNetFrom(spec, netModel);
        }
    }


    private static void populateEngineNetFrom(YSpecification spec,
                                              NetGraphModel netModel)  {
        YNet engineNet = editorToEngineNetMap.get(netModel);
        NetElementSummary editorNetSummary = new NetElementSummary(netModel);

        engineNet.setInputCondition(generateInputCondition(engineNet, editorNetSummary));
        engineNet.setOutputCondition(generateOutputCondition(engineNet, editorNetSummary));

        setElements(spec, engineNet, editorNetSummary);
        setFlows(engineNet, editorNetSummary);
        setCancellationSetDetail(editorNetSummary);
    }


    private static YInputCondition generateInputCondition(YNet engineNet,
                                                 NetElementSummary editorNetSummary) {
        InputCondition editorCondition = editorNetSummary.getInputCondition();
        YInputCondition engineInputCondition = new YInputCondition(
                editorCondition.getEngineId(), engineNet);

        String label = editorCondition.getLabel();
        if (!StringUtil.isNullOrEmpty(label)) {
            engineInputCondition.setName(XMLUtilities.quoteSpecialCharacters(label));
        }

        editorToEngineElementMap.put(editorCondition, engineInputCondition);
        return engineInputCondition;
    }


    private static YOutputCondition generateOutputCondition(YNet engineNet,
                                                   NetElementSummary editorNetSummary) {
        OutputCondition editorCondition = editorNetSummary.getOutputCondition();

        YOutputCondition engineOutputCondition = new YOutputCondition(
                editorCondition.getEngineId(), engineNet);

        String label = editorCondition.getLabel();
        if (!StringUtil.isNullOrEmpty(label)) {
            engineOutputCondition.setName(XMLUtilities.quoteSpecialCharacters(label));
        }

        editorToEngineElementMap.put(editorCondition, engineOutputCondition);
        return engineOutputCondition;
    }


    private static void setElements(YSpecification engineSpec,
                                    YNet engineNet,
                                    NetElementSummary editorNetSummary) {
        setConditions(engineNet, editorNetSummary);
        setAtomicTasks(engineSpec, engineNet, editorNetSummary);
        setCompositeTasks(engineNet, editorNetSummary);
    }


    private static void setConditions(YNet engineNet,
                                      NetElementSummary editorNetSummary) {

        for (Condition editorCondition : editorNetSummary.getConditions()) {
            editorToEngineElementMap.put(editorCondition,
                    editorCondition.generateYCondition(engineNet));
        }
    }


    private static void setAtomicTasks(YSpecification engineSpec,
                                       YNet engineNet,
                                       NetElementSummary editorNetSummary) {

        for (YAWLAtomicTask yawlAtomicTask : editorNetSummary.getAtomicTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlAtomicTask;

            YAtomicTask engineAtomicTask =
                    new YAtomicTask(
                            editorTask.getEngineId(),
                            editorToEngineJoin(editorTask),
                            editorToEngineSplit(editorTask),
                            engineNet
                    );

            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineAtomicTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineAtomicTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }

            String label = editorTask.getLabel();
            if (! StringUtil.isNullOrEmpty(label)) {
                engineAtomicTask.setName(XMLUtilities.quoteSpecialCharacters(label));
            }

            String doco = editorTask.getDocumentation();
            if (! StringUtil.isNullOrEmpty(doco)) {
                engineAtomicTask.setDocumentation(
                        XMLUtilities.quoteSpecialCharacters(doco));
            }

            generateTimeoutDetailForAtomicTask(engineAtomicTask, editorTask);

            if (editorTask.getDecomposition() != null) {
                YAWLServiceGateway engineDecomposition =
                        (YAWLServiceGateway) engineSpec.getDecomposition(
                                XMLUtilities.toValidXMLName(editorTask.getDecomposition().getLabel())
                        );
                if (engineDecomposition == null) {
                    engineDecomposition =
                            generateAtomicDecompositionFor(
                                    engineSpec,
                                    editorTask
                            );
                }
                engineAtomicTask.setDecompositionPrototype(engineDecomposition);
            }

            generateDecompositionParameters(
                    engineAtomicTask.getDecompositionPrototype(),
                    editorTask.getDecomposition()
            );

            populateTaskParameterQueries(engineAtomicTask, editorTask);
            populateMultipleInstanceDetail(engineAtomicTask, editorTask);
            populateResourceDetail(engineAtomicTask, editorTask);
            setCustomFormDetail(engineAtomicTask, editorTask);

            engineNet.addNetElement(engineAtomicTask);
            editorToEngineElementMap.put(editorTask, engineAtomicTask);
        }
    }

    private static void generateTimeoutDetailForAtomicTask(YTask engineTask, YAWLTask editorTask) {
        if (editorTask instanceof AtomicTask) {
            TaskTimeoutDetail timerDetail = ((AtomicTask) editorTask).getTimeoutDetail();
            if (timerDetail != null) {
                engineTask.setTimerParameters(timerDetail.getTimerParameters());
            }
        }
    }


    private static YAWLServiceGateway generateAtomicDecompositionFor(YSpecification engineSpec,
                                                                     YAWLTask editorTask) {

        WebServiceDecomposition editorDecomposition =
                ((YAWLAtomicTask)editorTask).getWSDecomposition();

        YAWLServiceGateway engineDecomposition =
                new YAWLServiceGateway(
                        XMLUtilities.toValidXMLName(editorDecomposition.getLabel()),
                        engineSpec
                );

        if (taskNeedsWebServiceDetail(editorTask)) {

            YAWLServiceReference engineService = new YAWLServiceReference(
                    editorDecomposition.getServiceURI(),
                    engineDecomposition
            );

            engineDecomposition.setYawlService(engineService);
        }

        engineSpec.addDecomposition(engineDecomposition);

        return engineDecomposition;
    }

    private static void generateDecompositionParameters(YDecomposition engineDecomposition,
                                                        Decomposition editorDecomposition) {
        if(editorDecomposition == null) {
            return;
        }

        //BEGIN: MLF merge extended decomposition attributes
        for (String key : editorDecomposition.getAttributes().keySet()) {
            String value = editorDecomposition.getAttribute(key);
            if (value.length() > 0) {
                engineDecomposition.setAttribute(key, XMLUtilities.quoteSpecialCharacters(value));
            }
        }
        //END: MLF

        generateDecompositionInputParameters(engineDecomposition, editorDecomposition);
        generateDecompositionOutputParameters(engineDecomposition, editorDecomposition);
    }

    private static void generateDecompositionInputParameters(YDecomposition engineDecomposition,
                                                             Decomposition editorDecomposition) {

        for (DataVariable editorInputVariable : editorDecomposition.getVariables().getInputVariables()) {
            // Don't need to create parameters for local variables.
            if (editorInputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
                continue;
            }

            generateEngineParameter(
                    engineDecomposition, editorInputVariable.getInputVariable());
        }
    }

    private static void generateEngineParameter(YDecomposition engineDecomposition,
                                                YParameter parameter) {

        if (parameter.getParamType() == YParameter._INPUT_PARAM_TYPE) {
            if (! StringUtil.isNullOrEmpty(parameter.getInitialValue())) {
                parameter.setInitialValue(XMLUtilities.quoteSpecialCharacters(parameter.getInitialValue()));
                engineDecomposition.addInputParameter(parameter);
            }
        }
        if (parameter.getParamType() == YParameter._OUTPUT_PARAM_TYPE) {
            if (! StringUtil.isNullOrEmpty(parameter.getDefaultValue())) {
                parameter.setDefaultValue(XMLUtilities.quoteSpecialCharacters(parameter.getDefaultValue()));
            }
            engineDecomposition.addOutputParameter(parameter);
        }

        flagInternalTypeUse(parameter.getDataTypeName());
    }


    private static void flagInternalTypeUse(String dataType) {
        for (YInternalType type : YInternalType.values()) {
            if (type.name().equals(dataType)) {
                type.setUsed(true);
                break;
            }
        }
    }

    private static void generateDecompositionOutputParameters(YDecomposition engineDecomposition,
                                                              Decomposition editorDecomposition) {

        //    int ordering = editorDecomposition.getVariableCount();

        for (DataVariable editorOutputVariable : editorDecomposition.getVariables().getOutputVariables()) {
            // Don't need to create parameters for local variables.
            if (editorOutputVariable.getUsage() == DataVariable.USAGE_LOCAL) {
                continue;
            }

            generateEngineParameter(
                    engineDecomposition, editorOutputVariable.getOutputVariable());
        }
    }

    private static void setCompositeTasks(YNet engineNet, NetElementSummary editorNetSummary) {

        for (YAWLCompositeTask yawlCompositeTask : editorNetSummary.getCompositeTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlCompositeTask;

            YCompositeTask engineCompositeTask =
                    new YCompositeTask(
                            editorTask.getEngineId(),
                            editorToEngineJoin(editorTask),
                            editorToEngineSplit(editorTask),
                            engineNet
                    );
            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineCompositeTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineCompositeTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }
            if (editorTask.hasLabel()) {
                engineCompositeTask.setName(
                        XMLUtilities.quoteSpecialCharacters(
                                editorTask.getLabel()
                        )
                );
            }

            if (editorTask.getDecomposition() != null) {
                NetGraphModel editorUnfoldingNet =
                        SpecificationUtilities.getNetModelFromName(
                                editorTask.getDecomposition().getLabel()
                        );

                engineCompositeTask.setDecompositionPrototype(
                         editorToEngineNetMap.get(editorUnfoldingNet)
                );
            }


            populateTaskParameterQueries(engineCompositeTask, editorTask);
            populateMultipleInstanceDetail(engineCompositeTask, editorTask);
            engineNet.addNetElement(engineCompositeTask);
            editorToEngineElementMap.put(editorTask, engineCompositeTask);
        }
    }
    private static void populateTaskParameterQueries(YTask engineTask, YAWLTask editorTask) {
        populateTaskInputParameterQueries(engineTask, editorTask);
        populateTaskOutputParameterQueries(engineTask, editorTask);
    }

    private static void populateTaskInputParameterQueries(YTask engineTask,
                                                          YAWLTask editorTask) {

        for (Parameter editorInputParameter : editorTask.getParameterLists().getInputParameters().getParameters()) {
            if (editorTask instanceof YAWLMultipleInstanceTask) {
                YAWLMultipleInstanceTask multiInstanceTask = (YAWLMultipleInstanceTask) editorTask;
                if (multiInstanceTask.getMultipleInstanceVariable() != null &&
                        !multiInstanceTask.getMultipleInstanceVariable().equals(editorInputParameter.getVariable())) {

                    setDataBindingForParam(engineTask, editorInputParameter, true);
                }
            } else {
                setDataBindingForParam(engineTask, editorInputParameter, true);
            }
        }
    }

    private static void setDataBindingForParam(YTask engineTask,
                                               Parameter editorParameter, boolean input) {
        String name = editorParameter.getVariable().getName();
        String query = editorParameter.getQuery();
        if ((query != null) && (! query.startsWith("#external:"))) {
            query = XMLUtilities.getTaggedOutputVariableWithContent(name, query);
        }
        if (input) {
            engineTask.setDataBindingForInputParam(query, name);
        }
        else {
            engineTask.setDataBindingForOutputExpression(query, name);
        }
    }

    private static void setCustomFormDetail(YTask engineTask, YAWLTask editorTask) {
        if (!(editorTask instanceof YAWLAtomicTask)) {
            return;
        }
        String urlStr = editorTask.getCustomFormURL();
        if (urlStr != null) {
            try {
                engineTask.setCustomFormURI(new URL(urlStr));
            }
            catch (MalformedURLException mue) {
                // do nothing
            }
        }
    }

    private static void populateResourceDetail(YTask engineTask, YAWLTask editorTask) {
        populateResourceMappingDetail(engineTask, editorTask);
    }

    private static void populateResourceMappingDetail(YTask engineTask, YAWLTask editorTask) {
        if (!(editorTask instanceof YAWLAtomicTask)) {
            return;
        }

        // pure routing tasks also don't have anything to do with resourcing.
        if (editorTask.getDecomposition() == null) {
            return;
        }

        YAWLAtomicTask atomicEditorTask = (YAWLAtomicTask) editorTask;

        if (!atomicEditorTask.getWSDecomposition().invokesWorklist()) {
            return;
        }

        // Below, we should have specified a resource mapping, but it looks as if
        // we haven't. We supply a default at this point.

        if (atomicEditorTask.getResourceMapping() == null) {
            atomicEditorTask.setResourceMapping(
                    new ResourceMapping()
            );
        }


        engineTask.getDecompositionPrototype().setExternalInteraction(
                atomicEditorTask.getWSDecomposition().isManualInteraction()
        );

        engineTask.getDecompositionPrototype().setCodelet(
                atomicEditorTask.getWSDecomposition().getCodelet()
        );

        populateLogPredicates(engineTask.getDecompositionPrototype(),
                atomicEditorTask.getWSDecomposition()) ;

        ResourceMap engineResourceMapping = new ResourceMap();

        populateOfferInteractionDetail(
                atomicEditorTask.getResourceMapping(),
                engineResourceMapping
        );

        populateAllocateInteractionDetail(
                atomicEditorTask.getResourceMapping(),
                engineResourceMapping
        );

        populateStartInteractionDetail(
                atomicEditorTask.getResourceMapping(),
                engineResourceMapping
        );

        populateSecondaryResourcesDetail(
                atomicEditorTask.getResourceMapping(),
                engineResourceMapping
        );

        populateTaskPrivileges(
                atomicEditorTask.getResourceMapping(),
                engineResourceMapping
        );

        engineTask.setResourcingXML(engineResourceMapping.toXML());
    }

    private static void populateOfferInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {

        engineResourceMapping.setOfferInteraction(
                new OfferInteraction(
                        convertEditorInteractionToEngineInteraction(
                                editorResourceMapping.getOfferInteractionPoint()
                        )
                )
        );

        if (editorResourceMapping.getOfferInteractionPoint() != ResourceMapping.SYSTEM_INTERACTION_POINT) {
            return;
        }

        //  we care only for specifying system interaction behaviour from now on.

        if (editorResourceMapping.getRetainFamiliarTask() != null) {
            engineResourceMapping.getOfferInteraction().setFamiliarParticipantTask(
                    ((YAWLTask) editorResourceMapping.getRetainFamiliarTask()).getEngineId()
            );
        }

        populateOfferParticipants(
                editorResourceMapping,
                engineResourceMapping
        );

        populateOfferRoles(
                editorResourceMapping,
                engineResourceMapping
        );

        populateOfferInputParameters(
                editorResourceMapping,
                engineResourceMapping
        );

        populateOfferFilters(
                editorResourceMapping,
                engineResourceMapping
        );

        populateRuntimeConstraints(
                editorResourceMapping,
                engineResourceMapping
        );
    }

    private static void populateOfferParticipants(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        if (editorResourceMapping.getBaseUserDistributionList() == null ||
                editorResourceMapping.getBaseUserDistributionList().size() == 0) {
            return;
        }

        for(Participant participant : editorResourceMapping.getBaseUserDistributionList()) {
            engineResourceMapping.getOfferInteraction().addParticipantUnchecked(
                    participant.getID()
            );
        }
    }

    private static void populateOfferRoles(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        if (editorResourceMapping.getBaseRoleDistributionList() == null ||
                editorResourceMapping.getBaseRoleDistributionList().size() == 0) {
            return;
        }

        for(Role role : editorResourceMapping.getBaseRoleDistributionList()) {
            engineResourceMapping.getOfferInteraction().addRoleUnchecked(role.getID());
        }
    }

    private static void populateOfferInputParameters(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        if (editorResourceMapping.getBaseVariableContentList() == null ||
                editorResourceMapping.getBaseVariableContentList().size() == 0) {
            return;
        }

        for(DataVariableContent content : editorResourceMapping.getBaseVariableContentList()) {
            if (content.getContentType() == DataVariableContent.DATA_CONTENT_TYPE) {
                continue;  // skip normal variables. We want just the role and user types to be added.
            }
            engineResourceMapping.getOfferInteraction().addInputParam(
                    content.getVariable().getName(),
                    convertVariableContentType(
                            content.getContentType()
                    )
            );
        }
    }

    private static void populateOfferFilters(ResourceMapping editorResourceMapping,
                                             ResourceMap engineResourceMapping) {
        for(AbstractSelector editorFilter : editorResourceMapping.getResourcingFilters()) {
            String filterName = editorFilter.getCanonicalName();
            if (filterName.startsWith("org.yawlfoundation.yawl.")) {
                filterName = filterName.substring(filterName.lastIndexOf('.') + 1);
            }
            GenericFilter engineFilter = new GenericFilter(filterName);

            // only want params with non-null values
            Map<String, String> paramMap = editorFilter.getParams();
            for (String name : paramMap.keySet()) {
                String value = paramMap.get(name);
                if ((value != null) && (value.length() > 0)) {
                    engineFilter.addParam(name, value);
                }
            }
            engineResourceMapping.getOfferInteraction().addFilter(
                    engineFilter
            );
        }
    }


    private static void populateRuntimeConstraints(ResourceMapping editorResourceMapping,
                                                   ResourceMap engineResourceMapping) {
        if (editorResourceMapping.isPrivilegeEnabled(ResourceMapping.CAN_PILE_PRIVILEGE)) {
            engineResourceMapping.getOfferInteraction().addConstraint(
                    new PiledExecution()
            );
        }

        if (editorResourceMapping.getSeparationOfDutiesTask() != null) {
            SeparationOfDuties constraint = new SeparationOfDuties();
            constraint.setKeyValue(
                    "familiarTask",
                    ((YAWLTask) editorResourceMapping.getSeparationOfDutiesTask()).getEngineId()
            );

            engineResourceMapping.getOfferInteraction().addConstraint(
                    constraint
            );
        }
    }

    private static int convertVariableContentType(int contentType) {
        switch(contentType) {
            case(DataVariableContent.PARTICIPANT_CONTENT_TYPE): {
                return OfferInteraction.USER_PARAM;
            }
            case(DataVariableContent.ROLE_CONTENT_TYPE): {
                return OfferInteraction.ROLE_PARAM;
            }
            default: {
                return OfferInteraction.USER_PARAM;
            }
        }
    }

    private static void populateAllocateInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        engineResourceMapping.setAllocateInteraction(
                new AllocateInteraction(
                        convertEditorInteractionToEngineInteraction(
                                editorResourceMapping.getAllocateInteractionPoint()
                        )
                )
        );

        if (editorResourceMapping.getAllocateInteractionPoint() == ResourceMapping.SYSTEM_INTERACTION_POINT) {
            String name = editorResourceMapping.getAllocationMechanism().getCanonicalName();
            if (name.startsWith("org.yawlfoundation.yawl.")) {
                name = name.substring(name.lastIndexOf('.') + 1);
            }

            engineResourceMapping.getAllocateInteraction().setAllocator(
                    new GenericAllocator(name));
        }
    }

    private static void populateStartInteractionDetail(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        engineResourceMapping.setStartInteraction(
                new StartInteraction(
                        convertEditorInteractionToEngineInteraction(
                                editorResourceMapping.getStartInteractionPoint()
                        )
                )
        );
    }


    private static void populateSecondaryResourcesDetail(
            ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {

        SecondaryResources sr = new SecondaryResources();
        for (Object o : editorResourceMapping.getSecondaryResourcesList()) {
            if (o instanceof Participant) {
                sr.getDefaultDataSet().addParticipantUnchecked(((Participant) o).getID());
            }
            else if (o instanceof Role) {
                sr.getDefaultDataSet().addRoleUnchecked(((Role) o).getID());
            }
            else if (o instanceof NonHumanResource) {
                sr.getDefaultDataSet().addNonHumanResourceUnchecked(((NonHumanResource) o).getID());
            }
            else if (o instanceof ResourcingCategory) {
                ResourcingCategory category = (ResourcingCategory) o;
                String subcat = category.getSubcategory();
                if (subcat != null) {
                    sr.getDefaultDataSet().addNonHumanCategoryUnchecked(category.getId(), subcat);
                }
                else sr.getDefaultDataSet().addNonHumanCategoryUnchecked(category.getId());
            }
        }
        engineResourceMapping.setSecondaryResources(sr);
    }

    private static int convertEditorInteractionToEngineInteraction(int editorInteraction)  {
        switch(editorInteraction) {
            case ResourceMapping.SYSTEM_INTERACTION_POINT: {
                return AbstractInteraction.SYSTEM_INITIATED;
            }
            case ResourceMapping.USER_INTERACTION_POINT: {
                return AbstractInteraction.USER_INITIATED;
            }
        }
        return AbstractInteraction.USER_INITIATED;
    }

    private static void populateTaskPrivileges(ResourceMapping editorResourceMapping, ResourceMap engineResourceMapping) {
        TaskPrivileges enginePrivileges = new TaskPrivileges();

        for(Integer enabledPrivilege : editorResourceMapping.getEnabledPrivileges()) {
            enginePrivileges.allowAll(convertPrivilege(enabledPrivilege));
        }

        engineResourceMapping.setTaskPrivileges(
                enginePrivileges
        );
    }

    private static int convertPrivilege(int editorPrivilege) {
        switch(editorPrivilege) {
            case(ResourceMapping.CAN_SUSPEND_PRIVILEGE): {
                return TaskPrivileges.CAN_SUSPEND;
            }
            case(ResourceMapping.CAN_REALLOCATE_STATELESS_PRIVILEGE): {
                return TaskPrivileges.CAN_REALLOCATE_STATELESS;
            }
            case(ResourceMapping.CAN_REALLOCATE_STATEFUL_PRIVILEGE): {
                return TaskPrivileges.CAN_REALLOCATE_STATEFUL;
            }
            case(ResourceMapping.CAN_DEALLOCATE_PRIVILEGE): {
                return TaskPrivileges.CAN_DEALLOCATE;
            }
            case(ResourceMapping.CAN_DELEGATE_PRIVILEGE): {
                return TaskPrivileges.CAN_DELEGATE;
            }
            case(ResourceMapping.CAN_SKIP_PRIVILEGE): {
                return TaskPrivileges.CAN_SKIP;
            }
            case(ResourceMapping.CAN_PILE_PRIVILEGE): {
                return TaskPrivileges.CAN_PILE;
            }
            default: {
                return TaskPrivileges.CAN_DEALLOCATE;
            }
        }
    }

    private static boolean taskNeedsWebServiceDetail(YAWLTask editorTask) {
        if (!(editorTask.getDecomposition() instanceof WebServiceDecomposition)) {
            return false;
        }

        WebServiceDecomposition decomposition =
                (WebServiceDecomposition) editorTask.getDecomposition();

        return ! decomposition.invokesWorklist();
    }


    private static void populateLogPredicates(YDecomposition engineDecomp,
                                              WebServiceDecomposition editorDecomp) {
        if ((editorDecomp.getLogPredicateStarted() != null) ||
                (editorDecomp.getLogPredicateCompletion() != null)) {
            YLogPredicate predicate = new YLogPredicate();
            predicate.setStartPredicate(editorDecomp.getLogPredicateStarted());
            predicate.setCompletionPredicate(editorDecomp.getLogPredicateCompletion());
            engineDecomp.setLogPredicate(predicate);
        }
    }

    private static void populateTaskOutputParameterQueries(YTask engineTask,
                                                           YAWLTask editorTask) {

        for (Parameter editorOutputParameter : editorTask.getParameterLists().getOutputParameters().getParameters()) {
            if (editorTask instanceof YAWLMultipleInstanceTask) {
                YAWLMultipleInstanceTask multiInstanceTask = (YAWLMultipleInstanceTask) editorTask;
                if (multiInstanceTask.getResultNetVariable() != null &&
                        !multiInstanceTask.getResultNetVariable().equals(editorOutputParameter.getVariable())) {
                    setDataBindingForParam(engineTask, editorOutputParameter, false);
                }
            } else {
                setDataBindingForParam(engineTask, editorOutputParameter, false);
            }
        }
    }

    private static void populateMultipleInstanceDetail(YTask engineTask,
                                                       YAWLTask editorTask) {

        if (!(editorTask instanceof YAWLMultipleInstanceTask)) {
            return;
        }

        YAWLMultipleInstanceTask editorMultiInstanceTask =
                (YAWLMultipleInstanceTask) editorTask;

        engineTask.setUpMultipleInstanceAttributes(
                String.valueOf(editorMultiInstanceTask.getMinimumInstances()),
                String.valueOf(editorMultiInstanceTask.getMaximumInstances()),
                String.valueOf(editorMultiInstanceTask.getContinuationThreshold()),
                editorToEngineMultiInstanceMode(editorMultiInstanceTask)
        );

        // data perspective input

        DataVariable editorTaskInstanceVariable =
                editorMultiInstanceTask.getMultipleInstanceVariable();

        String taskInstanceVariableName = null;

        if (editorTaskInstanceVariable != null) {
            taskInstanceVariableName = editorTaskInstanceVariable.getName();
            //    }

            HashMap<String, String> inputMapping = new HashMap<String, String>();

            inputMapping.put(
                    taskInstanceVariableName,
                    editorMultiInstanceTask.getAccessorQuery()
            );

            engineTask.setDataMappingsForTaskStarting(inputMapping);

            engineTask.setMultiInstanceInputDataMappings(
                    taskInstanceVariableName,
                    editorMultiInstanceTask.getSplitterQuery()
            );
        }
        // data perspective output

        DataVariable editorNetResultVariable =
                editorMultiInstanceTask.getResultNetVariable();

        String netResultVariableName = null;

        if (editorNetResultVariable != null) {
            netResultVariableName = editorNetResultVariable.getName();

            HashMap<String, String> outputMapping = new HashMap<String, String>();

            outputMapping.put(
                    editorMultiInstanceTask.getInstanceQuery(),
                    netResultVariableName
            );

            engineTask.setDataMappingsForTaskCompletion(
                    outputMapping
            );

            // Whatever I pass to the engine below must be exactly the
            // same text as the ..DataMappings... calls above.
            engineTask.setMultiInstanceOutputDataMappings(
                    editorMultiInstanceTask.getInstanceQuery(),
                    XMLUtilities.getTaggedOutputVariableWithContent(
                            editorNetResultVariable.getName(),
                            editorMultiInstanceTask.getAggregateQuery()
                    )
            );

        }
    }

    private static void setFlows(YNet engineNet, NetElementSummary editorNetSummary) {
        for (YAWLFlowRelation editorFlow : editorNetSummary.getFlows()) {
            YAWLVertex editorFlowSource = editorFlow.getSourceVertex();
            YAWLVertex editorFlowTarget = editorFlow.getTargetVertex();

            YExternalNetElement engineSource =
                    (YExternalNetElement) editorToEngineElementMap.get(editorFlowSource);
            YExternalNetElement engineTarget =
                    (YExternalNetElement) editorToEngineElementMap.get(editorFlowTarget);

            YFlow firstEngineFlow;

            if (editorFlowSource instanceof YAWLTask &&
                    editorFlowTarget instanceof YAWLTask) {

                EngineIdentifier engineID = SpecificationModel.getInstance().getUniqueIdentifier("ImplicitCondition");
                YCondition implicitEngineCondition = new YCondition(engineID.toString(), engineNet);
                implicitEngineCondition.setImplicit(true);
                engineNet.addNetElement(implicitEngineCondition);

                firstEngineFlow = new YFlow(engineSource, implicitEngineCondition);
                engineSource.addPostset(firstEngineFlow);

                YFlow secondEngineFlow = new YFlow(implicitEngineCondition, engineTarget);
                implicitEngineCondition.addPostset(secondEngineFlow);

                addFlowConditionMapping(editorFlow, implicitEngineCondition);
            }
            else { // no need for an implicit condition. Phew!
                firstEngineFlow = new YFlow(engineSource, engineTarget);
                engineSource.addPostset(firstEngineFlow);
            }

            // Pass in data perspective data.

            if (editorFlow.hasXorSplitAsSource() && !editorFlow.isDefaultFlow()) {
                firstEngineFlow.setEvalOrdering(editorFlow.getPriority());
                firstEngineFlow.setXpathPredicate(editorFlow.getPredicate());
            }

            if (editorFlow.hasXorSplitAsSource() || editorFlow.hasOrSplitAsSource()) {
                firstEngineFlow.setIsDefaultFlow(editorFlow.isDefaultFlow());
                if (editorFlow.hasOrSplitAsSource()) {
                    firstEngineFlow.setXpathPredicate(editorFlow.getPredicate());
                }
            }

            editorToEngineElementMap.put(editorFlow, firstEngineFlow);
        }
    }

    private static void setCancellationSetDetail(NetElementSummary editorNetSummary) {
        for (YAWLTask editorTriggerTask : editorNetSummary.getTasksWithCancellationSets()) {
           List<YExternalNetElement> engineCancellationSet = new ArrayList<YExternalNetElement>();
            for (YAWLCell element : editorTriggerTask.getCancellationSet().getSetMembers()) {
                if (element instanceof YAWLFlowRelation) {
                    engineCancellationSet.add(getConditionForFlow((YAWLFlowRelation) element));
                }
                else {
                    engineCancellationSet.add((YExternalNetElement)
                            editorToEngineElementMap.get(element)
                    );
                }
            }

            YTask engineTriggerTask = (YTask) editorToEngineElementMap.get(editorTriggerTask);
            engineTriggerTask.addRemovesTokensFrom(engineCancellationSet);
        }
    }


    private static int editorToEngineJoin(YAWLTask task) {
        if (task.hasJoinDecorator()) {
            JoinDecorator decorator = task.getJoinDecorator();
            switch (decorator.getType()) {
                case Decorator.AND_TYPE: return YTask._AND;
                case Decorator.OR_TYPE:  return YTask._OR;
             }
        }
        return YTask._XOR;       // default
    }

    private static int editorToEngineSplit(YAWLTask task) {
        if (task.hasSplitDecorator()) {
            SplitDecorator decorator = task.getSplitDecorator();
            switch (decorator.getType()) {
                case Decorator.OR_TYPE: return YTask._OR;
                case Decorator.XOR_TYPE: return YTask._XOR;
            }
        }
        return YTask._AND;       // default
    }

    private static String editorToEngineMultiInstanceMode(YAWLMultipleInstanceTask task) {
        switch(task.getInstanceCreationType()) {
            case YAWLMultipleInstanceTask.DYNAMIC_INSTANCE_CREATION: {
                return YMultiInstanceAttributes._creationModeDynamic;
            }
            default: {
                return YMultiInstanceAttributes._creationModeStatic;
            }
        }
    }

    private static void addFlowConditionMapping(YAWLFlowRelation editorFlow,
                                                YCondition engineCondition) {
        editorFlowEngineConditionMap.put(editorFlow, engineCondition);
    }

    private static YCondition getConditionForFlow(YAWLFlowRelation editorFlow) {
        return editorFlowEngineConditionMap.get(editorFlow);
    }


}
