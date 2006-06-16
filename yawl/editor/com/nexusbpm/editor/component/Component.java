package com.nexusbpm.editor.component;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A Capsela component is the basic element of the Capsela visual programming
 * language. Typically a component can appear as a member of a flow and as such
 * it can be executed by calling the <code>run()</code> method. A notable 
 * exception is the folder, which cannot appear within a flow and does not
 * not make sense to execute since its sole purpose is the organization of
 * other components.
 * 
 * @author   Felix L J Mayer
 * @version  $Revision: 1.355 $
 * @created  Dec 5, 2003
 * @hibernate.class table="COMPONENT" discriminator-value="0"
 * @hibernate.cache usage="transactional"
 * @hibernate.discriminator column="COMPONENT_TYPE_ID" type="integer"
 * @javabean.class name="Component" displayName="Component"
 */
public abstract class Component  {

	private final static Log LOG = LogFactory.getLog( Component.class );

//	private transient StringPreprocessor stringProcessor;
	
	/**
	 * @javabean.property displayName="String preprocessor" hidden="true"
	 */
//	public StringPreprocessor getStringProcessor() {
//		return stringProcessor;
//	}
//	
//	public void setStringProcessor( StringPreprocessor stringProcessor ) {
//		this.stringProcessor = stringProcessor;
//	}
	/**
	 * Creates a new empty <code>Component</code>, needed for Hibernate.
	 */
	public Component() {
		super();
	}//Component()
	/**
	 * Creates a new <code>Component</code> with mandatory attributes.
	 * @param name    the <code>Component</code>'s name
	 * @param folder  the <code>Component</code>'s parent folder
	 */
	public Component( String name) {
		super();
//		setName( name );
//		setFolder( folder );
	}//Component()
	/**
	 * @see com.ichg.capsela.framework.domain.DomainObject#construct()
	 */
	protected void construct() {
//		super.construct();
//		_executionStatus = new ExecutionStatus( this, ATTR_EXECUTION_STATUS, ExecutionStatus.STATUS_NEW, ExecutionStatus.REASON_UNKNOWN );
		_startTime = 0;
		_executionTime = 0;
		_instance = false;
//		_attributes = new HashMap<String, ComponentAttribute>();
//		_sourceEdges = new HashSet<ComponentEdge>();
//		_sinkEdges = new HashSet<ComponentEdge>();
//		_instances = new HashSet();
//		_aliasComponents = new HashSet();
//		_executionEvents = new HashSet();
	}//construct()

	/**
	 * @see com.ichg.capsela.framework.domain.DomainObject#deepClone()
	 */
//	public DomainObject deepClone() {
//		Component clone = (Component) super.deepClone();
//		// Parent _folder stays the same because we don't have another parent.
//		clone._instance = _instance;
//		if( _flowLocation != null ) {
//			clone._flowLocation = (Rectangle) _flowLocation.clone();
//		}//if
//		for( Iterator<Map.Entry<String, ComponentAttribute>> i = this.getComponentAttributes().entrySet().iterator(); i.hasNext(); ) {
//			Map.Entry<String, ComponentAttribute> entry = i.next();
//			ComponentAttribute attribute = entry.getValue();
//			ComponentAttribute attributeClone = (ComponentAttribute) attribute.deepClone();
//			attributeClone.setComponent( clone );
//			clone._attributes.put( entry.getKey(), attributeClone );
//		}//for
//		return clone;
//	}//deepClone()

	/**
	 * @see java.lang.Object#toString()
	 */
//	public String toString() {
//		return super.toString() + " typeID=" + _typeID + " name='" + _name + "'"
//			+ " executionStatus={" + _executionStatus + "}"
//			+ " attributes={" + idString( _attributes ) + "}"
//			+ " sourceEdges={" + idString( _sourceEdges ) + "}"
//			+ " sinkEdges={" + idString( _sinkEdges ) + "}"
//			+ " folder=" + Helper.id( _folder ) 
//			+ " targetEngineName='" + _targetEngineName + "'"
//			+ " startTime=" + _startTime + " executionTime=" + _executionTime
//			+ " isInstance=" + _instance; 
//	}//toString()

	/**
	 * @see com.ichg.capsela.framework.domain.IndependentDomainObject#isolate()
	 */
//	public void isolate() {
//		super.isolate();
//		_folder = null;
//		_attributes = null;
//		_sourceEdges = null;
//		_sinkEdges = null;
//		_aliasComponents = null;
//	}//isolate()
//	
	/**
	 * @see com.ichg.capsela.framework.domain.DomainObject#remove()
	 */
//	protected void remove() {
//		super.remove();
//		if( getFolder() != null ) {
//			removeFromCollection( getFolder().getComponents() );
//		}//if
//		// Need to clone the collection before iterating, otherwise we get 
//		// ConcurrentModificationException when removing something from it.
//		for( Iterator i = clonedSet( getSourceEdges() ).iterator(); i.hasNext(); ) {
//			ComponentEdge edge = (ComponentEdge) i.next();
//			removeSourceEdge( edge );
//		}//for
//		for( Iterator i = clonedSet( getSinkEdges() ).iterator(); i.hasNext(); ) {
//			ComponentEdge edge = (ComponentEdge) i.next();
//			removeSinkEdge( edge );
//		}//for
//		// The instances collection is not initialized for performance reasons.
//		// However, the called methods take initialization into account.
//		if( getTemplate() != null ) {
//			removeFromCollection( getTemplate().getInstances() );
//		}//if
//		for( Iterator i = collectionIterator( getInstances() ); i.hasNext(); ) {
//			Component component = (Component) i.next();
//			component.setTemplate( null );
//		}//for
//		for( Iterator i = collectionIterator( getAliasComponents() ); i.hasNext(); ) {
//			AliasComponent alias = (AliasComponent) i.next();
//			alias.setAliasReference( null );
//		}//for
//	}//remove()

	/**
     * unused.
	 * @return unused.
	 */
//	public PersistentDomainObject startPointForConsistency() {
//		return getFolder() == null ? this : getFolder();
//	}//startPointForConsistency()

	/**
	 * Find the lock for this component. First <code>super.findLock()</code>
	 * is called to find the lock for exactly this component. If there is no
	 * lock, the tree of parent flows is traversed upwards (by calling 
	 * <code>getFolder()</code>) looking for a locked one until a parent is not 
	 * a flow. If a locked flow is found, its lock returned. This means that
	 * a lock on a flow implicitely locks the entire component tree below it.
	 * @return the lock for this domain object
	 * @see com.ichg.capsela.framework.domain.IndependentDomainObject#findLock()
	 */
//	public DomainObjectLock findLock() {
//		DomainObjectLock lock = super.findLock();
//		if( lock == null && getFolder() != null ) {
//			lock = getFolder().findLock();
//		}//if
//		return lock;
//	}//findLock()

	/**
     * Returns true if a pessimistic lock is required for an update. For
     * components, a lock is required if the component is not an instance.
     * Since instances are only updated by the engine, pessimistic locking is 
	 * neither needed nor desired.<br>
     * Needed to update TransientSession without acquiring a lock on it.
     * @return true if a pessimistic lock is required for an update.
	 * @see com.ichg.capsela.framework.domain.IndependentDomainObject#lockRequiredForUpdate()
	 */
//	protected boolean lockRequiredForUpdate() {
//		return ! isInstance();
//	}//lockRequiredForUpdate()
	/**
     * Returns true if a pessimistic lock is required for deletion.
     * Needed to delete Component instances and Executables without acquiring
     * a lock on them.
     * @return true if a pessimistic lock is required for deletion.
	 * @see com.ichg.capsela.framework.domain.IndependentDomainObject#lockRequiredForDeletion()
	 */
//	protected boolean lockRequiredForDeletion() {
//		return ! isInstance();
//	}//lockRequiredForDeletion()

	/**
     * Called in the server to initialize any lazily loaded collections before
     * the object is serialized and sent to the client. If this method is called, 
     * all collections on this object are initialized.
	 * @see com.ichg.capsela.framework.domain.IndependentDomainObject#initialize(int)
	 */
//	protected void initialize( int depth ) throws CapselaException {
//		super.initialize( depth );
//		String className = Component.class.getName();
//		PersistenceManager persistenceManager = PersistenceManager.instance();
//		// Initialize the component attribute collection.
//		persistenceManager.initialize( getComponentAttributes(), className + "." + ATTR_ATTRIBUTES );
//		// Initialize the source edge collection.
//		persistenceManager.initialize( getSourceEdges(), className + "." + ATTR_SOURCE_EDGES );
//		if( depth > 0 ) {
//			for( Iterator i = getSourceEdges().iterator(); i.hasNext(); ) {
//				ComponentEdge edge = (ComponentEdge) i.next();
//				persistenceManager.initialize( edge.getSinkComponent(), depth - 1, null );
//			}//for
//		}//if
//		// Initialize the sink edge collection.
//		persistenceManager.initialize( getSinkEdges(), className + "." + ATTR_SINK_EDGES );
//		if( depth > 0 ) {
//			for( Iterator i = getSinkEdges().iterator(); i.hasNext(); ) {
//				ComponentEdge edge = (ComponentEdge) i.next();
//				persistenceManager.initialize( edge.getSourceComponent(), depth - 1, null );
//			}//for
//		}//if
//		// Initialize the alias component collection (alias components that alias this component).
//		persistenceManager.initialize( getAliasComponents(), className + "." + ATTR_ALIAS_COMPONENTS );
//		if( depth > 0 ) {
//			for( Iterator i = getAliasComponents().iterator(); i.hasNext(); ) {
//				Component c = (Component) i.next();
//				persistenceManager.initialize( c, depth - 1, null );
//			}//for
//		}//if
//		// Initialize the execution events collection.
//		persistenceManager.initialize( getExecutionEvents(), className + "." + ATTR_EXECUTION_EVENTS );
//		/* Do not initialize the instances collection for performance reasons.
//		persistenceManager.initialize( getInstances(), ATTR_INSTANCES );
//		// For performance reasons only the referencing AliasComponents of the
//		// instances are initialized; this is necessary for graph consistency.
//		for( Iterator i = getInstances().iterator(); i.hasNext(); ) {
//		Component component = (Component) i.next();
//		persistenceManager.initialize( 
//		component.getAliasComponents(), ATTR_ALIAS_COMPONENTS );
//		}//for
//		*/
//	}//initializeCollections()

	/**
	 * Validation for the flow checker on the client side. Exceptions should
	 * include a user-friendly message that explains what the user should do
	 * on the client to fix the problem. This method is meant to be overriden
	 * by subclasses with specific validation needs.
	 * @param validationErrors the collection where the validation errors
	 * (<tt>Exception</tt>s) will be added.
	 * @return the collection that was passed in as a parameter.
	 */
//	public Collection<ValidationMessage> clientValidate(Collection<ValidationMessage> validationErrors) {
//		return validationErrors;
//	}//clientValidate()
//
	/**
     * Called by Hibernate before the object is persisted.<br>
     * When overriding this method, make sure to call super.validate()!
	 * @see net.sf.hibernate.Validatable#validate()
	 */
//	public void validate() throws ValidationFailure {
//		super.validate();
//		// The component must have a name.
//		if( _name == null ) {
//			throw new ValidationFailure( idString() + ": _name == null" );
//		}//if
//		// The component must have a parent folder or a parent instance.
//		if( _folder == null && _template == null ) {
//			throw new ValidationFailure( idString() + ": _folder == null && _template == null" );
//		}//if
//		// The component cannot be its own parent folder.
//		if( _folder != null && _folder.equals( this ) ) {
//			throw new ValidationFailure( idString() + ": _folder.equals( this )" );
//		}//if
//		// Within a flow a component must have a location.
//		if( _folder != null && _folder instanceof FlowComponent && _flowLocation == null ) {
//			throw new ValidationFailure( idString() + 
//				": _folder instanceof FlowComponent && _flowLocation == null" );
//		}//if
		
		// The following lines are commented out because it is no longer true with dynamic attributes.
		
//		// A component can only have edges within a flow.
//		if( _folder != null && !(_folder instanceof FlowComponent) 
//			&& _sourceEdges != null && _sourceEdges.size() > 0 ) {
//			throw new ValidationFailure( idString() + 
//				": !(_folder instanceof FlowComponent) && _sourceEdges.size() > 0" );
//		}//if
//		if( _folder != null && !(_folder instanceof FlowComponent) 
//			&& _sinkEdges != null && _sinkEdges.size() > 0 ) {
//			throw new ValidationFailure( idString() + 
//				": !(_folder instanceof FlowComponent) && _sinkEdges.size() > 0" );
//		}//if
		
//		
//		// Check for the correct number of incoming and outgoing Edges.
//		validateSourceControlEdges(false, 0);
//		validateSinkControlEdges(false, 0);
//	}//validate()
	/**
	 * Validates the number of outgoing control edges of the referencing 
	 * ComponentVertex.
	 * @param strict whether to be strict and validate the minimums
	 * @param prospective how many extra prospective edges to take into account
	 * @throws ValidationFailure
	 */
//	public void validateSourceControlEdges(boolean strict, int prospective) throws ValidationFailure {
//		if( Helper.isInitialized( getSourceEdges() ) ) {
//			EdgeRange range = this.getSourceEdgeRange();
//			int size = getSourceControlEdges().size() + prospective;
//			// Make sure the number of source edges is not over the max.
//			if( size > range.getMax() ) {
//				throw new ValidationFailure( idString() + 
//					": getSourceControlEdges().size() > " + range.getMax() );
//			}//if
//			// Make sure the number of source edges is not under the min.
//			if(strict) {
//				if( size < range.getMin() ) {
//					throw new ValidationFailure( idString() + 
//						": getSourceControlEdges().size() < " + range.getMin() );
//				}//if
//			}//if
//		}//if
//	}//validateSourceEdges()
	/**
	 * Validates the number of incoming control edges of the referencing 
	 * ComponentVertex.
	 * @param strict whether to be strict and validate the minimums
	 * @param prospective how many extra prospective edges to take into account
	 * @throws ValidationFailure
	 */
//	public void validateSinkControlEdges(boolean strict, int prospective) throws ValidationFailure {
//		if( Helper.isInitialized( getSinkEdges() ) ) {
//			EdgeRange range = this.getSinkEdgeRange();
//			int size = getSinkControlEdges().size() + prospective;
//			// Make sure the number of sink edges is not over the max.
//			if( size > range.getMax() ) {
//				throw new ValidationFailure( idString() + 
//					": getSinkControlEdges().size() > " + range.getMax() );
//			}//if
//			// Make sure the number of sink edges is not under the min.
//			if(strict) {
//				if( size < range.getMin() ) {
//					throw new ValidationFailure( idString() + 
//						": getSinkControlEdges().size() < " + range.getMin() );
//				}//if
//			}//if
//		}//if
//	}//validateSinkEdges()

	/**
     * Called by the <tt>DomainObjectInterceptor</tt> when a domain object is
     * deleted.
     * @throws CallbackException if there is no lock for this domain object,
     *                           if the lock is held by another session, or if
     *                           there is an error acquiring a one-time lock.
	 * @see com.ichg.capsela.framework.domain.PersistentDomainObject#onDelete()
	 */
//	protected void onDelete() throws CallbackException {
//		super.onDelete();
//		// Make sure no alias components reference this component, or we will
//		// get a foreign key constraint violation when we delete this component.
//		for( Iterator i = _aliasComponents.iterator(); i.hasNext(); ) {
//			AliasComponent aliasComponent = (AliasComponent) i.next();
//			aliasComponent.setAliasReference( null );
//			// PersistenceManager.isPersisting() is true, so the AliasComponent will
//			// not be marked for update.
//			aliasComponent.setUpdateNeeded();
//			try {
//				DomainObjectManager.instance().acquireOneTimeLock( aliasComponent );
//			}//try
//			catch( CapselaException e ) {
//				throw new CallbackException( e );
//			}//catch
//		}//for
//	}//onDelete()

	/**
	 * Returns the valid range of source control edges for this component.
	 * @return the valid range for source edges
	 */
//	public EdgeRange getSourceEdgeRange() {
//		return new EdgeRange(0, 1);
//	}//getSourceEdgeRange()
//	/**
//	 * Returns the valid range of sink control edges for this component.
//	 * @return the valid range for sink edges
//	 */
//	public EdgeRange getSinkEdgeRange() {
//		return new EdgeRange(0, Integer.MAX_VALUE);
//	}//getSinkEdgeRange()


	/**
	 * @see com.ichg.capsela.framework.domain.PersistentDomainObject#trackDomainObjectEvents()
	 */
//	protected boolean trackDomainObjectEvents() {
//		return true;
//	}//trackDomainObjectEvents()
//
//
//	/**
//	 * @see com.ichg.capsela.framework.domain.DomainObject#getAttribute(java.lang.String)
//	 */
//	public Object getAttribute( String attributeName ) throws CapselaException {
//		// Call super.getAttribute() first to avoid LazyInitializationException.
//		if( super.hasAttribute( attributeName ) ) return super.getAttribute( attributeName );
//		ComponentAttribute attribute = getComponentAttribute( attributeName );
//		if( attribute instanceof ScalarAttribute ) {
//			return ( (ScalarAttribute) attribute ).getValue();
//		}//if
//		else {
//			return attribute;
//		}//else
//	}//getAttribute()
//	/**
//	 * @see com.ichg.capsela.framework.domain.DomainObject#hasAttribute(String)
//	 */
//	public boolean hasAttribute( String attributeName ) {
//		// Call super.hasAttribute() first to avoid LazyInitializationException.
//		if( super.hasAttribute( attributeName ) ) return true;
//		ComponentAttribute attribute = getComponentAttribute( attributeName );
//		return attribute != null;
//	}//hasAttribute()
//	/**
//	 * @param attributeName
//	 * @param value
//	 * @throws CapselaException
//	 */
//	public void setAttribute( String attributeName, Object value ) throws CapselaException {
//		ComponentAttribute attribute = getComponentAttribute( attributeName );
//		if( attribute == null ) {
//			super.setAttribute( attributeName, value );
//		}//if
//		else if( attribute instanceof ScalarAttribute ) {
//			( (ScalarAttribute) attribute ).setValue( value );
//		}//else if
//		else {
//		}//else
//	}//setAttribute()
//	/**
//	 * @see com.ichg.capsela.framework.domain.PersistentDomainObject#attributeClass(java.lang.String)
//	 */
//	public Class attributeClass( String attributeName ) throws CapselaException {
//		ComponentAttribute attribute = getComponentAttribute( attributeName );
//		if( attribute == null ) {
//			return super.attributeClass( attributeName );
//		}//if
//		else {
//			return attribute.getClass();
//		}//else
//	}//attributeClass()
//
//	/**
//	 * Gets the Component's primary key.
//	 * This method is only needed for the Hibernate mapping.
//	 * @return  the Component's primary key
//	 * @hibernate.id column="COMPONENT_ID" type="long" generator-class="sequence" 
//	 *   unsaved-value="-1"
//	 * @hibernate.generator-param name="sequence" value="COMPONENT_SEQ"
//	 * @javabean.property displayName="ID" hidden="true"
//	 */
//	public long getId() {
//		return super.getId();
//	}//getId()
//	/**
//	 * Return the version used for optimistic locking in long transactions.
//	 * This method is only needed for the Hibernate mapping.
//	 * NOTE Don't use optimistic locking because of problems with detached objects.
//	 * @return  the version used for optimistic locking in long transactions
//	 * hibernate.version column="COMPONENT_VERSION" type="integer"
//	 * @hibernate.property column="COMPONENT_VERSION" not-null="true" type="integer"
//	 * @javabean.property displayName="Version" hidden="true"
//	 */
//	public int getVersion() {
//		return super.getVersion();
//	}//getVersion()
//
//	/**
//     * Prepares this component to be executed.
//	 * @throws CapselaException
//	 */
//	public void prepare() throws CapselaException {
//		// This method is meant to be overidden.
//	}//prepare()
//	/**
//	 * <p>
//	 * Runs this <code>Component</code>, i.e. performs its business logic.
//	 * </p><p>
//	 * An <tt>EngineContext</tt> must be available in the <tt>EngineManager</tt>
//	 * when running this method. Also a <tt>TransientSession</tt> is available
//	 * on the current thread. 
//	 * </p>
//	 * @throws CapselaException
//	 */
//	public abstract void run() throws CapselaException;
//	/**
//	 * @return TODO: unused?
//	 */
//	public boolean canBeKilled() {
//		return false;
//	}//canBeKilled()
//	/**
//	 * Returns <tt>true</tt> if running this component submits engine tasks that
//	 * get executed in a separate MDB, which this component then waits on.
//	 * @return whether this component submits engine tasks that get executed in
//	 *         a separate MDB, which this component then waits on.
//	 */
//	public boolean runningSubmitsEngineTasks() {
//		return false;
//	}//runningSubmitsEngineTasks()
//	/**
//	 * Throws a <tt>ComponentException</tt> if the component's execution status
//	 * is not set to a status of running. This is a utility method used within the
//	 * <tt>run(EngineContext)</tt> method of components that spend a significant
//	 * amount of time inside said method. Checking the execution status every so
//	 * often in the <tt>run(EngineContext)</tt> method allows the component to be
//	 * killed cleanly.
//	 * @throws ComponentException if the execution status is not set to running
//	 */
//	protected void checkExecutionStatus() throws ComponentException {
//		if( ! this.getExecutionStatus().isRunning() ) {
//			throw new ComponentException( "Aborting component execution: " + this.idString() );
//		}//if
//	}//checkExecutionStatus()
//	
//	/** The name of the <tt>type ID</tt> attribute of this component. */
//	static public final String ATTR_TYPE_ID = "typeID";
//	private Long _typeID;
//	/**
//	 * Gets the primary key of the type discriminator.
//	 * @return  the primary key of the type discriminator
//	 * @hibernate.property column="COMPONENT_TYPE_ID" not-null="true" insert="false" update="false"
//	 * @javabean.property displayName="Type ID" hidden="true" readOnly="true"
//	 */
//	public Long getTypeID() {
//		return _typeID;
//	}//getType()
//	/**
//	 * TODO: make this protected again once XDoclet handles readOnly correctly again (should be in 1.3).
//	 * See http://opensource.atlassian.com/projects/xdoclet/browse/XDT-1461 for more information.
//	 * -- Dani
//	 * 
//	 * Sets the primary key of the type discriminator.
//	 * This method should only be used by Hibernate.
//	 * @param typeID  the primary key of the new type discriminator
//	 */
//	public void setTypeID( Long typeID ) {
//		_typeID = typeID;
//	}//setType()
//	
//	/** The name of the <tt>error output</tt> attribute of this component. */
//	static public final String ATTR_ERROR_OUTPUT = "errorOutput";
//	/**
//	 * This component's error output.
//	 * 
//	 * This error output will typically be errors related to dynamic attributes
//	 * not being able to be processed correctly.  Since there is typically a
//	 * lot of output from these errors and the errors are specific to each
//	 * component, it is necessary for the error output to be attached to the
//	 * component so that the user can later view it.
//	 */
//	private String _errorOutput;
//	/**
//	 * Gets the component's error output.
//	 * 
//	 * @see #_errorOutput
//	 * 
//	 * @return the error output description
//	 * @hibernate.property column="COMPONENT_ERROR_OUTPUT" length="4096" not-null="false"
//	 * @javabean.property displayName="Error Output" hidden="false"
//	 */
//	public String getErrorOutput() {
//		return _errorOutput;
//	}//getErrorOutput()
//	/**
//	 * Sets the component's error output.
//	 * @param errorOutput the error output to set for the component.
//	 */
//	public void setErrorOutput( String errorOutput ) {
//		updateAttribute( ATTR_ERROR_OUTPUT, errorOutput );
//		_errorOutput = errorOutput;
//	}//setErrorOutput()
//	
//    /** The name of the <tt>deleted</tt> attribute of this component. */
//    static public final String ATTR_DELETED = "deleted";
//    /**
//     * Whether this independent domain object has been deleted. Deleted
//     * independent domain objects are stored in the trash until they are
//     * actually removed from the database.
//     */
//    private Boolean _deleted;
//    /**
//     * Determines if this independent domain object has been deleted. Deleted
//     * independent domain objects are stored in the trash until they are
//     * actually removed from the database.
//     * @see #_deleted
//     * @return true if this component has been deleted, false otherwise
//     * @hibernate.property column="COMPONENT_DELETED"
//     * @javabean.property displayName="Is Deleted" hidden="true"
//     */
//    public Boolean isDeleted() {
//        return _deleted != null ? _deleted : Boolean.FALSE;
//    }//isDeleted()
//
//    /**
//     * Sets whether this independent domain object has been deleted.
//     * @see #_deleted
//     * @param isDeleted whether this object has been deleted.
//     */
//    public void setDeleted( Boolean isDeleted ) {
//        updateAttribute( ATTR_DELETED, isDeleted );
//        _deleted = isDeleted != null ? isDeleted : Boolean.FALSE;
//        if( isDeleted != null && isDeleted.booleanValue() == true ) {
//			// alias components that alias this component need to be updated (note
//			// that we iterate over a new set to avoid ConcurrentModificationExceptions)
//			Set aliasComponents = new HashSet( getAliasComponents() );
//			for( Iterator i = aliasComponents.iterator(); i.hasNext(); ) {
//				AliasComponent aliasComponent = (AliasComponent) i.next();
//				aliasComponent.setAliasReference( null );
//				aliasComponent.setUpdateNeeded();
//				if( ! aliasComponent.isUnsaved() ) {
//					try {
//						DomainObjectManager.instance().acquireOneTimeLock( aliasComponent );
//					}
//					catch( CapselaException e ) {
//						// already logged, although this shouldn't ever be reached
//					}
//				}
//			}//for
//        }
//    }//setDeleted()
//	
//	/** The name of the <tt>name</tt> attribute of this component. */
//	static public final String ATTR_NAME = "name";
//	/** The name of this component. */
//	private String _name;
//	/**
//	 * Gets the component's name.
//	 * @return the component's name
//	 * @hibernate.property column="COMPONENT_NAME" length="50" not-null="true"
//	 * @hibernate.column name="COMPONENT_NAME" unique-key="UQ_COMPONENT_NAME"
//	 * @javabean.property displayName="Name" hidden="false"
//	 */
//	public String getName() {
//		return _name;
//	}//getName()
//	/**
//	 * Sets the component's name.
//	 * @param  name  the component's new name
//	 */
//	public void setName( String name ) {
//		assert name != null : "name != null";
//		updateAttribute( ATTR_NAME, name );
//		_name = name;
//	}//setName()
//	/**
//	 * Removes the counter from this component's name, for example "My Flow [2]" -> "My Flow".
//	 * If there is no counter on this component's name, then nothing is done. This method is
//	 * used to guarantee unique component names within a given folder or flow.
//	 */
//	public void stripCounterFromName() {
//		_name = new NameAndCounter( _name ).getStrippedName();
//	}//stripCounterFromName()
//
//	/**
//	 * Adds a counter to this component's name, for example "My Flow" -> "My Flow [3]". If
//	 * there is no need to add a counter to this component's name, then no counter is added.
//	 * If a counter already exists on the name, but it is not needed, then it is removed.
//	 * This method is used to guarantee unique component names within a given folder or flow.
//	 */
//	public void addCounterToName() {
//
//		if( this.getFolder() != null && (Helper.isInitialized( getFolder().getComponents() )) ) {
//			Integer max = null;
//			String strippedName = new NameAndCounter( _name ).getStrippedName();
//			for( Iterator i = this.getFolder().getComponents().iterator(); i.hasNext(); ) {
//				Component c = (Component) i.next();
//
//				if( c.getIdentifier().equals( this.getIdentifier() ) ) continue;
//				NameAndCounter nac = new NameAndCounter( c.getName() );
//				if( nac.getStrippedName().equals( strippedName ) ) {
//					if( max == null || max.intValue() < nac.getCounter().intValue() ) {
//						max = nac.getCounter();
//					}//if
//				}//if
//			}//for
//			if( max != null ) {
//				_name = strippedName + "[" + (max.intValue() + 1) + "]";
//			}//if
//			else {
//				_name = strippedName;
//			}//else
//
//			if( LOG.isDebugEnabled() )
//				LOG.debug( "addCounterToName folder=" + getFolder().getIdentifier()
//						+ ";components=" + getFolder().getComponents().size()
//						+ ";max=" + max + ";strippedName=" + strippedName + ";name=" + _name );
//		}//if
//	}//addCounterToName()
//
//	/**
//	 * Takes a name and splits it into the actual name and the counter, for
//	 * example "My Flow [2]" -> {"My Flow", 2}, or "My Flow" -> {"My Flow", 0}.
//	 */
//	private class NameAndCounter {
//		private String _strippedName;
//		private Integer _counter;
//		/**
//		 * Constructor that splits the name into the actual name and counter.
//		 * @param origName the original name.
//		 */
//		private NameAndCounter( String origName ) {
//			_strippedName = origName;
//			_counter = new Integer( 0 );
//			if( origName.endsWith( "]" ) ) {
//				int i = origName.lastIndexOf( "[" );
//				if( i != -1 ) {
//					String count = origName.substring( i + 1, origName.length() - 1 );
//					try {
//						_counter = Integer.valueOf( count );
//						_strippedName = origName.substring( 0, i );
//					}//try
//					catch( NumberFormatException e ) {
//						// Apparently there's no counter.
//					}//catch
//				}//if
//			}//if
//		}//NameAndCounter()
//		private String getStrippedName() {
//			return _strippedName;
//		}//getName()
//		private Integer getCounter() {
//			return _counter;
//		}//getCounter()
//		/**
//		 * @see Object#toString()
//		 */
//		public String toString() {
//			return "NameAndCounter[" + _name + ", " + _counter + "]";
//		}//toString()
//	}//NameAndCounter
//	
//	/** The name of the <tt>description</tt> attribute of this component. */
//	static public final String ATTR_DESCRIPTION = "description";
//	/** The description of this component */
//	private String _description;
//	/**
//	 * Gets the component's description.
//	 * @return the component's description
//	 * @hibernate.property column="COMPONENT_DESCRIPTION" length="1024" not-null="false"
//	 * @javabean.property displayName="Description" hidden="false"
//	 */
//	public String getDescription() {
//		return _description;
//	}//getName()
//	/**
//	 * Sets the component's description.
//	 * @param description  the component's new description
//	 */
//	public void setDescription( String description ) {
//		updateAttribute( ATTR_DESCRIPTION, description );
//		_description = description;
//	}//setName()
//	
//	/** The name of the <tt>folder</tt> attribute of this component. */
//	static public final String ATTR_FOLDER = "folder";
//	/** The folder that contains this component. */
//	protected FolderComponent _folder;
//	/**
//	 * Gets the folder that contains this component.
//	 * @return  the folder that contains this component
//	 * @hibernate.many-to-one foreign-key="FKCOMPONENT_FOLDER" cascade="none"
//	 *   outer-join="false" class="com.ichg.capsela.domain.component.FolderComponent"
//	 * @hibernate.column name="FOLDER_COMPONENT_ID" index="IXCOMPONENT_FOLDER" unique-key="UQ_COMPONENT_NAME" 
//	 * @javabean.property displayName="Folder" hidden="true"
//	 */
//	public FolderComponent getFolder() {
//		return _folder;
//	}//getFolder()
//	/**
//	 * Sets the folder that contains this component.
//	 * Every component must be contained within a folder.
//	 * @param folder the new folder that contains this component
//	 */
//	public void setFolder( FolderComponent folder ) {
//		this.setFolder( folder, false );
//	}//setFolder()
//	/**
//	 * Sets the folder that contains this component and possibly modifies this
//	 * component's name so that it is unique within the specified folder.
//	 * Every component must be contained within a folder.
//	 * @param folder the new folder that contains this component
//	 * @param modifyNameIfNecessary whether or not to modify the name so that it's unique
//	 */
//	public void setFolder( FolderComponent folder, boolean modifyNameIfNecessary ) {
//		updateAttribute( ATTR_FOLDER, folder );
//		if( folder != null ) {
//			if( ! folder.isUnsaved() ) setFolderID( new Long( folder.getId() ) );
//			// If we are on the client and the parent folder's components collection
//			// has been initialized and does not contain this component,
//			if( ! PersistenceManager.isPersisting()
//				&& Helper.isInitialized( folder.getComponents() )
//				// This is important when the collection itself is moved to a new object.
//				&& (_folder == null || _folder.getComponents() != folder.getComponents()) ) {
//				// then remove it from the previous folder's collection.
//				if( _folder != null && Helper.isInitialized(_folder.getComponents())
//					&& containedOnce( _folder.getComponents() ) ) {
//					LOG.debug("setFolder removing " + getIdentifier() + " from " + _folder.getIdentifier());
//					removeFromCollection( _folder.getComponents() );
//				}//if
//				// and add this component to the parent folder's component collection
//				if (Helper.isInitialized(folder.getComponents())) {
//					addIfNotContained( folder.getComponents() );
//				}
//				// Done in the right order this works even if both collections are
//				// identical.
//			}//if
//		}//if
//		_folder = folder;
//		_folderID = ( folder != null ? new Long( folder.getId() ) : null );
//		if( modifyNameIfNecessary ) {
//			if( _folder != null )
//				this.addCounterToName();
//			else
//				this.stripCounterFromName();
//		}//if
//
//	}//setFolder()
//	
//	/** The name of the <tt>folder ID</tt> attribute of this component. */
//	static public final String ATTR_FOLDER_ID = "folderID";
//	/**
//	 * The primary key of the folder that contains this component. This
//	 * attribute is mapped for use in scalar queries.
//	 */
//	private Long _folderID;
//	/**
//	 * Gets the primary key of the folder that contains this component.
//	 * @return  the primary key of the folder that contains this component
//	 * @hibernate.property column="FOLDER_COMPONENT_ID" insert="false" update="false"
//	 * @javabean.property displayName="Folder ID" hidden="true" readOnly="true"
//	 */
//	public Long getFolderID() {
//		return _folderID;
//	}//getFolderID()
//	/**
//	 * TODO: make this protected again once XDoclet handles readOnly correctly again (should be in 1.3).
//	 * See http://opensource.atlassian.com/projects/xdoclet/browse/XDT-1461 for more information.
//	 * -- Dani
//	 * 
//	 * Sets the primary key of the folder that contains this component.
//	 * This method should only be used by Hibernate.
//	 * @param folderID  the primary key of the folder that contains this component.
//	 */
//	public void setFolderID( Long folderID ) {
//		_folderID = folderID;
//	}//setFolderID()
//	
//	/** The name of the <tt>component attributes</tt> attribute of this component. */
//	static public final String ATTR_ATTRIBUTES = "componentAttributes";
//	/** The attributes of this component. */
//	protected Map<String, ComponentAttribute> _attributes;
//	/**
//	 * Gets the component's attributes
//	 * @return the component's attributes
//	 * @hibernate.map role="componentAttributes" cascade="all-delete-orphan"
//	 *   inverse="true" lazy="true"
//	 * @hibernate.collection-key column="COMPONENT_ID"
//	 * @hibernate.collection-cache usage="transactional"
//	 * @hibernate.collection-index column="ATTRIBUTE_NAME" type="string" length="30" 
//	 *   not-null="true"
//	 * @hibernate.collection-one-to-many 
//	 *   class="com.ichg.capsela.domain.component.attributes.ComponentAttribute"
//	 * @javabean.property displayName="Attributes" hidden="true"
//	 */
//	public Map<String, ComponentAttribute> getComponentAttributes() {
//		return _attributes;
//	}//getComponentAttributes()
//	
//	/**
//	 * All the ComponentAttribute modifiers are protected because they should
//	 * only be used by the subclasses, which should provide more use-specific
//	 * modifiers; see for example FileComponent and EmailSenderComponent.
//	 * This particular modifier is set to public such that xml export will work 
//	 * properly for JavaBean property descriptors.
//	 * @param attributes
//	 */
//	public void setComponentAttributes( Map<String, ComponentAttribute> attributes ) {
//		_attributes = attributes;
//		updateAttribute(ATTR_ATTRIBUTES, _attributes);
//	}//setComponentAttributes()
//	/**
//     * Gets the attribute with the given name or <tt>null</tt> if no attribute
//     * with the given name exists for this component.
//	 * @param name the name of the attribute to return.
//	 * @return the attribute with the given name.
//	 */
//	public ComponentAttribute getComponentAttribute( String name ) {
//		return (ComponentAttribute) _attributes.get( name );
//	}//getComponentAttribute()
//	/**
//     * Returns whether this component has an attribute with the given name or
//     * not.
//	 * @param name the name of the attribute to check for.
//	 * @return <tt>true</tt> if this component has an attribute with the given
//     *         name.
//	 */
//	public boolean hasComponentAttribute( String name ) {
//		return this.getComponentAttributes().containsKey( name );
//	}//containsAttribute()
//	/**
//     * Adds the given attribute to this component.
//	 * @param attribute the attribute to add to the component.
//	 * @throws CapselaException if the attribute being added is dynamic and this
//     *                          component already has a static attribute with
//     *                          the same name.
//	 */
//	private void addComponentAttribute( ComponentAttribute attribute ) throws CapselaException {
//		if( super.hasAttribute( attribute.getName() ) && attribute.isDynamic() ) {
//			throw new CapselaException( attribute.getName() + " is already a static attribute." );
//		}//if
//		if( attribute.getComponent() == null || attribute.getComponent().equals( this ) == false ) {
//			attribute.setComponent( this );
//		}//if
//		_attributes.put( attribute.getName(), attribute );
//		updateAttribute( ATTR_ATTRIBUTES, _attributes );
//	}//addComponentAttribute()
//	/**
//     * Removes the attribute with the given name from this component.
//	 * @param name the name of the attribute to remove.
//	 */
//	public void removeComponentAttribute( String name ) {
//		ComponentAttribute attribute = this.getComponentAttributes().remove( name );
//		attribute.setComponent( null );
//		updateAttribute( ATTR_ATTRIBUTES, _attributes );
//	}//removeComponentAttribute()
//
//	/**
//	 * Adds a copy of the specified <tt>FileAttribute</tt> to this component's 
//	 * attributes and returns the added <tt>FileAttribute</tt>. If a 
//	 * <tt>FileAttribute</tt> corresponding to <tt>name</tt> already exists, the 
//	 * properties of the existing <tt>FileAttribute</tt> are modified, rather 
//	 * than adding a new one to keep Hibernate happy.
//     * @param name the name of the attribute to add.
//     * @param attribute the actual file attribute to add.
//     * @return the file attribute that was added or modified.
//	 * @throws CapselaException shouldn't ever be thrown.
//	 */
//	protected FileAttribute addFileAttribute( String name, FileAttribute attribute ) throws CapselaException {
//		FileAttribute result = addFileAttribute( name, attribute.getDisplayedName(), attribute.getSyntheticName(), attribute.isOverwrite() );
//		return result;
//	}//addFileAttribute()
//	/**
//	 * Adds a new <tt>FileAttribute</tt> (initialized according to the parameters) 
//	 * to this component's attributes and returns the added <tt>FileAttribute</tt>. 
//	 * If a <tt>FileAttribute</tt> corresponding to <tt>name</tt> already exists, 
//	 * the properties of the existing <tt>FileAttribute</tt> are modified, rather 
//	 * than adding a new one. This keeps Hibernate happy.
//     * @param name the name of the attribute to add.
//     * @param displayedName the displayed filename.
//     * @param syntheticName the actual filename.
//     * @param overwrite whether to overwrite files with the same displayed name.
//     * @return the file attribute that was added or modified.
//	 * @throws CapselaException shouldn't ever be thrown.
//	 */
//	protected FileAttribute addFileAttribute( String name, String displayedName, String syntheticName, boolean overwrite )
//	throws CapselaException {
//		// If the attribute is already in the Map, modify the existing instance. 
//		// Otherwise, create a new instance. This tells Hibernate whether it needs 
//		// to update or insert the attribute.
//		FileAttribute fileAttribute = (FileAttribute) getComponentAttribute( name );
//		if( fileAttribute != null ) {
//			fileAttribute.setDisplayedName( displayedName );
//			fileAttribute.setOverwrite( overwrite );
//			fileAttribute.setSyntheticName( syntheticName );
//		}//if
//		else {
//			fileAttribute = new FileAttribute( this, name, displayedName, syntheticName, overwrite );
//		}//else
//		addComponentAttribute( fileAttribute );
//		return fileAttribute;
//	}//addFileAttribute()
//	/**
//	 * Adds a copy of the specified <tt>ImageAttribute</tt> to this component's attributes
//	 * and returns the added <tt>ImageAttribute</tt>. If an <tt>ImageAttribute</tt>
//     * corresponding to <tt>name</tt> already exists, the properties of the existing
//     * <tt>ImageAttribute</tt> are modified, rather than adding a new one. This
//     * keeps Hibernate happy.
//     * @param name the name of the attribute to add.
//     * @param ia the image attribute to add.
//     * @return the image attribute that was added or modified.
//     * @throws CapselaException if this component already has a static attribute
//     *                          with the given name and the given attribute is
//     *                          dynamic.
//	 */
//	protected ImageAttribute addImageAttribute( String name, ImageAttribute ia )
//	throws CapselaException {
//		ImageAttribute result = addImageAttribute( name, ia.getImageIcon() );
//		return result;
//	}//addImageAttribute()
//	/**
//	 * Adds a new <tt>ImageAttribute</tt> (initialized according to the parameters) to
//	 * this component's attributes and returns the added <tt>ImageAttribute</tt>. If a
//	 * <tt>ImageAttribute</tt> corresponding to <tt>name</tt> already exists, the
//	 * properties of the existing <tt>ImageAttribute</tt> are modified, rather than adding
//	 * a new one. This keeps Hibernate happy.
//     * @param name the name of the attribute to add.
//     * @param image the image to use for the image attribute.
//     * @return the image attribute that was added or modified.
//     * @throws CapselaException if this component already has a static attribute
//     *                          with the given name and the given attribute is
//     *                          dynamic.
//	 */
//	protected ImageAttribute addImageAttribute( String name, ImageIcon image) throws CapselaException {
//		// If the attribute is already in the Map, modify the existing instance. Otherwise, create
//		// a new instance. This tells Hibernate whether it needs to update or insert the attribute.
//		ImageAttribute imageAttribute = (ImageAttribute) getComponentAttribute( name );
//		if(imageAttribute != null) {
//			imageAttribute.setImageIcon(image);
//		}//if
//		else {
//			imageAttribute = new ImageAttribute( this, name, image );
//		}//else
//		addComponentAttribute( imageAttribute );
//		return imageAttribute;
//	}//addImageAttribute()
//	/**
//	 * Adds a copy of the specified <tt>SqlQueryAttribute</tt> to this
//     * component's attributes and returns the added <tt>SqlQueryAttribute</tt>.
//     * If a <tt>SqlQueryAttribute</tt> corresponding to <tt>name</tt> already
//     * exists, the properties of the existing <tt>SqlQueryAttribute</tt> are
//     * modified, rather than adding a new one. This keeps Hibernate happy.
//     * @param name the name of the SQL query attribute to add.
//     * @param sqa the SQL query attribute to add.
//     * @return the SQL query attribute that was added.
//	 * @throws CapselaException if the attribute added is dynamic but a static
//     *                          attribute with the same name already exists
//     *                          (shouldn't ever happen).
//	 */
//	protected SqlQueryAttribute addSqlQueryAttribute( String name, SqlQueryAttribute sqa ) throws CapselaException {
//		SqlQueryAttribute result = addSqlQueryAttribute( name, sqa.getUsername(), sqa.getPassword(), sqa.getUrl(), sqa.getDriver(), sqa.getQuery() );
//		return result;
//	}//addSqlQueryAttribute()
//	/**
//	 * Adds a new <tt>SqlQueryAttribute</tt> (initialized according to the
//	 * parameters) to this component's attributes and returns the added
//	 * <tt>SqlQueryAttribute</tt>. If a <tt>SqlQueryAttribute</tt> corresponding
//	 * to <tt>name</tt> already exists, the properties of the existing
//	 * <tt>SqlQueryAttribute</tt> are modified, rather than adding a new one.
//     * @param name the name of the SQL query attribute to add.
//     * @param userName the username used for querying the SQL database.
//     * @param password the password used for querying the SQL database.
//     * @param url the JDBC connection URL.
//     * @param driver the SQL JDBC driver class name.
//     * @param query the actual query command for SQL.
//     * @return the SQL query attribute that was added.
//	 * @throws CapselaException if the attribute added is dynamic but a static
//     *                          attribute with the same name already exists
//     *                          (shouldn't ever happen).
//	 */
//	protected SqlQueryAttribute addSqlQueryAttribute( String name, String userName, 
//		String password, String url, String driver, String query ) throws CapselaException {
//		// If the attribute is already in the Map, modify the existing instance. Otherwise, create
//		// a new instance. This tells Hibernate whether it needs to update or insert the attribute.
//		SqlQueryAttribute sqlQueryAttribute = (SqlQueryAttribute) getComponentAttribute( name );
//		if( sqlQueryAttribute != null ) {
//			sqlQueryAttribute.setUsername( userName );
//			sqlQueryAttribute.setPassword( password );
//			sqlQueryAttribute.setUrl( url );
//			sqlQueryAttribute.setDriver( driver );
//			sqlQueryAttribute.setQuery( query );
//		}//if
//		else {
//			sqlQueryAttribute = new SqlQueryAttribute( this, name, userName, 
//		password, url, driver, query );
//		}//else
//		addComponentAttribute( sqlQueryAttribute );
//		return sqlQueryAttribute;
//	}//addSqlQueryAttribute()
//	/**
//	 * Adds a copy of the specified <tt>ScalarAttribute</tt> to this component's 
//	 * attributes and returns the added <tt>ScalarAttribute</tt>. If a 
//	 * <tt>ScalarAttribute</tt> corresponding to <tt>name</tt> already exists,
//	 * the properties of the existing <tt>ScalarAttribute</tt> are modified, 
//	 * rather than adding a new one.
//     * @param name the name of the attribute to add.
//     * @param sa the scalar attribute to add.
//     * @return the scalar attribute that was added or modified.
//	 * @throws CapselaException if this component already has a static attribute
//     *                          with the given name and the given attribute is
//     *                          dynamic.
//	 */
//	protected ScalarAttribute addScalarAttribute( String name, ScalarAttribute sa ) throws CapselaException {
//		ScalarAttribute result = addScalarAttribute( name, sa.getValue() );
//		return result;
//	}//addScalarAttribute()
//	/**
//	 * Adds a new <tt>ScalarAttribute</tt> (initialized according to the parameters) to
//	 * this component's attributes and returns the added <tt>ScalarAttribute</tt>. If a
//	 * <tt>ScalarAttribute</tt> corresponding to <tt>name</tt> already exists, the
//	 * properties of the existing <tt>ScalarAttribute</tt> are modified, rather than adding
//	 * a new one. This keeps Hibernate happy.
//     * @param name the name of the attribute to add.
//     * @param value the value to give the scalar attribute.
//     * @return the scalar attribute that was added or modified.
//	 * @throws CapselaException if this component already has a static attribute
//     *                          with the given name and the given attribute is
//     *                          dynamic.
//	 */
//	public ScalarAttribute addScalarAttribute( String name, Object value ) throws CapselaException {
//		// If the attribute is already in the Map, modify the existing instance. 
//		// Otherwise, create a new instance. This tells Hibernate whether it needs 
//		// to update or insert the attribute.
//		ScalarAttribute att = (ScalarAttribute) getComponentAttribute( name );
//		if( att != null ) {
//			att.setValue( value );
//		}//if
//		else {
//			att = new ScalarAttribute( this, name, value );
//		}//else
//		addComponentAttribute( att );
//		return att;
//	}//addScalarAttribute()
//	/**
//	 * Adds a new <tt>ComponentAttribute</tt> with the given value. If an
//     * attribute with the given name already exists an exception is thrown.
//     * @param name the name of the attribute to add.
//     * @param value the value to give the new attribute.
//     * @return the attribute that was added.
//     * @throws CapselaException if this component already has an attribute with
//     *                          the given name.
//	 */
//	public ComponentAttribute addComponentAttribute( String name, ComponentAttribute value ) throws CapselaException {
//		// If the attribute is already in the Map, modify the existing instance. 
//		// Otherwise, create a new instance. This tells Hibernate whether it needs 
//		// to update or insert the attribute.
//		ComponentAttribute att = (ComponentAttribute) getComponentAttribute( name );
//		if( att != null ) {
//			throw new CapselaException( "Attribute with the name: " + name + " already exists!" );
//		}//if
//		else {
//			if( value != null ) {
//				boolean equal = ( name == null ? value.getName() == null : name.equals( value.getName() ) );
//				if( ! equal ) {
//					value.setName( name );
//				}//if
//			}//if
//			addComponentAttribute( value );
//		}//else
//		return value;
//	}//addComponentAttribute()
//	
//	/** The name of the <tt>source edges</tt> attribute of this component. */
//	static public final String ATTR_SOURCE_EDGES = "sourceEdges";
//	/** The set of source <tt>ComponentEdge</tt>s for this component. */
//	protected Set<ComponentEdge> _sourceEdges;
//	/**
//	 * Gets the <code>Set</code> of source <code>ComponentEdge<code>s.
//	 * @return  the <code>Set</code> of source <code>ComponentEdge<code>s
//	 * @hibernate.set role="sourceEdges" cascade="all-delete-orphan" 
//	 *   lazy="true" inverse="true"
//	 * @hibernate.collection-key column="SOURCE_COMPONENT_ID"
//	 * TODO getting net.sf.hibernate.UnresolvableObjectException when loading flow
//	 *      after deleting component from it.
//	 * @hibernate.collection-cache usage="transactional"
//	 * @hibernate.collection-one-to-many 
//	 *   class="com.ichg.capsela.domain.component.ComponentEdge"
//	 * @javabean.property displayName="Source Edges" hidden="true"
//	 */
//	public Set<ComponentEdge> getSourceEdges() {
//		return _sourceEdges;
//	}//getSourceEdges()
//	/**
//	 * @param edges
//	 */
//	public void setSourceEdges( Set<ComponentEdge> edges ) {
////TODO		assert edges != null : "edges != null";
//		_sourceEdges = edges;
//		updateAttribute( ATTR_SOURCE_EDGES, _sourceEdges );
//	}//setSourceEdges()
//	/**
//     * Adds a "source edge" where this component is the source and the given
//     * component is the sink. Note that source edges are not sources for this
//     * component, but are edges where this component is the source!<br>
//     * If this component already has its maximum number of source control edges
//     * or the sink component already has its maximum number of sink control
//     * edges than no edge is created and <tt>null</tt> is returned.
//	 * @param sinkComponent the sink component for the created edge.
//	 * @return the newly created control edge or <tt>null</tt>.
//	 */
//	public ControlEdge addSourceControlEdge( Component sinkComponent ) {
//		ControlEdge controlEdge = null;
//		/* Only add the edge if this component hasn't reached its maximum number
//		 * of source control edges and the sink component hasn't reached its
//		 * maximum number of sink control edges.
//		 */
//		if( this.getSourceControlEdges().size() < this.getSourceEdgeRange().getMax() &&
//				sinkComponent.getSinkControlEdges().size() < sinkComponent.getSinkEdgeRange().getMax() ) {
//			controlEdge = new ControlEdge( this, sinkComponent );
//			_sourceEdges.add( controlEdge );
//			sinkComponent.addSinkEdge( controlEdge );
//			updateAttribute( ATTR_SOURCE_EDGES, _sourceEdges );
//		}//if
//		return controlEdge;
//	}//addSourceEdge()
//	/**
//     * Creates a new "source edge" where this component is the source and the
//     * given component is the sink. Note that source edges are not sources for
//     * this component, but are edges where this component is the source!
//	 * @param sinkComponent the sink component for the created edge.
//	 * @return the newly created data edge.
//	 */
//	public DataEdge addSourceDataEdge( Component sinkComponent ) {
//		DataEdge dataEdge = new DataEdge( this, sinkComponent );
//		_sourceEdges.add( dataEdge );
//		sinkComponent.addSinkEdge( dataEdge );
//		updateAttribute( ATTR_SOURCE_EDGES, _sourceEdges );
//		return dataEdge;
//	}//addSourceEdge()
//	/**
//	 * @param edge
//	 */
//	public void removeSourceEdge( ComponentEdge edge ) {
//		assert edge != null : "edge != null";
//		assert edge.containedOnce( _sourceEdges ) : "edge.containedOnce( _sourceEdges )";
//		boolean removed = _sourceEdges.remove( edge );
//		assert removed == true : "removed == true";
//		if( edge.getSinkComponent().isSinkEdge( edge ) ) {
//			edge.getSinkComponent().removeSinkEdge( edge );
//		}//if
//		updateAttribute( ATTR_SOURCE_EDGES, _sourceEdges );
//	}//addSourceEdge()
//	/**
//     * Returns whether the given edge is a "source edge" for this component,
//     * which means this component is the source for the given edge and not that
//     * the edge is a source for this component!
//	 * @param edge the edge to check.
//	 * @return whether the given edge is a "source edge" for this component.
//	 */
//	public boolean isSourceEdge( ComponentEdge edge ) {
//		return edge.containedOnce( _sourceEdges );
//	}//isSourceEdge()
//	/**
//     * Gets the set of all control edges going from this component to another
//     * component.
//     * @return the set of all control edges from this component.
//	 * @javabean.property displayName="Source Control Edges" hidden="true"
//	 */
//	public Set<ControlEdge> getSourceControlEdges() {
//		Set<ControlEdge> controlEdges = new HashSet<ControlEdge>( _sourceEdges.size() );
//		for( Iterator<ComponentEdge> i = _sourceEdges.iterator(); i.hasNext(); ) {
//			ComponentEdge edge = i.next();
//			if( edge instanceof ControlEdge ) controlEdges.add( (ControlEdge) edge );
//		}//for
//		return controlEdges;
//	}//getSourceControlEdges()
//	/**
//     * Gets the set of all data edges going from this component to another
//     * component.
//     * @return the set of all data edges from this component.
//	 * @javabean.property displayName="Source Data Edges" hidden="true"
//	 */
//	public Set getSourceDataEdges() {
//		Set<DataEdge> dataEdges = new HashSet<DataEdge>( _sourceEdges.size() );
//		for( Iterator<ComponentEdge> i = _sourceEdges.iterator(); i.hasNext(); ) {
//			ComponentEdge edge = i.next();
//			if( edge instanceof DataEdge ) dataEdges.add( (DataEdge) edge );
//		}//for
//		return dataEdges;
//	}//getSourceDataEdges()
//	/**
//     * Gets the edge connecting this component with the given component such
//     * that this component is the source for the edge and the given component
//     * is the sink.
//     * @param sink the sink component of the edge to be returned.
//	 * @return the appropriate "source edge" connecting this component and the
//     *         given component.
//	 */
//	public ComponentEdge getSourceEdgeWith( Component sink ) {
//		for(Iterator i = getSourceEdges().iterator(); i.hasNext(); ) {
//			ComponentEdge edge = (ComponentEdge)i.next();
//			if( edge.getSinkComponent().equals( sink ) ) {
//				return edge;
//			}//if
//		}//for
//		return null;
//	}//getSourceEdgeWith()
//	
//	/** The name of the <tt>sink edges</tt> attribute of this component. */
//	static public final String ATTR_SINK_EDGES = "sinkEdges";
//	/** The set of sink <tt>ComponentEdge</tt>s for this component. */
//	protected Set<ComponentEdge> _sinkEdges;
//	/**
//	 * Gets the <code>Set</code> of sink <code>ComponentEdge<code>s.
//	 * @return  the <code>Set</code> of sink <code>ComponentEdge<code>s
//	 * @hibernate.set role="sinkEdge" cascade="all-delete-orphan"
//	 *   lazy="true" inverse="true"
//	 * @hibernate.collection-key column="SINK_COMPONENT_ID"
//	 * TODO getting net.sf.hibernate.UnresolvableObjectException when loading flow
//	 *      after deleting component from it.
//	 * @hibernate.collection-cache usage="transactional"
//	 * @hibernate.collection-one-to-many 
//	 *   class="com.ichg.capsela.domain.component.ComponentEdge"
//	 * @javabean.property displayName="Sink Edges" hidden="true"
//	 */
//	public Set<ComponentEdge> getSinkEdges() {
//		return _sinkEdges;
//	}//getSinkEdges()
//	/**
//	 * Sets the <code>Set</code> of sink <code>ComponentEdge<code>s, needed for Hibernate.
//	 * @param edges  the new <code>Set</code> of sink <code>ComponentEdge<code>s
//	 */
//	public void setSinkEdges( Set<ComponentEdge> edges ) {
////		assert edges != null : "edges != null";
//		_sinkEdges = edges;
//		updateAttribute( ATTR_SINK_EDGES, _sinkEdges );
//	}//setSinkEdges()
//	/**
//     * Adds a "sink edge" to the component; note that this means that this
//     * component is the sink of the edge, NOT that the edge is a sink for this
//     * component!
//	 * @param edge the edge to add to this component.
//	 */
//	protected void addSinkEdge( ComponentEdge edge ) {
//		assert edge != null : "edge != null";
//		edge.addToCollection( _sinkEdges );
//		edge.setSinkComponent( this );
//		updateAttribute( ATTR_SINK_EDGES, _sinkEdges );
//	}//addSinkEdge()
//	/**
//     * Removes a "sink edge" from this component; note that this means that this
//     * component is the sink of the edge, NOT that the edge is a sink for this
//     * component!
//	 * @param edge the edge to remove from this component.
//	 */
//	protected void removeSinkEdge( ComponentEdge edge ) {
//		assert edge != null : "edge != null";
//		assert edge.containedOnce( _sinkEdges ) : "edge.containedOnce( _sinkEdges )";
//		boolean removed = _sinkEdges.remove( edge );
//		assert removed == true : "removed == true";
//		if( edge.getSourceComponent().isSourceEdge( edge ) ) {
//			edge.getSourceComponent().removeSourceEdge( edge );
//		}//if
//		updateAttribute( ATTR_SINK_EDGES, _sinkEdges );
//	}//addSinkEdge()
//	/**
//     * Returns whether or not this component is the sink of the given edge.
//	 * @param edge the edge to check.
//	 * @return whether this component is the sink of the given edge.
//	 */
//	public boolean isSinkEdge( ComponentEdge edge ) {
//		return edge.containedOnce( _sinkEdges );
//	}//isSinkEdge()
//	/**
//     * Gets the set of all control edges for which this component is the sink.
//     * @return the set of control edges for which this component is the sink.
//	 * @javabean.property displayName="Sink Control Edges" hidden="true"
//	 */
//	public Set getSinkControlEdges() {
//		Set<ControlEdge> controlEdges = new HashSet<ControlEdge>( _sinkEdges.size() );
//		for( Iterator<ComponentEdge> i = _sinkEdges.iterator(); i.hasNext(); ) {
//			ComponentEdge edge = i.next();
//			if( edge instanceof ControlEdge ) controlEdges.add( (ControlEdge) edge );
//		}//for
//		return controlEdges;
//	}//getSinkControlEdges()
//	/**
//     * Gets the set of all data edges for which this component is the sink.
//     * @return the set of data edges for which this component is the sink.
//	 * @javabean.property displayName="Sink Data Edges" hidden="true"
//	 */
//	public Set<DataEdge> getSinkDataEdges() {
//		Set<DataEdge> dataEdges = new HashSet<DataEdge>( _sourceEdges.size() );
//		for( Iterator<ComponentEdge> i = _sinkEdges.iterator(); i.hasNext(); ) {
//			ComponentEdge edge = i.next();
//			if( edge instanceof DataEdge ) dataEdges.add( (DataEdge) edge );
//		}//for
//		return dataEdges;
//	}//getSourceDataEdges()
//	/**
//	 * Gets the edge connecting this component with the given component such
//     * that this component is the sink for the edge and the given component
//     * is the source.
//     * @param source the source component of the edge to be returned.
//     * @return the appropriate "sink edge" connecting this component and the
//     *         given component.
//	 */
//	public ComponentEdge getSinkEdgeWith( Component source ) {
//		for(Iterator i = this.getSinkEdges().iterator(); i.hasNext(); ) {
//			ComponentEdge edge = (ComponentEdge)i.next();
//			if( edge.getSourceComponent().equals( source ) ) {
//				return edge;
//			}//if
//		}//for
//		return null;
//	}//getSinkEdgeWith()
//	
//	/** The name of the <tt>flow location</tt> attribute of this component. */
//	public final static String ATTR_FLOW_LOCATION = "flowLocation";
//	/** The location of the component on the flow */
//	private Rectangle _flowLocation;
//	/**
//	 * Gets the location of the component on the flow.
//	 * @return the location of the component on the flow.
//	 * @hibernate.property column="COMPONENT_FLOW_LOCATION" not-null="false"
//	 * @javabean.property displayName="Flow Location" hidden="true"
//	 */
//	public Rectangle getFlowLocation() {
//		return _flowLocation;
//	}//getFlowLocation()
//	/**
//	 * Sets the location of the component on the flow.
//	 * @param flowLocation the location on the flow where the component should be
//	 */
//	public void setFlowLocation( Rectangle flowLocation ) {
//		updateAttribute( ATTR_FLOW_LOCATION, flowLocation );
//		_flowLocation = flowLocation;
//	}//setFlowLocation()
//	
//	/** The name of the <tt>target engine name</tt> attribute of this component. */
//	public final static String ATTR_TARGET_ENGINE_NAME = "targetEngineName";
//	/** The name of the target engine on which this component should be run. */
//	private String _targetEngineName = null;
//	/**
//	 * Gets the name of the target engine on which this component should be run.
//	 * @return  the target engine name
//	 * @hibernate.property column="COMPONENT_ENGINE_NAME" length="50" not-null="false"
//	 * @javabean.property displayName="Target Engine Name" hidden="false"
//	 */
//	public String getTargetEngineName() {
//		return _targetEngineName;
//	}//getTargetEngineName()
//	/**
//	 * Sets the target engine name.
//	 * @param targetEngineName  the new target engine name
//	 */
//	public void setTargetEngineName( String targetEngineName ) {
//		updateAttribute( ATTR_TARGET_ENGINE_NAME, targetEngineName );
//		_targetEngineName = targetEngineName;
//	}//setTargetEngineName()
//	
//	/** The name of the <tt>execution status</tt> attribute of this component. */
//	static public final String ATTR_EXECUTION_STATUS = "executionStatus";
//	/** The <tt>ExecutionStatus</tt> of this component. */
//	private ExecutionStatus _executionStatus;
//	/**
//	 * Gets the execution status of the Component.
//	 * @return  the execution status of the Component
//	 * @hibernate.component class="com.ichg.capsela.domain.engine.ExecutionStatus"
//	 * @javabean.property displayName="Execution Status" hidden="true"
//	 */
//	public ExecutionStatus getExecutionStatus() {
//		return _executionStatus;
//	}//getProcessStatus()
//	/**
//	 * Sets the execution status of the Component.
//	 * @param executionStatus  the new execution status
//	 */
//	public void setExecutionStatus( ExecutionStatus executionStatus ) {
//		_executionStatus = executionStatus;
//		_executionStatus.setParent( this, ATTR_EXECUTION_STATUS );
//		updateAttribute( ATTR_EXECUTION_STATUS, executionStatus );
//	}//setExecutionStatus()
//
//	/**
//	 * Returns true if the component has an error, false otherwise. This is
//     * useful for building flows. Also added by request from bug #955
//     * @return whether this component has errored.
//	 * @javabean.property displayName="Error Boolean" hidden="false"
//	 */
//	public boolean getErrorBoolean() {
//		return _executionStatus.isError();
//	}
//	
//    /**
//     * Empty; here for the purposes of JavaBean contract specification.
//     * @param b unused
//     */
//	public void setErrorBoolean(boolean b) {
//		// Empty, here for the purposes of JavaBean contract specification
//	}
//	
	/** The name of the <tt>start time</tt> attribute of this component. */
	static public final String ATTR_START_TIME = "startTime";
	/** The time when the component started execution. */
	private long _startTime;
	/**
	 * Gets the time when the component started execution.
	 * @return  The time when the component started execution.
	 * @hibernate.property column="COMPONENT_START_TIME" not-null="true"
	 * @javabean.property displayName="Start Time" hidden="false"
	 */
	public long getStartTime() {
		return _startTime;
	}//getStartTime()
	/**
	 * Sets the time when the component started execution.
	 * @param  startTime  The time when the component started execution.
	 */
	public void setStartTime( long startTime ) {
//		updateAttribute( ATTR_START_TIME, startTime );
		_startTime = startTime;
	}//setStartTime()
	
	/** The name of the <tt>execution time</tt> attribute of this component. */
	static public final String ATTR_EXECUTION_TIME = "executionTime";
	/** The execution time of this component. */
	private long _executionTime;
	/**
	 * Gets the duration of the component's execution.
	 * @return  The duration of the component's execution.
	 * @hibernate.property column="COMPONENT_EXECUTION_TIME" not-null="true"
	 * @javabean.property displayName="Execution Time" hidden="false"
	 */
	public long getExecutionTime() {
		return _executionTime;
	}//getExecutionTime()
	/**
	 * Sets the duration of the component's execution.
	 * @param  executionTime  The duration of the component's execution.
	 */
	public void setExecutionTime( long executionTime ) {
//		updateAttribute( ATTR_EXECUTION_TIME, executionTime );
		_executionTime = executionTime;
	}//setExecutionTime()
	
	/** The name of the <tt>instance</tt> attribute of this component. */
	static public final String ATTR_INSTANCE = "instance";
	/**
	 * Whether this component is an instance in capsela. An instance is
	 * created each time a component is run and stores the results of that run.
	 */
	private boolean _instance;
	/**
	 * Determines if this component is an instance. An instance is created each
	 * time a component is run and stores the results of that run.
	 * @see #_instance
	 * @return  true if this component is an instance, false otherwise
	 * @hibernate.property column="COMPONENT_IS_INSTANCE" not-null="true"
	 * @javabean.property displayName="Is Instance" hidden="true"
	 */
	public boolean isInstance() {
		return _instance;
	}//isInstance()
	/**
	 * Sets whether this component is an instance.
	 * @see #_instance
	 * @param isInstance 
	 */
	public void setInstance( boolean isInstance ) {
//		updateAttribute( ATTR_INSTANCE, isInstance );
		_instance = isInstance;
	}//setInstance()
	
	/** The name of the <tt>commented</tt> attribute of this component. */
	static public final String ATTR_COMMENTED = "commented";
	/**
	 * Whether this component has been commented out. Commented instances do not
	 * get executed during the normal course of flow execution.
	 */
	private Boolean _commented;
	/**
	 * Returns <tt>true</tt> if this component has been commented out.
	 * @return <tt>true</tt> if this component has been commented out.
	 * @hibernate.property column="COMPONENT_IS_COMMENTED"
	 * @javabean.property displayName="Is Commented Out" hidden="false"
	 */
	public Boolean isCommented() {
		return _commented;
	}//isCommented()
	/**
	 * Sets whether or not this component is commented out.
	 * @param commented Whether or not this component is commented out.
	 */
	public void setCommented( Boolean commented ) {
//		updateAttribute( ATTR_COMMENTED, commented );
		_commented = commented;
	}//setCommented()
	
	/** The name of the <tt>pauseOnRun</tt> attribute of this component. */
	static public final String ATTR_PAUSE_ON_RUN = "pauseOnRun";
	/**
	 * Whether this component needs to pause before running, in order to
	 * allow the user to interact with or modify it.
	 */
	private Boolean _pauseOnRun;
	/**
	 * Returns <tt>true</tt> if this component needs to pause before running.
	 * @return <tt>true</tt> if this component needs to pause before running.
	 * @hibernate.property column="COMPONENT_PAUSE_ON_RUN"
	 * @javabean.property displayName="Pauses When Run" hidden="false"
	 */
	public Boolean isPauseOnRun() {
		return _pauseOnRun;
	}//isPauseOnRun()
	/**
	 * Sets whether or not this component pauses immediately prior to running.
	 * @param pauseOnRun Whether or not this component pauses immediately prior to running.
	 */
	public void setPauseOnRun( Boolean pauseOnRun ) {
//		updateAttribute( ATTR_PAUSE_ON_RUN, pauseOnRun );
		_pauseOnRun = pauseOnRun;
	}//setPauseOnRun()
	/**
	 * Returns the text of the JMS message which this component is to wait for if
	 * {@link #isPauseOnRun()} returns <tt>true</tt>.
	 * @return The text of the JMS message which this component is to wait for if it needs to wait.
	 */
//	public String getPauseString() {
//		return this.className() + "|@|" + this.getId();
//	}
	
	/** The name of the <tt>caller email</tt> attribute of this component. */
	public final static String ATTR_CALLER_EMAIL = "callerEmail";
	/** The email address for whoever started this flow by email. */
	private String _callerEmail = null;
	/**
	 * The email address of whoever called or ran this <tt>Component</tt>.
	 * This attribute is populated automatically by the server.
	 * @return the email address of whoever ran this component.
	 * @hibernate.property column="COMPONENT_CALLER_EMAIL" length="128" not-null="false"
	 * @javabean.property displayName="Caller Email" hidden="false"
	 */
	public String getCallerEmail() {
		return _callerEmail;
	}//getCallerEmail()
	/**
     * Sets the email address of whoever called or ran this <tt>Component</tt>.
	 * @param callerEmail the email address of whoever tan this component.
	 */
	public void setCallerEmail( String callerEmail ) {
//		updateAttribute( ATTR_CALLER_EMAIL, callerEmail );
		_callerEmail = callerEmail;
	}//setCallerEmail()
//	
//	/** The name of the <tt>instances</tt> attribute of this component. */
//	static public final String ATTR_INSTANCES = "instances";
//	/**
//	 * <p>The instances of this component. An instance is created whenever
//	 * <code>ClientSession.createInstance(Component)</code> is called, and it
//	 * is that instance which actually runs in the engine.</p>
//	 * <p>
//	 * NOTE: This collection is never initialized for performance reasons.
//	 * </p>
//	 */
//	protected Set _instances;
//	/**
//	 * <p>
//	 * Gets the instances of this <code>Component</code>. An instance is created 
//	 * whenever <code>ClientSession.createInstance(Component)</code> is called,
//	 * and it is that instance which actually runs in the engine.
//	 * </p><p>
//	 * NOTE: This collection is never initialized for performance reasons.
//	 * </p>
//	 * @return  the instances of this component
//	 * @hibernate.set role="instances" cascade="all-delete-orphan" lazy="true" inverse="true" order-by="COMPONENT_START_TIME"
//	 * @hibernate.collection-key column="COMPONENT_TEMPLATE_ID"
//	 * hibernate.collection-cache usage="transactional"  // MODIFIED BY DEAN, removed Instance caching
//	 * @hibernate.collection-one-to-many class="com.ichg.capsela.domain.component.Component"
//	 * @javabean.property displayName="Instances" hidden="true" readOnly="true"
//	 */
//	public Set getInstances() {
//		return _instances;
//	}//getInstances()
//	/**
//	 * TODO: make this protected again once XDoclet handles readOnly correctly again (should be in 1.3).
//	 * See http://opensource.atlassian.com/projects/xdoclet/browse/XDT-1461 for more information.
//	 * -- Dani
//	 * 
//	 * @param instances  the instances to set.
//	 */
//	public void setInstances( Set instances ) {
//		assert instances != null : "instances != null";
//		_instances = instances;
//	}//setInstances()
//	
//	/** The name of the <tt>template</tt> attribute of this component. */
//	public final static String ATTR_TEMPLATE = "template";
//    /**
//     * The template for this component. Components are either instances or
//     * templates, so when an instance is created the template is set as the
//     * component that the instance was created from.
//     */
//	protected Component _template;
//	/**
//     * @return the template this component was made from.
//	 * @hibernate.many-to-one foreign-key="FKCOMPONENT_TEMPLATE" outer-join="false"
//	 *   class="com.ichg.capsela.domain.component.Component" cascade="none"
//	 * @hibernate.column name="COMPONENT_TEMPLATE_ID" index="IXCOMPONENT_TEMPLATE" 
//	 * @javabean.property displayName="Template" hidden="true"
//	 */
//	public Component getTemplate() {
//		return _template;
//	}//getTemplate()
//	/**
//     * Sets the template that this component was made from. Only makes sense
//     * for an instance.
//	 * @param template the template component that this instance was made from.
//	 */
//	public void setTemplate( Component template ) {
//		updateAttribute( ATTR_TEMPLATE, template );
//		_template = template;
//		if( template != null ) {
//			setTemplateID( new Long( template.getId() ) );
//			if( ! PersistenceManager.isPersisting() ) {
//				// then add this component to the parent folder's component collection.
//				// The instances collection is not initialized for performance reasons.
//				// However, the called method takes initialization into account.
////				addIfNotContained( getTemplate().getInstances() );  TODO what was the purpose of this?
//			}//if 
//		}//if 
//	}//setTemplate()
//
//    /** The name of the <tt>template ID</tt> attribute of a component. */
//	static public final String ATTR_TEMPLATE_ID = "templateID";
//    /**
//     * The ID of this component's template.
//     * @see #_template
//     */
//	private Long _templateID;
//	/**
//	 * A convenience method that returns the ID of this component's template.
//	 * @return the template component's ID.
//	 * @hibernate.property column="COMPONENT_TEMPLATE_ID" insert="false" update="false"
//	 * @javabean.property displayName="Template ID" hidden="true" readOnly="true"
//	 */
//	public Long getTemplateID() {
//		return _templateID;
//	}//getTemplateID()
//	/**
//	 * TODO: make this protected again once XDoclet handles readOnly correctly again (should be in 1.3).
//	 * See http://opensource.atlassian.com/projects/xdoclet/browse/XDT-1461 for more information.
//	 * -- Dani
//	 * 
//     * Stores the ID of this component's template. Does not alter the template
//     * component, instead this is used to store inside this component the ID
//     * of the template component.
//	 * @param templateID the ID of the template component.
//	 */
//	public void setTemplateID( Long templateID ) {
//		updateAttribute( ATTR_TEMPLATE_ID, templateID );
//		_templateID = templateID;
//	}//setTemplateID()
//	
//	/** The name of the <tt>alias components</tt> attribute of this component. */
//	static public final String ATTR_ALIAS_COMPONENTS = "aliasComponents";
//	/** The aliases of this component. */
//	protected Set _aliasComponents;
//	/**
//	 * Gets the aliases of this component.
//	 * @return  the aliases of this component
//	 * @hibernate.set role="aliasComponents" cascade="save-update" lazy="true" inverse="true"
//	 * @hibernate.collection-key column="ALIAS_COMPONENT_ID"
//	 * @hibernate.collection-one-to-many class="com.ichg.capsela.domain.component.AliasComponent"
//	 * @javabean.property displayName="Alias Components" hidden="true"
//	 */
//	public Set getAliasComponents() {
//		return _aliasComponents;
//	}//getAliasComponents()
//	/**
//	 * @param aliasComponents
//	 */
//	public void setAliasComponents( Set aliasComponents ) {
//		assert aliasComponents != null : "aliasComponents != null";
//		_aliasComponents = aliasComponents;
//		updateAttribute(ATTR_ALIAS_COMPONENTS, _aliasComponents);
//	}//setAliasComponents()
//	
//	/** The name of the <tt>execution events</tt> attribute of this component. */
//	static public final String ATTR_EXECUTION_EVENTS = "executionEvents";
//	/** The <tt>ExecutionEvents</tt> of this component. */
//	protected Set _executionEvents;
//	/**
//	 * Gets the ExecutionEvents of this Component.
//	 * @return  the aliases of this component
//	 * @hibernate.set role="executionEvents" cascade="none" lazy="true" inverse="true"
//	 * @hibernate.collection-key column="TARGET_COMPONENT_ID"
//	 * hibernate.collection-cache usage="transactional"
//	 * @hibernate.collection-one-to-many class="com.ichg.capsela.domain.event.ExecutionEvent"
//	 * @javabean.property displayName="Execution Events" hidden="true"
//	 */
//	public Set getExecutionEvents() {
//		return _executionEvents;
//	}//getExecutionEvents()
//	/**
//	 * @param executionEvents
//	 */
//	public void setExecutionEvents( Set executionEvents ) {
//		assert executionEvents != null : "executionEvents != null";
//		_executionEvents = executionEvents;
//	}//setExecutionEvents()
//
//	/**
//	 * This function should only be used by the class
//	 * com.ichg.capsela.domain.util.XmlSerialization
//	 *
//	 * @see com.ichg.capsela.framework.domain.PersistentDomainObject#prepareForXmlExport()
//	 */
//	public void prepareForXmlExport() {
//
//		if( LOG.isDebugEnabled() ) LOG.debug( "Component.prepareForXmlExport id=" + getIdentifier() );
//
//		_instances = new HashSet();
//		_executionEvents = new HashSet();
//		_aliasComponents = new HashSet();
//
//		if( _sinkEdges != null && Helper.isInitialized(_sinkEdges)) {
//			Set<ComponentEdge> sinkEdges = new HashSet<ComponentEdge>();
//			sinkEdges.addAll( _sinkEdges );
//			_sinkEdges = sinkEdges;
//		} else {
//			_sinkEdges = new HashSet<ComponentEdge>();
//		}
//
//		if( _sourceEdges != null && Helper.isInitialized(_sourceEdges) ) {
//			Set<ComponentEdge> sourceEdges = new HashSet<ComponentEdge>();
//			sourceEdges.addAll( _sourceEdges );
//			_sourceEdges = sourceEdges;
//		} else {
//			_sourceEdges = new HashSet<ComponentEdge>();
//		}
//
//		if( _attributes != null && Helper.isInitialized(_attributes) ) {
//			Map<String, ComponentAttribute> attributes = new HashMap<String, ComponentAttribute>();
//			attributes.putAll( _attributes );
//			_attributes = attributes;
//		} else {
//			_attributes = new HashMap<String, ComponentAttribute>();
//		}
//
//		setId( Identifier.INVALID_ID );
//
//		if( LOG.isDebugEnabled() )
//			LOG.debug( "Component.prepareForXmlExport id=" + getIdentifier()
//					+ ";instances=" + _instances.size()
//					+ ";sourceEdges=" + _sourceEdges.size()
//					+ ";sinkEdges=" + _sinkEdges.size()
//					+ ";attribs=" + _attributes.size()
//					+ ";aliases=" + _aliasComponents.size()
//					+ ";executionEvents=" + _executionEvents.size() );
//
//	}//prepareForXmlExport()
//
//	/**
//	 * Deletes this component.
//	 * @throws CapselaException if there is an error updating the hibernate
//	 *                          database.
//	 * @see PersistentDomainObject#delete()
//	 */
//	public void delete() throws CapselaException {
//
//		// Hibernate doesn't maintain the cached collections; this means that if you
//		// delete a component, for example, then the component's folder's cached component collection
//		// does not get updated and you eventually get an UnresolvableObjectException...
//		// See http://www.hibernate.org/117.html#A33 for the official answer.
//
//		LOG.debug( "delete " + this );
//
//		deleteEdges();
//
//		FolderComponent f = (FolderComponent) getFolder();
//		if( f != null ) {
//			f.getComponents().remove( this );
//			PersistenceManager.instance().update( f );
//		}
//
//		super.delete();
//
//	}//delete()
//    
//    /**
//     * Removes all source and sink edges from the component, effectively
//     * isolating it in the flow so that it can be moved or deleted safely.
//     * If any of the edges are still connected to another component as well
//     * as to this component a warning will be logged and the edges will be
//     * removed from those components; those components are returned in a set
//     * in order to lock on them before commiting the update.
//     * @return the set of components that were still connected to the edges
//     *         connected to this component.
//     */
//    public Set<Component> deleteEdges()
//    {
//        // log (warn) if edges aren't taken care of for other components
//        boolean warn = false;
//        Set<Component> retval = new HashSet<Component>();
//        
//        // NOTE: don't use an iterator so we don't get ConcurrentModificationExceptions
//        ComponentEdge[] sinkEdges = (ComponentEdge[]) this.getSinkEdges().toArray( new ComponentEdge[ 0 ] );
//		for( int i = 0; i < sinkEdges.length; i++ ) {
//            if( sinkEdges[i].getSourceComponent().isSourceEdge( sinkEdges[i] ) )
//            {
//                /* if the edge is still attached to another component,
//                 * take care of it, but warn the user.
//                 */
//                warn = true;
//                retval.add( sinkEdges[i].getSourceComponent() );
//                sinkEdges[i].getSourceComponent().removeSourceEdge( sinkEdges[i] );
//            }
//			removeSinkEdge( sinkEdges[ i ] );
//		}
//
//		// NOTE: don't use an iterator so we don't get ConcurrentModificationExceptions
//		ComponentEdge[] sourceEdges = (ComponentEdge[]) this.getSourceEdges().toArray( new ComponentEdge[ 0 ] );
//		for( int i = 0; i < sourceEdges.length; i++ ) {
//            if( sourceEdges[i].getSinkComponent().isSinkEdge( sourceEdges[i] ) )
//            {
//                /* if the edge is still attached to another component,
//                 * take care of it, but warn the user.
//                 */
//                warn = true;
//                retval.add( sourceEdges[i].getSinkComponent() );
//                sourceEdges[i].getSinkComponent().removeSinkEdge( sourceEdges[i] );
//            }
//			removeSourceEdge( sourceEdges[ i ] );
//		}
//        
//        if( warn )
//            LOG.warn( "Edges were still attached to other components" );
//        
//        return retval;
//    }

}//Component
