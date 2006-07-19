/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.HashMap;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The RegisterSpecificationCommand registers a loaded specification
 * with a data context.
 * 
 * @author Matthew Sandoz
 *
 */
public class RegisterSpecificationCommand extends AbstractCommand{

	private EditorDataProxy parent;
	private YSpecification spec;
    Map<Object, DataProxy> map = new HashMap<Object, DataProxy>();
	
	public RegisterSpecificationCommand(EditorDataProxy parent, YSpecification spec) {
		this.spec = spec;
		this.parent = parent;
	}
	
	@Override
	protected void attach() throws Exception {
        VisitSpecificationOperation.visitSpecification(spec, new AttachVisitorImpl(parent));
	}

	@Override
	protected void detach() throws Exception {
        VisitSpecificationOperation.visitSpecification(spec, new DetachVisitorImpl(parent));
	}

	@Override
	protected void perform() throws Exception {
        VisitSpecificationOperation.visitSpecification(spec, new CreateVisitorImpl(parent));
	}

    class CreateVisitorImpl implements Visitor {
        DataProxy top;

        public CreateVisitorImpl(DataProxy top) {
            this.top = top;
        }

        public void visit(Object child, String childLabel) {
            DataContext context = top.getContext();
            DataProxy childProxy = context.createProxy(child, null);
            childProxy.setLabel(childLabel);
            RegisterSpecificationCommand.this.map.put(child, childProxy);
        }
    }
    class AttachVisitorImpl implements Visitor {
		DataProxy top;

		public AttachVisitorImpl(DataProxy top) {
			this.top = top;
		}

		public void visit(Object child, String childLabel) {
			DataContext context = top.getContext();
            DataProxy childProxy = RegisterSpecificationCommand.this.map.get(child);
//			DataProxy childProxy = context.getDataProxy(child, null);
			context.attachProxy(childProxy, child);
		}
	}
	class DetachVisitorImpl implements Visitor {
		DataProxy top;

		public DetachVisitorImpl(DataProxy top) {
			this.top = top;
		}

		public void visit(Object child, String childLabel) {
			DataContext context = top.getContext();
			DataProxy childProxy = context.getDataProxy(child, null);
			context.detachProxy(childProxy);
		}
	}
	class PrintVisitorImpl implements Visitor {
		DataProxy top;

		public PrintVisitorImpl(DataProxy top) {
			this.top = top;
		}

		public void visit(Object child, String childLabel) {
			System.out.println("visiting " + childLabel + "(" + child.getClass().getName() + ")");
		}
	}
}
