package au.edu.qut.yawl.elements;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.jaxb.ControlTypeCodeType;
import au.edu.qut.yawl.jaxb.DecompositionType;
import au.edu.qut.yawl.jaxb.ExternalNetElementFactsType;
import au.edu.qut.yawl.jaxb.FlowsIntoType;
import au.edu.qut.yawl.jaxb.NetFactsType;
import au.edu.qut.yawl.jaxb.SpecificationSetFactsType;
import au.edu.qut.yawl.jaxb.YAWLSpecificationFactsType;
import au.edu.qut.yawl.jaxb.NetFactsType.ProcessControlElements;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class TestJAXBPersistence extends TestCase {

	private void unmarshallGeneratedClasses() {
		JAXBContext jc = null;
		Unmarshaller u = null;
		try {
			jc = JAXBContext.newInstance("au.edu.qut.yawl.jaxb");
			// System.setProperty("jaxb.debug", "true");
			u = jc.createUnmarshaller();

			JAXBIDResolver resolver = new JAXBIDResolver();
			u.setProperty(IDResolver.class.getName(), resolver);
			u.setListener(resolver.createListener());
			JAXBElement o = (JAXBElement) u.unmarshal(new File(
					"D:\\yawl\\schema\\xyz2.xml"));

			SpecificationSetFactsType ssft = (SpecificationSetFactsType) o
					.getValue();
			for (YAWLSpecificationFactsType ysft : ssft.getSpecification()) {
				for (DecompositionType dt : ysft.getDecomposition()) {
					System.out.println(dt.getId());
					if (dt instanceof NetFactsType) {
						NetFactsType nft = (NetFactsType) dt;
						ProcessControlElements pce = nft
								.getProcessControlElements();
						for (ExternalNetElementFactsType eenef : pce
								.getTaskOrCondition()) {
							System.out.println("FROM:" + eenef.getId() + ":"
									+ eenef);
							for (FlowsIntoType fit : eenef.getFlowsInto()) {
								System.out.println("  TO:"
										+ fit.getNextElementRef());
								if (fit.getNextElementRef() != null
										&& fit.getNextElementRef().getIdref() != null)
									System.out.println("ref="
											+ fit.getNextElementRef().getId()
											+ "<"
											+ fit.getNextElementRef()
													.getIdref().getClass()
													.getName() + ">");
							}
						}
					}
				}
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	private void unmarshallExistingClasses() {
		JAXBContext jc = null;
		Unmarshaller u = null;
		try {
			// System.setProperty("jaxb.debug", "true");
			jc = JAXBContext.newInstance(SpecificationSet.class
			 ,YSpecification.class
					, YMetaData.class
					, YAWLServiceGateway.class
					, YNet.class
					, YDecomposition.class
					, YParameter.class
					, YVariable.class
					, YNetElement.class
					, YExternalNetElement.class
					, YTask.class
					, YAtomicTask.class
					, YCompositeTask.class
					, YMultiInstanceAttributes.class
					, ControlTypeCodeType.class
					, YInputCondition.class
					, YOutputCondition.class
					, YCondition.class
					);
			u = jc.createUnmarshaller();

//			 MyIDResolver resolver = new MyIDResolver() ;
//			 u.setProperty(IDResolver.class.getName(),resolver);
//			 u.setListener(resolver.createListener());

			ValidationEventCollector collector = new ValidationEventCollector();
			u.setEventHandler(collector);
			SpecificationSet set = (SpecificationSet) u.unmarshal(new File(
					"D:\\yawl\\schema\\xyz2.xml"));

			Marshaller m = jc.createMarshaller();
//			SchemaFactory f = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//			javax.xml.validation.Schema s = null;
//			try {
////				s = f.newSchema(new File("D:\\java\\jaxb-ri-20060407\\bin\\YAWL_SchemaBeta8.xsd"));
//				s = f.newSchema(new File("D:\\yawl\\schema\\YAWL_SchemaBeta8.xsd"));
//			} catch (SAXException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			m.setSchema(s);
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
				public String[] getPreDeclaredNamespaceUris() {
					return new String[]{
						"http://www.citi.qut.edu.au/yawl",
						"http://www.w3.org/2001/XMLSchema-instance",
						"http://www.w3.org/2001/XMLSchema" 
					};
				}
				public String[] getContextualNamespaceDecls() {
					return new String[] {
					"yawl","http://www.citi.qut.edu.au/yawl"}; 
					}
				public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
					if( "http://www.w3.org/2001/XMLSchema".equals(namespaceUri) )
			            return "xs";
					if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) )
			            return "xsi";
					if( "http://www.citi.qut.edu.au/yawl".equals(namespaceUri) )
			            return "";
				    return suggestion;
				}
			};
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper",mapper);
			m.marshal(set, System.err);
			System.exit(0);
			ValidationEvent[] ve = collector.getEvents();
			for (int i = 0; i < ve.length; i++) {
				System.out.println(ve[i].getMessage());
			}
			YSpecification spec = set.getItems().get(0);
			System.out.println(set.getVersion());
//			System.out.println(spec.getUri());
			System.out.println("METADATA DESCRIPTION:" + spec.getMetaData()
					.getDescription());
			System.out.println("#CREATORS:" + spec.getMetaData()
					.getCreators().size());
			System.out.println("#SUBJECTS:" + spec.getMetaData()
					.getSubjects().size());
//			System.out.println(spec.getDecompositionList()
//					.size() + " decompositions:");
//			for (int j = 0; j < spec.getDecompositionList().size(); j++) {
//				YDecomposition decomposition = spec.getDecompositionList().get(j);
//				System.out.println("-->"
//						+ decomposition.toString());
//				if (decomposition.getInputParameters() != null) {
//					for (Map.Entry<String, YParameter> parm:decomposition.getInputParameters().entrySet()) {
//						System.out.println("--->" + parm.getValue().getName() + "=" + parm.getValue().getDataTypeName());
//					}
//				}
//				if (decomposition instanceof YNet) {
//					YNet ynet = (YNet) decomposition;
//					YNet.ProcessControlElements pce = ynet.getProcessControlElements();
//					if (pce.getInputCondition() != null) System.out.println("--->input condition=" + pce.getInputCondition().getID());
//					for(YExternalNetElement element:pce.getTaskOrCondition()) {
//						System.out.println("---->task " + element.getID());
//					}
//					if (pce.getOutputCondition() != null) System.out.println("--->output condition=" + pce.getOutputCondition().getID());
//				}
//			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public void testX() {
		unmarshallExistingClasses();
//		unmarshallGeneratedClasses();
	}
}
