/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
                handler.openFile();
                break;
            }
            case OpenFile: {
                handler.openFile(args[0]);
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
                handler.saveFile();
                break;
            }
            case SaveAs: {
                handler.saveFileAs();
                break;
            }
            case Close: {
                handler.closeFile();
                break;
            }
            case Exit: {
                if (handler.closeFileOnExit()) {
                    System.exit(0);
                }
                break;
            }
        }

        publisher.publishFileUnbusyEvent();
        editor.setCursor(oldCursor);
    }

}
