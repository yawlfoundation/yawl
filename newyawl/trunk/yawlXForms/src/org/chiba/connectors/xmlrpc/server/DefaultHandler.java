package org.chiba.connectors.xmlrpc.server;

import org.chiba.connectors.xmlrpc.DocTransformer;
import org.chiba.connectors.xmlrpc.DocTransformerException;

import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;

public class DefaultHandler {
    String FORMDIR = "../../forms";

    public DefaultHandler() {
        System.out.println("DefaultHandler: initialising");
    }

    public Hashtable loadInstance(Hashtable params) {
        System.out.println("DefaultHandler: running loadInstance");

        if (!params.containsKey("file")) {
            System.out.println("loadInstance: no parameter 'file'");
            Hashtable ret = new Hashtable();
            ret.put("status", "error");
            ret.put("error", "Parameter 'file' not passed to function");
            return ret;
        }

        String filename = (String) params.get("file");
        System.out.println("Looking for file: " + FORMDIR + "/" + filename);
        File f = new File(FORMDIR + "/" + filename);
        try {
            DocTransformer dt = new DocTransformer(f);
            System.out.println("Returning OK hash");
            return dt.getHash();
        } catch (DocTransformerException e) {
            e.printStackTrace();
            Hashtable ret = new Hashtable();
            ret.put("status", "error");
            ret.put("error", "Exception: " + e.getMessage());
            return ret;
        }
    }

    public Hashtable saveInstance(Hashtable params) {
        System.out.println("DefaultHandler: running saveInstance");

        if (!params.containsKey("file")) {
            System.out.println("saveInstance: no parameter 'file'");
            Hashtable ret = new Hashtable();
            ret.put("status", "error");
            ret.put("error", "Parameter 'file' not passed to function");
            return ret;
        }
        if (!params.containsKey("doc")) {
            System.out.println("saveInstance: no parameter 'doc'");
            Hashtable ret = new Hashtable();
            ret.put("status", "error");
            ret.put("error", "Parameter 'doc' not passed to function");
            return ret;
        }

        try {
            String filename = (String) params.get("file");
            System.out.println("Saving to file: " + FORMDIR + "/" + filename);
            File file = new File(FORMDIR + "/" + filename);
            FileWriter writer = new FileWriter(file);

            byte[] docBytes = (byte[]) params.get("doc");

            DocTransformer dt = new DocTransformer(docBytes);
            writer.write(dt.getString());

            System.out.println("Doc = " + dt.getString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Hashtable ret = new Hashtable();
            ret.put("status", "error");
            ret.put("error", "Exception: " + e.getMessage());
            return ret;
        }

        Hashtable ret = new Hashtable();
        ret.put("status", "ok");
        return ret;
    }
}
    
    
