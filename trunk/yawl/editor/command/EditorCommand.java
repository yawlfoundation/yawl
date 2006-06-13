package command;

import java.util.Date;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;

public class EditorCommand {
	
	public static void executeCopyCommand(SharedNode source, SharedNode target) {
	//preliminaries...
		DataContext targetDataContext = target.getProxy().getContext();
		YSpecification spec = (YSpecification) source.getProxy().getData();
		try {
			YSpecification newSpec = (YSpecification) spec.clone();
			newSpec.setID(target.getProxy().getData().toString() + spec.getName());
			DataProxy dp = targetDataContext.getDataProxy(newSpec, null);
			targetDataContext.put(dp);
			System.out.println("Copying to " + newSpec.getID());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
}
