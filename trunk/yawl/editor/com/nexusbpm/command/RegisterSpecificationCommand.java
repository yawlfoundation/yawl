package com.nexusbpm.command;

import operation.VisitSpecificationOperation.Visitor;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

public class RegisterSpecificationCommand {


	
	class VisitorImpl implements Visitor {
    	DataProxy top;
    	public VisitorImpl(DataProxy top) {
    		this.top = top;
    	}
    	
        public void visit(Object parent, Object child, String childLabel) {
        	DataContext context = top.getContext();
        	DataProxy proxy = context.getDataProxy(child, null);
        	proxy.setLabel(childLabel);
        	top.getContext().getHierarchy().put(context.getDataProxy(parent, null), proxy);
        }
        }



}
