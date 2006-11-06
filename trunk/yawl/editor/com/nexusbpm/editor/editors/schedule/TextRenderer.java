/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class TextRenderer {

	public abstract String render(Object toRender);
	
	public static TextRenderer getHourlyInstance() { 
		return new TextRenderer() {
			public String render(Object toRender) {
				SimpleDateFormat sdf = new SimpleDateFormat("hh a");
				return sdf.format((Date) toRender);
			}
		};
	}
	
	public static TextRenderer getMinutelyInstance() { 
		return new TextRenderer() {
			public String render(Object toRender) {
				SimpleDateFormat sdf = new SimpleDateFormat("mm 'minutes'");
				return sdf.format((Date) toRender);
			}
		};
	}
	
	public static TextRenderer getDailyInstance() { 
		return new TextRenderer() {
			public String render(Object toRender) {
				String retval = "";
				if (toRender.equals("L")) {
					retval = "Last day of the month";
				} else if (toRender.equals("LW")) {
					retval = "Last weekday of the month";
				} else {
					 retval = "month day " + toRender.toString();
				}
				return retval;
			}
		};
	}
	
	
}
