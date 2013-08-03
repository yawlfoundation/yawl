package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.engine.AnalysisResultsParser;
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


    static {
         int POOL_SIZE = Runtime.getRuntime().availableProcessors();
        _executor = Executors.newFixedThreadPool(POOL_SIZE);
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
        SpecificationFileHandler handler = new SpecificationFileHandler();
        Publisher publisher = Publisher.getInstance();
        YAWLEditor editor = YAWLEditor.getInstance();
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        publisher.publishFileBusyEvent();

        switch (action) {
            case Open: {
                handler.processOpenRequest();
                break;
            }
            case OpenFile: {
                handler.processOpenRequest(args[0]);
                break;
            }
            case Validate: {
                editor.showProblemList("Specification Validation Problems",
                    new EngineSpecificationValidator().getValidationResults());
                break;
            }
            case Analyse: {
                editor.showProblemList("Analysis Results",
                        new AnalysisResultsParser().getAnalysisResults(
                                SpecificationModel.getInstance()));
                break;
            }
            case Save: {
                handler.processSaveRequest();
                break;
            }
            case SaveAs: {
                handler.processSaveAsRequest();
                break;
            }
            case Close: {
                handler.processCloseRequest();
                break;
            }
            case Exit: {
                handler.processExitRequest();
                break;
            }
        }

        publisher.publishFileUnbusyEvent();
        editor.setCursor(oldCursor);
    }

}
