package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.engine.EngineSpecificationValidator;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Michael Adams
 * @date 26/07/12
 */
public class FileOperations {

    private enum Action { Open, OpenFile, Validate, Analyse, Save, SaveAs, Close, Exit }

    private static ExecutorService _executor;
    private static YAWLEditor _editor;
    private static SpecificationFileHandler _handler;
    private static Publisher _publisher;


    static {
         int POOL_SIZE = Runtime.getRuntime().availableProcessors();
        _executor = Executors.newFixedThreadPool(POOL_SIZE);
        _editor = YAWLEditor.getInstance();
        _handler = SpecificationFileHandler.getInstance();
        _publisher = Publisher.getInstance();
    }


    public static void open()  { run(Action.Open); }

    public static void open(String fileName)  { run(Action.OpenFile, fileName); }

    public static void validate()  { run(Action.Validate); }

    public static void analyse()  { run(Action.Analyse); }

    public static void save() { run(Action.Save); }

    public static void saveAs()  { run(Action.SaveAs); }

    public static void close()  { run(Action.Close); }

    public static void exit()  { run(Action.Exit); }


    private static void run(final Action action, final String... args) {
        _executor.execute(new Runnable() {
            public void run() {
                processAction(action, args);
            }
        });
    }


    private static void processAction(Action action, String... args) {
        Cursor oldCursor = _editor.getCursor();
        _editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        _publisher.publishFileBusyEvent();

        switch (action) {
            case Close: {
                _handler.processCloseRequest();
                break;
            }
            case Save: {
                _handler.processSaveRequest();
                break;
            }
            case SaveAs: {
                _handler.processSaveAsRequest();
                break;
            }
            case Open: {
                _handler.processOpenRequest();
                break;
            }
            case OpenFile: {
                _handler.processOpenRequest(args[0]);
                break;
            }
            case Exit: {
                _handler.processExitRequest();
                break;
            }
            case Validate: {
                _editor.showProblemList("Specification Validation Problems",
                    EngineSpecificationValidator.getValidationResults(
                            SpecificationModel.getInstance()));
                break;
            }
            case Analyse: {
                _editor.showProblemList("Analysis Results",
                        SpecificationModel.getInstance().analyse());
                break;
            }
        }

        Publisher.getInstance().publishFileUnbusyEvent();
        YAWLEditor.getInstance().setCursor(oldCursor);
    }

}
