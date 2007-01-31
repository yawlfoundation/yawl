package com.nexusbpm.editor.util;

abstract public class AbstractPopulatedRunnable implements Runnable {

	protected Object[] objects;
	
	public AbstractPopulatedRunnable(Object... objects) {
		this.objects = objects;	
	}
	
	abstract public void run();
}
