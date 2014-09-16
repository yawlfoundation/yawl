package org.yawlfoundation.yawl.engine;

import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 26/03/2004
 * Time: 15:54:07
 * 
 */
public class TestMarlonsEagerNessExperiment {
    private static YIdentifier _idForTopNet;
    private static YWorkItemRepository _workItemRepository;

    public static void main(String[] args) throws YSchemaBuildingException, YQueryException, YEngineStateException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException {
        YEngine _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        URL fileURL = TestMarlonsEagerNessExperiment.class.getResource("MarlonsEagerExperiment.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = YMarshal.
                        unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        YEngine engine2 = YEngine.getInstance();
        engine2.loadSpecification(specification);
        _idForTopNet = engine2.startCase(specification.getSpecificationID(), null, null, null, null, null, false);

        _workItemRepository = engine2.getWorkItemRepository();
    }
}
