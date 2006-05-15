/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.forms;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.schema.ElementCreationInstruction;
import au.edu.qut.yawl.schema.ElementReuseInstruction;
import au.edu.qut.yawl.schema.Instruction;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.worklist.model.*;
import org.jdom.JDOMException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Creates schemas and "empty" instances based upon the schemas using sets of instructions
 * sent to the YAWL engine to generate forms for both launching cases and editing work items.
 * Empty instances are put together by the instantiation of InstanceBuilder.
 * @author Guy Redding 29/10/2004
 */
public class SchemaCreator {

    private static boolean debug = false;

    private static String schemaName = null;
    private static String schemaPath = null;
    private static String formName = null;
    private static String instanceName = null;
    private static String instancePath = null;
    private static String workItemID = null;
    private static String sessionHandle = null;
    private static String rootTagName = null;
    private static String specID = null;
    private static BufferedWriter bw = null;


    /**
     * Empty Constructor
     */
    public SchemaCreator() {
    }


    /**
     * Creates a schema to launch a case.
     * @param _specID the specid.
     * @param _sessionHandle
     * @param _worklistController
     * @param context
     * @throws IOException
     * @throws JDOMException
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     * @throws URISyntaxException
     */
    public static void createCaseSchema(String _specID, String _sessionHandle,
                                        WorklistController _worklistController, File context)
            throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException, URISyntaxException {

        sessionHandle = _sessionHandle;
        specID = _specID;

        SpecificationData specData = _worklistController.getSpecificationData(_specID, _sessionHandle);
        List inputParams = specData.getInputParams();
        String data = specData.getAsXML();

        List instructions = Collections.synchronizedList(new ArrayList());

        for (int i = 0; i < inputParams.size(); i++) {
            YParameter inputParam = (YParameter) inputParams.get(i);

            if (debug) {
                System.out.println("CASE input param datatypename: " + inputParam.getDataTypeName());
                System.out.println("CASE input param datatypenamespace: " + inputParam.getDataTypeNameSpace());
                System.out.println("CASE input param direction: " + inputParam.getDirection());
                System.out.println("CASE input param documentation: " + inputParam.getDocumentation());
                System.out.println("CASE input param elementname: " + inputParam.getElementName());
                System.out.println("CASE input param initial value: " + inputParam.getInitialValue());
                System.out.println("CASE input param name: " + inputParam.getName());
                System.out.println("CASE input param isuntyped: " + inputParam.isUntyped());
            }

            if (null != inputParam.getElementName()) {
                if (debug) System.out.println("CASE input param REUSE element name: " + inputParam.getElementName());
                String elementName = inputParam.getElementName();
                ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);
                instructions.add(instruction);
            } else if (null != inputParam.getDataTypeName()) {
                if (debug) System.out.println("CASE input param CREATION data type name: " + inputParam.getDataTypeName());
                String elementName = inputParam.getName();
                String typeName = inputParam.getDataTypeName();
                boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                ElementCreationInstruction instruction = new ElementCreationInstruction(
                        elementName,
                        typeName,
                        isPrimitiveType);
                instructions.add(instruction);
            }
            // currently we convert untyped parameters into creation parameters,
            // due to a bug in YAWL
            else if (inputParam.isUntyped()) {
                //System.out.println("input param isUntyped.");
                //UntypedElementInstruction instruction = new UntypedElementInstruction();
                //instructions.add(instruction);

                if (debug) System.out.println("CASE input param CREATION (untyped) data type name: " + inputParam.getDataTypeName());
                String elementName = inputParam.getName();
                //String typeName = inputParam.getDataTypeName();
                String typeName = "boolean";
                boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                ElementCreationInstruction instruction = new ElementCreationInstruction(
                        elementName,
                        typeName,
                        isPrimitiveType);
                instructions.add(instruction);
            }
        }

        XMLToolsForYAWL xmlToolsForYawl = new XMLToolsForYAWL();

        String schemaLibrary = specData.getSchemaLibrary();

        xmlToolsForYawl.setPrimarySchema(schemaLibrary);

// now needs the root element name
        rootTagName = specData.getRootNetID();

        String myNewSchema = xmlToolsForYawl.createYAWLSchema((Instruction[]) instructions.toArray(new Instruction[instructions.size()]), rootTagName);

        // the new schema file is sent to InstanceBuilder to make a new
        // instance from this schema
        String specid = new String(specData.getID());
        specid = specid.substring(0, specid.length() - 4);

        formName = new String(specid + ".xhtml");

        // make a new file and print schema output to the file
        StringBuffer tempSpecid = new StringBuffer(specid);
        // append '0's to specid if its less than 3 characters.
        // due to requirements of the filename suffix in File.createTempFile()
        while (tempSpecid.length() < 3) {
            tempSpecid.append('0');
        }

        File f = File.createTempFile(tempSpecid.toString(), ".xsd", context);

        schemaName = new String(f.getName());
        schemaPath = new String(f.getAbsolutePath());

        if (debug) {
            System.out.println("CSC File getPath(): " + f.getPath());
            System.out.println("CSC File getAbsolutePath(): " + f.getAbsolutePath());
            System.out.println("CSC File getName(): " + f.getName());
        }

        fileSetup(f);
        writeFile(myNewSchema);
        f = null;

//		make a new file and add the instance string to it, if not null
        f = File.createTempFile(tempSpecid.toString(), ".xml", context);
        instanceName = new String(f.getName());
        instancePath = new String(f.getAbsolutePath());

        //f = new File(context+File.separator+instanceName);
        fileSetup(f);
        writeFile(data);
        f = null;

        if (debug) {
            System.out.println("CSC SCHEMANAME == " + schemaName);
            System.out.println("CSC SCHEMAPATH == " + schemaPath);
            System.out.println("CSC INSTANCENAME == " + instanceName);
            System.out.println("CSC INSTANCEPATH == " + instancePath);
            System.out.println("CSC FORMNAME == " + formName);
        }

        // now build the instance from the YAWL schema
        InstanceBuilder ib = new InstanceBuilder(schemaName, schemaPath, instanceName, instancePath, _sessionHandle, rootTagName);
    }

    /**
     * Builds a schema from the XSD component found in YAWL that will be the foundation
     * for building a form for editing work items.  This class is currently very similar to
     * CaseSchemaCreator.class.  These two classes should eventually become one class.
     * @param _workItemID The work item to build a schema for.
     * @param _sessionHandle The session handle for the current YAWL session.
     * @param worklistController An instance of a work list controller.
     * @throws IOException
     * @throws JDOMException
     * @throws YSchemaBuildingException
     * @throws YSyntaxException
     */
    public static void createSchema(String _workItemID, String _sessionHandle,
                                    WorklistController worklistController, File context)
            throws IOException, JDOMException, YSchemaBuildingException, YSyntaxException {

        // set global variables
        workItemID = _workItemID;
        sessionHandle = _sessionHandle;

        WorkItemRecord item = worklistController.getRemotelyCachedWorkItem(_workItemID);

        if (item != null) {
            //first of all get the task information which contains the parameter signatures.
            TaskInformation taskInfo = worklistController.getTaskInformation(
                    item.getSpecificationID(),
                    item.getTaskID(),
                    _sessionHandle);

            String specID = taskInfo.getSpecificationID();

            //next get specification data which will contain the input schema library
            //that we are going to use to build the schema that we want for this task.
            SpecificationData specData = worklistController.getSpecificationData(specID, _sessionHandle);

//now we get the parameters signature for the task
            YParametersSchema paramsSignature = taskInfo.getParamSchema();

//now for each input param build an instruction
            List inputParams = paramsSignature.getInputParams();

            List instructions = Collections.synchronizedList(new ArrayList());

// this is an XML string containing the instance data for the current task
// this can be the instance file, and if identical elements are found in
// the schema during instance creation, the creation of new elements in
// the instance will be ignored.
            String data = item.getDataListString();

            for (int i = 0; i < inputParams.size(); i++) {
                YParameter inputParam = (YParameter) inputParams.get(i);

                if (debug) {
                    System.out.println("input param datatypename: " + inputParam.getDataTypeName());
                    System.out.println("input param datatypenamespace: " + inputParam.getDataTypeNameSpace());
                    System.out.println("input param direction: " + inputParam.getDirection());
                    System.out.println("input param documentation: " + inputParam.getDocumentation());
                    System.out.println("input param elementname: " + inputParam.getElementName());
                    System.out.println("input param initial value: " + inputParam.getInitialValue());
                    System.out.println("input param name: " + inputParam.getName());
                    System.out.println("input param isuntyped: " + inputParam.isUntyped());
                }

                if (null != inputParam.getElementName()) {
                    if (debug) {
                        System.out.println("input param REUSE element name: " + inputParam.getElementName());
                    }
                    String elementName = inputParam.getElementName();
                    ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);
                    instructions.add(instruction);
                } else if (null != inputParam.getDataTypeName()) {
                    if (debug) {
                        System.out.println("input param CREATION data type name: " + inputParam.getDataTypeName());
                    }
                    String elementName = inputParam.getName();
                    String typeName = inputParam.getDataTypeName();
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName,
                            typeName,
                            isPrimitiveType);
                    instructions.add(instruction);
                }
                // currently we convert untyped parameters into creation parameters
                // due to a bug in YAWL
                else if (inputParam.isUntyped()) {
                    //System.out.println("input param isUntyped.");
                    //UntypedElementInstruction instruction = new UntypedElementInstruction();
                    //instructions.add(instruction);

                    if (debug) {
                        System.out.println("input param CREATION (untyped) data type name: " + inputParam.getDataTypeName());
                    }
                    String elementName = inputParam.getName();
                    //String typeName = inputParam.getDataTypeName();
                    String typeName = "boolean";
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(inputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName,
                            typeName,
                            isPrimitiveType);
                    instructions.add(instruction);
                }
                if (debug) System.out.println();
            }

//for each output param build an instruction
            List outputParams = paramsSignature.getOutputParams();

            for (int i = 0; i < outputParams.size(); i++) {

                YParameter outputParam = (YParameter) outputParams.get(i);

                if (debug) {
                    System.out.println("output param datatypename: " + outputParam.getDataTypeName());
                    System.out.println("output param datatypenamespace: " + outputParam.getDataTypeNameSpace());
                    System.out.println("output param direction: " + outputParam.getDirection());
                    System.out.println("output param documentation: " + outputParam.getDocumentation());
                    System.out.println("output param elementname: " + outputParam.getElementName());
                    System.out.println("output param initial value: " + outputParam.getInitialValue());
                    System.out.println("output param name: " + outputParam.getName());
                    System.out.println("output param isuntyped: " + outputParam.isUntyped());
                }

                if (null != outputParam.getElementName()) {
                    if (debug) {
                        System.out.println("output param REUSE element name: " + outputParam.getElementName());
                    }
                    String elementName = outputParam.getElementName();
                    ElementReuseInstruction instruction = new ElementReuseInstruction(elementName);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter

                    if (instructions.contains(instruction) == true) {

                        // Matching element REUSE instruction found.
                        Instruction tempInstruction;
                        int position = instructions.indexOf(instruction);
                        tempInstruction = (Instruction) instructions.get(position);
                        if (tempInstruction.getElementName().compareTo(instruction.getElementName()) == 0) {
                            instructions.remove(position);
                        }
                    } else {
                        if (debug) {
                            System.out.println("No matching element REUSE instruction found: " + elementName);
                        }
                    }

                    instructions.add(instruction);
                } else if (null != outputParam.getDataTypeName()) {
                    if (debug) {
                        System.out.println("output param CREATION data type name: " + outputParam.getDataTypeName());
                    }
                    String elementName = outputParam.getName();
                    String typeName = outputParam.getDataTypeName();
                    boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(outputParam.getDataTypeNameSpace());
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName,
                            typeName,
                            isPrimitiveType);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter
                    Instruction[] ins = (Instruction[]) instructions.toArray(new Instruction[instructions.size()]);

                    boolean match = false;
                    for (int j = 0; j < ins.length; j++) {
                        if (ins[j].getElementName().compareTo(elementName) == 0) {
                            match = true;
                            ins[j] = instruction; // replace old instruction with this one
                        }
                    }

                    if (match == true) {
                        // convert updated array back to the instructions arraylist
                        instructions.clear();
                        for (int j = 0; j < ins.length; j++) {
                            instructions.add(ins[j]);
                        }
                    } else {
                        instructions.add(instruction);
                    }
                }
                // currently we convert untyped parameters into creation parameters
                // due to a bug in YAWL
                else if (outputParam.isUntyped()) {
                    //UntypedElementInstruction instruction = new UntypedElementInstruction();
                    //instructions.add(instruction);
                    if (debug) {
                        System.out.println("output param CREATION (untyped) data type name: " + outputParam.getDataTypeName());
                    }
                    String elementName = outputParam.getName();
                    //String typeName = outputParam.getDataTypeName();
                    String typeName = "boolean";
                    //boolean isPrimitiveType = "http://www.w3.org/2001/XMLSchema".equals(outputParam.getDataTypeNameSpace());
                    boolean isPrimitiveType = true;
                    ElementCreationInstruction instruction = new ElementCreationInstruction(
                            elementName,
                            typeName,
                            isPrimitiveType);

                    // if an instruction with the same name already exists in the instruction list
                    // remove it and add the instruction for this parameter
                    Instruction[] ins = (Instruction[]) instructions.toArray(new Instruction[instructions.size()]);
                    boolean match = false;

                    for (int j = 0; j < ins.length; j++) {
                        if (debug) System.out.println(j + ".");
                        if (ins[j].getElementName().compareTo(elementName) == 0) {
                            match = true;
                            ins[j] = instruction; // replace old instruction with this one
                        }
                    }

                    if (match == true) {
                        // convert updated array back to the instructions arraylist
                        instructions.clear();
                        for (int j = 0; j < ins.length; j++) {
                            instructions.add(ins[j]);
                        }
                    } else {
                        instructions.add(instruction);
                    }

                }

                if (debug) System.out.println();
            }

            XMLToolsForYAWL xmlToolsForYawl = new XMLToolsForYAWL();

            String schemaLibrary = specData.getSchemaLibrary();

            xmlToolsForYawl.setPrimarySchema(schemaLibrary);

// now needs the root element name
            rootTagName = taskInfo.getDecompositionID();

            String myNewSchema = xmlToolsForYawl.createYAWLSchema(
                    (Instruction[]) instructions.toArray(new Instruction[instructions.size()]),
                    rootTagName);

            // the new schema file is sent to InstanceBuilder to make a new
            // instance from this schema
            String specid = new String(item.getSpecificationID());
            specid = specid.substring(0, specid.length() - 4);

            formName = new String(specid + item.getTaskID() + ".xhtml");

            StringBuffer tempSpecid = new StringBuffer(specid);
            // append '0's to the temporary specid filename if it's less than 3 characters.
            // due to requirements of the filename suffix in File.createTempFile()
            while (tempSpecid.length() < 3) {
                tempSpecid.append('0');
            }

            File f = File.createTempFile(tempSpecid.toString(), ".xsd", context);

            schemaName = new String(f.getName());
            schemaPath = new String(f.getAbsolutePath());

            if (debug) {
                System.out.println("SC File getPath(): " + f.getPath());
                System.out.println("SC File getAbsolutePath(): " + f.getAbsolutePath());
                System.out.println("SC File getName(): " + f.getName());
                System.out.println("SC File getPath(): " + f.getPath());
                System.out.println("SC File getAbsolutePath(): " + f.getAbsolutePath());
                System.out.println("SC File getName(): " + f.getName());
            }

            fileSetup(f);
            writeFile(myNewSchema);
            f = null;

            // make a new temp file and add the instance string to it
            f = File.createTempFile(tempSpecid.toString(), ".xml", context);
            instanceName = new String(f.getName());
            instancePath = new String(f.getAbsolutePath());

            fileSetup(f);
            writeFile(data);
            f = null;

            if (debug) {
                System.out.println("SC SCHEMANAME == " + schemaName);
                System.out.println("SC SCHEMAPATH == " + schemaPath);
                System.out.println("SC INSTANCENAME == " + instanceName);
                System.out.println("SC INSTANCEPATH == " + instancePath);
                System.out.println("SC FORMNAME == " + formName);
            }

            // now build the instance from the YAWL schema
            InstanceBuilder ib = new InstanceBuilder(schemaName, schemaPath, instanceName, instancePath, _sessionHandle, rootTagName);
        }
    }


    /**
     * Set up a new file for writing to.
     * @param f Filename
     */
    private static void fileSetup(File f) {

        if (f.exists() == true) {
            if (f.delete() == true) {
                if (debug) {
                    System.out.println("File " + f.getName() + " existed -- deleted.");
                }
            }
        }

        try {
            if (f.createNewFile() == true) {
                if (debug) {
                    System.out.println("New File " + f.getName() + " created.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bw != null) {
            bw = null;
        }

        if ((bw == null) && (f.canWrite() == true)) {

            // start a new instance of the file writer ready for (over)writing items
            try {
                bw = new BufferedWriter(new FileWriter(f, false));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.out.println(f.getName() + " error. --");
        }
    }


    /**
     * Write string data to a file.
     * @param data Data to write to the file as a string.
     * @throws IOException
     */
    private static void writeFile(String data) throws IOException {

        if (bw != null) {
            bw.write(data);
            bw.newLine();
            bw.flush();
        }
        bw = null;
    }


    /**
     * @return The name given to the xform file.
     */
    public static String getFormName() {
        return formName;
    }


    /**
     * @return The name given to the XML instance file.
     */
    public static String getInstanceName() {
        return instanceName;
    }


    /**
     * @return The name given to the XSD schema file.
     */
    public static String getSchemaName() {
        return schemaName;
    }


    /**
     * @return The session handle that this xform belongs to.
     */
    // redundant?
    public static String getSessionHandle() {
        return sessionHandle;
    }


    /**
     * @return Returns the case specification ID.
     */
    // redundant?
    public static String getSpecID() {
        return specID;
    }


    /**
     * @return Returns the work item ID.
     */
    // redundant?
    public static String getWorkItemID() {
        return workItemID;
    }


    /**
     * @return The name of the root tag for the XML instance file.
     */
    public static String getRootTagName() {
        return rootTagName;
    }


    /**
     * @return Returns the schemaPath.
     */
    public static String getSchemaPath() {
        return schemaPath;
    }


    /**
     * @return Returns the instancePath.
     */
    public static String getInstancePath() {
        return instancePath;
    }


    /**
     * @param specID The specID to set.
     */
    public static void setSpecID(String _specID) {
        specID = _specID;
    }


    /**
     * @param workItemID The workItemID to set.
     */
    public static void setWorkItemID(String _workItemID) {
        workItemID = _workItemID;
    }
}