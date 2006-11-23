package com.nexusbpm.editor.configuration;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetDialog;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;

public class TestPropertySheets {

	public static Properties p = new Properties();

	public static void main(String[] args) {
		try {
			p.load(ClassLoader.getSystemResourceAsStream("testresources/quartz.server.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertySheetTableModel m = new PropertySheetTableModel() ;
		Property[] ps = new Property[p.size()];
		int i = 0;
		Enumeration e = p.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			String value = p.getProperty(key);
			AbstractProperty pr = new StringProperty(key.substring(0, key.indexOf(".")), key, value);
			ps[i++] = pr;
		}
		m.setProperties(ps);
		PropertySheetTable t = new PropertySheetTable(m);
		m.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				System.out.println(e.toString());
			}
		});
		t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		PropertySheetPanel panel = new PropertySheetPanel(t);
		PropertySheetDialog dialog = new PropertySheetDialog();
		dialog.add(panel);
		dialog.setTitle("Edit configuration");
		dialog.pack();
		dialog.setModal(true);
		boolean b = dialog.ask();
		if (b) {
			p.list(System.out);
		} else {
			System.out.println("dont save"); 
		}
		
		System.out.println(b);
		dialog.dispose();
	}
	static class StringProperty extends AbstractProperty {

		private String name;
		private String value;
		private String category;
		
		public StringProperty(String category, String name, String value) {
			this.name = name;
			this.category = category;
			this.value = value;
		}

		public void firePropertyChange(Object a, Object b) {
			super.firePropertyChange(a, b);
		}
		
		public void setValue(Object value) {
			this.value = value.toString();
		}
		
		public Object getValue() {
			return value;
		}
		
		public String getCategory() {
			return category;
		}

		public String getDisplayName() {
			return name;
		}

		public String getName() {
			return name;
		}

		public String getShortDescription() {
			return name;
		}

		public Class getType() {
			return String.class;
		}

		public boolean isEditable() {
			return true;
		}

		public void readFromObject(Object arg0) {
			System.out.println("tried to read from " + arg0);
		}

		public void writeToObject(Object arg0) {
			System.out.println("tried to write to " + arg0);
		}
	}
}

