package au.edu.qut.yawl.util;

import java.io.StringReader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;

@Entity
public class MultiInstanceDataList  {

	private Long id;
	
	private int position = 0;
	private int maxposition = 0;
	

	
	private Set<MultiInstanceData> data = new HashSet<MultiInstanceData>();
	//List<Element> elements = new LinkedList<Element>();
	
	public MultiInstanceDataList() {
		
	}

	
	@Transient
	public Element get(YIdentifier id) {
		try {

			Iterator<MultiInstanceData> it = data.iterator();
			while (it.hasNext()) {
				MultiInstanceData dataelem = it.next();
				if (dataelem.getIdentifierlink().equals(id.toString())) {

					String element = dataelem.getData();
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(new StringReader(element));
					Element inputData = doc.getRootElement();
					inputData.detach();			

					return inputData;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Transient
	public void put(YIdentifier id, Element elem) {
		Document doc = new Document();
		doc.setRootElement(elem);		
		String s = new XMLOutputter().outputString(doc);
		MultiInstanceData dataitem = new MultiInstanceData(s);
		dataitem.setIdentifierlink(id.toString());
		data.add(dataitem);


	}

	@OneToMany(cascade=CascadeType.ALL)
	public Set<MultiInstanceData> getData() {
		return data;
	}

	public void setData(Set<MultiInstanceData> data) {
		this.data = data;
	}

	@Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Basic
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	public int getMaxposition() {
		return maxposition;
	}

	public void setMaxposition(int maxposition) {
		this.maxposition = maxposition;
	}

	@Basic
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Transient
	public int size() {
		return data.size();
	}

}
