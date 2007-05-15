package com.nexusbpm.services.sas;

import java.util.Collection;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sas.iom.WorkspaceFactory;
import com.sas.iom.WorkspaceFactoryException;
import com.sas.iom.SAS.ILanguageService;
import com.sas.iom.SAS.IWorkspace;
import com.sas.iom.SAS.ILanguageServicePackage.CarriageControlSeqHolder;
import com.sas.iom.SAS.ILanguageServicePackage.LineTypeSeqHolder;
import com.sas.iom.SASIOMDefs.GenericError;
import com.sas.iom.SASIOMDefs.StringSeqHolder;

/**
 * <b>Use case:</b><br>
 * <p>Sends code to a SAS mainframe to perform statistical computations. Four
 * "in" string parameters may be used.  The component saves the error and result
 * output from SAS.
 *
 * @author             Dean Mao
 * @created            February 19, 2003
 * @hibernate.subclass discriminator-value="19"
 * @javabean.class     name="SasComponent"
 *                     displayName="Sas Component"
 */
public class SasComponent {

	private static final long serialVersionUID = 2680645159636167256L;
	private static final Log LOG = LogFactory.getLog( SasComponent.class );
	private SasServiceData data;
	
	/**
	 * Creates a new empty <code>SasComponent</code>, needed for Hibernate.
	 */
	public SasComponent() {
		super();
	}//SasComponent()

	public SasComponent(SasServiceData data) {
		this.data = data;
	}

	/**
	 * Executes the SAS component.
	 * @see com.nexusbpm.services.component.Component#run()
	 */
	public void run() throws Exception {

		String host = "165.2.187.115";
		String port = "5307";

		Properties serverProperties = new Properties();
		serverProperties.put( "host", host );
		serverProperties.put( "port", port );
		serverProperties.put( "protocol", "bridge" );
//		serverProperties.put( "userName", this.getUsername() );
//		serverProperties.put( "password", this.getPassword() );

		String code = "";//getCode();
		if( LOG.isTraceEnabled() ) LOG.trace( "code: \n" + code );

		WorkspaceFactory factory = new WorkspaceFactory();
		IWorkspace workspace = factory.createWorkspaceByServer( serverProperties );
		ILanguageService sasLanguage = workspace.LanguageService();

		try {
			sasLanguage.Submit( code );

			// Get the log.
			CarriageControlSeqHolder logCarriageControlHldr = new CarriageControlSeqHolder();
			LineTypeSeqHolder logLineTypeHldr = new LineTypeSeqHolder();
			StringSeqHolder logHldr = new StringSeqHolder();
			sasLanguage.FlushLogLines( Integer.MAX_VALUE, logCarriageControlHldr, logLineTypeHldr, logHldr );
			String[] logLines = logHldr.value;
			StringBuffer logBuffer = new StringBuffer();
			for( int i = 0; i < logLines.length; i++ ) {
				logBuffer.append( logLines[ i ] ).append( '\n' );
			}
//			this.setLog( logBuffer.toString() );
			if( LOG.isTraceEnabled() ) LOG.trace( "sas log: " + logBuffer.toString() );

			// Get the result.
			CarriageControlSeqHolder listCarriageControlHldr = new CarriageControlSeqHolder();
			LineTypeSeqHolder listLineTypeHldr = new LineTypeSeqHolder();
			StringSeqHolder listHldr = new StringSeqHolder();
			sasLanguage.FlushListLines( Integer.MAX_VALUE, listCarriageControlHldr, listLineTypeHldr, listHldr );
			String[] listLines = listHldr.value;
			StringBuffer resultBuffer = new StringBuffer();
			for( int i = 0; i < listLines.length; i++ ) {
				resultBuffer.append( listLines[ i ] ).append( '\n' );
			}
			String resultString = resultBuffer.toString();
			resultString = resultString.replaceAll( "\\<thead\\>", "" );
			resultString = resultString.replaceAll( "\\</thead\\>", "" );
			resultString = resultString.replaceAll( "\\<tbody\\>", "" );
			resultString = resultString.replaceAll( "\\</tbody\\>", "" );
//			this.setResult( resultString );
			if( LOG.isTraceEnabled() ) LOG.trace( "sas result: " + resultString );

			// Determine whether or not an error occurred.
//			if( this.getLog().indexOf( "\nERROR" ) != -1 ) {
//				data.setReturnedErrorBoolean( true );
//			}//if
//			else {
//				data.setReturnedErrorBoolean( false );
//			}//else

//			if( LOG.isTraceEnabled() ) LOG.trace( "SAS work with name: " + this.getName() + " completed successfully" );

		}//try
		catch( WorkspaceFactoryException e ) {
//			LOG.info( "SAS job '" + this.getName() + "' did not complete due to error: " + e.getLocalizedMessage() );
			throw new Exception( e );
		}//catch
		catch( GenericError e ) {
//			LOG.info( "SAS job '" + this.getName() + "' did not complete due to error: " + e.getLocalizedMessage() );
			throw new Exception( e );
		}//catch
		finally {
			factory.shutdown();
		}//finally
	}//run()

}//SasComponent
