package au.edu.qut.yawl.elements;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "http://www.citi.qut.edu.au/yawl")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecificationSetFactsType", propOrder = {
    "items"
})
public class SpecificationSet {
    @XmlElement(name="specification", namespace = "http://www.citi.qut.edu.au/yawl", required = true)
	private List<YSpecification> items = new ArrayList<YSpecification>();
    @XmlAttribute(required = true)
    protected String version;
    public List<YSpecification> getItems() {
		return items;
	}
    public String getVersion() {
		return version;
	}
}
