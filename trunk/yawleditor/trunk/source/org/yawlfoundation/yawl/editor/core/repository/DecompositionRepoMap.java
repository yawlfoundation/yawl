package org.yawlfoundation.yawl.editor.core.repository;

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 6/08/13
 */
public abstract class DecompositionRepoMap extends RepoMap {

    protected XNode shellSpecification;
    protected XNodeParser parser;

    protected DecompositionRepoMap() { }

    protected DecompositionRepoMap(String path) {
        super(path);
    }

    /**
     * Adds a decomposition to the repository
     * @param name a reference name for the decomposition
     * @param description a description of it
     * @param decomposition the decomposition to add
     * @return the id of the added decomposition (may be 'uniquified')
     */
    protected String add(String name, String description, YDecomposition decomposition) {
        if (! anyAreNull(name, description, decomposition)) {
            RepoRecord record = addRecord(
                    new RepoRecord(name, description, toXML(decomposition)));
            if (record != null) return record.getName();
        }
        return null;
    }


    /**
     * Gets a decomposition from the repository
     * @param name a reference name for the decomposition
     * @return the referenced decomposition, or null if not found
     */
    public YDecomposition get(String name) throws YSyntaxException {
        RepoRecord record = getRecord(name);
        return record != null ? parse(record.getValue()) : null;
    }


    /**
     * Removes a decomposition from the repository
     * @param name a reference name for the decomposition
     * @return whether the removal was successful
     */
    public YDecomposition remove(String name) {
        RepoRecord record = removeRecord(name);
        if (record != null) {
            try {
                return parse(record.getValue());
            }
            catch (YSyntaxException yse) {
                // fall through to null
            }
        }
        return null;
    }


    /**
     * Gets a sorted list of descriptors for all stored decompositions
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getDescriptors() {
        return super.getDescriptors();
    }


    protected abstract String toXML(YDecomposition decomposition);


    protected abstract void addXsiAttribute(XNode decompositionNode);


    /**
     * Creates a decomposition from its XML description
     * @param xml the XML to parse
     * @return the populated task decomposition
     */
    protected YDecomposition parse(String xml) throws YSyntaxException {
        if (xml == null) return null;
        if (parser == null) parser = new XNodeParser();
        XNode decompositionNode = parser.parse(xml);
        return decompositionNode == null ? null :
                unmarshalDecomposition(decompositionNode);
    }


    protected XNode createShellSpecification() {
        try {
            YSpecification s = new YSpecification("repoShell");
            s.setMetaData(new YMetaData());
            s.setSchema("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>");
            YNet root = new YNet("rootNet", s);
            root.setInputCondition(new YInputCondition("in", root));
            root.setOutputCondition(new YOutputCondition("out", root));
            s.setRootNet(root);

            String repoShellXML = YMarshal.marshal(s);
            return new XNodeParser().parse(repoShellXML);
        }
        catch (YSyntaxException yse) {
            return null;
        }
    }


    protected YDecomposition unmarshalDecomposition(XNode decompositionNode)
            throws YSyntaxException {
        if (shellSpecification == null) shellSpecification = createShellSpecification();
        XNode specNode = shellSpecification.getChild("specification");
        addXsiAttribute(decompositionNode);   // for task or net decomposition
        specNode.addChild(decompositionNode);
        YSpecification specification = YMarshal.unmarshalSpecifications(
                shellSpecification.toString(true), false).get(0);
        specNode.removeChild(decompositionNode);
        for (YDecomposition decomposition : specification.getDecompositions()) {
            if (! decomposition.getID().equals("rootNet")) {
                return decomposition;
            }
        }
        return null;
    }


    protected Set<String> getValues(Set<String> nameSet) {
        if (nameSet == null) return Collections.emptySet();

        Set<String> valueSet = new HashSet<String>();
        for (String name : nameSet) {
            RepoRecord record = getRecord(name);
            if (record != null) {
                valueSet.add(record.getValue());
            }
        }
        return valueSet;
    }

}
