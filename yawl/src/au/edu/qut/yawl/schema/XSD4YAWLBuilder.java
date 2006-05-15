/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.schema;

import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.*;
import org.eclipse.xsd.impl.XSDModelGroupImpl;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDPrototypicalSchema;
import org.eclipse.xsd.util.XSDSchemaBuildingTools;
import org.w3c.dom.Element;

import java.util.*;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 6/08/2004
 * Time: 13:23:23
 * 
 *
 * This class provide several useful helper methods over the XSD library that allow
 * for the creation of new schemas consisting of parts of the schema used to create this one.
 *
 */
public class XSD4YAWLBuilder {
    private XSDSchema _schema;
    private XSDFactory _xsdFactory;


    public XSD4YAWLBuilder() {
        _xsdFactory = XSDSchemaBuildingTools.getXSDFactory();
        _schema = XSDUtil.createBlankSchema();
    }

    /**
     * Sets up an XSD4YAWLBuilder Object
     * @param schemaElemement the w3c Element that is the root of an XML schema.
     * i.e. this element is the one written like so: "<xs:schema ..> ... </xs:schema>"
     */
    public void setSchema(Element schemaElemement) throws YSyntaxException {
        XSDPrototypicalSchema proto = XSDPrototypicalSchema.getInstance();
        _schema = proto.createSchema(schemaElemement);

        if (_schema.getTargetNamespace() != null) {
            throw new YSyntaxException("YAWL does not support schemas with target namespaces.");
        }
    }


    /**
     * Returns a Set of the global type names as strings.
     * @return global type names Set.
     */
    public Set getGlobalTypeNames() {
        if (_schema == null) {
            return null;
        }
        Set returns = new HashSet();
        EList list = _schema.getTypeDefinitions();
        for (int i = 0; i < list.size(); i++) {
            XSDTypeDefinition definition = (XSDTypeDefinition) list.get(i);
            returns.add(definition.getName());
        }
        return returns;
    }


    /**
     * Returns a Set of global element names.
     * @return global element names Set.
     */
    public Set getGlobalElementNames() {
        if (_schema == null) {
            return null;
        }
        Set returns = new HashSet();
        EList list = _schema.getElementDeclarations();
        for (int i = 0; i < list.size(); i++) {
            XSDElementDeclaration element = (XSDElementDeclaration) list.get(i);
            returns.add(element.getName());
        }
        return returns;
    }


    /**
     * Gets a self containd element definition off a schema.  i.e. the method traces
     * down all references and collects them together.
     * @param elementName the name of the type
     * @return
     */
    public String getSelfContainedSchemaElementDeclaration(String elementName) {
        return "todo";
    }


    /**
     * Gets a self containd type definition off a schema.  i.e. the method traces
     * down all references and collects them together.
     * @param typeName the name of the type
     * @return
     */
    public String getSelfContainedSchemaTypeDeclaration(String typeName) {
        return "todo";
    }


    /**
     * Returns what XSD call a free standing deep copy of the concrete type.  This is not really
     * that deep though - it doesn't resolve references for example.
     * @param typeName
     * @return
     */
    protected XSDTypeDefinition getTypeDefinition(String typeName) {
        if (_schema != null) {
            EList list = _schema.getTypeDefinitions();
            for (int i = 0; i < list.size(); i++) {
                XSDTypeDefinition typeDeclaration = (XSDTypeDefinition) list.get(i);
                if (typeDeclaration.getName().equals(typeName)) {
                    return typeDeclaration;
                }
            }
        }
        return null;
    }


    /**
     * Helper method: Returns the element by name.
     * @param elementName
     * @return
     */
    protected XSDElementDeclaration getElementDefinition(String elementName) {
        if (_schema != null) {
            EList list = _schema.getElementDeclarations();
            for (int i = 0; i < list.size(); i++) {
                XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) list.get(i);
                if (elementDeclaration.getName().equals(elementName)) {
                    return elementDeclaration;
                }
            }
        }
        return null;
    }


    /**
     * returns the type definition in the schema.  Do not try to copy this somewhere else as
     * it would cause data loss in the original schema object.
     * @param typeName
     * @return
     */
    protected XSDTypeDefinition getOriginalTypeDefinition(String typeName) {
        if (_schema != null) {
            EList list = _schema.getTypeDefinitions();
            for (int i = 0; i < list.size(); i++) {
                XSDTypeDefinition typeDeclaration = (XSDTypeDefinition) list.get(i);
                if (typeDeclaration.getName().equals(typeName)) {
                    return typeDeclaration;
                }
            }
        }
        return null;
    }


    /**
     * Returns a set of element definitions.
     * @param globalDefinitionName the name of the definition (must be a global element name).
     * @return if "globalDefinitionName" exists in the schema return the type for "globalDefinitionName" and
     * adds to the set the types that "globalDefinitionName" depends upon.
     */
    protected Set getGlobalDefinitionsForElement(String globalDefinitionName) {
        Set set = new HashSet();
        XSDElementDeclaration elementDecl = getElementDefinition(globalDefinitionName);
        if (elementDecl != null) {
            set.addAll(getGlobalDependencies(elementDecl));
        }
        Set results = uniqueifyDefinitions(set);
        return results;
    }


    /**
     * Ensures that there are no repeated elements or types in the definitions.
     * @param definitions
     * @return the set uniqueified
     */
    public Set uniqueifyDefinitions(Set definitions) {
        Map elements = new HashMap();
        Map types = new HashMap();
        Map attributes = new HashMap();
        for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
            XSDComponent xsdComponent = (XSDComponent) iterator.next();
            if (xsdComponent instanceof XSDElementDeclaration) {
                XSDElementDeclaration elementDec = (XSDElementDeclaration) xsdComponent;
                elements.put(elementDec.getName(), elementDec);
            } else if (xsdComponent instanceof XSDTypeDefinition) {
                XSDTypeDefinition typeDef = (XSDTypeDefinition) xsdComponent;
                types.put(typeDef.getName(), typeDef);
            } else if (xsdComponent instanceof XSDAttributeDeclaration) {
                XSDAttributeDeclaration attribute = (XSDAttributeDeclaration) xsdComponent;
                attributes.put(attribute.getName(), attribute);
            } else {
                throw new RuntimeException(XSDUtil.convertToString(xsdComponent));
            }
        }
        Set set = new HashSet();
        set.addAll(elements.values());
        set.addAll(types.values());
        set.addAll(attributes.values());
        return set;
    }


    /**
     * Returns a set of cloned type definitions.
     * @param globalDefinitionName the name of the definition (must be a global type name).
     * @return if "globalDefinitionName" exists in the schema return the type for "globalDefinitionName" and
     * adds to the set the types that "globalDefinitionName" depends upon.
     */
    protected Set getGlobalDefinitionsForType(String globalDefinitionName) {
        Set set = new HashSet();
        XSDTypeDefinition typeDef = getTypeDefinition(globalDefinitionName);
        if (typeDef != null) {
            set.addAll(getGlobalDependencies(typeDef));
        }
        Set results = uniqueifyDefinitions(set);
        return results;
    }


    /**
     * Takes a set of originalComponents (either element decl or type defs) and rebuilds them
     * as self contained Components (cloned).   This means if an element is referenced or a type
     * is referenced the reference is replaced by a copy of the pointed to component.
     * @param originalComponents
     * @return the copied component
     */
    protected Set rebuildComponentsAsSelfContainedCopies(Set originalComponents) {
        Set returns = new HashSet();
        for (Iterator iterator = originalComponents.iterator(); iterator.hasNext();) {
            XSDConcreteComponent component = (XSDConcreteComponent) iterator.next();
            if (component instanceof XSDTypeDefinition) {
                XSDTypeDefinition typeDef = (XSDTypeDefinition) component;
                returns.add(buildSelfContainedCopy(typeDef));
            } else if (component instanceof XSDElementDeclaration) {
                XSDElementDeclaration origElemDecl = (XSDElementDeclaration) component;
                returns.add(buildSelfContainedCopy(origElemDecl));
            } else if (component instanceof XSDAttributeDeclaration) {
                XSDAttributeDeclaration origAttDecl = (XSDAttributeDeclaration) component;
                returns.add(buildSelfContainedCopy(origAttDecl));
            } else {
                throw new RuntimeException("components should only be types, elements, or attributes");
            }

        }
        return returns;
    }


    private XSDElementDeclaration buildSelfContainedCopy(XSDElementDeclaration origElemDecl) {
        if (origElemDecl.isElementDeclarationReference()) {
            return buildSelfContainedCopy(origElemDecl.getResolvedElementDeclaration());
        } else {
            XSDElementDeclaration clonedElem = _xsdFactory.createXSDElementDeclaration();
            clonedElem.setName(origElemDecl.getName());

            XSDTypeDefinition oriTypeDef = origElemDecl.getTypeDefinition();
            if (isSchema4SchemaType(oriTypeDef)) {
                clonedElem.setTypeDefinition(oriTypeDef);
            } else {
                XSDTypeDefinition clonedTypeDef =
                        buildSelfContainedCopy(oriTypeDef);
                clonedTypeDef.setName(null);
                clonedElem.setAnonymousTypeDefinition(clonedTypeDef);
            }
            return clonedElem;
        }
    }


    private XSDTypeDefinition buildSelfContainedCopy(XSDTypeDefinition origTypeDef) {
        if (origTypeDef instanceof XSDComplexTypeDefinition) {
            return buildSelfContainedCopy((XSDComplexTypeDefinition) origTypeDef);
        } else if (origTypeDef instanceof XSDSimpleTypeDefinition) {
            return buildSelfContainedCopy((XSDSimpleTypeDefinition) origTypeDef);
        }
        return null;
    }


    private XSDSimpleTypeDefinition buildSelfContainedCopy(XSDSimpleTypeDefinition origSimpleType) {
        if (isSchema4SchemaType(origSimpleType)) {
            return origSimpleType;
        } else {
            XSDSimpleTypeDefinition clonedSimpleType =
                    (XSDSimpleTypeDefinition) origSimpleType.
                    cloneConcreteComponent(true, false);
            return clonedSimpleType;
        }
    }


    private XSDComplexTypeDefinition buildSelfContainedCopy(XSDComplexTypeDefinition origCompTypeDef) {
        XSDComplexTypeDefinition clonedTypeDef = (XSDComplexTypeDefinition)
                origCompTypeDef.cloneConcreteComponent(false, false);
        XSDParticle clonedContent = buildSelfContainedCopy((XSDParticle) origCompTypeDef.getContent());
        clonedTypeDef.setContent(clonedContent);

        EList originAttrList = origCompTypeDef.getAttributeContents();
        for (int i = 0; i < originAttrList.size(); i++) {
            XSDAttributeUse origAttUse = (XSDAttributeUse) originAttrList.get(i);
            XSDAttributeUse clonedAttUse = buildSelfContainedCopy(origAttUse);
            clonedTypeDef.getAttributeContents().add(clonedAttUse);
        }
        return clonedTypeDef;
    }


    private XSDAttributeUse buildSelfContainedCopy(XSDAttributeUse origAttrUse) {
        XSDAttributeUse clonedAttUse = (XSDAttributeUse)
                origAttrUse.cloneConcreteComponent(false, false);
        XSDAttributeDeclaration origAttDec = origAttrUse.getContent();
        XSDAttributeDeclaration clonedAttDec = buildSelfContainedCopy(origAttDec);

        clonedAttUse.setContent(clonedAttDec);
        return clonedAttUse;
    }


    private XSDAttributeDeclaration buildSelfContainedCopy(XSDAttributeDeclaration origAttribute) {
        if (origAttribute.isAttributeDeclarationReference()) {
            return buildSelfContainedCopy(origAttribute.getResolvedAttributeDeclaration());
        } else {
            XSDAttributeDeclaration clonedAttrbt = (XSDAttributeDeclaration)
                    origAttribute.cloneConcreteComponent(true, false);
            XSDSimpleTypeDefinition attType = origAttribute.getTypeDefinition();
            if (isSchema4SchemaType(attType)) {
                clonedAttrbt.setTypeDefinition(attType);
            } else {
                XSDSimpleTypeDefinition clonedAttType = buildSelfContainedCopy(attType);
                clonedAttType.setName(null);
                clonedAttrbt.setAnonymousTypeDefinition(clonedAttType);
            }
            return clonedAttrbt;
        }
    }


    private XSDParticle buildSelfContainedCopy(XSDParticle originalParticle) {
        EList contents = originalParticle.eContents();
        XSDParticle clonedParticle = _xsdFactory.createXSDParticle();
        for (int i = 0; i < contents.size(); i++) {
            XSDParticleContent origParticleContent = (XSDParticleContent) contents.get(i);
            if (origParticleContent instanceof XSDModelGroup) {
                XSDModelGroupImpl origModelGroup = (XSDModelGroupImpl) origParticleContent;
                clonedParticle.setContent(buildSelfContainedCopy(origModelGroup));
            } else if (origParticleContent instanceof XSDElementDeclaration) {
                XSDElementDeclaration origElementDecl = (XSDElementDeclaration) origParticleContent;
                XSDElementDeclaration clonedElemDec = buildSelfContainedCopy(origElementDecl);
                if (originalParticle.getMinOccurs() != 1) {
                    clonedParticle.setMinOccurs(originalParticle.getMinOccurs());
                }
                if (originalParticle.getMaxOccurs() != 1) {
                    clonedParticle.setMaxOccurs(originalParticle.getMaxOccurs());
                }
                clonedParticle.setContent(clonedElemDec);
            }
        }
        return clonedParticle;
    }


    private XSDModelGroup buildSelfContainedCopy(XSDModelGroup origModelGroup) {
        XSDModelGroup clonedModelGroup = (XSDModelGroup) origModelGroup.cloneConcreteComponent(false, false);
        XSDCompositor compositor = origModelGroup.getCompositor();
        String compositorName = compositor.getName();
        if (compositorName.equals("sequence")
                || compositorName.equals("choice")
                || compositorName.equals("group")) {
            EList elements = origModelGroup.getContents();
            for (int i = 0; i < elements.size(); i++) {
                XSDParticle particle = (XSDParticle) elements.get(i);
                clonedModelGroup.getContents().add(buildSelfContainedCopy(particle));
            }
        }
        return clonedModelGroup;
    }


    /**
     * Returns whether or the lement declararation is global.
     * @param elementDecl the element declaration.
     * @return ...
     */
    protected boolean isGlobalElementDeclaration(XSDElementDeclaration elementDecl) {
        String elementName = elementDecl.getQName();
        Set elementNames = getGlobalElementNames();
        for (Iterator iterator = elementNames.iterator(); iterator.hasNext();) {
            String nextTypeName = (String) iterator.next();
            if (nextTypeName.equals(elementName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the global complex types and element declarations needed for self containment.
     * @param typeDef the complex type potentially referring to global definitions.
     * @return global definitions needed for "typeDef"
     */
    protected Collection getGlobalDependencies(XSDTypeDefinition typeDef) {
        Set set = new HashSet();
        if (isGlobalTypeDefinition(typeDef)) {
            set.add(typeDef);
        }
        if (typeDef instanceof XSDComplexTypeDefinition) {
            XSDComplexTypeDefinition complexType = (XSDComplexTypeDefinition) typeDef;
            XSDParticle particle = (XSDParticle) complexType.getContent();
            set.addAll(getGlobalDependencies(particle));

            EList attributes = complexType.getAttributeContents();
            for (int i = 0; i < attributes.size(); i++) {
                XSDAttributeUse attribute = (XSDAttributeUse) attributes.get(i);
                XSDAttributeDeclaration attDecl = attribute.getAttributeDeclaration();
                set.addAll(getGlobalDependencies(attDecl));
            }
        } else if (typeDef instanceof XSDSimpleTypeDefinition) {
            XSDSimpleTypeDefinition simpleType = (XSDSimpleTypeDefinition) typeDef;
            if (isGlobalTypeDefinition(simpleType)) {
                set.add(simpleType);
            }
        }
        return set;
    }


    /**
     * Returns whether or the type definition is global or not.
     * @param typeDef the type def
     * @return whether it is a global definition.
     */
    protected boolean isGlobalTypeDefinition(XSDTypeDefinition typeDef) {
        String typeName = typeDef.getQName();
        String schema4SchemaNS = _schema.getSchemaForSchemaNamespace();
        boolean isSchemaForSchemaType = typeDef.hasNameAndTargetNamespace(typeName, schema4SchemaNS);
        if (isSchemaForSchemaType) {
            return false;
        }
        Set typeNames = getGlobalTypeNames();
        for (Iterator iterator = typeNames.iterator(); iterator.hasNext();) {
            String nextTypeName = (String) iterator.next();
            if (nextTypeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Given an XSDParticle return global definitions needed.
     * @param particle the particle
     * @return a set of global definitions that this particle relies upon.
     */
    protected Collection getGlobalDependencies(XSDParticle particle) {
        Set set = new HashSet();
        EList contents = particle.eContents();
        for (int i = 0; i < contents.size(); i++) {
            XSDParticleContent particleContent = (XSDParticleContent) contents.get(i);
            if (particleContent instanceof XSDModelGroup) {
                XSDModelGroupImpl modelGroup = (XSDModelGroupImpl) particleContent;
                set.addAll(getGlobalDependencies(modelGroup));
            } else if (particleContent instanceof XSDElementDeclaration) {
                XSDElementDeclaration elementDecl = (XSDElementDeclaration) particleContent;
                XSDTypeDefinition tDef = elementDecl.getTypeDefinition();
                if (tDef == null) {
                    XSDElementDeclaration emptyElem = elementDecl.getResolvedElementDeclaration();
                    XSDElementDeclaration refElem = getElementDefinition(emptyElem.getName());
                    set.addAll(getGlobalDependencies(refElem));
                    if (isGlobalElementDeclaration(refElem)) {
                        set.add(refElem);
                    }
                } else {
                    set.addAll(getGlobalDependencies(elementDecl));
                }
            }
        }
        return set;
    }


    /**
     * Given an XSDElementDeclaration return global definitions needed.
     * @param elem the element declaration in focus.
     * @return a set of the global definitions that this element needs.
     */
    protected Collection getGlobalDependencies(XSDElementDeclaration elem) {
        Set set = new HashSet();
        if (isGlobalElementDeclaration(elem)) {
            set.add(elem);
        }
        XSDTypeDefinition typeDef = elem.getTypeDefinition();

        set.addAll(getGlobalDependencies(typeDef));
        return set;
    }


    /**
     * Gets the global definitions for a given model group.
     * @param modelGroup the model group.
     * @return a set of global definitions it depends upon.
     */
    protected Collection getGlobalDependencies(XSDModelGroupImpl modelGroup) {
        Set set = new HashSet();
        XSDCompositor compositor = modelGroup.getCompositor();
        String compositorName = compositor.getName();
        if (compositorName.equals("sequence")
                || compositorName.equals("choice")
                || compositorName.equals("group")) {
            EList elements = modelGroup.getContents();
            for (int i = 0; i < elements.size(); i++) {
                XSDParticle particle = (XSDParticle) elements.get(i);
                set.addAll(getGlobalDependencies(particle));
            }
        }
        return set;
    }


    /**
     * Gets the global definitions needed for this complex type's attributes to resolve properly.
     * @param attribute the complex type with the attributes
     * @return a set of the definitions that this type needs for its attributes to resolve.
     */
    protected Collection getGlobalDependencies(XSDAttributeDeclaration attribute) {
        Set results = new HashSet();
        XSDSimpleTypeDefinition attType = attribute.getTypeDefinition();
        if (attribute.isAttributeDeclarationReference()) {
            results.addAll(getGlobalDependencies(attribute.getResolvedAttributeDeclaration()));
        } else {
            if (isGlobalAttributeDeclaration(attribute)) {
                results.add(attribute);
            }
        }
        results.addAll(getGlobalDependencies(attType));
        return results;
    }


    /**
     * Helper method to return whether a given atribute is globally defined or not.
     * @param attribute the attribute in question.
     * @return see above.
     */
    protected boolean isGlobalAttributeDeclaration(XSDAttributeDeclaration attribute) {
        EList attributes = _schema.getAttributeDeclarations();
        return attributes.contains(attribute);
    }


    /**
     * Creates an XSD Schema document with a root element called data.  This data element
     * then contains sub-elements that are the union of the desired type names and desired
     * element names.  For each desired type one element is created and for each desired
     * element it is used directly.
     * Note that it is a syntax error to desired element names and type names to overlap.
     * @param desiredNamesToTypes a 2 dimensional array of strings to strings all in QName format.
     * @param definitions a set of XSDConcreteComponents
     * (i.e. XSDTypeDefinitions and XSDElementDEclarations)
     * @return a schema built accordingly
     */
    protected XSDSchema createYAWLSchema
            (String[][] desiredNamesToTypes, Set definitions, String rootElementName)
            throws YSchemaBuildingException {
        //first check the arguments
        checkArguments(desiredNamesToTypes);

        List theElements = createElements(desiredNamesToTypes, definitions);
        XSDSchema schema = XSDUtil.createBlankSchema();
        schema.setElementFormDefault(XSDForm.QUALIFIED_LITERAL);
        XSDElementDeclaration data = _xsdFactory.createXSDElementDeclaration();
        data.setName(rootElementName);

        // Create an anonymous complex type.
        //
        XSDComplexTypeDefinition datasComplexTypeDefinition = _xsdFactory.createXSDComplexTypeDefinition();
        XSDParticle datasOuterParticle = _xsdFactory.createXSDParticle();

        XSDModelGroup datasModelGroupSequence = _xsdFactory.createXSDModelGroup();
        datasModelGroupSequence.setCompositor(XSDCompositor.SEQUENCE_LITERAL);

        for (Iterator iterator = theElements.iterator(); iterator.hasNext();) {
            XSDParticleContent declaration = (XSDParticleContent) iterator.next();
            XSDParticle elementParticle = _xsdFactory.createXSDParticle();
            datasModelGroupSequence.getContents().add(elementParticle);
            elementParticle.setContent(declaration);
        }
        datasOuterParticle.setContent(datasModelGroupSequence);
        datasComplexTypeDefinition.setContent(datasOuterParticle);
        data.setAnonymousTypeDefinition(datasComplexTypeDefinition);
        schema.getContents().add(data);
        //this is a fall back option to avoid having to make each type self-contained
//        addDefinitions(schema, definitions);
        return schema;
    }

    /**
     * Checks the argumenbts for the createYAWLSchema() method.
     * @param desiredNamesToTypes [name of element] --> {type of element](optional)
     */
    private void checkArguments(String[][] desiredNamesToTypes) {
        for (int i = 0; i < desiredNamesToTypes.length; i++) {
            String[] nameToType = desiredNamesToTypes[i];
            if (nameToType.length < 2 || nameToType.length > 3) {
                throw new IllegalArgumentException("Problem: expecting nameToType[] to be of " +
                        "length 2-3. Not[" + nameToType.length + "]");
            }
            String elementName = nameToType[0];
            if (!qName(elementName) && !elementName.equals("xs:any")) {
                throw new IllegalArgumentException("Problem elementname: " + elementName +
                        " is not a QName");
            }
            String typeName = nameToType[1];
            if (typeName != null && !qName(typeName)) {
                throw new IllegalArgumentException("Problem typename: " + typeName +
                        " is not a QName");
            }
        }
    }


    private void addDefinitions(XSDSchema schema, Set definitions) {
        if (null != definitions) {
            for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
                XSDConcreteComponent comp = (XSDConcreteComponent) iterator.next();
                XSDConcreteComponent ccomp = comp.cloneConcreteComponent(true, false);
                schema.getContents().add(ccomp);
            }
        }
    }


    /**
     * Creates a map of element definitions.
     * @param elementNamesToTypeNames domain = the element name; range = (can be null) the type name.
     * if type name is null then the element name must be found (as an XSDElement) in set definitions;
     * else if type is not null then type must be found (as an XSDTypeDef) in definitions.
     * @param definitions a set od XSDDefinitions to use.
     * @return a nice 2-d array of element names (strings) to XSDElementDeclarations.
     * @throws YSchemaBuildingException
     */
    private List createElements(String[][] elementNamesToTypeNames, Set definitions)
            throws YSchemaBuildingException {
        List elements = new ArrayList();

        for (int i = 0; i < elementNamesToTypeNames.length; i++) {
            String[] elemNameToTypeName = elementNamesToTypeNames[i];

            String elementName = elemNameToTypeName[0];
            String typeName = elemNameToTypeName[1];
            boolean isSchema4Schema = isSchemaForSchemaComponent(elemNameToTypeName);
            if (null == typeName) {
                //the assumption is that an XS:any decl will have the following format:
                //String[]{"any",null,"http://www.w3.org/2001/XMLSchema"}
                if (elementName.equals("xs:any") && isSchema4Schema) {
                    XSDWildcard any = _xsdFactory.createXSDWildcard();
                    any.setProcessContents(XSDProcessContents.LAX_LITERAL);
                    elements.add(any);
                } else {
                    //there MUST be an element in the definitions with this name.
                    XSDElementDeclaration elementDecl =
                            getXSDElementDeclaration(elementName, definitions);
                    if (null == elementDecl) {
                        throw new YSchemaBuildingException(
                                "Expected a corresponding element definition for " +
                                elementName);
                    } else if (isAnElementReference(elementDecl)) {
                        throw new YSchemaBuildingException(
                                "elementDecl = " + XSDUtil.convertToString(elementDecl));
                    } else {
                        elements.add(elementDecl);
                    }
                }
            } else {
                //there exists an element name and a type name therefore we must search the
                //definitions list for a type definition.  And then build an element from the type.
                XSDTypeDefinition typeDef =
                        getXSDTypeDefinition(typeName, definitions, isSchema4Schema);
                if (null == typeDef) {
                    throw new YSchemaBuildingException(
                            "Expected a corresponding type definition for " + typeName);
                }
                XSDElementDeclaration element = _xsdFactory.createXSDElementDeclaration();
                element.setName(elementName);

                if (isSchema4SchemaType(typeDef)) {
                    element.setTypeDefinition(typeDef);
                } else {
                    XSDTypeDefinition clonedDef =
                            buildSelfContainedCopy(typeDef);
                    clonedDef.setName(null);
                    element.setAnonymousTypeDefinition(clonedDef);
                }
                elements.add(element);
            }
        }
        return elements;
    }


    private boolean isSchemaForSchemaComponent(String[] elemNameToTypeName) {
        return elemNameToTypeName.length == 3 ?
                XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(elemNameToTypeName[2])
                :
                false;
    }


    protected boolean isSchema4SchemaType(XSDTypeDefinition typeDefinition) {
        XSDSchema schema = _schema.getSchemaForSchema();
        return schema.equals(typeDefinition.getSchema());
    }


    /**
     * Returns if the element is an element proper or an element reference.
     * @param elementDecl
     * @return
     */
    protected boolean isAnElementReference(XSDElementDeclaration elementDecl) {
        return elementDecl.isElementDeclarationReference();
    }


    /**
     *
     * @param typeName the name of the type
     * @param definitions a list of XSDElementDeclarations and XSDTypeDefinitions
     * @return XSDTypeDefinition with getName() == typeName
     * @throws YSchemaBuildingException
     */
    private XSDTypeDefinition getXSDTypeDefinition(String typeName, Set definitions, boolean isSchemaType)
            throws YSchemaBuildingException {
        if (null != definitions) {
            try {
                for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
                    XSDConcreteComponent component = (XSDConcreteComponent) iterator.next();
                    if (component instanceof XSDTypeDefinition) {
                        XSDTypeDefinition typeDef = (XSDTypeDefinition) component;
                        String name = typeDef.getName();
                        if (name.equals(typeName)) {
                            return typeDef;
                        }
                    }
                }
            } catch (ClassCastException e) {
                throw new YSchemaBuildingException(
                        "Input param definitions contained an unexpected class: " + definitions);
            }
        }
        if (typeName.startsWith("xs") || isSchemaType) {
            int begin = typeName.indexOf(':');
            String primitiveTypeName = null;
            if (begin > 1) {
                primitiveTypeName = typeName.substring(begin + 1, typeName.length());
            } else {
                primitiveTypeName = typeName;
            }
            return getSchemaForSchemaTypeDef(primitiveTypeName);
        }
        return null;
    }


    /**
     * Gets the Schema for schema type def with name
     * @param name
     * @return
     */
    public XSDSimpleTypeDefinition getSchemaForSchemaTypeDef(String name) {
        XSDSchema schem4Schema = _schema.getSchemaForSchema();

        return schem4Schema.resolveSimpleTypeDefinition(name);
    }


    /**
     *
     * @param elementName the element name
     * @param definitions a list of XSDElementDeclarations and XSDTypeDefinitions
     * @return the XSDElementDeclaration
     */
    private XSDElementDeclaration getXSDElementDeclaration(String elementName, Set definitions)
            throws YSchemaBuildingException {
        try {
            for (Iterator iterator = definitions.iterator(); iterator.hasNext();) {
                XSDConcreteComponent component = (XSDConcreteComponent) iterator.next();
                if (component instanceof XSDElementDeclaration) {
                    XSDElementDeclaration elementDecl = (XSDElementDeclaration) component;
                    String name = elementDecl.getName();
                    if (name.equals(elementName)) {
                        return elementDecl;
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new YSchemaBuildingException(
                    "Input param definitions contained an unexpected class: " + definitions);
        }
        return null;
    }


    /**
     * conforms to QName syntax
     * @param name
     * @return if
     */
    private boolean qName(String name) {
        return name != null && name.matches("[a-z|A-Z|0-9|[-]|[_]]+");
    }


    /**
     * Accessor for the schema input into the builder.  Warning: it would not be wise to
     * modify this as the method returns the original copy.
     * @return the original schema
     */
    protected XSDSchema getOriginalSchema() {
        return _schema;
    }
}