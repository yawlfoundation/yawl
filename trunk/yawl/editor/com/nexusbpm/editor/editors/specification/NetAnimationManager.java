package com.nexusbpm.editor.editors.specification;

import java.util.Date;

public class NetAnimationManager {

//	public 
//	
//	public void addListener(NetAnimationListener listener) {
//		
//	}

	private Thread thread = new Thread() {
		@Override
		public void run() {
			while (true) {
//				System.out.println("writing");
//				Object[] cells = (Object[]) NetGraph.this.getGraphLayoutCache().getCells(false, true, false, false);
//				for (Object cell: cells) {
//					((NetCell) cell).getAttributes().put(NetCell.LABEL, new Date().toString());
//					AnimationUpdateEvent event = new AnimationUpdateEvent((NetCell) cell);
//					NetGraph.this.cellChanged(event);
//				}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}
		}
	};
	

	
}
