/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.configuration;

import com.l2fprod.common.propertysheet.AbstractProperty;

public class ConfigurationProperty extends AbstractProperty {

		private String name;
		private String displayName;
		private String value;
		private String category;
		private String description;
		
		public ConfigurationProperty(String category, String displayName, String name, String value, String description) {
			this.name = name;
			this.category = category;
			this.value = value;
			this.displayName = displayName;
			this.description = description;
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
			return displayName;
		}

		public String getName() {
			return name;
		}

		public String getShortDescription() {
			return description;
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
