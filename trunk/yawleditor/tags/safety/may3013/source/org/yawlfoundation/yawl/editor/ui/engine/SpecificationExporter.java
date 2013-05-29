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

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.core.identity.EngineIdentifier;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetElementSummary;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourcingCategory;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;
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

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecificationExporter extends EngineEditorInterpretor {

    private static YSpecificationHandler _spec = SpecificationModel.getHandler();

    // todo - make sure editor saveas matches core saveas
    public static boolean checkAndExportEngineSpecToFile(SpecificationModel model,
                                                         String fullFileName) {
        boolean success = false;
        try {
            if (checkUserDefinedDataTypes(model)) {
                YLayout layout = new LayoutExporter().parse(model);
                populateSpecification(model);
                analyseIfNeeded(model);
                if (fullFileName != null) {
                    _spec.saveAs(fullFileName, layout, UserSettings.getFileSaveOptions());
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
        YNet engineNet = (YNet) editorNet.getDecomposition();
        engineNet.setID(XMLUtilities.toValidXMLName(editorNet.getName()));
        return engineNet;
    }


     private static void populateEngineNets(YSpecification spec) {
        for (NetGraphModel netModel : editorToEngineNetMap.keySet()) {
            populateEngineNetFrom(spec, netModel);
        }
    }


    private static void populateEngineNetFrom(YSpecification spec,
                                              NetGraphModel netModel)  {
        NetElementSummary editorNetSummary = new NetElementSummary(netModel);
        setElements(editorNetSummary);
 //       setFlows(engineNet, editorNetSummary);
 //       setCancellationSetDetail(editorNetSummary);
    }



    private static void setElements(NetElementSummary editorNetSummary) {

        // temp
        editorToEngineElementMap.put(editorNetSummary.getInputCondition(),
                editorNetSummary.getInputCondition().getShadowCondition());
        editorToEngineElementMap.put(editorNetSummary.getOutputCondition(),
                editorNetSummary.getOutputCondition().getShadowCondition());

        setAtomicTasks(editorNetSummary);
        setCompositeTasks(editorNetSummary);
    }


    private static void setAtomicTasks(NetElementSummary editorNetSummary) {

        for (YAWLAtomicTask yawlAtomicTask : editorNetSummary.getAtomicTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlAtomicTask;

            YTask engineAtomicTask = editorTask.getShadowTask();

            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineAtomicTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineAtomicTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }

            populateResourceDetail(engineAtomicTask, editorTask);
            editorToEngineElementMap.put(editorTask, engineAtomicTask);
        }
    }


    private static void setCompositeTasks(NetElementSummary editorNetSummary) {

        for (YAWLCompositeTask yawlCompositeTask : editorNetSummary.getCompositeTasks()) {
            YAWLTask editorTask = (YAWLTask) yawlCompositeTask;

            YTask engineCompositeTask = editorTask.getShadowTask();

            if (editorTask.isConfigurable()) {
                DefaultConfigurationExporter defaultConfig = new DefaultConfigurationExporter();
                ConfigurationExporter config = new ConfigurationExporter();
                engineCompositeTask.setConfiguration(config.getTaskConfiguration(editorTask));
                engineCompositeTask.setDefaultConfiguration(defaultConfig.getTaskDefaultConfiguration(editorTask));
            }
            editorToEngineElementMap.put(editorTask, engineCompositeTask);
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

        if (!atomicEditorTask.getDecomposition().requiresResourcingDecisions()) {
            return;
        }

        // Below, we should have specified a resource mapping, but it looks as if
        // we haven't. We supply a default at this point.

        if (atomicEditorTask.getResourceMapping() == null) {
            atomicEditorTask.setResourceMapping(
                    new ResourceMapping()
            );
        }



        engineTask.getDecompositionPrototype().setCodelet(
                atomicEditorTask.getDecomposition().getCodelet()
        );


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
                    ((YAWLTask) editorResourceMapping.getRetainFamiliarTask()).getID()
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
                    ((YAWLTask) editorResourceMapping.getSeparationOfDutiesTask()).getID()
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

//    private static void setCancellationSetDetail(NetElementSummary editorNetSummary) {
//        for (YAWLTask editorTriggerTask : editorNetSummary.getTasksWithCancellationSets()) {
//           List<YExternalNetElement> engineCancellationSet = new ArrayList<YExternalNetElement>();
//            for (YAWLCell element : editorTriggerTask.getCancellationSet().getSetMembers()) {
//                if (element instanceof YAWLFlowRelation) {
//                    engineCancellationSet.add(getConditionForFlow((YAWLFlowRelation) element));
//                }
//                else {
//                    engineCancellationSet.add((YExternalNetElement)
//                            editorToEngineElementMap.get(element)
//                    );
//                }
//            }
//
//            YTask engineTriggerTask = (YTask) editorToEngineElementMap.get(editorTriggerTask);
//            engineTriggerTask.addRemovesTokensFrom(engineCancellationSet);
//        }
//    }


    private static void addFlowConditionMapping(YAWLFlowRelation editorFlow,
                                                YCondition engineCondition) {
        editorFlowEngineConditionMap.put(editorFlow, engineCondition);
    }

    private static YCondition getConditionForFlow(YAWLFlowRelation editorFlow) {
        return editorFlowEngineConditionMap.get(editorFlow);
    }


}
