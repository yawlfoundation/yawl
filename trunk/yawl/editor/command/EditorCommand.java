package command;

import java.net.URI;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;

public class EditorCommand {
//id like to move some of the utility of these to the yspec, util context or dao classes
	private static final Log LOG = LogFactory.getLog(EditorCommand.class);
		
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
	
	public static void executeCopyCommand(SharedNode source, SharedNode target) {
		DataContext targetDataContext = target.getProxy().getContext();
		YSpecification spec = (YSpecification) source.getProxy().getData();
		try {
			YSpecification newSpec = (YSpecification) spec.clone();
			newSpec.setDbID(null);
			String dataroot = target.getProxy().getData().toString();
			URI desturi = joinUris(new URI(dataroot), new URI(spec.getID()));
			newSpec.setID(desturi.toASCIIString());
			DataProxy dp = targetDataContext.getDataProxy(newSpec, null);
			targetDataContext.put(dp);
			LOG.info("Copying specification " + spec.getID() + " to " + newSpec.getID());
		} catch (Exception e) {
			LOG.error("Error copying specification " + spec.getID(), e);
		}
	}
}