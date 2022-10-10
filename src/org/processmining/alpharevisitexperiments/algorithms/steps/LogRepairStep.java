package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public abstract class LogRepairStep extends Step {

    protected LogRepairStep(String name) {
        super(name);
    }

    public abstract LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor);


    @Override
    public String getTypeName() {
        return "Repairing Event Log";
    }
}
