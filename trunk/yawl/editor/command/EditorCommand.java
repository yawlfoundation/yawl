package command;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.TestDataContext;

import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.tree.SharedNode;

public class EditorCommand {
		
	private static URI joinUris(URI parent, URI child) {
		URI retval = null;
		String text = child.getRawPath();
		int index = text.lastIndexOf("/") + 1;
		try {
			retval = new URI(parent.getScheme(), parent.getAuthority(), parent.getPath() + "/" + URLDecoder.decode(text.substring(index), "UTF-8"), child.getQuery(), child.getFragment());
			retval = retval.normalize();
		} catch (Exception e) {e.printStackTrace();}
		return retval;
	}
	
	public static void executeCopyCommand(DataProxy source, DataProxy target) {
	//preliminaries...
		DataContext targetDataContext = target.getContext();
		YSpecification spec = (YSpecification) source.getData();
		try {
			YSpecification newSpec = (YSpecification) spec.clone();
			String dataroot = target.getData().toString();
			URI desturi = joinUris(new URI(dataroot), new URI(spec.getID()));
			newSpec.setID(desturi.toASCIIString());
			DataProxy dp = targetDataContext.getDataProxy(newSpec, null);
			targetDataContext.put(dp);
			System.out.println("spec id is " + spec.getID());
			System.out.println("Copying to " + newSpec.getID());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}