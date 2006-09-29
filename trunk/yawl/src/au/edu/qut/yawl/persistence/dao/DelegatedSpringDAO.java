/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.engine.EngineFactory;

public class DelegatedSpringDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedSpringDAO.class );
	
	public DelegatedSpringDAO() {

		ApplicationContext context = EngineFactory.getApplicationContext(); 
		if (context!=null) {
			addType( YSpecification.class, (DAO) context.getBean("SpecSpringDAO") );
			addType( YNetRunner.class, (DAO) context.getBean("RunnerSpringDAO") );
			addType( Problem.class, (DAO) context.getBean("ProblemSpringDAO") );
			addType( YWorkItem.class, (DAO) context.getBean("WorkItemSpringDAO") );
			addType( YIdentifier.class, (DAO) context.getBean("IdentifierSpringDAO") );
			addType( YAWLServiceReference.class, (DAO) context.getBean("ServiceRefSpringDAO") );
		}
	}
	
	public List getChildren( Object object ) {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}


}
