package au.edu.qut.yawl.persistence.engine;

import java.io.File;
import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.persistence.TestHibernateMarshal;
import au.edu.qut.yawl.unmarshal.YMarshal;


public class TestEngineRestore extends TestCase {
	    
	public TestEngineRestore( String arg0 ) {
		super( arg0 );
		// TODO Auto-generated constructor stub
	}
	
//	public void testWriteFile() {
//		Set<String> filenames = new HashSet<String>();
//		filenames.add("DeadlockingSpecification.xml");
//		
//		for( String filename : filenames ) {
//			try {
//				File file = new File( TestEngineRestore.class.getResource( filename ).getFile() );
//				List specifications = YMarshal.unmarshalSpecifications( file.getAbsolutePath() );
//				YSpecification inputSpec = (YSpecification) specifications.iterator().next();
//				String xml = YMarshal.marshal( inputSpec );
//
//				File outputFile = new File( "7.1-" + filename );
//				FileWriter out = new FileWriter( outputFile );
//				StringReader reader = new StringReader( xml );
//				int c;
//				while( ( c = reader.read() ) != -1 )
//					out.write( c );
//				out.close();
//			}
//			catch( Exception e ) {
//				e.printStackTrace();
//			}
//		}
//	}
	
//	public void testSetup() throws Exception {
//		Dao dao = DaoFactory.createHibernateDao(new YSpecification("test"));
//	}
//	
	public void testRun() throws Exception {
        URL fileURL = TestHibernateMarshal.class.getResource("TestSpecMinimal.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = (YSpecification) YMarshal.unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        YEngine engine =  EngineFactory.createYEngine();
        EngineClearer.clear(engine);
        engine.loadSpecification(specification);
        YIdentifier identifier = engine.startCase(specification.getID().toString(), null, null);
        Set currWorkItems = YWorkItemRepository.getInstance().getEnabledWorkItems();
        System.out.println("blah");
	}
//	
//	public void testUpdate() throws Exception {
//		throw new Exception("unimplemented test");
//	}
//	
//	public void testDelete() throws Exception {
//		YSpecification spec = new YSpecification("test");
//		YNet net = new YNet("abc", spec);
//		YNetRunner runner = new YNetRunner(net, null);
//		Dao dao = DaoFactory.createHibernateDao(runner);
//		dao.startTransaction();
//		dao.create(runner);
//		dao.commitTransaction();
//		dao.delete(runner);
//	}
//	
//	public void testCreate() throws Exception {
//		YSpecification spec = new YSpecification("test");
//		YNet net = new YNet("abc", spec);
//		YNetRunner runner = new YNetRunner(net, null);
//		Dao dao = DaoFactory.createHibernateDao(runner);
//		dao.create(runner);
//	}
//	
//	public void testTransaction() throws Exception {
//		YSpecification spec = new YSpecification("test");
//		YNet net = new YNet("abc", spec);
//		YNetRunner runner = new YNetRunner(net, null);
//		Dao dao = DaoFactory.createHibernateDao(runner);
//		dao.startTransaction();
//		dao.create(runner);
//		dao.commitTransaction();
//	}
//	
//	public void testRollbackTransaction() throws Exception {
//		YSpecification spec = new YSpecification("test");
//		YNet net = new YNet("abc", spec);
//		YNetRunner runner = new YNetRunner(net, null);
//		Dao dao = DaoFactory.createHibernateDao(runner);
//		dao.startTransaction();
//		dao.create(runner);
//		dao.rollbackTransaction();
//	}

}
