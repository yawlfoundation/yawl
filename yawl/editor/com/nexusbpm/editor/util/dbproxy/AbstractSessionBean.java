package com.nexusbpm.editor.util.dbproxy;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A generic superclass for session beans that provides default implementations
 * for all required methods (except ejbCreate() because of XDoclet).
 * @author  Felix L J Mayer
 * @version $Revision: 1.6 $
 * @created Dec 5, 2003
 * @ejb.bean generate="false"
 */
public abstract class AbstractSessionBean implements SessionBean {

	static private final Log LOG = LogFactory.getLog( AbstractSessionBean.class );

	private SessionContext _context;
	/**
	 * @return  the SessionContext of this EJB
	 */
	public SessionContext sessionContext() {
		return _context;
	}//setSessionContext()
	/**
	 * Sets the SessionContext of this EJB. This method should only be called by
	 * the container.
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext( SessionContext context ) {
		_context = context;
		LOG.trace( "setSessionContext()" );
	}//setSessionContext()

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
		// The default implementation does nothing.
	}//ejbRemove()
	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		// The default implementation does nothing.
	}//ejbActivate()
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		// The default implementation does nothing.
	}//ejbPassivate()

}//AbstractSessionBean
