package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public class IdentityLogRepair extends LogRepairStep {
    public final static String NAME = "Skip Log Repair";


    final ExperimentOption[] options = {
    };

    public IdentityLogRepair() {
        super(NAME);
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        return logProcessor;
    }
}
