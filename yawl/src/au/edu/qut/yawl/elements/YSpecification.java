/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Where;

import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.unmarshal.SchemaForSchemaValidator;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * Objects of this type are a specification of a Workflow checkSchema model in YAWL.
 * @author Lachlan Aldred
 * 
 * 
 * ***************************************************************************************
 * 
 * objects of this class are the outer container of a process model.   Such an object is a 
 * process specification/model.   A process specification in YAWL basically contains a set
 * of YNets (process nets).
 * 
 * Set class to non-final for purposes of hibernate
 * 
 */
@Entity
public class YSpecification implements Parented, Cloneable, YVerifiable, Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
	private String _specURI;
    public List<YDecomposition> _decompositions = new ArrayList<YDecomposition>();
    private String _name;
    private String _documentation;
    private String _betaVersion;
    public static final String _Beta2 = "Beta 2";
    public static final String _Beta3 = "Beta 3";
    public static final String _Beta4 = "Beta 4";
    public static final String _Beta6 = "Beta 6";
    public static final String _Beta7_1 = "Beta 7.1"; 
    private transient XMLToolsForYAWL _xmlToolsForYAWL = new XMLToolsForYAWL();
    public static final String _loaded = "loaded";
    public static final String _unloaded = "unloaded";
    private YMetaData _metaData = new YMetaData();
    private Integer _version;
    private Integer _optimisticLockVersion;
    private Long _dbid;

    @Transient
    public Object getParent() {return null;}
    @Transient
    public void setParent(Object something) {}
    
    @Column(name="version")
    public Integer getVersion() {
    	return _version;
    }
    
    public void setVersion(Integer version) {
    	_version = version;
    }
    
    @Version
    @Column(name="optimistic_lock_version")
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private Integer getOptimisticLockVersion() {
    	return _optimisticLockVersion;
    }
    
    @SuppressWarnings({"UNUSED_SYMBOL"})
    public void setOptimisticLockVersion(Integer version) {
    	_optimisticLockVersion = version;
    }

    /**
     * Null constructor inserted for hibernate
     *
     */
    public YSpecification() {
    	_specURI = null;
        _xmlToolsForYAWL = new XMLToolsForYAWL();
    }

    public YSpecification(String specURI) {
        _specURI = specURI;
        _xmlToolsForYAWL = new XMLToolsForYAWL();
        
    }
    
    @OneToMany(mappedBy="parent",cascade = {CascadeType.ALL}, fetch= FetchType.EAGER)
    //@OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="clone='false'")
    public void setDecompositions(List<YDecomposition> set) {
    		this._decompositions = set;    	
	}

    @OneToMany(mappedBy="parent",cascade = {CascadeType.ALL}, fetch= FetchType.EAGER)
    //@OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="clone='false'")
    public List<YDecomposition> getDecompositions() {
    	return _decompositions;
    }
    
    @Transient
    public YNet getRootNet() {
        YNet retval = null;
        for (YDecomposition net: getDecompositions())
        	if (net instanceof YNet 
        			&& ((YNet) net).getRootNet().equalsIgnoreCase("true")) {
        		retval = (YNet) net;
        		break;
        	}
    	return retval;
    }


    @Transient
    public void setRootNet(YNet rootNet) {
        if( getRootNet() != null ) {
            getRootNet().setRootNet("false");
        }
        rootNet.setRootNet("true");
    	if (getRootNet() == null) {
    		getDecompositions().add(rootNet);
    	}
    }


    /**
     * Gets the version number of this specification.
     * @return the version of the engine that this specification works for.
     */
    @Column(name="specification_version")
    public String getBetaVersion() {
        return _betaVersion;
    }

    /**
     * Sets the version number of the specification.
     * @param version
     */
    public void setBetaVersion(String version) {
        if (version!=null) {
    	
    	if (_Beta2.equals(version) ||
                _Beta3.equals(version) ||
                _Beta4.equals(version) ||
                _Beta6.equals(version) ||
                _Beta7_1.equals(version)) {
            this._betaVersion = version;
        } else if ("beta3".equals(version)) {
            this._betaVersion = _Beta3;
        } else {
            throw new IllegalArgumentException("Param version [" +
                    version + "] is not allowed.");
        }
        }
    }

    @Transient
    public boolean usesSimpleRootData() {
        return YSpecification._Beta2.equals(_betaVersion) ||
                YSpecification._Beta3.equals(_betaVersion);
    }

    @Transient
    public boolean isSchemaValidating() {
        return !YSpecification._Beta2.equals(_betaVersion);
    }

    /**
     * Used by hibernate annotations only!
     * 
     * @return the schema XML string
     * The string may be very long
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    @Lob
    private String getSchema() {
    	return _xmlToolsForYAWL.getSchemaString();
    }
    /**
     * Allows users to add schemas.
     * @param schemaString
     * @throws YSchemaBuildingException
     */
    public void setSchema(String schemaString) throws YSchemaBuildingException, YSyntaxException {
        _xmlToolsForYAWL.setPrimarySchema(schemaString);
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<specification uri=\"");
        xml.append(_specURI);
        xml.append("\">");
        if (_name != null) {
            xml.append("<name>").
                    append(_name).
                    append("</name>");
        }
        if (_documentation != null) {
            xml.append("<documentation>").
                    append(_documentation).
                    append("</documentation>");
        }
        xml.append(_metaData.toXML());
        xml.append(_xmlToolsForYAWL.getSchemaString());
        if( getRootNet() != null ) {
            xml.append("<decomposition id=\"").
                    append(getRootNet().getId()).
                    append("\" xsi:type=\"NetFactsType\" ").
                    append(format(getRootNet().getAttributes())).
                    append(">");
            xml.append(getRootNet().toXML());
            xml.append("</decomposition>");
        }
        for (YDecomposition decomposition : _decompositions) {
            if (!decomposition.getId().equals(getRootNet().getId())) {
                xml.append("<decomposition id=\"").
                        append(decomposition.getId()).
                        append("\"");
                xml.append(" xsi:type=\"");
                if (decomposition instanceof YNet) {
                    xml.append("NetFactsType");
                } else {
                    xml.append("WebServiceGatewayFactsType");
                }
                xml.append("\" ");

                //AJH: Add in any additional attributes
                xml.append(format(decomposition.getAttributes()));
                xml.append(">").append(decomposition.toXML());
                xml.append("</decomposition>");
            }
        }
        xml.append("</specification>");
        return xml.toString();
    }


    /**
     * Formats a hashtable of string1 to string2 mappings, into
     * a an XML atribute param-value format.
     * e.g. style="bold" format="plain" bling="yes" ...
     * @param attributes
     * @return the param-value formatted string
     */
    private String format(Map<String,String> attributes) {
        StringBuffer buf = new StringBuffer();
        Set<String> keys = attributes.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            String value = attributes.get(key);
            buf.
                    append(key).
                    append("=\"").
                    append(YTask.marshal(value)). //if it's escaped XML, handle it
                    append("\"");
            if(iterator.hasNext()) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }


    public String getName() {
        return _name;
    }

    @Basic
    public void setName(String name) {
        this._name = name;
    }

    public String getDocumentation() {
        return _documentation;
    }

    @Column(name="documentation")
    public void setDocumentation(String documentation) {
        _documentation = documentation;
    }

//    private transient Map<String, YDecomposition> convenienceDecompositionMap;
    
    @Transient
    public YDecomposition getDecomposition(String id) {
//    	if (convenienceDecompositionMap == null) {
//    		populateDecompositionMap();
//    	}
//        return convenienceDecompositionMap.get(id);
        for( YDecomposition decomp : _decompositions ) {
            if( decomp.getId().equals( id ) ) {
                return decomp;
            }
        }
        return null;
    }

    public void setDecomposition(YDecomposition decomposition) {
//    	if (convenienceDecompositionMap == null) {
//    		populateDecompositionMap();
//    	}
    	
        if (!_decompositions.contains(decomposition)) {
        	_decompositions.add(decomposition);
        }
//		convenienceDecompositionMap.put(decomposition.getId(), decomposition);
    }
    
//    private void populateDecompositionMap() {
//    	convenienceDecompositionMap = new HashMap<String, YDecomposition>();
//        for (YDecomposition decomposition : _decompositions) {
//            convenienceDecompositionMap.put(decomposition.getId(), decomposition);
//        }
//    }
    


    //#####################################################################################
    //                              VERIFICATION TASKS
    //#####################################################################################
    public List <YVerificationMessage> verify() {
        List<YVerificationMessage> messages =
                new ArrayList<YVerificationMessage> ();
        for (YDecomposition decomposition : _decompositions) {
            messages.addAll(decomposition.verify());
        }
        //check all nets are being used
        //check that decomposition works
        if (getRootNet() != null) {
            messages.addAll(checkDecompositionUsage());
            messages.addAll(checkForInfiniteLoops());
            messages.addAll(checkForEmptyExecutionPaths());
        } else {
            messages.add(
                    new YVerificationMessage(this,
                            "Specifications must have a root net.",
                            YVerificationMessage.ERROR_STATUS)
            );
        }
        messages.addAll(checkDataTypesValidity());
        return messages;
    }


    private List checkDataTypesValidity() {
        List msgs = new ArrayList();
        String schemaString = _xmlToolsForYAWL.getSchemaString();
        String errors = SchemaForSchemaValidator.getInstance().validateSchema(schemaString);
        BufferedReader bReader = new BufferedReader(new StringReader(errors));
        String result;
        try {
            while ((result = bReader.readLine()) != null) {
                if (result.length() > 0) {
                    msgs.add(new YVerificationMessage(
                            this,
                            "DataType problem: " + result,
                            YVerificationMessage.ERROR_STATUS));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgs;
    }


    private List checkForEmptyExecutionPaths() {
        List messages = new ArrayList();
        for (Iterator iterator = _decompositions.iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            if (decomposition instanceof YNet) {
            	List<YExternalNetElement> visited = new ArrayList<YExternalNetElement>();
                visited.add(((YNet) decomposition).getInputCondition());

                List<YExternalNetElement> visiting = getEmptyPostsetAtThisLevel(visited);
                while (visiting.size() > 0) {
                    if (visiting.contains(((YNet) decomposition).getOutputCondition())) {
                        messages.add(new YVerificationMessage(
                                decomposition,
                                "The net (" + decomposition +
                                ") may complete without any generated work.  " +
                                "Check the empty tasks linking from i to o.",
                                YVerificationMessage.WARNING_STATUS));
                    }
                    visiting.removeAll(visited);
                    visited.addAll(visiting);
                    visiting = getEmptyPostsetAtThisLevel(visiting);
                }
            }
        }
        return messages;
    }

    @Transient
    private List<YExternalNetElement> getEmptyPostsetAtThisLevel(List<YExternalNetElement> aSet) {
    	Set<YExternalNetElement> elements = YNet.getPostset(aSet);
        List<YExternalNetElement> resultSet = new ArrayList<YExternalNetElement>();
        for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (element instanceof YTask && ((YTask) element).getDecompositionPrototype() == null ||
                    element instanceof YCondition) {
                resultSet.add(element);
            }
        }
        return resultSet;
    }


    private List <YVerificationMessage> checkForInfiniteLoops() {
        List <YVerificationMessage> messages =
                new ArrayList<YVerificationMessage>();
        //check inifinite loops under rootnet and generate error messages
        Set relevantNets = new HashSet();
        relevantNets.add(getRootNet());
        Set relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, false));
        //check inifinite loops not under rootnet and generate warning messages
        Set netsBeingUsed = new HashSet();
        unfoldNetChildren(getRootNet(), netsBeingUsed, null);
        relevantNets = new HashSet(_decompositions);
        relevantNets.removeAll(netsBeingUsed);
        relevantTasks = selectEmptyAndDecomposedTasks(relevantNets);
        messages.addAll(checkTheseTasksForInfiniteLoops(relevantTasks, true));
        return messages;
    }


    private List <YVerificationMessage> checkTheseTasksForInfiniteLoops(Set relevantTasks, boolean generateWarnings) {
        List<YVerificationMessage> messages =
                new ArrayList<YVerificationMessage>();
        for (Iterator iterator = relevantTasks.iterator(); iterator.hasNext();) {
            YExternalNetElement q = (YExternalNetElement) iterator.next();
            Set visited = new HashSet();
            visited.add(q);

            Set visiting = getEmptyTasksPostset(visited);
            while (visiting.size() > 0) {
                if (visiting.contains(q)) {
                    messages.add(new YVerificationMessage(
                            q,
                            "The element (" + q + ") plays a part in an inifinite " +
                            "loop/recursion in which no work items may be created.",
                            generateWarnings ?
                            YVerificationMessage.WARNING_STATUS :
                            YVerificationMessage.ERROR_STATUS));
                }
                visiting.removeAll(visited);
                visited.addAll(visiting);
                visiting = getEmptyTasksPostset(visiting);
            }
        }
        return messages;
    }


    private Set selectEmptyAndDecomposedTasks(Set relevantNets) {
        Set relevantTasks = new HashSet();
        for (Iterator iterator = relevantNets.iterator(); iterator.hasNext();) {
            YDecomposition decomposition = (YDecomposition) iterator.next();
            relevantTasks.addAll(unfoldNetChildren(decomposition, new HashSet(), "emptyTasks"));
            relevantTasks.addAll(unfoldNetChildren(decomposition, new HashSet(), "decomposedTasks"));
        }
        return relevantTasks;
    }

    @Transient
    private Set getEmptyTasksPostset(Set set) {
        Set resultSet = new HashSet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            YTask task = (YTask) iterator.next();
            if (task.getDecompositionPrototype() instanceof YNet) {
                YInputCondition input = ((YNet) task.getDecompositionPrototype()).getInputCondition();
                List<YExternalNetElement> tasks = input.getPostsetElements();
                for (Iterator iterator2 = tasks.iterator(); iterator2.hasNext();) {
                    YTask task2 = (YTask) iterator2.next();
                    if (task2.getDecompositionPrototype() == null || task2.getDecompositionPrototype() instanceof YNet) {
                        resultSet.add(task2);
                    }
                }
            } else {
            	List<YExternalNetElement> postSet = task.getPostsetElements();
            	Set<YExternalNetElement> taskPostSet = YNet.getPostset(postSet);
                for (Iterator iterator2 = taskPostSet.iterator(); iterator2.hasNext();) {
                    YTask task2 = (YTask) iterator2.next();
                    if (task2.getDecompositionPrototype() == null) {
                        resultSet.add(task2);
                    }
                }
            }
        }
        return resultSet;
    }


    private List <YVerificationMessage> checkDecompositionUsage() {
        List<YVerificationMessage> messages =
                new ArrayList<YVerificationMessage>();
        Set netsBeingUsed = new HashSet();
        unfoldNetChildren(getRootNet(), netsBeingUsed, null);
        Set specifiedDecompositons = new HashSet(_decompositions);
        specifiedDecompositons.removeAll(netsBeingUsed);

        Set decompositionsNotBeingUsed = specifiedDecompositons;
        if (decompositionsNotBeingUsed.size() > 0) {
            for (Iterator iterator = decompositionsNotBeingUsed.iterator(); iterator.hasNext();) {
                YDecomposition decomp = (YDecomposition) iterator.next();
                messages.add(new YVerificationMessage(decomp, "The decompositon(" + decomp.getId() +
                        ") is not being used in this specification.", YVerificationMessage.WARNING_STATUS));
            }
        }
        return messages;
    }


    private Set unfoldNetChildren(YDecomposition decomposition, Set netsAlreadyExplored, String criterion) {
        Set resultSet = new HashSet();
        netsAlreadyExplored.add(decomposition);
        if (decomposition instanceof YAWLServiceGateway) {
            return resultSet;
        }
        Set visited = new HashSet();
        Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
        visiting.add(((YNet) decomposition).getInputCondition());
        do {
            visited.addAll(visiting);
            visiting = YNet.getPostset(visiting);
            visiting.removeAll(visited);
            for (Iterator iterator = visiting.iterator(); iterator.hasNext();) {
                YExternalNetElement element = (YExternalNetElement) iterator.next();
                if (element instanceof YTask) {
                    YDecomposition decomp = ((YTask) element).getDecompositionPrototype();
                    if (decomp != null) {
                        if (decomp instanceof YNet) {
                            if ("decomposedTasks".equals(criterion)) {
                                resultSet.add(element);
                            }
                        }
                        if (!netsAlreadyExplored.contains(decomp)) {
                            resultSet.addAll(unfoldNetChildren(decomp, netsAlreadyExplored, criterion));
                        }
                    } else if ("emptyTasks".equals(criterion)) {
                        resultSet.add(element);
                    }
                } else if (element instanceof YCondition) {
                    if ("allConditions".equals(criterion)) {
                        resultSet.add(element);
                    }
                }
            }
        } while (visiting.size() > 0);
        return resultSet;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getDbID() {
    	return _dbid;
    }
    
    public void setDbID(Long dbid) {
    	_dbid = dbid;
    }
    
    /**
     * 
     * @return the id
     */
    @Column(name="uri")
    public String getID() {
        return _specURI;
    }


    /**
     * Method inserted for hibernate.
     * @param id
     */
    public void setID( String id ) {
		_specURI = id;
	}

	/*
	 * Temporal database concepts
	 * inserted valid start time and valid end time for a particular
	 * specification. This can be used for the log to check which specification
	 * was actually used at a particular time.   
	 * */
	

	boolean archived = false;
	
	
	public void markArchived() {
    	archived = true;
	}

    /**
     * Returns the XML tools for YAWL.
     * @return the XMLTools4Yawl object.
     */
	@Transient
    public XMLToolsForYAWL getToolsForYAWL() {
        return _xmlToolsForYAWL;
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name="metadata_fk")
    public YMetaData getMetaData() {
        return _metaData;
    }

    public void setMetaData(YMetaData metaData) {
        _metaData = metaData;
    }

	@Override
	public Object clone() throws CloneNotSupportedException {
		YSpecification retval = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos
					.toByteArray());
			ois = new ObjectInputStream(bin);
			retval = (YSpecification) ois.readObject();
			retval._xmlToolsForYAWL = new XMLToolsForYAWL();
			retval._xmlToolsForYAWL.setPrimarySchema(_xmlToolsForYAWL.getSchemaString());
			return retval;
		} catch (Exception e) {
			CloneNotSupportedException ex = new CloneNotSupportedException();
			ex.fillInStackTrace();
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				oos.close();
				ois.close();
			} catch (IOException e) {
			}
		}
	}
    
    public YSpecification deepClone() {
        try {
            YSpecification clone = (YSpecification) super.clone();
            
            clone._dbid = null;
            clone._metaData = (YMetaData) _metaData.clone();
            clone._xmlToolsForYAWL = (XMLToolsForYAWL) _xmlToolsForYAWL.clone();
            
            clone._decompositions = new ArrayList<YDecomposition>();
            for( YDecomposition decomposition : getDecompositions() ) {
                YDecomposition cloneDecomp = decomposition.deepClone();
                cloneDecomp.setParent( clone );
                clone.getDecompositions().add( cloneDecomp );
            }
            
            for( YDecomposition decomposition : getDecompositions() ) {
                String decompID = decomposition.getId();
                if( decomposition instanceof YNet ) {
                    YNet net = (YNet) decomposition;
                    YNet cloneNet = (YNet) clone.getDecomposition( decompID );
                    for( YExternalNetElement element : net.getNetElements() ) {
                        String elementID = element.getID();
                        if( element instanceof YTask ) {
                            YTask task = (YTask) element;
                            YTask cloneTask = (YTask) cloneNet.getNetElement( elementID );
                            if( task.getDecompositionPrototype() != null ) {
                                YDecomposition prototype = task.getDecompositionPrototype();
                                String prototypeID = prototype.getId();
                                YDecomposition clonePrototype = clone.getDecomposition( prototypeID );
                                
                                cloneTask.setDecompositionPrototype( clonePrototype );
                            }
                        }
                    }
                }
            }
            
            return clone;
        }
        catch( CloneNotSupportedException e ) {
            throw new Error( e );
        }
    }
    @Basic
	public boolean getArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
