package au.edu.qut.yawl.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.ValidationEventCollector;

import au.edu.qut.yawl.elements.SpecificationSet;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
//import au.edu.qut.yawl.jaxb.ControlTypeCodeType;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class StringProducerJaxb extends StringProducerXML {

	private static StringProducerXML INSTANCE = new StringProducerJaxb();
	
	public static StringProducerXML getInstance() {return INSTANCE;}
	
	private JAXBContext getJAXBContext() throws JAXBException{
		return JAXBContext.newInstance(SpecificationSet.class,
				YSpecification.class, YMetaData.class,
				YAWLServiceGateway.class, YNet.class, YDecomposition.class,
				YParameter.class, YVariable.class, YNetElement.class,
				YExternalNetElement.class, YTask.class, YAtomicTask.class,
				YCompositeTask.class, YMultiInstanceAttributes.class,
//				ControlTypeCodeType.class, 
				YInputCondition.class,
				YOutputCondition.class, YCondition.class, YFlow.class);
	}
	
	private SpecificationSet getSpecificationSet(String fileName, boolean isXmlFileInPackage) {
		Unmarshaller u = null;
		SpecificationSet set = null;
		try {
			JAXBContext jc = getJAXBContext();
			u = jc.createUnmarshaller();
			JAXBIDResolver resolver = new JAXBIDResolver();
			u.setProperty(IDResolver.class.getName(), resolver);
			u.setListener(resolver.createListener());

			ValidationEventCollector collector = new ValidationEventCollector();
			u.setEventHandler(collector);
			File file = getTranslatedFile(fileName, isXmlFileInPackage);
			set = (SpecificationSet) u.unmarshal(file);
			
			Object o = resolver.resolve("name", YExternalNetElement.class).call();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public YSpecification getSpecification(String fileName, boolean isXmlFileInPackage) {
		return getSpecificationSet(fileName, isXmlFileInPackage).getItems().get(0);
	}

	private Marshaller getMarshaller() throws Exception{
		Marshaller m = getJAXBContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
			public String[] getPreDeclaredNamespaceUris() {
				return new String[] { "http://www.yawl.fit.qut.edu.au/",
						"http://www.w3.org/2001/XMLSchema-instance",
						"http://www.w3.org/2001/XMLSchema" };
			}

			public String getPreferredPrefix(String namespaceUri,
					String suggestion, boolean requirePrefix) {
				if ("http://www.w3.org/2001/XMLSchema".equals(namespaceUri))
					return "xs";
				if ("http://www.w3.org/2001/XMLSchema-instance"
						.equals(namespaceUri))
					return "xsi";
				if ("http://www.yawl.fit.qut.edu.au/".equals(namespaceUri))
					return "yawl";
				return suggestion;
			}
		};
		m.setProperty("com.sun.xml.bind.namespacePrefixMapper", mapper);
		return m;		
	}
	
	public String getXMLString(String fileName, boolean isXmlFileInPackage) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Unmarshaller u = null;
		String retval = "";
		try {
			SpecificationSet set = getSpecificationSet(fileName, isXmlFileInPackage);
			getMarshaller().marshal(set, new OutputStreamWriter(baos));
			retval = baos.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}
}
