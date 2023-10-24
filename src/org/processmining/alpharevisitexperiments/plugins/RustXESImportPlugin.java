package org.processmining.alpharevisitexperiments.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.alpharevisitexperiments.bridge.RustBridge;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import java.io.File;


@Plugin(name = "[BETA] Import XES Log File with Rust",
        parameterLabels = {"Filename"}, returnLabels = {
        "Log (single process)"}, returnTypes = {XLog.class})
@UIImportPlugin(description = "[BETA]  Import XES Log File with Rust", extensions = {"mxml", "xml", "gz",
        "zip", "xes", "xez"})
public class RustXESImportPlugin {
    @PluginVariant(requiredParameterLabels = {0})
    public Object importFile(PluginContext context, String filename) throws Exception {
        System.out.println("IMPORT XES: " + filename);
        XLog log = RustBridge.importXESasXLog(filename);
        return log;
    }

    @PluginVariant(requiredParameterLabels = {0})
    public Object importFile(PluginContext context, File file) throws Exception {
        System.out.println("IMPORT XES FILE: " + file.getAbsolutePath());
        XLog log = RustBridge.importXESasXLog(file.getAbsolutePath());
        return log;
    }

}

