package au.edu.qut.yawl.elements;

import java.util.ArrayList;
import java.util.List;

public class SpecificationSet {
	private List<YSpecification> items = new ArrayList<YSpecification>();
    protected String version;
    public List<YSpecification> getItems() {
		return items;
	}
    public String getVersion() {
		return version;
	}
}
