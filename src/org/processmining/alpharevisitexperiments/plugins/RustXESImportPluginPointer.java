package org.processmining.alpharevisitexperiments.plugins;


import org.processmining.alpharevisitexperiments.bridge.RustBridge;
import org.processmining.alpharevisitexperiments.bridge.RustLogPointer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.ProvidedObjectLifeCycleListener;
import org.processmining.framework.providedobjects.ProvidedObjectID;

import java.io.File;


//@Plugin(name = "Import XES Log as Pointer (Rust)",
//        parameterLabels = {"Filename"}, returnLabels = {
//        "Log (single process)"}, returnTypes = {RustLogPointer.class})
//@UIImportPlugin(description = "Import XES Log as Pointer (Rust)", extensions = {"mxml", "xml", "gz",
//        "zip", "xes", "xez"})
public class RustXESImportPluginPointer {
    //    @PluginVariant(requiredParameterLabels = {0})
    public Object importFile(PluginContext context, String filename) throws Exception {
        context.getProvidedObjectManager().getProvidedObjectLifeCylceListeners().add(new ProvidedObjectLifeCycleListener() {
            @Override
            public void providedObjectCreated(ProvidedObjectID providedObjectID, PluginContext pluginContext) {

            }

            @Override
            public void providedObjectFutureReady(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectNameChanged(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectObjectChanged(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectDeleted(ProvidedObjectID providedObjectID) {
                System.out.println("Object deleted" + providedObjectID);
            }
        });
        System.out.println("IMPORT XES: " + filename);
        long logPointer = RustBridge.importXESasPointer(filename);
        return new RustLogPointer(logPointer);
    }

    //    @PluginVariant(requiredParameterLabels = {0})
    public Object importFile(UIPluginContext context, File file) throws Exception {
        context.getProvidedObjectManager().getProvidedObjectLifeCylceListeners().add(new ProvidedObjectLifeCycleListener() {
            @Override
            public void providedObjectCreated(ProvidedObjectID providedObjectID, PluginContext pluginContext) {

            }

            @Override
            public void providedObjectFutureReady(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectNameChanged(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectObjectChanged(ProvidedObjectID providedObjectID) {

            }

            @Override
            public void providedObjectDeleted(ProvidedObjectID providedObjectID) {
                System.out.println("Object deleted" + providedObjectID);
            }
        });
        System.out.println("IMPORT XES FILE: " + file.getAbsolutePath());
        long logPointer = RustBridge.importXESasPointer(file.getAbsolutePath());
        RustLogPointer rustLogPointer = new RustLogPointer(logPointer);
        context.getProvidedObjectManager().getProvidedObjectLifeCylceListeners().add(new ProvidedObjectLifeCycleListener() {
            @Override
            public void providedObjectCreated(ProvidedObjectID providedObjectID, PluginContext pluginContext) {
                System.out.println("CREATED!???! " + providedObjectID);

            }

            @Override
            public void providedObjectFutureReady(ProvidedObjectID providedObjectID) {
                System.out.println("FUTURE READY!???! " + providedObjectID);

            }

            @Override
            public void providedObjectNameChanged(ProvidedObjectID providedObjectID) {
                System.out.println("NAME CHANGED!???! " + providedObjectID);

            }

            @Override
            public void providedObjectObjectChanged(ProvidedObjectID providedObjectID) {
                System.out.println("CHANGED!???! " + providedObjectID);

            }

            @Override
            public void providedObjectDeleted(ProvidedObjectID providedObjectID) {
                System.out.println("DELETED!???! " + providedObjectID);
                rustLogPointer.destroy();
            }
        });
        context.getProvidedObjectManager().createProvidedObject("Log Pointer", rustLogPointer, RustLogPointer.class, context);
//        final ProMResource<?> netRes = context.getGlobalContext().getResourceManager().getResourceForInstance(rustLogPointer);
        return rustLogPointer;
    }

}
